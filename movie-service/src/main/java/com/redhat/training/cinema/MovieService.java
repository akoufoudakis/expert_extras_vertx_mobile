package com.redhat.training.cinema;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

@ProxyGen
public interface MovieService {

    final static String ADDRESS = "movies";

    static MovieService create(MongoClient mongoClient) {
        return new MovieServiceImpl(mongoClient);
    }

    static MovieService createProxy(Vertx vertx) {
        return new MovieServiceVertxEBProxy(vertx, ADDRESS);
    }

    void getMovies(String genre, Handler<AsyncResult<JsonArray>> resulthandler);

    void getMovie(String movieId, Handler<AsyncResult<JsonObject>> resultHandler);

    void findMoviesByName(String name, Handler<AsyncResult<JsonArray>> resultHandler);

}
