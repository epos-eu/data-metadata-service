package org.epos.handler.operations.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.micrometer.core.lang.NonNull;

import org.apache.commons.codec.digest.DigestUtils;
import org.epos.eposdatamodel.*;
import org.epos.handler.beans.DiscoveryItem;
import org.epos.handler.beans.OrganizationBean;
import org.epos.handler.beans.DiscoveryItem.DiscoveryItemBuilder;
import org.epos.handler.constants.EnvironmentVariables;
import org.epos.handler.dbapi.model.*;
import org.epos.handler.dbapi.service.DBService;
import org.epos.handler.dbapi.util.EDMUtil;
import org.epos.handler.facets.FacetsGeneration;
import org.epos.handler.operations.common.AvailableFormatsGeneration;
import org.epos.handler.operations.monitoring.ZabbixExecutor;
import org.epos.handler.support.BBoxToPolygon;
import org.epos.handler.support.Utils;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.*;
import java.util.stream.Collectors;
import static org.epos.handler.dbapi.util.DBUtil.getFromDB;
import static org.epos.handler.operations.resources.FilterSearch.checkTemporalExtent;

public class SearchGenerationJPA {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchGenerationJPA.class);

	private static final String API_PATH_DETAILS  = EnvironmentVariables.API_CONTEXT+"/resources/details?id=";

	private static final String NORTHEN_LAT  = "epos:northernmostLatitude";
	private static final String SOUTHERN_LAT  = "epos:southernmostLatitude";
	private static final String WESTERN_LON  = "epos:westernmostLongitude";
	private static final String EASTERN_LON  = "epos:easternmostLongitude";

	private static final String PARAMETER__SCIENCE_DOMAIN = "sciencedomains";
	private static final String PARAMETER__SERVICE_TYPE = "servicetypes";

	public static JsonObject generate(JsonObject response, JsonObject parameters) {

		LOGGER.info("Requests start - JPA method");

		long startTime = System.currentTimeMillis();
		EntityManager em = new DBService().getEntityManager();

		List<EDMDataproduct> dataproductsNoFilter = getFromDB(em, EDMDataproduct.class, "dataproduct.findAllByState", "STATE", "PUBLISHED");
		List<EDMDistribution> edmDistributions = getFromDB(em, EDMDistribution.class, "distribution.findAllByState", "STATE", "PUBLISHED");
		//List<EDMOrganization> edmOrganizations = getFromDB(em, EDMOrganization.class, "organization.findAllByState", "STATE", "PUBLISHED");
		em.close();

		List<EDMDataproduct> dataproducts = doFilters(dataproductsNoFilter, parameters);

		ArrayList<DiscoveryItem> discoveryList = new ArrayList<>();

		LOGGER.info("Start for each");

		Set<String> keywords = new HashSet<>();
		Set<EDMCategory> scienceDomains = new HashSet<>();
		Set<EDMCategory> serviceTypes = new HashSet<>();
		Set<EDMOrganization> organizations = new HashSet<>();


		dataproducts.forEach(dataproduct -> {
			Set<String> facetsDataProviders = new HashSet<String>();
			/*String ddss = dataproduct.getDataproductIdentifiersByInstanceId().stream()
					.filter(identifier -> identifier.getType().equals("DDSS-ID")).findFirst().orElse(new EDMDataproductIdentifier()).getIdentifier();*/

			List<String> categoryList = dataproduct.getDataproductCategoriesByInstanceId().stream()
					.map(EDMDataproductCategory::getCategoryByCategoryId)
					.filter(Objects::nonNull)
					.filter(e->e.getUid().contains("category:"))
					.map(EDMCategory::getUid)
					.collect(Collectors.toList());
			
			List<EDMWebservice> webservices = dataproduct.getIsDistributionsByInstanceId().stream()
					.map(EDMIsDistribution::getDistributionByInstanceDistributionId)
					.filter(Objects::nonNull)
					.map(EDMDistribution::getWebserviceByAccessService)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());

			//dataproduct organization for filter
			if(dataproduct.getPublishersByInstanceId() != null ) {
				for (EDMEdmEntityId edmMetaId : dataproduct.getPublishersByInstanceId().stream().map(EDMPublisher::getEdmEntityIdByMetaOrganizationId).collect(Collectors.toList())) {
					if (edmMetaId.getOrganizationsByMetaId() != null && !edmMetaId.getOrganizationsByMetaId().isEmpty()) {
						ArrayList<EDMOrganization> list = new ArrayList<>(edmMetaId.getOrganizationsByMetaId());
						list.sort(EDMUtil::compareEntityVersion);
						EDMOrganization edmDataproductRelatedOrganization = list.get(0);
						if(edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId() != null && !edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId().isEmpty()) {
							organizations.add(edmDataproductRelatedOrganization);
						}
					}
				}
			}

			//webservice organization for filter
			List<EDMEdmEntityId> serviceProviders = webservices.stream()
					.map(EDMWebservice::getEdmEntityIdByProvider)
					.filter(Objects::nonNull)
					.collect(Collectors.toList());
			for (EDMEdmEntityId edmMetaId : serviceProviders){
				if (edmMetaId.getOrganizationsByMetaId() != null && !edmMetaId.getOrganizationsByMetaId().isEmpty()) {
					ArrayList<EDMOrganization> list = new ArrayList<>(edmMetaId.getOrganizationsByMetaId());
					list.sort(EDMUtil::compareEntityVersion);
					EDMOrganization edmDataproductRelatedOrganization = list.get(0);
					if(edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId() != null && !edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId().isEmpty()) {
						organizations.add(edmDataproductRelatedOrganization);
					}
				}
			}

			if (dataproduct.getIsDistributionsByInstanceId() != null) {
				ArrayList<String> distrs = new ArrayList<>();
				dataproduct.getIsDistributionsByInstanceId().forEach(dist ->
				distrs.add(dist.getDistributionByInstanceDistributionId().getMetaId())
						);
				edmDistributions.forEach(distribution -> {
					Set<String> facetsServiceProviders = new HashSet<String>();
					if (distrs.contains(distribution.getMetaId())) {
						// distribution
						String title = null;
						if (distribution.getDistributionTitlesByInstanceId() != null && !distribution.getDistributionTitlesByInstanceId().isEmpty()) {
							EDMDistributionTitle edmDistributionTitles = new ArrayList<>(distribution.getDistributionTitlesByInstanceId()).get(0);
							title = edmDistributionTitles.getTitle();
						}

						String description = null;
						if (distribution.getDistributionDescriptionsByInstanceId() != null && !distribution.getDistributionDescriptionsByInstanceId().isEmpty()) {
							EDMDistributionDescription edmDistributionDescription = new ArrayList<>(distribution.getDistributionDescriptionsByInstanceId()).get(0);
							description = edmDistributionDescription.getDescription();
						}

						if(dataproduct.getPublishersByInstanceId() != null ) {
							for (EDMEdmEntityId edmMetaId : dataproduct.getPublishersByInstanceId().stream().map(EDMPublisher::getEdmEntityIdByMetaOrganizationId).collect(Collectors.toList())) {
								if (edmMetaId.getOrganizationsByMetaId() != null && !edmMetaId.getOrganizationsByMetaId().isEmpty()) {
									ArrayList<EDMOrganization> list = new ArrayList<>(edmMetaId.getOrganizationsByMetaId());
									list.sort(EDMUtil::compareEntityVersion);
									EDMOrganization edmDataproductRelatedOrganization = list.get(0);
									if(edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId() != null && !edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId().isEmpty()) {
										facetsDataProviders.add(edmDataproductRelatedOrganization.getOrganizationLegalnameByInstanceId().stream().findFirst().get().getLegalname());
									}
								}
							}
						}

						if(distribution.getWebserviceByAccessService() != null && distribution.getWebserviceByAccessService().getEdmEntityIdByProvider() != null) {
							EDMEdmEntityId edmMetaId = distribution.getWebserviceByAccessService().getEdmEntityIdByProvider();
							if (edmMetaId.getOrganizationsByMetaId() != null && !edmMetaId.getOrganizationsByMetaId().isEmpty()) {
								ArrayList<EDMOrganization> list = new ArrayList<>(edmMetaId.getOrganizationsByMetaId());
								list.sort(EDMUtil::compareEntityVersion);
								EDMOrganization edmWebserviceRelatedOrganization = list.get(0);
								if(edmWebserviceRelatedOrganization.getOrganizationLegalnameByInstanceId() != null && !edmWebserviceRelatedOrganization.getOrganizationLegalnameByInstanceId().isEmpty()) {
									facetsServiceProviders.add(edmWebserviceRelatedOrganization.getOrganizationLegalnameByInstanceId().stream().findFirst().get().getLegalname());
								}
							}
						}

						discoveryList.add(new DiscoveryItemBuilder(distribution.getMetaId(),
								EnvironmentVariables.API_HOST + API_PATH_DETAILS + distribution.getMetaId())
								.uid(distribution.getUid())
								.title(title)
								.description(description)
								.availableFormats(AvailableFormatsGeneration.generate(distribution))
								.setSha256id(DigestUtils.sha256Hex(distribution.getUid()))
								.setDataprovider(facetsDataProviders)
								.setServiceProvider(facetsServiceProviders)
								.setDataproductCategories(categoryList.isEmpty()? null : categoryList)
								.build());
					}
				});
			}

			//filter section
			//keyword for filter
			if(dataproduct.getKeywords()!=null) {
				keywords.addAll(Arrays.stream(dataproduct.getKeywords().split(",\t")).map(String::toLowerCase).map(String::trim).collect(Collectors.toList()));
			}


			//scienceDomain for filter
			if (Objects.nonNull(dataproduct.getDataproductCategoriesByInstanceId()) && !dataproduct.getDataproductCategoriesByInstanceId().isEmpty()){
				scienceDomains.addAll(dataproduct.getDataproductCategoriesByInstanceId().stream()
						.map(EDMDataproductCategory::getCategoryByCategoryId)
						.filter(Objects::nonNull).collect(Collectors.toList()));
			}

			//serviceType for filter
			webservices.forEach(webservice -> {
				if (Objects.nonNull(webservice.getWebserviceCategoriesByInstanceId()) && !webservice.getWebserviceCategoriesByInstanceId().isEmpty()){
					serviceTypes.addAll(webservice.getWebserviceCategoriesByInstanceId().stream()
							.map(EDMWebserviceCategory::getCategoryByCategoryId)
							.filter(Objects::nonNull).collect(Collectors.toList()));
				}
			});
		});

		LOGGER.info("Final number of results: "+discoveryList.size());
		//monitoring
		if(EnvironmentVariables.MONITORING.equals("true")) {
			LOGGER.info("Monitoring cross-validation activated, check if the services are up and running...");

			for(JsonElement item : ZabbixExecutor.getInstance().getHostResults()) {
				discoveryList.forEach(dlitem->{
					if(item.getAsJsonObject().get("id").getAsString().equals(dlitem.getSha256id())) {
						dlitem.setStatus(item.getAsJsonObject().get("status").getAsInt());
						if(dlitem.getStatus()==2) {
							dlitem.setStatusTimestamp(item.getAsJsonObject().get("timestamp").getAsString());
						}
					}
				});
			}
		}

		if(parameters.has("facets") && parameters.get("facets").getAsString().equals("true")) {
			switch(parameters.get("facetstype").getAsString()) {
			case "categories":
				FacetsGeneration.generateResponseUsingCategories(response, discoveryList);
				break;
			case "dataproviders":
				FacetsGeneration.generateResponseUsingDataproviders(response, discoveryList);
				break;
			case "serviceproviders":
				FacetsGeneration.generateResponseUsingServiceproviders(response, discoveryList);
				break;
			default:
				break;
			}

		}else {
			response.add("results", Utils.gson.toJsonTree(discoveryList));
		}

		LOGGER.info("Number of organizations retrieved "+organizations.size());

		JsonArray filters = new JsonArray();
		JsonArray kw = new JsonArray();
		ArrayList<String> keywordsCollection = new ArrayList<>(keywords);

		keywordsCollection.removeIf(Objects::isNull);


		Collections.sort(keywordsCollection);

		ArrayList<EDMOrganization> organizationsCollection = new ArrayList<>(organizations);
		organizationsCollection.sort((o1, o2) -> {
			String legalNameOrganization1 = o1.getOrganizationLegalnameByInstanceId().stream().map(EDMOrganizationLegalname::getLegalname).collect(Collectors.toList()).get(0);
			String legalNameOrganization2 = o2.getOrganizationLegalnameByInstanceId().stream().map(EDMOrganizationLegalname::getLegalname).collect(Collectors.toList()).get(0);
			return legalNameOrganization1.compareTo(legalNameOrganization2);
		});


		keywordsCollection.forEach(r -> {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("name", r);
			jsonObject.addProperty("id", Base64.getEncoder().encodeToString(r.getBytes()));
			kw.add(jsonObject);
		});
		JsonObject keywordsObject = new JsonObject();
		keywordsObject.addProperty("name", "keywords");
		keywordsObject.add("children", kw);

		JsonArray orgs = new JsonArray();
		organizationsCollection.forEach(r -> {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("name",r.getOrganizationLegalnameByInstanceId().stream().map(EDMOrganizationLegalname::getLegalname).collect(Collectors.toList()).get(0));
			jsonObject.addProperty("id", r.getMetaId());
			orgs.add(jsonObject);
		});

		JsonObject organizationsObject = new JsonObject();
		organizationsObject.addProperty("name", "organisations");
		organizationsObject.add("children", orgs);


		JsonArray scienceDomainArray = new JsonArray();
		scienceDomains.forEach(r -> {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("name",r.getName());
			jsonObject.addProperty("id", r.getId());
			scienceDomainArray.add(jsonObject);
		});

		JsonObject scienceDomainObject = new JsonObject();
		scienceDomainObject.addProperty("name", PARAMETER__SCIENCE_DOMAIN);
		scienceDomainObject.add("children", scienceDomainArray);


		JsonArray serviceTypeArray = new JsonArray();
		serviceTypes.forEach(r -> {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("name",r.getName());
			jsonObject.addProperty("id", r.getId());
			serviceTypeArray.add(jsonObject);
		});

		JsonObject serviceTypeObject = new JsonObject();
		serviceTypeObject.addProperty("name", PARAMETER__SERVICE_TYPE);
		serviceTypeObject.add("children", serviceTypeArray);


		filters.add(keywordsObject);
		filters.add(organizationsObject);
		filters.add(scienceDomainObject);
		filters.add(serviceTypeObject);

		response.add("filters", filters);


		long endTime = System.currentTimeMillis();
		long duration = (endTime - startTime);

		LOGGER.info("Result done in ms: "+duration);

		return response;

	}


	public static List<EDMDataproduct> doFilters(List<EDMDataproduct> datasetList, JsonObject parameters) {

		PeriodOfTime temporal = checkTemporalExtent(parameters);

		//ArrayList<String> orgIds = checkOrganizations(parameters);

		datasetList = filterByFullText(datasetList, parameters);

		datasetList = filterByKeywords(datasetList, parameters);

		datasetList = filterByOrganizations(datasetList, parameters);

		datasetList = filterByDateRange(datasetList, temporal);

		datasetList = filterByBoundingBox(datasetList, parameters);

		datasetList = filterByScienceDomain(datasetList, parameters);

		datasetList = filterByServiceType(datasetList, parameters);


		return datasetList;
	}

	private static List<EDMDataproduct> filterByScienceDomain(List<EDMDataproduct> datasetList, JsonObject parameters) {
		if(parameters.has(PARAMETER__SCIENCE_DOMAIN)) {
			ArrayList<EDMDataproduct> tempDatasetList = new ArrayList<>();
			List<String> scienceDomainsParameters = List.of(parameters.get(PARAMETER__SCIENCE_DOMAIN).getAsString().split(","));
			datasetList.forEach(dataproduct -> {
				if (Objects.nonNull(dataproduct.getDataproductCategoriesByInstanceId()) && !dataproduct.getDataproductCategoriesByInstanceId().isEmpty()){
					List<String> scienceDomainOfDataproduct = dataproduct.getDataproductCategoriesByInstanceId().stream()
							.map(EDMDataproductCategory::getCategoryByCategoryId)
							.filter(Objects::nonNull)
							.map(EDMCategory::getId)
							.collect(Collectors.toList());

					if(!Collections.disjoint(scienceDomainOfDataproduct, scienceDomainsParameters)){
						tempDatasetList.add(dataproduct);
					}
				}
			});
			datasetList = tempDatasetList;
		}
		return datasetList;
	}

	private static List<EDMDataproduct> filterByServiceType(List<EDMDataproduct> datasetList, JsonObject parameters) {
		if (parameters.has(PARAMETER__SERVICE_TYPE)) {
			ArrayList<EDMDataproduct> tempDatasetList = new ArrayList<>();
			List<String> serviceTypesParameters = List.of(parameters.get(PARAMETER__SERVICE_TYPE).getAsString().split(","));

			datasetList.forEach(dataproduct -> {

				List<EDMWebservice> webservices = dataproduct.getIsDistributionsByInstanceId().stream()
						.map(EDMIsDistribution::getDistributionByInstanceDistributionId)
						.filter(Objects::nonNull)
						.map(EDMDistribution::getWebserviceByAccessService)
						.filter(Objects::nonNull)
						.collect(Collectors.toList());

				for (EDMWebservice webservice : webservices) {
					if (Objects.nonNull(webservice.getWebserviceCategoriesByInstanceId()) && !webservice.getWebserviceCategoriesByInstanceId().isEmpty()) {
						List<String> serviceTypesOfWebservice = webservice.getWebserviceCategoriesByInstanceId().stream()
								.map(EDMWebserviceCategory::getCategoryByCategoryId)
								.filter(Objects::nonNull)
								.map(EDMCategory::getId)
								.collect(Collectors.toList());

						if (!Collections.disjoint(serviceTypesOfWebservice, serviceTypesParameters)) {
							tempDatasetList.add(dataproduct);
							break;
						}
					}
				}

			});
			datasetList = tempDatasetList;
		}
		return datasetList;
	}

	private static List<EDMDataproduct> filterByBoundingBox(List<EDMDataproduct> datasetList, JsonObject parameters) {
		//check if the bbox passed inside the parameters is complete, if not exit and return the whole list of dataproduct
		if (!parameters.has(NORTHEN_LAT)
				|| !parameters.has(SOUTHERN_LAT)
				|| !parameters.has(WESTERN_LON)
				|| !parameters.has(EASTERN_LON))
			return datasetList;
		GeometryFactory geometryFactory = new GeometryFactory();
		WKTReader reader = new WKTReader( geometryFactory );
		try {
			//try to parse the bbox
			final Geometry inputGeometry = reader.read(BBoxToPolygon.transform(parameters));
			//check if the bbox is parsed
			if(inputGeometry!=null) {
				//temporary dataproduct list, it will collect the dataproduct which are, even in part, contained inside the bbox
				ArrayList<EDMDataproduct> tempDatasetList = new ArrayList<>();
				//utility set to contain the all the uid of the selected dataproduct to avoid duplicates
				Set<String> uidSet = new HashSet<>();
				//iterate over every dataproduct
				for (EDMDataproduct ds : datasetList) {
					//if the uid belong to an already selected dataproduct just skip the iteration
					if(uidSet.contains(ds.getMetaId())) continue;
					//iterate over every distribution related to the dataproduct taken into account
					ds.getIsDistributionsByInstanceId().stream()
					.map(EDMIsDistribution::getDistributionByInstanceDistributionId)
					.map(EDMDistribution::getWebserviceByAccessService)
					.filter(Objects::nonNull)
					//iterate over the webservice related to the distribution
					.forEach(ws -> {
						if(Objects.nonNull(ws.getWebserviceSpatialsByInstanceId())){
							//loop on every spatial of the webservice
							for (EDMWebserviceSpatial wsSpatial : ws.getWebserviceSpatialsByInstanceId()) {
								try {
									//parse the spatial of the webservice
									Geometry dsGeometry = reader.read(wsSpatial.getLocation());
									//if the dataproduct hasn't been selected yet and the spatial of the webservice
									//intersect with the bbox, the dataproduct is selected.
									if (!uidSet.contains(ds.getMetaId()) && inputGeometry.intersects(dsGeometry)) {
										tempDatasetList.add(ds);
										uidSet.add(ds.getMetaId());
									}
								} catch (ParseException e) {
									LOGGER.error("Error occurs during BBOX dataproduct parsing", e);
								}
							}
						}
					});
					//if the uid belong to an already selected dataproduct just skip the iteration
					if(uidSet.contains(ds.getMetaId())) continue;
					//same operation done before for the webservice's spatial but now for the dataprodut spatial
					if(ds.getDataproductSpatialsByInstanceId() != null){
						for(EDMDataproductSpatial dsSpatial : ds.getDataproductSpatialsByInstanceId()){
							Geometry dsGeometry = reader.read(dsSpatial.getLocation());
							if (!uidSet.contains(ds.getMetaId()) && inputGeometry.intersects(dsGeometry)) {
								tempDatasetList.add(ds);
								uidSet.add(ds.getMetaId());
							}
						}
					}
					//if the uid belong to an already selected dataproduct just skip the iteration
					if(uidSet.contains(ds.getMetaId())) continue;
					//if the dataproduct doesn't have any spatial information it is taken
					if ( ds.getDataproductSpatialsByInstanceId() == null || ds.getDataproductSpatialsByInstanceId().isEmpty() ) {
						tempDatasetList.add(ds);
						uidSet.add(ds.getMetaId());
					}
				}
				//replace the old dataset list with the new temporary filtered dataproduct list
				datasetList = tempDatasetList;
			}
		} catch (org.locationtech.jts.io.ParseException e) {
			LOGGER.error("Error occurs during BBOX input parsing ",e);
		}
		return datasetList;
	}

	private static List<EDMDataproduct> filterByDateRange(List<EDMDataproduct> datasetList, PeriodOfTime temporal) {
		if(temporal.getStartDate()!=null && temporal.getEndDate()!=null) {
			ArrayList<EDMDataproduct> tempDatasetList = new ArrayList<>();
			datasetList.forEach(ds -> {
				List<PeriodOfTime> dsTemporalList = ds.getDataproductTemporalsByInstanceId() != null ?
						ds.getDataproductTemporalsByInstanceId().stream()
						.map(elem -> {
							PeriodOfTime periodOfTime = new PeriodOfTime();
							periodOfTime.setStartDate(
									elem.getStartdate() != null ?
											elem.getStartdate().toLocalDateTime() : null
									);
							periodOfTime.setEndDate(
									elem.getEnddate() != null ?
											elem.getEnddate().toLocalDateTime() : null
									);
							return periodOfTime;
						}).collect(Collectors.toList())
						: new ArrayList<>();
				if( dsTemporalList.size() > 0) {
					if((dsTemporalList.get(0).getStartDate() == null || temporal.getEndDate() == null || dsTemporalList.get(0).getStartDate().isBefore(temporal.getEndDate()))
							&& (temporal.getStartDate() == null || dsTemporalList.get(0).getEndDate() == null || temporal.getStartDate().isBefore(dsTemporalList.get(0).getEndDate()))
							&& (dsTemporalList.get(0).getStartDate() == null || dsTemporalList.get(0).getEndDate() == null || dsTemporalList.get(0).getStartDate().isBefore(dsTemporalList.get(0).getEndDate()))
							&& (temporal.getStartDate() == null || temporal.getEndDate() == null || temporal.getStartDate().isBefore(temporal.getEndDate())))
						tempDatasetList.add(ds);
				}
			});
			datasetList = tempDatasetList;
		}
		return datasetList;
	}

	private static List<EDMDataproduct> filterByOrganizations(List<EDMDataproduct> datasetList, JsonObject parameters) {
		if(parameters.has("organisations")) {
			List<String> organisations = Arrays.asList(parameters.get("organisations").getAsString().split(","));

			HashSet<EDMDataproduct> tempDatasetList = new HashSet<>();
			datasetList.forEach(ds -> {

				List<String> value = new ArrayList<>();
				for (EDMEdmEntityId edmMetaId : ds.getPublishersByInstanceId().stream().map(EDMPublisher::getEdmEntityIdByMetaOrganizationId).collect(Collectors.toList())) {
					if (edmMetaId.getOrganizationsByMetaId() != null && !edmMetaId.getOrganizationsByMetaId().isEmpty()) {
						ArrayList<EDMOrganization> list = new ArrayList<>(edmMetaId.getOrganizationsByMetaId());
						value.add(list.get(0).getMetaId());
					}
				}

				List<EDMEdmEntityId> provider = ds.getIsDistributionsByInstanceId().stream()
						.map(EDMIsDistribution::getDistributionByInstanceDistributionId)
						.filter(Objects::nonNull)
						.map(EDMDistribution::getWebserviceByAccessService)
						.filter(Objects::nonNull)
						.map(EDMWebservice::getEdmEntityIdByProvider)
						.filter(Objects::nonNull).collect(Collectors.toList());
				for (EDMEdmEntityId edmMetaId : provider){
					if (edmMetaId.getOrganizationsByMetaId() != null && !edmMetaId.getOrganizationsByMetaId().isEmpty()) {
						ArrayList<EDMOrganization> list = new ArrayList<>(edmMetaId.getOrganizationsByMetaId());
						value.add(list.get(0).getMetaId());
					}
				}

				if(!Collections.disjoint(organisations, value)){
					tempDatasetList.add(ds);
				}

			});
			datasetList = new ArrayList<>(tempDatasetList);
		}
		return datasetList;
	}

	private static List<EDMDataproduct> filterByKeywords(List<EDMDataproduct> datasetList, JsonObject parameters) {
		if(parameters.has("keywords")) {
			ArrayList<EDMDataproduct> tempDatasetList = new ArrayList<>();
			String[] keywords = parameters.get("keywords").getAsString().split(",");
			datasetList.forEach(ds -> {
				if(!Collections.disjoint(Arrays.stream(ds.getKeywords().split(",")).map(String::toLowerCase).map(String::trim).collect(Collectors.toList()), Arrays.asList(keywords))){
					tempDatasetList.add(ds);
				}
			});
			datasetList = tempDatasetList;
		}
		return datasetList;
	}

	private static List<EDMDataproduct> filterByFullText(List<EDMDataproduct> datasetList, JsonObject parameters) {
		if(parameters.has("q")) {
			HashSet<EDMDataproduct> tempDatasetList = new HashSet<>();
			String[] qs = parameters.get("q").getAsString().toLowerCase().split(",");

			for (EDMDataproduct edmDataproduct : datasetList) {
				Map<String, Boolean> qSMap = Arrays.stream(qs)
						.collect(Collectors.toMap(
								key -> key, value -> Boolean.FALSE
								));

				if(Objects.nonNull(edmDataproduct.getKeywords())){
					List<String> dataproductKeywords = Arrays.stream(edmDataproduct.getKeywords().split(","))
							.map(String::toLowerCase)
							.map(String::trim)
							.collect(Collectors.toList());

					for (String q : qSMap.keySet()) {
						if (dataproductKeywords.contains(q)) qSMap.put(q, Boolean.TRUE);
					}
				}


				if (edmDataproduct.getIsDistributionsByInstanceId() != null && !edmDataproduct.getIsDistributionsByInstanceId().isEmpty()) {
					for (EDMDistribution edmDistribution : edmDataproduct.getIsDistributionsByInstanceId().stream().map(EDMIsDistribution::getDistributionByInstanceDistributionId).collect(Collectors.toList())) {

						//distribution title
						if (edmDistribution.getDistributionTitlesByInstanceId() != null) {
							edmDistribution.getDistributionTitlesByInstanceId()
							.stream().map(EDMDistributionTitle::getTitle)
							.forEach(title ->
							{
								for (String q : qSMap.keySet()) {
									if (title.toLowerCase().contains(q)) {
										qSMap.put(q, Boolean.TRUE);
									}
								}
							}
									);
						}

						//distribution description
						if (edmDistribution.getDistributionDescriptionsByInstanceId() != null) {
							edmDistribution.getDistributionDescriptionsByInstanceId()
							.stream().map(EDMDistributionDescription::getDescription)
							.forEach(description ->
							{
								for (String q : qSMap.keySet()) {
									if (description.toLowerCase().contains(q)) {
										qSMap.put(q, Boolean.TRUE);
									}
								}
							}
									);
						}

						//webservice
						if (edmDistribution.getWebserviceByAccessService() != null) {
							EDMWebservice edmWebservice = edmDistribution.getWebserviceByAccessService();

							//webservice title
							if (edmWebservice.getName() != null) {
								for (String q : qSMap.keySet()) {
									if (edmWebservice.getName().toLowerCase().contains(q)) {
										qSMap.put(q, Boolean.TRUE);
									}
								}
							}

							//webservice description
							if (edmWebservice.getDescription() != null) {
								for (String q : qSMap.keySet()) {
									if (edmWebservice.getDescription().toLowerCase().contains(q)) {
										qSMap.put(q, Boolean.TRUE);
									}
								}
							}

							if(Objects.nonNull(edmWebservice.getKeywords())){
								List<String> webserviceKeywords = Arrays.stream(edmWebservice.getKeywords().split(","))
										.map(String::toLowerCase)
										.map(String::trim)
										.collect(Collectors.toList());

								for (String q : qSMap.keySet()) {
									if (webserviceKeywords.contains(q)) qSMap.put(q, Boolean.TRUE);
								}
							}

						}

					}
				}

				//take a dataproduct onlly if every element of the freetext search is satisfied
				if (qSMap.values().stream().allMatch(b -> b)) tempDatasetList.add(edmDataproduct);
			}

			datasetList = new ArrayList<>(tempDatasetList);
		}

		return datasetList;

	}

}
