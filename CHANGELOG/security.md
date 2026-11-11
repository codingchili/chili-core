# Security sweep

Quick read-only security review of `core/main`, covering tokens, hashing, serialization, transports and TLS.
Findings come from reading the code; no exploit tests have been written and nothing has been fixed yet.

## High

- `TokenFactory.canonicalizeTokenWithCrypto`: the signed bytes are `properties || domain || expiry` with no separator,
  so a domain that ends in a digit can donate that digit to the expiry without breaking the signature. Domain `svc1`
  with expiry `1700000000` signs the same bytes as domain `svc` with expiry `11700000000` (around the year 2340).
  The default domain is `UUID.randomUUID()`, which ends in a digit about 62% of the time; usernames such as `bob2` are
  also affected. HMAC and RSA-signed tokens share this canonicalization.
  Fix: length-prefix each part or sign a single serialized `{properties, domain, expiry}` structure. Changing the
  format invalidates tokens that have already been issued.
- `TokenFactory.verifySignature` + `SecuritySettings.getKeystore`: the keystore used for verification is chosen by the
  token's `alias` property. An unknown alias makes the server generate a new self-signed RSA certificate and cache it
  forever. An unauthenticated client sending `type=SHA256withRSA` with a random alias per request causes an RSA key
  generation per request (inside a `synchronized` method, blocking all keystore lookups), unbounded cache growth and a
  WARNING log line each time. Tokens can't be forged this way, but the server can be worn down.
  Fix: reject aliases that aren't configured in the verify path instead of falling back to a self-signed certificate.

## Medium / Low

- **Configuration hot reload (mitigation added):** by default `Configurations` watches the configuration directory and
  applies changes to a running application, so anything with write access to `conf/` (including the security settings and
  keystore list) can change its behavior. `configurationReload: false` in `system.yaml` turns this off, see
  `upgrades.md`. It's still on by default for compatibility. Consider making it off by default in a 2.0 release.

- **Token signatures depend on `prettyEncoding`:** the signed bytes include the JSON of `token.getProperties()` as
  written by the global `Serializer.json` mapper, so services with different `prettyEncoding` settings can't verify each
  other's tokens (see `upgrades.md`, "`prettyEncoding` off by default"). Resolve with the High canonicalization fix.

- ~~**Self-signed fallback broken (found during the QUIC work):**~~ **Fixed**, see `upgrades.md` ("Listener configuration works as
  documented"): `TestCertificate` generates the certificate with the JDK only. **This reopens the next finding and the High
  alias finding**: a secure listener without a configured keystore serves a throwaway certificate again (with a warning in the log), and an
  unknown token alias again costs an RSA key generation per request.

- `SecuritySettings.generateSelfSigned`: a missing or misnamed keystore only logs a warning and then a throwaway
  self-signed certificate is served. Consider failing at startup when `secure: true`.
- `SecuritySettings.loadKeystore`: a keystore load failure calls `System.exit(0)`. Exit code 0 hides the failure from
  supervisors and CI; it should be non-zero.
- `RestHelper`: CORS origin, methods and headers are all `*` on every REST listener and can't be configured. Impact is
  limited because tokens travel in the request body rather than cookies, but it should be configurable.
- `StreamQuery`, `IndexedMap`, `MongoDBMap` (`$regex`) and `ElasticMap` (`wildcardQuery`/`regexpQuery`) use query text
  unescaped. Downstream services that pass client input to `.matches()` or `.like()` are open to ReDoS and query
  injection. A Javadoc warning or an escaping helper would help.
- `HashFactory.hash` turns every exception into `HashMismatchException`, hiding real errors such as bad Argon2 settings
  or running out of memory. Not exploitable, but makes misconfiguration hard to spot.

## Reviewed and fine

- HMAC comparison runs in constant time (`ByteComparator`), and a token's algorithm is only accepted if it matches a
  configured one, so algorithm-confusion attacks don't apply.
- Kryo is only used for in-memory `copy()` (`IndexedMapVolatile`), never to deserialize network data.
- Jackson has no default typing enabled, so there are no polymorphic-deserialization gadgets.
- `SecretFactory` uses `SecureRandom`; defaults are HmacSHA512 and 64-byte secrets.
- Request body size limits apply on REST and WebSocket.

## Encrypted configuration values, secrets store, bake, node keys (1.4.9)

- `enc:v1:` values (`EncryptedValue`): X25519 + HKDF-SHA256 + ChaCha20-Poly1305 (JDK only), the data key wrapped once
  per recipient node; decrypted by `Configurations` on load with the node key (`NodeKey`, `conf/system/node.key`,
  `CHILI_NODE_KEY`, `-Dchili.node.key`). A value this node can't open fails the load of that file only, naming the
  file and key; tampering fails the authentication. Files loaded with encrypted values are never saved back.
- `generate --store <dir>` keeps the generated values outside the configuration (the configuration's own values are
  ignored); `--encrypt` writes them encrypted to the nodes that read each file (launcher `configs`, `blocks`, `hosts`,
  `<nodes>/<host>.pub`); `bake` merges a store into the configuration, per block too; `keygen` writes a node key (600)
  and prints its public key, refuses to replace one without `--force`.
- `Launcher.main` exits with status 1 when a service of the block fails to deploy (was 0: container runtimes and
  scripts saw no failure). `CoreStrings.getAbsolutePath` keeps an absolute path (it was prefixed with the working
  directory, so files outside the application, a secrets store, could not be read).
- Tests: `EncryptedValueTest` (round trip for 1 and 3 recipients, non-recipient, tampering of every byte, versions,
  configuration load, missing key fails only the used file, key file), `SecretStoreTest` (store, per-file recipients,
  the private key only for its signer's nodes, bake plain/encrypted/none/per block, missing values, changed nodes),
  `LauncherCommandExecutorTest` (keygen refuses to overwrite).


## Configuration files by name, store renames, strict configuration guard (1.4.9)

- **By name (`ServiceFiles`):** a deployable in the package `<...>.<name>` reads `service/<name>.yaml`,
  `service/<name>/**` and `<name>/**` (e.g. `realm/` of `com.example.realm.Service`). `bake` (the files of each block)
  and `generate --encrypt` (the recipients of a file) derive it from the block's class names; the launcher's
  `configs` map, `SecretRecipients.unmapped` and its bake warning are removed (`configs` in a file is ignored).
- **Store renames:** security `renames` (old path to new, e.g. `service/routing: service/router`): `generate --store`
  and `bake --store` move the saved values (plain, `encrypted/`, the line of `encrypted/nodes.txt`) once, with a log
  line; both present: a warning, the new one is kept (`SecretStoreRenames`).
- **Guard (`ConfigurationGuard`):** `Configurations` records each loaded file and the classes that loaded it; after
  the launcher deployed its block, and on later loads, a file with generated secrets that no service of the JVM owns
  by name is a warning naming the loading service, the file and the fix. System `strict` (or `-Dchili.strict`,
  `CHILI_STRICT`) fails the start instead (exit 1); a later load is an error log.
