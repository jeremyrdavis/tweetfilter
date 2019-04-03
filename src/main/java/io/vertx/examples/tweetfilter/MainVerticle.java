package io.vertx.examples.tweetfilter;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.vertx.core.Future;
import io.vertx.core.file.OpenOptions;
import io.vertx.reactivex.FlowableHelper;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.file.AsyncFile;
import io.vertx.reactivex.core.file.FileSystem;

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
    fileSystem
      .rxReadFile("Word_Filter-Sheet1.csv")
      .toFlowable()
      .flatMap(buffer -> Flowable.fromArray(buffer.toString().replace(",", "")))
      .forEach(System.out::println);
/*
    fileSystem.open("Word_Filter-Sheet1.csv", new OpenOptions(), result -> {
      AsyncFile file = result.result();
      Flowable<Buffer> flowable = file.toFlowable();
      flowable
        .flatMap(w -> )
        .forEach(System.out::println);
    });
*/
    startFuture.complete();

  }

}
