package io.vertx.examples.tweetfilter;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.junit5.Checkpoint;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.assertj.core.api.Assertions.*;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  @DisplayName("Should start a Web Server on port 8888")
  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  void start_http_server(Vertx vertx, VertxTestContext testContext) throws Throwable {
    vertx.createHttpClient().getNow(8888, "localhost", "/", response -> testContext.verify(() -> {
      assertTrue(response.statusCode() == 200);
      response.handler(body -> {
        assertTrue(body.toString().contains("Hello from Vert.x!"));
        testContext.completeNow();
      });
    }));
  }

  @Timeout(value = 2, timeUnit = TimeUnit.SECONDS)
  @Test
  @DisplayName("Should return all clear")
  public void testThatWordIsCaught(Vertx vertx, VertxTestContext tc) {

    WebClient webClient = WebClient.create(vertx);
    Checkpoint deploymentCheckpoint = tc.checkpoint();
    Checkpoint requestCheckpoint = tc.checkpoint();

    vertx.deployVerticle(new MainVerticle(), tc.succeeding(id -> {

      deploymentCheckpoint.flag();

      webClient.post(8081, "localhost", "/api/censor")
        .as(BodyCodec.string())
        .sendJsonObject(new JsonObject().put("text", "cats are ass"), tc.succeeding(resp -> {
          tc.verify(() -> {
            assertThat(resp.statusCode()).isEqualTo(200);
            assertThat(resp.body()).isNotEmpty();
            assertThat(resp.body()).contains("Tweetfilter all clear!");
            requestCheckpoint.flag();
          });
        }));
    }));

  }


}
