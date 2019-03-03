package com.codingchili.core.security;

import java.util.function.Consumer;

import com.codingchili.core.configuration.CoreStrings;

/**
 * Keystore builder for use in #{@link com.codingchili.core.configuration.system.SecuritySettings}.
 *
 * @param <E> the fluent object to return when builder is completed.
 */
public class KeyStoreBuilder<E> {
    private boolean readPasswordFromConsole = false;
    private KeyStoreReference keystore = new KeyStoreReference();
    private Consumer<KeyStoreReference> completer;
    private E fluent;

    /**
     * @param fluent    the object to be returned when building is completed.
     * @param completer called with the result.
     */
    public KeyStoreBuilder(E fluent, Consumer<KeyStoreReference> completer) {
        this.completer = completer;
        this.fluent = fluent;
    }

    /**
     * Completes the building by calling the completer and returning
     * the provided object as a fluent, allowing the building class to
     * resume fluent calls to the provider of the builder.
     *
     * @return object specified by the builder provider.
     */
    public E build() {
        if (readPasswordFromConsole) {
            keystore.setPassword(PasswordReader.fromConsole(CoreStrings.getKeystorePrompt(keystore)));
        }

        completer.accept(keystore);
        return fluent;
    }

    /**
     * @param pwd password to set for the keystore.
     * @return fluent
     */
    public KeyStoreBuilder<E> setPassword(String pwd) {
        keystore.setPassword(pwd);
        return this;
    }

    /**
     * @param path to the keystore .jks file.
     * @return fluent
     */
    public KeyStoreBuilder<E> setPath(String path) {
        keystore.setPath(path);
        return this;
    }

    /**
     * @param shortName the keystore identifier to set.
     * @return fluent
     */
    public KeyStoreBuilder<E> setShortName(String shortName) {
        keystore.setShortName(shortName);
        return this;
    }

    /**
     * reads the password from the console when build is called.
     *
     * @return fluent
     */
    public KeyStoreBuilder<E> readPasswordFromConsole() {
        this.readPasswordFromConsole = true;
        return this;
    }
}
