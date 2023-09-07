package org.epos.handler.operations.resources;

import java.util.*;
import java.util.stream.Collectors;

import org.epos.handler.beans.AvailableFormat;
import org.epos.handler.beans.DiscoveryItem;
import org.epos.handler.beans.AvailableFormat.AvailableFormatBuilder;
import org.epos.handler.beans.DiscoveryItem.DiscoveryItemBuilder;
import org.epos.handler.constants.EnvironmentVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.epos.handler.dbapi.dbapiimplementation.*;
import org.epos.handler.support.BBoxToPolygon;
import org.epos.handler.support.Utils;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTReader;
import org.epos.eposdatamodel.*;

import static org.epos.handler.support.Utils.gson;

public class TNASearchGeneration {

	private static final Logger LOGGER = LoggerFactory.getLogger(TNASearchGeneration.class); 


	private static final String NORTHEN_LAT  = "epos:northernmostLatitude";
	private static final String SOUTHERN_LAT  = "epos:southernmostLatitude";
	private static final String WESTERN_LON  = "epos:westernmostLongitude";
	private static final String EASTERN_LON  = "epos:easternmostLongitude";
	private static final String API_PATH_DETAILS  = EnvironmentVariables.API_CONTEXT+"/tna/details?id=";
	private static final String API_PATH_EXECUTE_FACILITY  = EnvironmentVariables.API_CONTEXT+"/tna/show-facilities?id=";
	private static final String API_PATH_EXECUTE_EQUIPMENTS  = EnvironmentVariables.API_CONTEXT+"/tna/show-equipments?id=";


	public static JsonObject generate(JsonObject response, JsonObject parameters) {

		List<Facility> facilityList = new FacilityDBAPI().getAllByState(State.PUBLISHED);
		JsonObject results = new JsonObject();
		JsonObject filters = new JsonObject();
		response.add("results", results);
		response.add("filters", filters);

		JsonArray paleomagnetism = new JsonArray();
		JsonArray volcanology = new JsonArray();
		JsonArray rockPhysics = new JsonArray();
		JsonArray analyticalAndMicrospy = new JsonArray();
		JsonArray analogueModelling = new JsonArray();




		if(parameters.has("q")) {
			String[] qS = parameters.get("q").getAsString().toLowerCase().split(",");
			facilityList = facilityList.stream()
					.filter(facility->Utils.stringContainsItemFromList(facility.getDescription().toLowerCase(),qS)||Utils.stringContainsItemFromList(facility.getTitle().toLowerCase(),qS))
					.collect(Collectors.toList());
		}

		if(parameters.has(NORTHEN_LAT)
				&& parameters.has(SOUTHERN_LAT)
				&& parameters.has(WESTERN_LON)
				&& parameters.has(EASTERN_LON)) {
			GeometryFactory geometryFactory = new GeometryFactory();

			WKTReader reader = new WKTReader( geometryFactory );
			try {
				final Geometry inputGeometry = reader.read(BBoxToPolygon.transform(parameters));
				if(inputGeometry!=null) {
					ArrayList<Facility> tempFacilityList = new ArrayList<>();
					facilityList.forEach(ds -> {
						int tempListSize = tempFacilityList.size();
						if(ds.getSpatialExtent()!=null && tempFacilityList.size()==tempListSize) {
							ds.getSpatialExtent().forEach(spatial->{
								try {
									Geometry dsGeometry = reader.read(spatial.getLocation());
									if(inputGeometry.intersects(dsGeometry)) {
										tempFacilityList.add(ds);
									}
								} catch (org.locationtech.jts.io.ParseException e) {
									LOGGER.error("Error occurs during BBOX dataproduct parsing {}",e);
								}
							});
						} 

						if(ds.getSpatialExtent()==null && tempFacilityList.size()==tempListSize) { 
							tempFacilityList.add(ds);
						}
					});
					facilityList = tempFacilityList;
				}
			} catch (org.locationtech.jts.io.ParseException e) {
				LOGGER.error("Error occurs during BBOX input parsing {}",e);
			}
		}


		ArrayList<DiscoveryItem> discoveryList = new ArrayList<DiscoveryItem>();


		facilityList.forEach(facilityItem -> {
			ArrayList<AvailableFormat> formats = new ArrayList<AvailableFormat>();
			formats.add(new AvailableFormatBuilder()
					.format("application/epos.map.geo+json")
					.href(EnvironmentVariables.API_HOST+ API_PATH_EXECUTE_FACILITY + facilityItem.getUid())
					.label("GEOJSON")
					.build());
			formats.add(new AvailableFormatBuilder()
					.format("application/epos.table.geo+json")
					.href(EnvironmentVariables.API_HOST+ API_PATH_EXECUTE_EQUIPMENTS + facilityItem.getUid())
					.label("GEOJSON")
					.build());
			try {
				discoveryList.add(new DiscoveryItemBuilder(facilityItem.getUid(), EnvironmentVariables.API_HOST + API_PATH_DETAILS + facilityItem.getUid())
						.title(facilityItem.getTitle())
						.description(facilityItem.getDescription())
						.availableFormats(formats)
						.build());
			}catch(Exception e) {
				LOGGER.info("Error inserting the following facility in list: "+facilityItem.getUid());
			}
		});

		if(parameters.has("facets") && parameters.get("facets").getAsString().equals("true")) {
			/*for(DiscoveryItem item : discoveryList) {
				switch(item.getDdss()) {
				case "Paleomagnetism":
					paleomagnetism.add(gson.toJsonTree(item));
					break;
				case "Volcanology":
					volcanology.add(gson.toJsonTree(item));
					break;
				case "RockPhysics":
					rockPhysics.add(gson.toJsonTree(item));
					break;
				case "AnalyticalMicroscopy":
					analyticalAndMicrospy.add(gson.toJsonTree(item));
					break;
				case "AnalogueModelling":
					analogueModelling.add(gson.toJsonTree(item));
					break;
				}

			}*/

			JsonObject paleomagnetismDomain = new JsonObject();
			paleomagnetismDomain.addProperty("name", "Paleomagnetism");
			paleomagnetismDomain.add("facilities", paleomagnetism);

			JsonObject volcanologyDomain = new JsonObject();
			volcanologyDomain.addProperty("name", "Volcanology");
			volcanologyDomain.add("facilities", volcanology);

			JsonObject rockPhysicsDomain = new JsonObject();
			rockPhysicsDomain.addProperty("name", "Rock physics");
			rockPhysicsDomain.add("facilities", rockPhysics);

			JsonObject analyticalAndMicrospyDomain = new JsonObject();
			analyticalAndMicrospyDomain.addProperty("name", "Analytical and microscopy");
			analyticalAndMicrospyDomain.add("facilities", analyticalAndMicrospy);

			JsonObject analogueModellingDomain = new JsonObject();
			analogueModellingDomain.addProperty("name", "Analogue modelling");
			analogueModellingDomain.add("facilities", analogueModelling);


			results.addProperty("name", "domains");
			JsonArray resultsChildren = new JsonArray();
			resultsChildren.add(paleomagnetismDomain);
			resultsChildren.add(volcanologyDomain);
			resultsChildren.add(rockPhysicsDomain);
			resultsChildren.add(analyticalAndMicrospyDomain);
			resultsChildren.add(analogueModellingDomain);
			results.add("children", resultsChildren);

		}
		else {
			results.addProperty("name", "domains");
			results.add("children", gson.toJsonTree(discoveryList));

		}

		//if(discoveryList.isEmpty()) response.add("results", new JsonArray());
		//else response.add("results", gson.toJsonTree(discoveryList));

		return response;

	}
}
