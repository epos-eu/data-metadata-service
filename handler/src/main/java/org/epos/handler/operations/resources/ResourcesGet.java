package org.epos.handler.operations.resources;

import static org.epos.handler.support.Utils.gson;

import java.util.Map;

import org.epos.handler.HeaderParser;
import org.epos.handler.enums.APIActionType;
import org.epos.handler.enums.DataOriginType;
import org.epos.handler.enums.RequestDomainType;
import org.epos.handler.interfaces.Operation;
import org.epos.handler.operations.common.*;
import org.epos.handler.operations.monitoring.MonitoringGeneration;
import org.epos.handler.operations.sender.ContactPointGet;
import org.epos.router_framework.exception.RoutingMessageHandlingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class ResourcesGet implements Operation{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourcesGet.class); 
	
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
		case SEARCH:
			if(header.getDomain().equals(RequestDomainType.RESOURCES))
				return SearchGenerationJPA.generate(response, parameters).toString();
			if(header.getDomain().equals(RequestDomainType.TNA))
				return TNASearchGeneration.generate(response, parameters).toString();
			return null;
		case DETAILS:
			if(header.getDomain().equals(RequestDomainType.RESOURCES))
				return DetailsItemGenerationJPA.generate(response, parameters, header).toString();
			if(header.getDomain().equals(RequestDomainType.TNA))
				return TNADetailsItemGeneration.generate(response, parameters, header).toString();
			return null;
		case MONITORING:
			return MonitoringGeneration.generate(response, parameters, header).toString();
		case PLUGINS:
			return PluginGeneration.generate(response, parameters, "plugin").toString();
		case VALIDATIONS:
			return new JsonObject().toString();
		case SHOWFACILITIES:
			return FacilitiesGeoJsonGeneration.generate(response, parameters).toString();
		case SHOWEQUIPMENTS:
			return EquipmentsGeoJsonGeneration.generate(response, parameters).toString();
			
		default:
			return DataModelItemGeneration.generate(response, parameters, type).toString();
		}
	}

}
