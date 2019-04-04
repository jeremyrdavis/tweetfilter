package io.vertx.examples.tweetfilter;

import io.reactivex.Observable;
import io.vertx.core.Future;
import io.vertx.core.file.OpenOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.file.AsyncFile;
import io.vertx.reactivex.core.file.FileSystem;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Thanks to @medal.tv
 * https://gist.github.com/PimDeWitte
 * https://gist.github.com/PimDeWitte/c04cc17bc5fa9d7e3aee6670d4105941
 * for the list of words
 */
public class MainVerticle extends AbstractVerticle {

  ArrayList<String> badWords;

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    badWords = new ArrayList<String>();

    FileSystem fileSystem = vertx.fileSystem();
    fileSystem.open("Word_Filter-Sheet1.csv", new OpenOptions(), res ->{
      AsyncFile file = res.result();
      Observable<Buffer> observable = file.toObservable();
      observable.forEach(data -> {
        String[] splitData = data.toString().replace("\n", "").split(",");
        badWords.addAll(Arrays.asList(splitData));
      });
    });

    Router baseRouter = Router.router(vertx);
    baseRouter.route("/").handler(this::indexHandler);

    Router apiRouter = Router.router(vertx);
    apiRouter.route("/*").handler(BodyHandler.create());
    apiRouter.post("/censor").handler(this::censorshipHandler);

    baseRouter.mountSubRouter("/api", apiRouter);

    vertx
      .createHttpServer()
      .requestHandler(baseRouter::accept)
      .listen(8081, result -> {
        if (result.succeeded()) {
          startFuture.complete();
        } else {
          startFuture.fail(result.cause());
        }
      });

  }

  private void indexHandler(RoutingContext routingContext) {
    HttpServerResponse response = routingContext.response();
    response
      .putHeader("Content-Type", "text/html")
      .end("Tweetfilter");
  }

  private void censorshipHandler(RoutingContext routingContext) {

    System.out.println("censorshipHandler");

    String statusInput = routingContext
      .getBodyAsJson()
      .getString("text");

    List<String> statusWords = Arrays.asList(statusInput.split(" "));

    boolean allClear = Collections.disjoint(badWords, statusWords);

    if (allClear) {
      routingContext.response()
        .putHeader("Content-Type", "text/html")
        .end("Tweetfilter all clear!");
    }else{
      routingContext.response()
        .putHeader("Content-Type", "text/html")
        .end("Tweetfilter match!");
    }

}
