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
import org.epos.handler.dbapi.model.EDMWebservice;
import org.epos.handler.dbapi.model.EDMWebserviceCategory;
import org.epos.handler.dbapi.model.EDMWebserviceRelation;
import org.epos.handler.dbapi.service.DBService;
import org.epos.handler.enums.APIActionType;
import org.epos.handler.enums.DataOriginType;
import org.epos.handler.interfaces.Operation;
import org.epos.router_framework.exception.RoutingMessageHandlingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import org.epos.handler.beans.ProcessingServiceSimple;

public class ProcessingGet implements Operation{

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingGet.class); 

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
		case SERVICESLIST:
			EntityManager em = new DBService().getEntityManager();
			List<EDMWebservice> webservicesList = getFromDB(em, EDMWebservice.class, "webservice.findAllByState", "STATE", "PUBLISHED");
			List<CategoryScheme> categorySchemesList = new CategorySchemeDBAPI().getAll().stream()
					.filter(e->e.getUid().contains("category:icsdprocessing"))
					.collect(Collectors.toList());

			List<ProcessingServiceSimple> webservices = new ArrayList<ProcessingServiceSimple>();

			webservicesList.forEach(ws->{
				boolean isHidden = false;
				for( EDMWebserviceCategory cat : ws.getWebserviceCategoriesByInstanceId()) {
					if(cat.getCategoryByCategoryId().getUid().equals("_system_hidden")) 
						isHidden = true;
				}
				if(!isHidden) {
					ws.getWebserviceCategoriesByInstanceId().forEach(cat->{
						System.out.println(cat.getCategoryByCategoryId().getUid());
						categorySchemesList.forEach(sch ->{
							if(sch.getInstanceId().equals(cat.getCategoryByCategoryId().getScheme())) {
								ProcessingServiceSimple los = new ProcessingServiceSimple();
								los.setId(ws.getMetaId());
								los.setName(ws.getName());
								los.setDescription(ws.getDescription());
								los.setDependencyServices(ws.getWebserviceRelationByInstanceId().stream().map(EDMWebserviceRelation::getWebserviceByInstanceWebserviceId_0).map(EDMWebservice::getMetaId).collect(Collectors.toList()));
								webservices.add(los);
							}
						});
					});
				}
			});
			return gson.toJsonTree(webservices).toString();
		default:
			return "[]";
		}
	}

}
