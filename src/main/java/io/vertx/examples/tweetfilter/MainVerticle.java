package io.vertx.examples.tweetfilter;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.core.file.OpenOptions;
import io.vertx.reactivex.RxHelper;
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

  Observable prohibitedWords;

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
//        System.out.println("Read data: " + data.toString("UTF-8"));
        badWords.addAll(Arrays.asList(splitData));
      });
    });

/*
    prohibitedWords = Observable.fromArray(fileSystem.rxReadFile("Word_Filter-Sheet1.csv")
                        .map(buffer -> buffer.toString().replace(",","").split(" "))
                        .toObservable());
    prohibitedWords.subscribe(badWords::add);
*/

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

/*
    System.out.println("text: " + statusInput);
    prohibitedWords.forEach(System.out::println);


    Observable<Boolean> allWordsOk = prohibitedWords.zipWith(statusWords, (prohibited, status) -> {
      boolean retVal = Collections.disjoint(Arrays.asList(prohibited), Arrays.asList(status));
      System.out.println("prohibited: " + prohibited.toString());
      System.out.println("status: " + status);
      System.out.println(retVal);
      return retVal;
    });

    System.out.println(allWordsOk.subscribe());

    List prohibitedArray = Arrays.asList(prohibitedWords);
    prohibitedArray.forEach(System.out::println);

    Single<Boolean> ok = statusWords.all(words -> {
      boolean retVal = Collections.disjoint(Arrays.asList(prohibitedWords), Arrays.asList(words));
      return retVal;
    });

    System.out.println(ok);

    ok.subscribe(allClear ->{
      if (allClear) {
        routingContext.response()
          .putHeader("Content-Type", "text/html")
          .end("Tweetfilter all clear!");
      }else{
        routingContext.response()
          .putHeader("Content-Type", "text/html")
          .end("Tweetfilter match!");
      }
    });
*/

/*
    if (match) {
      routingContext.response()
        .putHeader("Content-Type", "text/html")
        .end("Tweetfilter");
    }else{
      routingContext.response()
        .putHeader("Content-Type", "text/html")
        .end("Tweetfilter");

    }
*/

/*
    prohibitedWords.contains(status).subscribe(res -> {
      if (res) {
        routingContext.response()
          .putHeader("Content-Type", "text/html")
          .end("Tweetfilter match!");
      } else {
        routingContext.response()
          .putHeader("Content-Type", "text/html")
          .end("Tweetfilter all clear!");
      }
    });
*/
  }

/*
    Observable words = Observable.fromArray(status.split(" "));

    words.distinct().forEach(w -> {

    });
    Flowable<String> statusWords = Flowable.fromArray(status.split(" "));

    statusWords.forEach(w -> System.out.println(w));

    statusWords.forEach(w -> {
      prohibitedMatch(w).doOnSuccess();
    }).dispose();
  }

  public Maybe<Boolean> prohibitedMatch(String word) {
    return prohibitedWords.contains(word).toMaybe();
  }
*/


}
