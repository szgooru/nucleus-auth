package org.gooru.nucleus.handlers.auth.services;

import io.vertx.core.json.JsonObject;

public interface Service {

  public void validateSession(String sessionToken);
  
  public void updateExpiryTime(String sessionToken, long expiry);
  
  public JsonObject getSessionData();
}
