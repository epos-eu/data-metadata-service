package org.epos.handler.operations.resources;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;

import org.epos.eposdatamodel.*;
import org.epos.handler.dbapi.dbapiimplementation.OrganizationDBAPI;
import org.epos.handler.dbapi.model.EDMDataproduct;
import org.epos.handler.support.Utils;
import org.epos.handler.support.spatial.BBoxToPolygon;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

public class FilterSearch {

	private static final Logger LOGGER = LoggerFactory.getLogger(FilterSearch.class); 

	private static final String NORTHEN_LAT  = "epos:northernmostLatitude";
	private static final String SOUTHERN_LAT  = "epos:southernmostLatitude";
	private static final String WESTERN_LON  = "epos:westernmostLongitude";
	private static final String EASTERN_LON  = "epos:easternmostLongitude";


	public static List<DataProduct> doFilters(List<DataProduct> datasetList, JsonObject parameters, List<Distribution> distributionList, List<WebService> webserviceList) {
		
		PeriodOfTime temporal = checkTemporalExtent(parameters);
		
		ArrayList<String> orgIds = checkOrganizations(parameters);
		
		datasetList = filterByFullText(datasetList, parameters, distributionList, webserviceList);
		
		datasetList = filterByKeywords(datasetList, parameters);

		datasetList = filterByOrganizations(datasetList, orgIds, distributionList, webserviceList);
		
		datasetList = filterByDateRange(datasetList, temporal);

		datasetList = filterByBoundingBox(datasetList, parameters, distributionList, webserviceList);
		
		return datasetList;
	}

	private static List<DataProduct> filterByBoundingBox(List<DataProduct> datasetList, JsonObject parameters, List<Distribution> distributionList, List<WebService> webserviceList) {
		if(parameters.has(NORTHEN_LAT)
				&& parameters.has(SOUTHERN_LAT)
				&& parameters.has(WESTERN_LON)
				&& parameters.has(EASTERN_LON)) {
			GeometryFactory geometryFactory = new GeometryFactory();

			WKTReader reader = new WKTReader( geometryFactory );
			try {
				final Geometry inputGeometry = reader.read(BBoxToPolygon.transform(parameters));
				if(inputGeometry!=null) {
					ArrayList<DataProduct> tempDatasetList = new ArrayList<>();
					datasetList.forEach(ds -> {
						int tempListSize = tempDatasetList.size();
						if(ds.getDistribution()!=null) {
							ArrayList<String> distrs = new ArrayList<String>();
							ds.getDistribution().forEach(dist->{
								if(dist.getUid()!= null) {
									distrs.add(dist.getUid().replace("file:///app/", ""));
								}
							});
							for(Distribution dx : distributionList) {
								if(distrs.contains(dx.getUid())) {
									if(dx.getAccessService()!=null) {
										for(WebService ws :webserviceList) {
											if(dx.getAccessService().getUid() != null && ws.getUid().replace("file:///app/", "").equals(dx.getAccessService().getUid().replace("file:///app/", ""))){
												if(ws.getSpatialExtent()!=null) {
													ws.getSpatialExtent().forEach(spatial->{
														try {
															Geometry dsGeometry = reader.read(spatial.getLocation());
															if(inputGeometry.intersects(dsGeometry)) {
																tempDatasetList.add(ds);
															}
														} catch (org.locationtech.jts.io.ParseException e) {
															LOGGER.error("Error occurs during BBOX dataproduct parsing {}",e);
														}
													});
												}
											}
										}
									}
								}
							}
						}

						if(ds.getSpatialExtent()!=null && tempDatasetList.size()==tempListSize) {
							ds.getSpatialExtent().forEach(spatial->{
								try {
									Geometry dsGeometry = reader.read(spatial.getLocation());
									if(inputGeometry.intersects(dsGeometry)) {
										tempDatasetList.add(ds);
									}
								} catch (org.locationtech.jts.io.ParseException e) {
									LOGGER.error("Error occurs during BBOX dataproduct parsing {}",e);
								}
							});
						} 

						if(ds.getSpatialExtent()==null && tempDatasetList.size()==tempListSize) { 
							tempDatasetList.add(ds);
						}
					});
					datasetList = tempDatasetList;
				}
			} catch (org.locationtech.jts.io.ParseException e) {
				LOGGER.error("Error occurs during BBOX input parsing {}",e);
			}
		}
		//Set<String> collect = datasetList.stream().map(DataProduct::getUid).collect(Collectors.toSet());
		//LOGGER.info("n° of dataproduct after bbox filter: " + datasetList.size());
		//LOGGER.info("Dataproduct after bbox filter: " + collect);
		return datasetList;
	}

	private static List<DataProduct> filterByDateRange(List<DataProduct> datasetList, PeriodOfTime temporal) {
		if(temporal.getStartDate()!=null && temporal.getEndDate()!=null) {
			ArrayList<DataProduct> tempDatasetList = new ArrayList<>();
			datasetList.forEach(ds -> {
				if(ds.getTemporalExtent()!=null && ds.getTemporalExtent().size() > 0) {
					if((ds.getTemporalExtent().get(0).getStartDate() == null || temporal.getEndDate() == null || ds.getTemporalExtent().get(0).getStartDate().isBefore(temporal.getEndDate()))
							&& (temporal.getStartDate() == null || ds.getTemporalExtent().get(0).getEndDate() == null || temporal.getStartDate().isBefore(ds.getTemporalExtent().get(0).getEndDate()))
							&& (ds.getTemporalExtent().get(0).getStartDate() == null || ds.getTemporalExtent().get(0).getEndDate() == null || ds.getTemporalExtent().get(0).getStartDate().isBefore(ds.getTemporalExtent().get(0).getEndDate()))
							&& (temporal.getStartDate() == null || temporal.getEndDate() == null || temporal.getStartDate().isBefore(temporal.getEndDate())))
						tempDatasetList.add(ds);
				}
			});
			datasetList = tempDatasetList;
		}
		return datasetList;
	}

	private static List<DataProduct> filterByOrganizations(List<DataProduct> datasetList, ArrayList<String> orgIds, List<Distribution> distributionList, List<WebService> webserviceList) {
		if(!orgIds.isEmpty()) {
			ArrayList<DataProduct> tempDatasetList = new ArrayList<>();
			datasetList.forEach(ds -> {
				for(String organizationID : ds.getPublisher().stream().map(LinkedEntity::getInstanceId).collect(Collectors.toList())) {
					if(orgIds.contains(organizationID)) {
						tempDatasetList.add(ds);
					}
				}
				for(Distribution distr : distributionList) {
					ds.getDistribution().forEach(subDistr -> {
						if(subDistr.getInstanceId().equals(distr.getInstanceId())) {
							webserviceList.forEach(ws -> {
								if(distr.getAccessService()!=null && distr.getAccessService().getInstanceId().equals(ws.getInstanceId())){
									for(String orgId : orgIds) {
										if(ws.getProvider()!=null && (orgId.contains(ws.getProvider().getInstanceId()) || ws.getProvider().getInstanceId().contains(orgId))) {
											tempDatasetList.add(ds);
										}	
									}	
								}
							});
						}
					});
				}
			});
			datasetList = tempDatasetList;
		}
		return datasetList;
	}

	private static List<DataProduct> filterByKeywords(List<DataProduct> datasetList, JsonObject parameters) {
		if(parameters.has("keywords")) {
			ArrayList<DataProduct> tempDatasetList = new ArrayList<>();
			List<String> keywords = Arrays.asList(parameters.get("keywords").getAsString().split(",")).stream().map(kw -> new String(Base64.decodeBase64(kw))).collect(Collectors.toList());
			datasetList.forEach(ds -> {
				if(!Collections.disjoint(Arrays.stream(ds.getKeywords().split(",\t")).map(String::toLowerCase).map(String::trim).collect(Collectors.toList()), Arrays.asList(keywords))){
					tempDatasetList.add(ds);
				}
			});
			datasetList = tempDatasetList;
		}
		return datasetList;
	}

	private static List<DataProduct> filterByFullText(List<DataProduct> datasetList, JsonObject parameters, List<Distribution> distributionList, List<WebService> webserviceList) {
		if(parameters.has("q")) {
			HashSet<DataProduct> tempDatasetList = new HashSet<>();
			HashSet<Distribution> tempDistributionList = new HashSet<>();
			HashSet<String> webservicesIDs = new HashSet<String>();
			String[] qS = parameters.get("q").getAsString().toLowerCase().split(",");

			datasetList.forEach(ds -> {
				if(!Collections.disjoint(Arrays.stream(ds.getKeywords().split(",")).map(String::toLowerCase).map(String::trim).collect(Collectors.toList()), Arrays.asList(qS)))
					tempDatasetList.add(ds);
			});

			webserviceList.forEach(ws -> {
				if(ws.getDescription() != null && Utils.stringContainsItemFromList(ws.getDescription().toLowerCase(),qS)) {
					webservicesIDs.add(ws.getUid());
				}
				if(ws.getName() != null && Utils.stringContainsItemFromList(ws.getName().toLowerCase(),qS)) {
					webservicesIDs.add(ws.getUid());
				}
				if(!Collections.disjoint(Arrays.stream(ws.getKeywords()!=null? ws.getKeywords().split(",") : new String().split(",")).map(String::toLowerCase).map(String::trim).map(e->e.toLowerCase()).collect(Collectors.toList()), Arrays.asList(qS)))
					webservicesIDs.add(ws.getUid());
			});

			distributionList.forEach(ds -> {

				if(ds.getDescription() != null && ds.getDescription().size()>0
						&& ds.getTitle() != null && ds.getTitle().size()>0) {
					for(String descriptionItem : ds.getDescription()) {
						if(Utils.stringContainsItemFromList(descriptionItem.toLowerCase(),qS)) tempDistributionList.add(ds);
					}
					for(String titleItem : ds.getTitle()) {
						if(Utils.stringContainsItemFromList(titleItem.toLowerCase(),qS)) tempDistributionList.add(ds);
					}
				}

				if(ds.getAccessService()!=null && webservicesIDs.contains(ds.getAccessService().getUid())) {
					tempDistributionList.add(ds);
				}
			});

			List<String> distributionsIDs = tempDistributionList.stream().map(e->e.getUid()).collect(Collectors.toList());

			datasetList.forEach(ds -> {
				if(ds.getDistribution().stream().anyMatch(id -> distributionsIDs.contains(id.getUid()))) {
					tempDatasetList.add(ds);
				}
			});


			datasetList = new ArrayList<>(tempDatasetList);

		}
		return datasetList;

	}

	private static ArrayList<String> checkOrganizations(JsonObject parameters) {
		OrganizationDBAPI orgadbapi = new OrganizationDBAPI();
		orgadbapi.setMetadataMode(false);
		ArrayList<String> orgIds = new ArrayList<String>();
		if(parameters.has("organisations")) {
			Arrays.asList(parameters.get("organisations").getAsString().split(",")).forEach(orgString->{
				for(Organization org : orgadbapi.getAllByState(State.PUBLISHED)) {
					if(org.getInstanceId()!=null && org.getInstanceId().equals(orgString)) {
						orgIds.add(org.getInstanceId());
					}
				}
			});
		}
		return orgIds;
	}

	public static PeriodOfTime checkTemporalExtent(JsonObject parameters) {
		PeriodOfTime temporal = new PeriodOfTime();
		try {
			if(parameters.has("schema:startDate")) temporal.setStartDate(convertToLocalDateTimeViaSqlTimestamp(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(parameters.get("schema:startDate").getAsString().replace("T", " ").replace("Z", ""))));
			if(parameters.has("schema:endDate")) temporal.setEndDate(convertToLocalDateTimeViaSqlTimestamp(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(parameters.get("schema:endDate").getAsString().replace("T", " ").replace("Z", ""))));
		} catch (ParseException e1) {
			LOGGER.error("Error occurs during search caused by Date parsing {}",e1);
		}
		return temporal;
	}

	public static LocalDateTime convertToLocalDateTimeViaSqlTimestamp(Date dateToConvert) {
		return new java.sql.Timestamp(
				dateToConvert.getTime()).toLocalDateTime();
	}

}
