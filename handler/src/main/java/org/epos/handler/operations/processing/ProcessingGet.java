package org.epos.handler.operations.processing;

import static org.epos.handler.dbapi.util.DBUtil.getFromDB;
import static org.epos.handler.support.Utils.gson;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.epos.eposdatamodel.CategoryScheme;
import org.epos.eposdatamodel.State;
import org.epos.handler.HeaderParser;
import org.epos.handler.dbapi.dbapiimplementation.CategorySchemeDBAPI;
import org.epos.handler.dbapi.model.EDMEdmEntityId;
import org.epos.handler.dbapi.model.EDMOrganization;
import org.epos.handler.dbapi.model.EDMOrganizationLegalname;
import org.epos.handler.dbapi.model.EDMWebservice;
import org.epos.handler.dbapi.model.EDMWebserviceCategory;
import org.epos.handler.dbapi.model.EDMWebserviceRelation;
import org.epos.handler.dbapi.service.DBService;
import org.epos.handler.enums.APIActionType;
import org.epos.handler.enums.DataOriginType;
import org.epos.handler.interfaces.Operation;
import org.epos.handler.operations.common.DetailsItemGenerationJPA;
import org.epos.router_framework.exception.RoutingMessageHandlingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import org.epos.handler.beans.DataServiceProvider;
import org.epos.handler.beans.ProcessingServiceSimple;import org.epos.handler.constants.EnvironmentVariables;

public class ProcessingGet implements Operation{

	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessingGet.class); 

	private static final String API_PATH_DETAILS  = EnvironmentVariables.API_CONTEXT+"/processing/details/";

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
			EntityManager em = new DBService().getEntityManager();
			List<EDMWebservice> webservicesList = getFromDB(em, EDMWebservice.class, "webservice.findAllByState", "STATE", "PUBLISHED");
			List<CategoryScheme> categorySchemesList = new CategorySchemeDBAPI().getAll().stream()
					.filter(e->e.getUid().contains("category:icsdprocessing"))
					.collect(Collectors.toList());

			Map<String, List<ProcessingServiceSimple>> returnMap = new HashMap<String, List<ProcessingServiceSimple>>();

			webservicesList.forEach(ws->{
				boolean isHidden = false;
				for( EDMWebserviceCategory cat : ws.getWebserviceCategoriesByInstanceId()) {
					if(cat.getCategoryByCategoryId().getUid().equals("_system_hidden")) 
						isHidden = true;
				}
				for( EDMWebserviceCategory cat : ws.getWebserviceCategoriesByInstanceId()) {
					System.out.println(cat.getCategoryByCategoryId().getName());
					if(cat.getCategoryByCategoryId().getUid().contains("environment_type")) 
						returnMap.put(cat.getCategoryByCategoryId().getName(), new ArrayList<ProcessingServiceSimple>());
				}
				if(!isHidden) {
					String category = null;
					ProcessingServiceSimple los = null;
					for(EDMWebserviceCategory cat : ws.getWebserviceCategoriesByInstanceId()) {
						if(cat.getCategoryByCategoryId().getUid().contains("environment_type")) {
							category = cat.getCategoryByCategoryId().getName();
						}
						for(CategoryScheme sch : categorySchemesList) {
							if(sch.getInstanceId().equals(cat.getCategoryByCategoryId().getScheme())) {
								los = new ProcessingServiceSimple();
								los.setId(ws.getMetaId());
								los.setName(ws.getName());
								los.setDescription(ws.getDescription());
								los.setHref(EnvironmentVariables.API_HOST + API_PATH_DETAILS + ws.getMetaId());
								//los.setProvider(getProviders(List.of(ws.getEdmEntityIdByProvider())));
								if (ws.getEdmEntityIdByProvider() != null) {
									List<DataServiceProvider> serviceProviders = getProviders(List.of(ws.getEdmEntityIdByProvider()));
									if (!serviceProviders.isEmpty()){
										los.setProvider(serviceProviders.get(0).getDataProviderLegalName());
									}
								}
							}
						}
					}
					if(category!=null && los!=null) {
						returnMap.get(category).add(los);
					}
				}
			});
			return gson.toJsonTree(returnMap).toString();
		case DETAILS:
			return ProcessingDetailsItemGenerationJPA.generate(response, parameters, header).toString();
		default:
			return "[]";
		}
	}
	
	private static List<DataServiceProvider> getProviders(List<EDMEdmEntityId> organizationsCollection) {
		List<EDMOrganization> organizations = new ArrayList<>();
		for (EDMEdmEntityId edmMetaId : organizationsCollection) {
			if (edmMetaId.getOrganizationsByMetaId() != null && !edmMetaId.getOrganizationsByMetaId().isEmpty()) {
				ArrayList<EDMOrganization> list = edmMetaId.getOrganizationsByMetaId().stream()
						.filter(e -> e.getState().equals(State.PUBLISHED.toString()))
						.collect(Collectors.toCollection(ArrayList::new));
				organizations.addAll(list);
			}
		}

		List<DataServiceProvider> organizationStructure = new ArrayList<>();
		for (EDMOrganization org : organizations) {
			// only take into account the organization with legalname
			if (org.getOrganizationLegalnameByInstanceId() != null && !org.getOrganizationLegalnameByInstanceId().isEmpty()) {

				// se il data provider nei metadati è un figlio -> allora mostra in gui solo il figlio
				// se il data provider indicato nei metadati è un padre -> allora mostrare in gui il padre con tutti i suoi figli

				String mainOrganizationLegalName;
				List<DataServiceProvider> relatedOrganizations = new ArrayList<>();

				mainOrganizationLegalName = org.getOrganizationLegalnameByInstanceId().stream()
						.map(EDMOrganizationLegalname::getLegalname)
						.collect(Collectors.joining("."));

				if (Objects.nonNull(org.getSon()) && !org.getSon().isEmpty()) {
					relatedOrganizations.addAll(
							org.getSon().stream()
							.filter(relatedOrganization ->
							relatedOrganization.getOrganizationLegalnameByInstanceId() != null &&
							!relatedOrganization.getOrganizationLegalnameByInstanceId().isEmpty())
							.map(relatedOrganization -> {

								String relatedOrganizationLegalName = relatedOrganization.getOrganizationLegalnameByInstanceId()
										.stream().map(EDMOrganizationLegalname::getLegalname)
										.collect(Collectors.joining("."));
								DataServiceProvider relatedDataprovider = new DataServiceProvider();
								relatedDataprovider.setDataProviderLegalName(relatedOrganizationLegalName);
								relatedDataprovider.setDataProviderUrl(relatedOrganization.getUrl());
								if(relatedOrganization.getAddressByAddressId()!=null)relatedDataprovider.setCountry(relatedOrganization.getAddressByAddressId().getCountry());
								return relatedDataprovider;

							})
							.collect(Collectors.toList())
							);
					relatedOrganizations.sort(Comparator.comparing(DataServiceProvider::getDataProviderLegalName));
				}

				DataServiceProvider dataServiceProvider = new DataServiceProvider();
				dataServiceProvider.setDataProviderLegalName(mainOrganizationLegalName);
				dataServiceProvider.setRelatedDataProvider(relatedOrganizations);
				dataServiceProvider.setDataProviderUrl(org.getUrl());
				if(org.getAddressByAddressId()!=null) dataServiceProvider.setCountry(org.getAddressByAddressId().getCountry());

				organizationStructure.add(dataServiceProvider);

			}

		}


		organizationStructure.sort(Comparator.comparing(DataServiceProvider::getDataProviderLegalName));
		return organizationStructure;
	}

}
