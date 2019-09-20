package com.redhat.training.cinema.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class GatewayVerticle extends AbstractVerticle {

    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.get("/api/movies").handler(this::getMovies);
        router.get("/api/movies/:genre").handler(this::getMovies);
        router.get("/api/movies/find/:name").handler(this::findMovies);
        router.get("/api/movie/:movieId").handler(this::getMovieById);
        router.get("/api/user/:userId").handler(this::getUser);

        router.route("/api/*").failureHandler(rc -> rc.response().setStatusCode(500).end());

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8080);

    }

    public void getMovies(RoutingContext rc) {

        JsonObject query = new JsonObject();
        DeliveryOptions options = new DeliveryOptions();
        options.addHeader("action", "listmovies");
        if (rc.pathParam("genre") != null) {
            query.put("genre", rc.pathParam("genre"));
        }

        vertx.eventBus().<JsonArray>send("movie.service", query, options, ar -> {
            if (ar.succeeded()) {
                rc.response()
                        .putHeader("Content-Type", "application/json")
                        .end(ar.result().body().encodePrettily());
            } else {
                rc.response().setStatusCode(500)
                        .putHeader("Content-Type", "text/plain")
                        .end(ar.cause().getMessage());
            }
        });
    }

    public void findMovies(RoutingContext rc) {
        JsonObject query = new JsonObject().put("name", rc.pathParam("name"));
        DeliveryOptions options = new DeliveryOptions();
        options.addHeader("action", "findmovies");
        vertx.eventBus().<JsonArray>send("movie.service", query, options, ar -> {
            rc.response()
                    .putHeader("Content-Type", "application/json")
                    .end(ar.result().body().encodePrettily());
        });
    }

    public void getMovieById(RoutingContext rc) {
        JsonObject query = new JsonObject().put("movieId", rc.pathParam("movieId"));
        DeliveryOptions options = new DeliveryOptions();
        options.addHeader("action", "getmoviebyid");
        vertx.eventBus().<JsonObject>send("movie.service", query, options, ar -> {
            if (ar.succeeded()) {
                rc.response()
                        .putHeader("Content-Type", "application/json")
                        .end(ar.result().body().encodePrettily());
            } else {
                rc.response()
                        .setStatusCode(500)
                        .putHeader("Content-Type", "text/plain")
                        .end(ar.cause().getMessage());
            }
        });
    }

    private void getUser(RoutingContext rc) {
        JsonObject query = new JsonObject().put("userId", rc.pathParam("userId"));
        vertx.eventBus().<JsonObject>send("users.service", query, ar -> {
            if(ar.succeeded()) {
                rc.response()
                        .putHeader("Content-Type", "application/json")
                        .end(ar.result().body().encodePrettily());
            } else {
                rc.response()
                        .setStatusCode(500)
                        .putHeader("Content-Type", "text/plain")
                        .end(ar.cause().getMessage());
            }
        });
    }


}
