package org.epos.handler.operations.processing;

import static org.epos.handler.support.Utils.gson;

import java.util.Map;
import org.epos.handler.HeaderParser;
import org.epos.handler.enums.APIActionType;
import org.epos.handler.enums.DataOriginType;
import org.epos.handler.interfaces.Operation;
import org.epos.router_framework.exception.RoutingMessageHandlingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class ProcessingPost implements Operation{

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingPost.class); 

	@Override
	public String operationAction(HeaderParser header, Map<String, Object> headers, String payload, JsonObject response) throws RoutingMessageHandlingException {

		JsonObject parameters = new JsonObject();
		if(!payload.equals("") && !header.getOrigin().equals(DataOriginType.INGESTOR)
				&& !header.getOrigin().equals(DataOriginType.BACKOFFICE))
			parameters = gson.fromJson(payload,JsonObject.class);

		LOGGER.info("Received parameters from endpoint: "+parameters.toString());

		JsonObject superParameters = new JsonObject();

		LOGGER.info("params {}" ,  superParameters.toString());

		APIActionType type = APIActionType.valueOf(header.getObject());

		switch(type) {
		case ENVIRONMENT:
			return ProcessingDetailsItemGenerationJPA.generate(response, parameters, header).toString();
		default:
			return "[]";
		}
	}

}
