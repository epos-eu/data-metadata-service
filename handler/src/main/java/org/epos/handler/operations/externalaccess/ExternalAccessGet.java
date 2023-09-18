package org.epos.handler.operations.externalaccess;

import static org.epos.handler.support.Utils.gson;

import java.util.Map;

import org.epos.handler.HeaderParser;
import org.epos.handler.enums.DataOriginType;
import org.epos.handler.interfaces.Operation;
import org.epos.handler.operations.common.DetailsItemGenerationJPA;
import org.epos.handler.operations.common.PluginGeneration;
import org.epos.router_framework.exception.RoutingMessageHandlingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ExternalAccessGet implements Operation{

	private static final Logger LOGGER = LoggerFactory.getLogger(ExternalAccessGet.class); 

	@Override
	public String operationAction(HeaderParser header, Map<String, Object> headers, String payload, JsonObject response) throws RoutingMessageHandlingException {

		JsonObject parameters = new JsonObject();
		if(payload!="" && !header.getOrigin().equals(DataOriginType.INGESTOR)
				&& !header.getOrigin().equals(DataOriginType.BACKOFFICE))
			parameters = gson.fromJson(payload,JsonObject.class);

		LOGGER.info("Received parameters from endpoint: "+parameters.toString());

		response = DetailsItemGenerationJPA.generate(response, parameters, header);
		if(!response.equals(new JsonObject())) {

			if(!parameters.isJsonNull())
				response.add("params", parameters.getAsJsonObject());
			else
				response.add("params", new JsonObject());
			if(response.has("operationid")) {
				JsonObject conversionParameters = new JsonObject();
				conversionParameters.addProperty("type", "plugins");
				conversionParameters.addProperty("operation", response.get("operationid").getAsString());

				JsonArray softwareConversionList = PluginGeneration.generate(new JsonObject(), conversionParameters, "plugin");
				if(!softwareConversionList.isJsonNull() && !softwareConversionList.get(0).isJsonNull()) {
					JsonObject conversion = softwareConversionList.get(0).getAsJsonObject();
					JsonObject singleConversion = new JsonObject();
					singleConversion.addProperty("operation", response.get("operationid").getAsString());
					singleConversion.addProperty("requestContentType", conversion
							.get("action").getAsJsonObject()
							.get("object").getAsJsonObject()
							.get("encodingFormat").getAsString());
					singleConversion.addProperty("responseContentType", conversion
							.get("action").getAsJsonObject()
							.get("result").getAsJsonObject()
							.get("encodingFormat").getAsString());
					response.add("conversion", singleConversion );
				}
			}
		}
		return response.toString();
	}

}
