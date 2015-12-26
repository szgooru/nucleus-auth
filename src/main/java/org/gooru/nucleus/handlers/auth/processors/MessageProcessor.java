package org.gooru.nucleus.handlers.auth.processors;

import org.gooru.nucleus.handlers.auth.constants.MessageConstants;
import org.gooru.nucleus.handlers.auth.processors.exceptions.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

public class MessageProcessor implements Processor{
  
  private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
  private Message<Object> message;
  String sessionToken;
  
  public MessageProcessor(Message<Object> message) {
    this.message = message;
  }
  @Override
  public JsonObject process() {
    JsonObject result = null;
    try {
      final String msgOp = message.headers().get(MessageConstants.MSG_HEADER_OP);
      sessionToken = message.headers().get(MessageConstants.MSG_HEADER_TOKEN);
      if(sessionToken == null) {
        LOGGER.error("Unable to authorize. Invalid authorization header");
        throw new InvalidRequestException("Unable to authorize. Invalid authorization header");
      }
      
      switch (msgOp) {
      case MessageConstants.MSG_OP_AUTH_WITH_PREFS:
        result = processAuth();
        break;
      default:
        LOGGER.error("Invalid operation type passed in, not able to handle");
        throw new InvalidRequestException();
      }
      return result;
    } catch (InvalidRequestException ire) {
      //TODO: handle exception
    }
    
    return null;
  }
  
  private JsonObject processAuth() {
    JsonObject result = new JsonObject();
    //TODO:
    //Call redis to validate the session
    //get prefereces 
    //set header to mb response MessageConstants.MSG_OP_STATUS to MSG_OP_STATUS_SUCCESS or MSG_OP_STATUS_ERROR
    result.put(MessageConstants.MSG_OP_STATUS, MessageConstants.MSG_OP_STATUS_SUCCESS);
    result.put(MessageConstants.MSG_KEY_PREFS, new JsonObject());
    result.put(MessageConstants.MSG_USER_ID, MessageConstants.MSG_USER_ANONYMOUS);
    //body to MSG_KEY_PREFS and MSG_USER_ID (where userId would be anonymous or actual user id
    
    return result;
  }

}
