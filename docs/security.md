# Security 


For role based access control see the documentation on the authorization handler in the [protocol](protocol) chapter.

### Input validation
Input validation can be done using the Validator with ValidatorSettings.
Validator settings specifies how a valid or invalid input looks like and
specifies an action. The action may reject the input, replace the bad parts or accept the input if it matches the settings. All matches uses regular expressions which may be configured and used together with the configurations system.

```java
Validator validator = new Validator(() -> {
    ValidatorSettings settings = new ValidatorSettings()
        .add("only_cats", new RegexAction())
});
```

### TokenFactory


### HashFactory


### SecretFactory


### ByteComparator


### Password reader

