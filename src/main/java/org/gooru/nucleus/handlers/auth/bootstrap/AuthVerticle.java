package org.gooru.nucleus.handlers.auth.bootstrap;

import org.gooru.nucleus.handlers.auth.app.components.RedisServer;
import org.gooru.nucleus.handlers.auth.constants.MessageConstants;
import org.gooru.nucleus.handlers.auth.constants.MessagebusEndpoints;
import org.gooru.nucleus.handlers.auth.processors.ProcessorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

public class AuthVerticle extends AbstractVerticle {

  static final Logger LOGGER = LoggerFactory.getLogger(AuthVerticle.class);

  @Override
  public void start() throws Exception {
    
    startApplication();

    EventBus eb = vertx.eventBus();

    eb.consumer(MessagebusEndpoints.MBEP_AUTH, message -> {
      vertx.executeBlocking(future -> {
        JsonObject result = new ProcessorBuilder(message).build().process();
        future.complete(result);
      } , res -> {
        JsonObject result = (JsonObject) res.result();
        DeliveryOptions options = new DeliveryOptions().addHeader(MessageConstants.MSG_OP_STATUS, result.getString(MessageConstants.MSG_OP_STATUS));
        message.reply(result, options);
      });
    }).completionHandler(result -> {
      if (result.succeeded()) {
        LOGGER.info("Auth end point ready to listen");
      } else {
        LOGGER.error("Error registering the auth handler. Halting the auth machinery");
        Runtime.getRuntime().halt(1);
      }
    });
  }

  @Override
  public void stop() throws Exception {
    super.stop();
    RedisServer.getInstance().finalizeRedisClient();
  }
  
  private void startApplication() {
    RedisServer.getInstance().initializeRedisClient(vertx, config());
  }

}
