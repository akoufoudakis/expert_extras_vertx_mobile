package com.redhat.training.cinema;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.healthchecks.HealthCheckHandler;
import io.vertx.ext.healthchecks.Status;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class RouterVerticle extends AbstractVerticle {

    private MovieService movieService;

    @Override
    public void start() {
        movieService = MovieService.createProxy(vertx);
        Router router = Router.router(vertx);
        router.get("/movies").handler(this::listMovies);
        router.get("/movies/:genre").handler(this::listMovies);
        router.get("/movie/:movieId").handler(this::getMovie);
        router.get("/movies/find/:name").handler(this::findByName);

        HealthCheckHandler healthCheckHandler = HealthCheckHandler.create(vertx)
                .register("health", f -> f.complete(Status.OK()));
        router.get("/health").handler(healthCheckHandler);

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8080);

    }

    public void listMovies(RoutingContext rc) {
        String genre = null;
        if(rc.request().getParam("genre") != null) {
            genre = rc.request().getParam("genre");
        }
        movieService.getMovies(genre, ar -> {
            if (ar.succeeded()) {
                rc.response()
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .end(ar.result().encodePrettily());
            } else {
                rc.fail(ar.cause());
            }
        });
    }

    public void getMovie(RoutingContext rc) {
        String movieId = rc.request().getParam("movieId");
        movieService.getMovie(movieId, ar -> {
            if (ar.succeeded()) {
                JsonObject json = ar.result();
                if(ar.result() != null) {
                    rc.response()
                            .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                            .end(json.encodePrettily());
                } else {
                    rc.fail(404);
                }
            } else {
                rc.fail(ar.cause());
            }
        });
    }

    public void findByName(RoutingContext rc) {
        String name = rc.request().getParam("name");
        movieService.findMoviesByName(name, ar -> {
            if (ar.succeeded()) {
                rc.response()
                        .putHeader(HttpHeaders.CONTENT_TYPE, "application/json")
                        .end(ar.result().encodePrettily());
            } else {
                rc.fail(ar.cause());
            }
        });
    }

}
