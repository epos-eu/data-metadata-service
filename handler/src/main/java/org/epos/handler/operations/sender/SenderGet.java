package org.epos.handler.operations.sender;

import static org.epos.handler.support.Utils.gson;

import java.util.Map;

import org.epos.handler.HeaderParser;
import org.epos.handler.enums.APIActionType;
import org.epos.handler.enums.DataOriginType;
import org.epos.handler.enums.RequestDomainType;
import org.epos.handler.interfaces.Operation;
import org.epos.handler.operations.common.*;
import org.epos.handler.operations.monitoring.MonitoringGeneration;
import org.epos.router_framework.exception.RoutingMessageHandlingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class SenderGet implements Operation{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(SenderGet.class); 
	
	@Override
	public String operationAction(HeaderParser header, Map<String, Object> headers, String payload, JsonObject response) throws RoutingMessageHandlingException {
		
		JsonObject parameters = new JsonObject();
		parameters = gson.fromJson(payload,JsonObject.class);

		LOGGER.info("Received parameters from endpoint: "+parameters.toString());

		JsonObject superParameters = new JsonObject();

		LOGGER.info("params {}" ,  superParameters.toString());
		
		APIActionType type = APIActionType.valueOf(header.getObject());
		
		switch(type) {
		case SENDEMAIL:
			return ContactPointGet.generate(response, parameters).toString();
			
		default:
			return null;
		}
	}

}
