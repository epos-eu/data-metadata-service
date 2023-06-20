package org.epos.handler.operations.resources;

import static org.epos.handler.support.Utils.gson;

import java.util.ArrayList;
import java.util.Optional;
import org.epos.eposdatamodel.Facility;
import org.epos.eposdatamodel.Location;
import org.epos.handler.dbapi.dbapiimplementation.FacilityDBAPI;
import org.epos.library.feature.Feature;
import org.epos.library.feature.FeaturesCollection;
import org.epos.library.geometries.Geometry;
import org.epos.library.geometries.Point;
import org.epos.library.geometries.PointCoordinates;
import org.epos.library.geometries.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;


public class FacilitiesGeoJsonGeneration {

	private static final Logger LOGGER = LoggerFactory.getLogger(FacilitiesGeoJsonGeneration.class); 

	public static JsonObject generate(JsonObject response, JsonObject parameters) {
		JsonObject params = (parameters!=null && parameters.has("params"))? gson.fromJson(parameters.get("params").getAsString(), JsonObject.class) : new JsonObject();

		LOGGER.info("Parameters {}",params);

		Facility facilitySelected = new FacilityDBAPI().getByUidPublished(parameters.get("id").getAsString());

		FeaturesCollection geojson = new FeaturesCollection();

		// FACILITY
		Feature feature = new Feature();
		feature.addSimpleProperty("Name", Optional.ofNullable(facilitySelected.getTitle()).orElse(""));
		feature.addSimpleProperty("Description", Optional.ofNullable(facilitySelected.getDescription()).orElse(""));
		feature.addSimpleProperty("Type", Optional.ofNullable(facilitySelected.getType()).orElse(""));

		for(Location loc : facilitySelected.getSpatialExtent()) {
			String location = loc.getLocation();
			boolean isPoint = location.contains("POINT");
			location = location.replaceAll("POLYGON", "").replaceAll("POINT", "").replaceAll("\\(", "").replaceAll("\\)", "");
			String[] coordinates = location.split("\\,");
			Geometry geometry = null;
			
			if(isPoint) {
				geometry = new Point();
				for(String coo : coordinates) {
					String[] cooz = coo.split(" ");
					if(cooz.length==2) {
						((Point) geometry).setCoordinates(new PointCoordinates(Double.parseDouble(cooz[0]), Double.parseDouble(cooz[1])));
					}else {
						((Point) geometry).setCoordinates(new PointCoordinates(Double.parseDouble(cooz[1]), Double.parseDouble(cooz[2])));
					}
				}
			}else {
				geometry = new Polygon();
				ArrayList<PointCoordinates> deep = new ArrayList<PointCoordinates>();
				for(String coo : coordinates) {
					String[] cooz = coo.split(" ");
					if(cooz.length==2) {
						deep.add(new PointCoordinates(Double.parseDouble(cooz[0]), Double.parseDouble(cooz[1])));
					}else {
						deep.add(new PointCoordinates(Double.parseDouble(cooz[1]), Double.parseDouble(cooz[2])));
					}
				}
				((Polygon) geometry).setStartingPoint(deep.get(0));
				for(int i = 1; i<deep.size();i++)
					((Polygon) geometry).addAdditionalPoint(deep.get(i));	
			}
			feature.setGeometry(geometry);
		}
		
		geojson.addFeature(feature);

		return new Gson().toJsonTree(geojson).getAsJsonObject();
	}
}
