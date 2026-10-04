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
