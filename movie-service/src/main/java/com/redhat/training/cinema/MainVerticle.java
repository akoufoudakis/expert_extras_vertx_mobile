package com.redhat.training.cinema;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        ConfigStoreOptions jsonConfigStore = new ConfigStoreOptions().setType("json");
        ConfigStoreOptions appStore = new ConfigStoreOptions().setType("configmap")
                .setFormat("yaml")
                .setConfig(new JsonObject()
                        .put("name", System.getenv("APP_CONFIGMAP_NAME"))
                        .put("key", System.getenv("APP_CONFIGMAP_KEY")));

        ConfigRetrieverOptions options = new ConfigRetrieverOptions();
        jsonConfigStore.setConfig(config());
        options.addStore(jsonConfigStore);

        if (System.getenv("KUBERNETES_NAMESPACE") != null) {
            options.addStore(appStore);
        } else {
            jsonConfigStore.setConfig(config());
            options.addStore(jsonConfigStore);
        }

        ConfigRetriever.create(vertx, options).getConfig(ar -> {
            if (ar.succeeded()) {
                deployVerticles(ar.result(), startFuture);
            } else {
                System.out.println("Failed to retrieve the configuration.");
                startFuture.fail(ar.cause());
            }
        });

    }

    private void deployVerticles(JsonObject config, Future<Void> startFuture) {

        Future<String> routerVerticleFuture = Future.future();
        Future<String> movieVerticleFuture = Future.future();

        DeploymentOptions options = new DeploymentOptions();
        options.setConfig(config);
        vertx.deployVerticle(new MovieVerticle(), options, movieVerticleFuture.completer());
        vertx.deployVerticle(new RouterVerticle(), options, routerVerticleFuture.completer());

        CompositeFuture.all(routerVerticleFuture, movieVerticleFuture).setHandler(ar -> {
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
