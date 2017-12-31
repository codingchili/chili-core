package com.codingchili.realm.model;

import com.codingchili.core.storage.AsyncStorage;
import com.codingchili.realm.instance.model.entity.PlayerCreature;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.Collection;

import static com.codingchili.common.Strings.ID_ACCOUNT;
import static com.codingchili.core.configuration.CoreStrings.ID_NAME;
import static io.vertx.core.Future.failedFuture;

/**
 * @author Robin Duda
 * <p>
 * Storage for characters.
 */
public class CharacterDB implements AsyncCharacterStore {
    private final AsyncStorage<PlayerCreature> characters;

    public CharacterDB(AsyncStorage<PlayerCreature> map) {
        this.characters = map;
    }

    @Override
    public void create(Handler<AsyncResult<Void>> future, PlayerCreature character) {
        characters.putIfAbsent(character, future);
    }

    @Override
    public void findByUsername(Handler<AsyncResult<Collection<PlayerCreature>>> future, String username) {
        characters.query(ID_ACCOUNT).equalTo(username).execute(future);
    }

    @Override
    public void findOne(Handler<AsyncResult<PlayerCreature>> future, String username, String character) {
        characters.query(ID_ACCOUNT).equalTo(username)
                .and(ID_NAME).equalTo(character).execute(get -> {

            if (get.succeeded() && get.result().size() != 0) {
                future.handle(Future.succeededFuture(get.result().iterator().next()));
            } else {
                future.handle(Future.failedFuture(new CharacterMissingException(character)));
            }
        });
    }

    @Override
    public void remove(Handler<AsyncResult<Void>> future, String username, String character) {
        characters.query(ID_ACCOUNT).equalTo(username)
                .and(ID_NAME).equalTo(character)
                .execute(query -> {
                    if (query.succeeded() && query.result().size() > 0) {
                        characters.remove(character, future);
                    } else {
                        future.handle(failedFuture(new CharacterMissingException(character)));
                    }
                });
    }
}
