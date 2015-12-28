package org.gooru.nucleus.handlers.auth.app.components;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

public class RedisServer {

  private static Logger LOGGER = LoggerFactory.getLogger(RedisServer.class);

  private static final String DEFAULT_REDIS_CONFIG = "redisConfig";
  private static RedisServer redisServer = null;
  private static RedisClient client = null;

  private RedisServer() {

  }

  public void initializeRedisClient(Vertx vertx, JsonObject config) {
    JsonObject redisConfig = config.getJsonObject(DEFAULT_REDIS_CONFIG);
    RedisOptions redisOptions = new RedisOptions();
    for (Map.Entry<String, Object> entry : redisConfig) {
      switch (entry.getKey()) {
      case "host":
        redisOptions.setHost((String) entry.getValue());
        break;
      case "port":
        redisOptions.setPort(Integer.valueOf(entry.getValue().toString()));
        break;
      case "encoding":
        redisOptions.setEncoding((String) entry.getValue());
        break;
      case "tcpKeepAlive":
        redisOptions.setTcpKeepAlive((Boolean)entry.getValue());
        break;
      case "tcpNoDelay":
        redisOptions.setTcpNoDelay((Boolean) entry.getValue());
        break;
      case "auth":
        redisOptions.setAuth((String) entry.getValue());
        break;
      case "select":
        redisOptions.setSelect(Integer.valueOf((String) entry.getValue()));
        break;
      }
    }
    
    client = RedisClient.create(vertx, redisOptions);
    if(client != null) {
      LOGGER.info("Redis Client initialized successfully");
    } else {
      LOGGER.error("Error in redis client initialization, check configuration");
    }
  }

  public void finalizeRedisClient() {
    if (client != null) {
      client.close(closeHandler -> {
        if (closeHandler.succeeded()) {
          LOGGER.debug("Redis client has been closed successfully");
        } else {
          LOGGER.error("Error in closing redis client", closeHandler.cause());
        }
      });
    }
  }

  public RedisClient getClient() {
    return client;
  }

  public static RedisServer getInstance() {
    if (redisServer != null) {
      return redisServer;
    }

    return new RedisServer();
  }
}
