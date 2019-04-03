package io.vertx.examples.tweetfilter;

import io.reactivex.Flowable;
import io.vertx.core.Future;
import io.vertx.core.file.OpenOptions;
import io.vertx.reactivex.FlowableHelper;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.buffer.Buffer;
import io.vertx.reactivex.core.file.AsyncFile;
import io.vertx.reactivex.core.file.FileSystem;

public class MainVerticle extends AbstractVerticle {

  @Override
  public void start(Future<Void> startFuture) throws Exception {

    FileSystem fileSystem = vertx.fileSystem();
    fileSystem.open("Word_Filter-Sheet1.csv", new OpenOptions(), result -> {
      AsyncFile file = result.result();
      Flowable<Buffer> observable = file.toFlowable();
      observable.forEach(data -> System.out.println("Read data: " + data.toString("UTF-8")));
    });
    startFuture.complete();

  }

}
