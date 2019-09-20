package com.redhat.training.cinema.users;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class RouterVerticle extends AbstractVerticle {

    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.get("/user/:userId").handler(this::getUser);

        HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx)
                .register("health", f -> f.complete(Status.OK()));
        router.get("/health").handler(healthCheckHandler);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8080);
    }


    private void getUser(RoutingContext rc) {
        JsonObject query = new JsonObject().put("userId", rc.pathParam("userId"));
        vertx.eventBus().<JsonObject>send(config().getString("eventbus.users"), query, ar -> {
            if(ar.succeeded()) {
                if (ar.result().body().isEmpty()) {
                    rc.fail(404);
                } else {
                    rc.response()
                            .putHeader("Content-Type", "application/json")
                            .end(ar.result().body().encodePrettily());
                }
            } else {
                rc.fail(ar.cause());
            }
        });

    }

}
