package com.codingchili.authentication.model;

import com.codingchili.core.security.Account;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author Robin Duda
 * Asynchronous account store.
 */
public interface AsyncAccountStore {
    /**
     * Finds an account in the store.
     *
     * @param future   callback
     * @param username username of the account to find by username.
     */
    void get(Handler<AsyncResult<Account>> future, String username);

    /**
     * Authenticates an user in the accountstore.
     *
     * @param future  callback
     * @param account unauthenticated account containing username and password.
     */
    void authenticate(Handler<AsyncResult<Account>> future, Account account);

    /**
     * Registers a new account in the store.
     *
     * @param future  callback
     * @param account contains account data to be created.
     */
    void register(Handler<AsyncResult<Account>> future, Account account);
}
