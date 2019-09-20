package com.redhat.training.cinema.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.HttpEndpoint;

import java.util.Optional;

public class MovieApiVerticle extends AbstractVerticle {

    ServiceDiscovery discovery;

    @Override
    public void start() {
        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
        vertx.eventBus().<JsonObject>consumer("movie.service", message -> {
            HttpEndpoint.getClient(discovery, new JsonObject().put("name", "movie-service"), ar -> {
                if (ar.failed()) {
                    message.fail(500, "No service available");
                } else {
                    String action = message.headers().get("action");
                    switch(action) {
                        case "listmovies": {
                            String queryString = "/movies";
                            if (!message.body().isEmpty()) {
                                queryString = queryString.concat("/" + message.body().getString("genre"));
                            }
                            ar.result().getNow(queryString, r -> {
                                handleResponse(r, message);
                            }).close();
                            break;
                        }
                        case "findmovies": {
                            ar.result().getNow("/movies/find/" + message.body().getString("name"), r -> {
                                handleResponse(r, message);
                            }).close();
                            break;
                        }
                        case "getmoviebyid": {
                            ar.result().getNow("/movie/" + message.body().getString("movieId"), r -> {
                                r.exceptionHandler(t -> message.fail(500, t.getMessage()));
                                if (r.statusCode() != 200) {
                                    message.fail(500, "Service Unavailable. Status code " + r.statusCode());
                                }
                                r.bodyHandler(body -> {
                                    message.reply(body.toJsonObject());
                                });
                            }).close();
                            break;
                        }
                    }
                }
            });
        });
    }

    private void handleResponse(HttpClientResponse response, Message<JsonObject> message) {
        response.exceptionHandler(t -> message.fail(500, t.getMessage()));
        if (response.statusCode() != 200) {
            message.fail(500, "Service Unavailable. Status code " + response.statusCode());
        }
        response.bodyHandler(body -> {
            message.reply(body.toJsonArray());
        });
    }

    @Override
    public void stop() throws Exception {
        Optional.ofNullable(discovery).ifPresent(d -> d.close());
    }

}
