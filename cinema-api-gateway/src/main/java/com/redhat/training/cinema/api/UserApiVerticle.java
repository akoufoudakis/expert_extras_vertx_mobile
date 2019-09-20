package com.redhat.training.cinema.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

public class UserApiVerticle extends AbstractVerticle {

    @Override
    public void start() {

        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
        vertx.eventBus().<JsonObject>consumer("users.service", message -> {


            HttpEndpoint.getClient(discovery, new JsonObject().put("name", "cinema-users-service"), ar -> {
                if (ar.failed()) {
                    message.fail(500, "No service available");
                } else {
                    ar.result().getNow("/user/" + message.body().getString("userId"), r -> {
                        r.exceptionHandler(t -> message.fail(500, t.getMessage()));
                        if (r.statusCode() != 200) {
                            message.fail(500, "Service Unavailable. Status code " + r.statusCode());

                        }
                        r.bodyHandler(body -> {
                            message.reply(body.toJsonObject());
                        });
                    }).close();
                }
            });
        });
    }

}
