package org.epos.handler.operations.processing;

import static org.epos.handler.dbapi.util.DBUtil.getFromDB;
import static org.epos.handler.support.Utils.gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.epos.eposdatamodel.CategoryScheme;
import org.epos.handler.HeaderParser;
import org.epos.handler.dbapi.dbapiimplementation.CategorySchemeDBAPI;
import org.epos.handler.dbapi.model.EDMDataproduct;
import org.epos.handler.dbapi.model.EDMDistribution;
import org.epos.handler.dbapi.model.EDMDistributionDescription;
import org.epos.handler.dbapi.model.EDMDistributionTitle;
import org.epos.handler.dbapi.model.EDMIspartofDataproduct;
import org.epos.handler.dbapi.service.DBService;
import org.epos.handler.enums.APIActionType;
import org.epos.handler.enums.DataOriginType;
import org.epos.handler.interfaces.Operation;
import org.epos.handler.operations.resources.SearchGenerationJPA;
import org.epos.router_framework.exception.RoutingMessageHandlingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import org.epos.handler.beans.ProcessingServiceSimple;

public class ProcessingPost implements Operation{

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingPost.class); 

	@Override
	public String operationAction(HeaderParser header, Map<String, Object> headers, String payload, JsonObject response) throws RoutingMessageHandlingException {

		System.out.println("IMMA HERE");
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
