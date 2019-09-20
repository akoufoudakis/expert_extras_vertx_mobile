package com.redhat.training.cinema.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.kubernetes.KubernetesServiceImporter;

public class MainVerticle extends AbstractVerticle {


    @Override
    public void start(Future<Void> startFuture) throws Exception {

        ServiceDiscovery discovery = ServiceDiscovery.create(vertx);
        discovery.registerServiceImporter(new KubernetesServiceImporter(),
                new JsonObject().put("namespace", "redblue-cinema"));

        Future<String> gatewayVerticleFuture = Future.future();
        Future<String> movieApiVerticleFuture = Future.future();
        Future<String> userApiVerticleFuture = Future.future();

        DeploymentOptions options = new DeploymentOptions();

        vertx.deployVerticle(new GatewayVerticle(), options, gatewayVerticleFuture.completer());
        vertx.deployVerticle(new MovieApiVerticle(), options, movieApiVerticleFuture.completer());
        vertx.deployVerticle(new UserApiVerticle(), options, userApiVerticleFuture);

        CompositeFuture.all(gatewayVerticleFuture, movieApiVerticleFuture).setHandler(ar -> {
            if (ar.succeeded()) {
                System.out.println("Verticles deployed successfully.");
                startFuture.complete();
            } else {
                System.out.println("WARNINIG: Verticles NOT deployed successfully.");
                startFuture.fail(ar.cause());
            }
        });

    }

}
