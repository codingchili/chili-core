# Security 

Covered in this chapter
- using TLS with listeners
- generating and verifying tokens
- input validation
- hashing passwords securely

For role based access control see the documentation on the authorization handler in the [protocol](protocol) chapter.

### Keystores
Keystores are used for TLS or for signing tokens.

Configuring a keystore

```java
Configurations.security()
    .addKeystore()
        .setPassword("changeit")
        .setShortName("main")
        .setPath("~/keystore.jks")
        .build();
```

Or using security.yaml (see: [configuration](configuration))

```yaml
---
keystores:
- path: "./keystore.jks"
  password: "changeit" 
```

The keystore is then referenced by it's 'shortName'. If the short name
is not set, it will be inferred from the path as the file name. In this
case it would have been 'keystore.jks'. Keystores can be added in this way
to configure TLS security for the TCP listeners.

There is also an interactive password input that can be used which attempts to use masked input if available. It can be used by

```java
Configurations.security()
    .addKeystore()
        .readPasswordFromConsole()
        .setPath("~/keystore.jks")
        .build();
```

Please note that this is blocking so it's best to use it before starting the system context. Or running it on a dedicated thread which can block. The blocking pool is not recommended, as the blocked thread checker will trigger.

### Transport layer security

Enabling TLS for the `RestListener`, `TcpListener` or the `WebsocketListener` is done in the following way. This requires that a keystore has already been setup. To use a test certificate that is automatically generated on startup either set the keystore reference
to something non-existing, or call .setSecure(true) without specifying a keystore.

ALPN is supported for HTTP/2 but requires Netty/JVM support.

```java
context.listener(() -> {
    return new RestListener()
        .settings(new ListenerSettings()
            .setKeystore("keystore.jks"))    
    }
).setHandler(done -> {
    // started.
});
```

Requires a keystore to be configured in the security settings. When an automatically generated test certificate is generated a warning will be printed to the logs. Please take care to verify that this doesn't happen in a production environment.

Easy way for test

```java
ListenerSettings.getDefaultSettings()
    .setPort(443)
    .setSecure(true);

context.listener(RestListener::new);
```

The certificate will be regenerated on each application startup, for development it is recommended to disable hostname verification for localhost using chrome://flags etc.

**Notes**
- The `UdpListener` does not support DTLS yet.
- TLS over cluster is configured in Hazelcast.
- TLS over eventbus is configured in the Vert.x options.

### Input validation
Input validation can be done using the Validator with ValidatorSettings.
Validator settings specifies how a valid or invalid input looks like and
specifies an action. The action may reject the input, replace the bad parts or accept the input if it matches the settings. All matches uses regular expressions which may be configured and used together with the configurations system.

```java
class ValidatorExample {
    public static void main(String[] args){
        Validator validator = new Validator();
        
        // 1. the name of the validation ruleset will be shown when 
        //    validation fails.
        // 2. requires that the input length is between 0 and 256 chars.
        // 3. only apply for field 'animal/creature' , default: all keys. 
        // 4. rejects the input if it contains 'doggo' (blacklist)
        // 5. accepts input only if it matches cats/kittens. (whitelist)
        // 6. convert any input matching kittens into unicorn.
        validator.add("only_cats")
                .length(4, 256)
                .addKeys("animal", "creature")
                .addRegex(RegexAction.REJECT, "doggo")
                .addRegex(RegexAction.ACCEPT, "(cats|kittens)+")
                .addRegex(RegexAction.SUBSTITUTE, "(kittens)", "unicorn");
        
        // fails validation; shorter than 4. (#2)
        validator.validate(json("{'creature': 'bee'}"));
        
        // passes validation, not in explicit fieldset. (#3)
        validator.validate(json("{'human': true}"));
        
        // fails validation due to REJECT action. (#4)
        validator.validate(json("{'animal': 'doggo'}"));
        
        // also fails validation, as "doggo x2" is not whitelisted. (#5)
        validator.validate(json("{'animal': 'doggo x2'}"));
        
        // converted to "unicorn." (#6)
        validator.validate(json("{'animal': 'kittens'}"));
    }
    
    private JsonObject json(String json) {
        return new JsonObject.readFrom(json);
    }
}
```

Prefer using whitelists and be careful with the fieldset chosen, the default is to run the validation rules on all of the fields. Be sure to thoroughly test all regular expressions and be aware of the pitfalls.

The validator also supports nested objects and validating arrays of objects. Strings, numerical and bool values may be validated as well. 

### TokenFactory
The token factory is used to generate tokens, signed or using a keyed hash.

```java
TokenFactory factory = new TokenFactory(context, "secret".getBytes());

Token token = new Token()
    .setDomain("username")
    .setExpiry(System.currentTimeMillis() + 5000) // in 5 seconds.
    .addProperty("admin", false);

// async operation - no crypto stuff on the event loop.
factory.hmac(token).setHandler(done -> {
    if (done.succeeded()) {
        System.out.println(token.getKey());
        
        factory.verify(token).setHandler(verified -> {
            // verified.succeeded() == true
        });
        
        // modify the token, no longer valid.
        token.setProperty("admin", true);
        factory.verify(token).setHandler(verified -> {
            // verified.succeeded() == false
        });
    }
});

```

To sign a token using a private key from a keystore, a keystore needs to
be configured using the `SecuritySettings`. This can be obtained during
runtime from `Configurations.security()`, or configured in security.yaml.
This is basically a cheap implementation of JWT, with less scope.

Signing our token

```
TokenFactory factory = new TokenFactory(context, 'hmac-secret'.getBytes());

Token token = new Token("admin")
    .expire(2, TimeUnit.DAYS);
    
// crypto stuff is async - don't block the event loop.
factory.sign(token).setHandler(done -> {
    // if done.succeeded token is signed successfully.
});

// verified in the same way using 
factory.verify(token).setHandler((done) -> {
   // if done.succeeded token is valid. 
});
```

**Default algorithms**
These can be changed through security.yaml or programmatically using `Configurations.security()`.

|Signed Token|HMAC Token|
|---|---|
|HmacSHA512|SHA256withRSA|

Certificates will **never** be used for HMAC tokens. Please read this to understand more about tokens [Risks regarding JWT's](https://auth0.com/blog/critical-vulnerabilities-in-json-web-token-libraries/) even though JWT is not being used it's good to know. The [RFC7519](https://tools.ietf.org/html/rfc7519) is also a good read.

### HashFactory
Implements hashing of passwords using [Argon2](https://github.com/P-H-C/phc-winner-argon2). See the [configuration](configuration)
 
 Hashing a password
 
 ```java
 HashFactory hasher = new HashFactory(context);
hasher.hash("password").setHandler(done -> {
    if (done.succeeded()) {
        String hashed = done.result();
    } else {
        // handle error.
    }
});
 ```

Verifying a password

```java
hasher.verify(done -> {
    boolean match = done.succeeded();
}, hashed, "password");
```

Example argon hash

```console
$argon2i$v=19$m=8192,t=1,p=4$OTK0wxGzvJ7rYqAqJiWCCQ$NEfMs5/e957y+t9/OY2tslLezHLu8BnGGat5WEU9XNE
```
 
See the [configuration](configuration) chapter for more information on how to configure Argon.

**Note**: For best security the Argon parameters must be tuned for the production system. Avoid comparing secrets using String::equals as it leaks timing information.

### SecretFactory
Generate CSPRNG secrets of the given length encoded as base64.

```java
String secret = SecretFactory.generate(1024); 
```

### ByteComparator
Compares two strings or byte arrays in constant time, that is - leaking minimal timing information.

```
boolean stringEquals = ByteComparator.compare("apples", "oranges");
boolean byteEquals = ByteComparator.compare(new byte[], new byte[]);
```

### Password reader

The password reader used by the keystore build can also be used directly.

Example

```java
String password = PasswordReader.fromConsole("Enter pass: ");
```