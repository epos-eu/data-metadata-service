package org.epos.handler.operations.resources;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.epos.eposdatamodel.Equipment;
import org.epos.eposdatamodel.Facility;
import org.epos.eposdatamodel.State;
import org.epos.handler.dbapi.dbapiimplementation.EquipmentDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.FacilityDBAPI;
import org.epos.library.feature.Feature;
import org.epos.library.feature.FeaturesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.epos.handler.support.Utils.gson;


public class EquipmentsGeoJsonGeneration {

	private static final Logger LOGGER = LoggerFactory.getLogger(EquipmentsGeoJsonGeneration.class); 

	public static JsonObject generate(JsonObject response, JsonObject parameters) {
		JsonObject params = (parameters!=null && parameters.has("params"))? gson.fromJson(parameters.get("params").getAsString(), JsonObject.class) : new JsonObject();

		LOGGER.info("Parameters {}",params);
		
		Facility facilitySelected = new FacilityDBAPI().getByUidPublished(parameters.get("id").getAsString());
		
		FeaturesCollection geojson = new FeaturesCollection();

		List<Equipment> equipmentList = new EquipmentDBAPI().getAllByState(State.PUBLISHED).stream().filter(equipment -> equipment.getIsPartOf().contains(facilitySelected.getMetaId())).collect(Collectors.toList());
		
		for(Equipment equipment : equipmentList) {
			
			Feature feature = new Feature();
			
			feature.addSimpleProperty("Name", Optional.ofNullable(equipment.getName()).orElse(""));
			feature.addSimpleProperty("Description", Optional.ofNullable(equipment.getDescription()).orElse(""));
			feature.addSimpleProperty("Type", Optional.ofNullable(equipment.getType()).orElse(""));
			feature.addSimpleProperty("Category", Optional.ofNullable(equipment.getCategory().toString()).orElse(""));
			feature.addSimpleProperty("Dynamic range", Optional.ofNullable(equipment.getDynamicRange()).orElse(""));
			feature.addSimpleProperty("Filter", Optional.ofNullable(equipment.getFilter()).orElse(""));
			feature.addSimpleProperty("Manufacturer", Optional.ofNullable(equipment.getManufacturer() != null ? equipment.getManufacturer().getUid() : "").orElse(""));
			feature.addSimpleProperty("Orientation", Optional.ofNullable(equipment.getOrientation()).orElse(""));
			feature.addSimpleProperty("Page url", Optional.ofNullable(equipment.getPageURL()).orElse(""));
			feature.addSimpleProperty("Resolution", Optional.ofNullable(equipment.getResolution()).orElse(""));
			feature.addSimpleProperty("Sample period", Optional.ofNullable(equipment.getSamplePeriod()).orElse(""));
			feature.addSimpleProperty("Serial number", Optional.ofNullable(equipment.getSerialNumber()).orElse(""));
			
			geojson.addFeature(feature);
		}

		return new Gson().toJsonTree(geojson).getAsJsonObject();
	}
}
