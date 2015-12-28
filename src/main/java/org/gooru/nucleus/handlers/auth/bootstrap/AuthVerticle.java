package org.gooru.nucleus.handlers.auth.bootstrap;

import org.gooru.nucleus.handlers.auth.app.components.RedisServer;
import org.gooru.nucleus.handlers.auth.constants.MessageConstants;
import org.gooru.nucleus.handlers.auth.constants.MessagebusEndpoints;
import org.gooru.nucleus.handlers.auth.processors.exceptions.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

public class AuthVerticle extends AbstractVerticle {

  static final Logger LOGGER = LoggerFactory.getLogger(AuthVerticle.class);

  @Override
  public void start() throws Exception {

    startApplication();

    EventBus eb = vertx.eventBus();

    eb.consumer(MessagebusEndpoints.MBEP_AUTH, message -> {
      vertx.executeBlocking(future -> {
        String msgOp = message.headers().get(MessageConstants.MSG_HEADER_OP);
        String sessionToken = message.headers().get(MessageConstants.MSG_HEADER_TOKEN);

        if (sessionToken == null) {
          LOGGER.error("Unable to authorize. Invalid authorization header");
          throw new InvalidRequestException("Unable to authorize. Invalid authorization header");
        }

        if (msgOp.equalsIgnoreCase(MessageConstants.MSG_OP_AUTH_WITH_PREFS)) {

          RedisClient redisClient = RedisServer.getInstance().getClient();
          redisClient.get(sessionToken, getHandler -> {
            JsonObject result;
            if (getHandler.succeeded()) {
              if(getHandler.result() != null) {
              result = new JsonObject(getHandler.result());
              result.put(MessageConstants.MSG_OP_STATUS, MessageConstants.MSG_OP_STATUS_SUCCESS);
              
              redisClient.expire(sessionToken, 60 * 60, updateHandler -> {
                if (updateHandler.succeeded()) {
                  LOGGER.info("expiry time of session {} is updated", sessionToken);
                }
              });
              } else {
                LOGGER.error("Session not found. Invalid session");
                result = new JsonObject().put(MessageConstants.MSG_OP_STATUS, MessageConstants.MSG_OP_STATUS_ERROR);
              }
            } else {
              LOGGER.error("unable to get key from redis");
              result = new JsonObject().put(MessageConstants.MSG_OP_STATUS, MessageConstants.MSG_OP_STATUS_ERROR);
            }
            future.complete(result);
          });
        } else {
          throw new InvalidRequestException();
        }
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
