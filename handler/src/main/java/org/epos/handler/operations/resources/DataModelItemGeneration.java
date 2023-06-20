package org.epos.handler.operations.resources;

import org.epos.eposdatamodel.State;
import org.epos.handler.dbapi.dbapiimplementation.ContactPointDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.DataProductDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.DistributionDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.EquipmentDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.FacilityDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.OperationDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.OrganizationDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.PersonDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.ServiceDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.SoftwareApplicationDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.SoftwareSourceCodeDBAPI;
import org.epos.handler.dbapi.dbapiimplementation.WebServiceDBAPI;
import org.epos.handler.enums.APIActionType;
import org.epos.handler.support.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DataModelItemGeneration {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataModelItemGeneration.class); 

	public static JsonArray generate(JsonObject response, JsonObject parameters, APIActionType edmType) {
		switch(edmType) {
		case CONTACTPOINT:
			return Utils.gson.toJsonTree(new ContactPointDBAPI().getAllByState(State.PUBLISHED)).getAsJsonArray();
		case DATAPRODUCT:
			return Utils.gson.toJsonTree(new DataProductDBAPI().getAllByState(State.PUBLISHED)).getAsJsonArray();
		case DISTRIBUTION:
			return Utils.gson.toJsonTree(new DistributionDBAPI().getAllByState(State.PUBLISHED)).getAsJsonArray();
		case EQUIPMENT:
			return Utils.gson.toJsonTree(new EquipmentDBAPI().getAllByState(State.PUBLISHED)).getAsJsonArray();
		case FACILITY:
			return Utils.gson.toJsonTree(new FacilityDBAPI().getAllByState(State.PUBLISHED)).getAsJsonArray();
		case OPERATION:
			return Utils.gson.toJsonTree(new OperationDBAPI().getAllByState(State.PUBLISHED)).getAsJsonArray();
		case ORGANISATION:
			return Utils.gson.toJsonTree(new OrganizationDBAPI().getAllByState(State.PUBLISHED)).getAsJsonArray();
		case PERSON:
			return Utils.gson.toJsonTree(new PersonDBAPI().getAllByState(State.PUBLISHED)).getAsJsonArray();
		case SERVICE:
			return Utils.gson.toJsonTree(new ServiceDBAPI().getAllByState(State.PUBLISHED)).getAsJsonArray();
		case SOFTWARE:
			JsonArray softwares = new JsonArray();
			JsonObject softwareSourceCode = new JsonObject();
			JsonObject softwareApplication = new JsonObject();
			softwareApplication.add("SoftwareApplications", Utils.gson.toJsonTree(new SoftwareApplicationDBAPI().getAllByState(State.PUBLISHED)));
			softwareSourceCode.add("SoftwareSourceCode", Utils.gson.toJsonTree(new SoftwareSourceCodeDBAPI().getAllByState(State.PUBLISHED)));
			softwares.add(softwareSourceCode);
			softwares.add(softwareApplication);
			return softwares;
		case WEBSERVICE:
			return Utils.gson.toJsonTree(new WebServiceDBAPI().getAllByState(State.PUBLISHED)).getAsJsonArray();
		default:
			return new JsonArray();
		}
	}
}
