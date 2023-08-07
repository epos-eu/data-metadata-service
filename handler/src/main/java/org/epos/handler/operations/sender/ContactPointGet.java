package org.epos.handler.operations.sender;

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
import org.epos.handler.dbapi.DBAPIClient;
import org.epos.handler.dbapi.dbapiimplementation.ContactPointDBAPI;
import org.epos.handler.dbapi.model.*;
import org.epos.handler.dbapi.service.DBService;
import org.epos.handler.dbapi.util.EDMUtil;
import org.epos.handler.operations.common.AvailableFormatsGeneration;
import org.epos.handler.operations.monitoring.ZabbixExecutor;
import org.epos.handler.support.Utils;
import org.epos.handler.support.spatial.BBoxToPolygon;
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

public class ContactPointGet {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContactPointGet.class);
	
	private static final DBAPIClient dbapi = new DBAPIClient();
	
	public static JsonObject generate(JsonObject response, JsonObject parameters) {

		LOGGER.info("Requests start - JPA method");
		
		System.out.println(parameters);
		
		List<String> contactPointsIds = Arrays.asList(parameters.getAsJsonObject().get("ids").getAsString().split(","));
		
		System.out.println(contactPointsIds);
		
		JsonArray listEmails = new JsonArray();
		
		contactPointsIds.forEach(contactId ->
			dbapi.retrieve(ContactPoint.class, new DBAPIClient.GetQuery().instanceId(contactId))
			.forEach(contact-> contact.getEmail().forEach(mail->listEmails.add(mail))));


		response.add("emails", listEmails);
		
		System.out.println(response);
		return response;
	}

}
