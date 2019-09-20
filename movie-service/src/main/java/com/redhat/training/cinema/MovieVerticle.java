package com.redhat.training.cinema;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.serviceproxy.ServiceBinder;

public class MovieVerticle extends AbstractVerticle {

    private MongoClient client;

    @Override
    public void start(Future<Void> startFuture) {

        client = MongoClient.createShared(vertx, config());

        new ServiceBinder(vertx)
                .setAddress("movies")
                .register(MovieService.class, MovieService.create(client));

        startFuture.complete();
    }

}
