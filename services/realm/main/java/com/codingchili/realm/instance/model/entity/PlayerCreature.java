package com.codingchili.realm.instance.model.entity;

import com.codingchili.realm.instance.context.GameContext;
import com.codingchili.realm.instance.model.events.Event;

import com.codingchili.core.listener.transport.ClusterRequest;
import com.codingchili.core.protocol.ResponseStatus;
import com.codingchili.core.protocol.Serializer;

import static com.codingchili.core.configuration.CoreStrings.*;

/**
 * @author Robin Duda
 * <p>
 * model for player characters.
 */
public class PlayerCreature extends SimpleCreature {
    private String instance = "starting_instance_1";
    private String realm;
    private String className;
    private String account;

    public PlayerCreature() {
    }

    public PlayerCreature(String id) {
        this.id = id;
        this.name = id;
    }

    @Override
    public void setContext(GameContext context) {
        protocol.annotated(this);
        context.subscribe(this);
        super.setContext(context);
    }

    public String getAccount() {
        return account;
    }

    public PlayerCreature setAccount(String account) {
        this.account = account;
        return this;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    @Override
    public void handle(Event event) {
        context.getInstance().bus().send(realm, event, reply -> {
            if (reply.succeeded()) {

                ClusterRequest request = new ClusterRequest(reply.result());
                String status = request.data().getString(PROTOCOL_STATUS);

                if (!ResponseStatus.ACCEPTED.equals(ResponseStatus.valueOf(status))) {
                    onError(request.data().encodePrettily());
                    context.remove(this);
                }
            } else {
                onError(throwableToString(reply.cause()));
                context.remove(this);
            }
        });
    }

    private void onError(String msg) {
        context.getLogger(getClass())
                .event("disconnect")
                .put("account", account)
                .put("character", getName())
                .send("failed to message client: " + msg);
    }

    public static void main(String[] args) {
        System.out.println(Serializer.pack(new PlayerCreature()));
    }
}
