package com.redhat.training.cinema;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;
import java.util.Optional;

public class MovieServiceImpl implements MovieService {

    private MongoClient mongoClient;

    MovieServiceImpl(MongoClient mongoClient) {

        this.mongoClient = mongoClient;

    }

    @Override
    public void getMovies(String genre, Handler<AsyncResult<JsonArray>> resultHandler) {

        JsonObject query = new JsonObject();

        if (genre != null && !genre.isEmpty()) {
            query.put("genre", genre);
        }

        mongoClient.find("movies", query, ar -> {
            handleResult(ar, resultHandler);

        });

    }

    @Override
    public void getMovie(String movieId, Handler<AsyncResult<JsonObject>> resultHandler) {

        JsonObject query = new JsonObject();
        query.put("movieId", movieId);

        mongoClient.find("movies", query, ar -> {
            if (ar.succeeded()) {
                Optional<JsonObject> result = ar.result().stream().findFirst();
                if (result.isPresent()) {
                    resultHandler.handle(Future.succeededFuture(result.get()));
                } else {
                    resultHandler.handle(Future.succeededFuture(null));
                }
            } else {
                resultHandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void findMoviesByName(String name, Handler<AsyncResult<JsonArray>> resultHandler) {

        JsonObject query = new JsonObject();
        query.put("name", new JsonObject().put("$regex", name));

        mongoClient.find("movies", query, ar -> {
            handleResult(ar, resultHandler);
        });
    }

    private void handleResult(AsyncResult<List<JsonObject>> ar, Handler<AsyncResult<JsonArray>> resultHandler) {
        if (ar.succeeded()) {
            JsonArray movies = new JsonArray();
            ar.result().forEach(movie -> movies.add(movie));
            resultHandler.handle(Future.succeededFuture(movies));
        } else {
            resultHandler.handle(Future.failedFuture(ar.cause()));
        }
    }

}
