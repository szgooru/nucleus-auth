package org.gooru.nucleus.handlers.auth.processors;

import io.vertx.core.json.JsonObject;

public interface Processor {
  public JsonObject process();
}
