package org.gooru.nucleus.handlers.auth.services;

import org.gooru.nucleus.handlers.auth.app.components.RedisServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;

public class RedisService implements Service {
  
  private static Logger LOGGER = LoggerFactory.getLogger(RedisService.class);
  private JsonObject sessionData = null;
  
  @Override
  public void validateSession(String sessionToken) {
    RedisClient redisClient = RedisServer.getInstance().getClient();
    LOGGER.info("validating session for token {}", sessionToken);
    
    redisClient.get(sessionToken, handler -> {
      if(handler.succeeded()) {
        sessionData = new JsonObject(handler.result());
        LOGGER.info("handler.succeed");
      } else {
        LOGGER.error("unable to get key from redis, may be invalid session");
      }
    });
  }

  @Override
  public JsonObject getSessionData() {
    return sessionData;
  }

  @Override
  public void updateExpiryTime(String sessionToken, long expiry) {
    RedisClient redisClient = RedisServer.getInstance().getClient();
    redisClient.expireat(sessionToken, expiry, handler -> {
      if(handler.succeeded()) {
        LOGGER.info("expiry time of session {} is updated by {} seconds", sessionToken, expiry);
      }
    });
  }

}
