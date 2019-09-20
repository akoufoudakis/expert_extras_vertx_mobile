package com.redhat.training.cinema.users;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.Optional;

public class UserVerticle extends AbstractVerticle {


    private MongoClient client;

    @Override
    public void start() {

        client = MongoClient.createShared(vertx, config());
        vertx.eventBus().<JsonObject>consumer(config().getString("eventbus.users"), message -> {
            JsonObject userId = message.body();
            client.find("users", userId, ar -> {
                if (ar.succeeded()) {
                    if(!ar.result().isEmpty()) {
                        Optional<JsonObject> result = ar.result().stream().findFirst();
                        message.reply(result.get());
                    } else {
                        message.reply(new JsonObject());
                    }
                } else {
                    message.fail(500, ar.cause().getMessage());
                }
            });
        });

    }

}
