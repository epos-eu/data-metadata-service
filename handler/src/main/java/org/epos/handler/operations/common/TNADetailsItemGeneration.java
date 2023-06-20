package org.epos.handler.operations.common;

import org.epos.handler.constants.EnvironmentVariables;
import org.epos.handler.dbapi.dbapiimplementation.*;
import org.epos.handler.HeaderParser;
import org.epos.handler.beans.AvailableFormat;
import org.epos.handler.beans.SpatialInformation;
import org.epos.handler.beans.AvailableFormat.AvailableFormatBuilder;
import org.epos.handler.beans.DiscoveryItem.DiscoveryItemBuilder;
import org.epos.eposdatamodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;


import static org.epos.handler.support.Utils.gson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class TNADetailsItemGeneration {

	private static final Logger LOGGER = LoggerFactory.getLogger(TNADetailsItemGeneration.class); 

	private static final String API_PATH_DETAILS  = EnvironmentVariables.API_CONTEXT+"/tna/details?id=";
	private static final String API_PATH_EXECUTE_FACILITY  = EnvironmentVariables.API_CONTEXT+"/tna/show-facilities?id=";
	private static final String API_PATH_EXECUTE_EQUIPMENTS  = EnvironmentVariables.API_CONTEXT+"/tna/show-equipments?id=";

	public static JsonObject generate(JsonObject response, JsonObject parameters, HeaderParser header) {

		JsonObject params = (parameters!=null && parameters.has("params"))? gson.fromJson(parameters.get("params").getAsString(), JsonObject.class) : new JsonObject();

		LOGGER.info("Parameters {}",params);
		
		Facility facilitySelected = new FacilityDBAPI().getByUidPublished(parameters.get("id").getAsString());
		List<Service> serviceList = new ServiceDBAPI().getAllByState(State.PUBLISHED);
		List<Equipment> equipmentList = new EquipmentDBAPI().getAllByState(State.PUBLISHED);
		
		org.epos.handler.beans.Facility returnFacility = new org.epos.handler.beans.Facility();
		
		ArrayList<AvailableFormat> formats = new ArrayList<AvailableFormat>();
		formats.add(new AvailableFormatBuilder()
				.format("application/epos.map.geo+json")
				.href(EnvironmentVariables.API_HOST+ API_PATH_EXECUTE_FACILITY + facilitySelected.getMetaId())
				.label("GEOJSON")
				.build());
		formats.add(new AvailableFormatBuilder()
				.format("application/epos.table.geo+json")
				.href(EnvironmentVariables.API_HOST+ API_PATH_EXECUTE_EQUIPMENTS + facilitySelected.getMetaId())
				.label("GEOJSON")
				.build());
		
		returnFacility.setAvailableFormats(formats);
		returnFacility.setCategories(facilitySelected.getCategory());
		returnFacility.setDescription(facilitySelected.getDescription());
		returnFacility.setEquipments(equipmentList.stream().map(Equipment::getName).collect(Collectors.toList())); // TODO: get equipments
		returnFacility.setHref(EnvironmentVariables.API_HOST + API_PATH_DETAILS + facilitySelected.getMetaId());
		returnFacility.setId(facilitySelected.getMetaId());
		returnFacility.setPageurl(facilitySelected.getPageURL());
		returnFacility.setServices(serviceList.stream().filter(e->facilitySelected.getRelation().contains(e.getMetaId())).map(e -> e.getName()).collect(Collectors.toList()));
		for(Location s : facilitySelected.getSpatialExtent())
			returnFacility.getSpatial().addPaths(SpatialInformation.doSpatial(s.getLocation()),SpatialInformation.checkPoint(s.getLocation()));
		returnFacility.setTitle(facilitySelected.getTitle());
		returnFacility.setType(facilitySelected.getType());
		
		return gson.toJsonTree(returnFacility).getAsJsonObject();
	}
}
