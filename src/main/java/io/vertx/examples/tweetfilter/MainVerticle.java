package io.vertx.examples.tweetfilter;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.vertx.core.Future;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.file.FileSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Thanks to @medal.tv
 * https://gist.github.com/PimDeWitte
 * https://gist.github.com/PimDeWitte/c04cc17bc5fa9d7e3aee6670d4105941
 * for the list of words
 */
public class MainVerticle extends AbstractVerticle {


  @Override
  public void start(Future<Void> startFuture) throws Exception {

    FileSystem fileSystem = vertx.fileSystem();

    Single<String> words =
    fileSystem
      .rxReadFile("Word_Filter-Sheet1.csv")
      .map(buffer -> buffer.toString().replace(",",""));

    words.contains("ass").subscribe(onNext -> {
      System.out.println("match");
    });

    words.toFlowable().forEach(word -> System.out.println(word));

    startFuture.complete();
  }

}
