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
import org.epos.handler.enums.ProviderType;
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

		String id = parameters.getAsJsonObject().get("id").getAsString();
		System.out.println(id);
		ProviderType type = ProviderType.valueOf(parameters.getAsJsonObject().get("type").getAsString());
		System.out.println(type);

		JsonArray listEmails = new JsonArray();

		switch(type) {
		case SERVICEPROVIDERS:
			for(WebService ws : dbapi.retrieve(WebService.class, new DBAPIClient.GetQuery().instanceId(id))) {
				for(LinkedEntity le : ws.getContactPoint()) {
					dbapi.retrieve(ContactPoint.class, new DBAPIClient.GetQuery().instanceId(le.getInstanceId()))
					.forEach(contact-> contact.getEmail().forEach(mail->listEmails.add(mail)));
				}	
			}
			break;
		case DATAPROVIDERS:
			for(DataProduct dp : dbapi.retrieve(DataProduct.class, new DBAPIClient.GetQuery().instanceId(id))) {
				for(LinkedEntity le : dp.getContactPoint()) {
					dbapi.retrieve(ContactPoint.class, new DBAPIClient.GetQuery().instanceId(le.getInstanceId()))
					.forEach(contact-> contact.getEmail().forEach(mail->listEmails.add(mail)));
				}	
			}
			break;
		case ALL:
			for(Distribution dist : dbapi.retrieve(Distribution.class, new DBAPIClient.GetQuery().instanceId(id))) {
				for(WebService ws : dbapi.retrieve(WebService.class, new DBAPIClient.GetQuery().instanceId(dist.getAccessService().getInstanceId()))) {
					for(LinkedEntity le : ws.getContactPoint()) {
						dbapi.retrieve(ContactPoint.class, new DBAPIClient.GetQuery().instanceId(le.getInstanceId()))
						.forEach(contact-> contact.getEmail().forEach(mail->listEmails.add(mail)));
					}	
				}
				
				for(LinkedEntity le : dist.getDataProduct()) {
					for(DataProduct dp : dbapi.retrieve(DataProduct.class, new DBAPIClient.GetQuery().instanceId(le.getInstanceId()))) {
						for(LinkedEntity le2 : dp.getContactPoint()) {
							dbapi.retrieve(ContactPoint.class, new DBAPIClient.GetQuery().instanceId(le2.getInstanceId()))
							.forEach(contact-> contact.getEmail().forEach(mail->listEmails.add(mail)));
						}	
					}
				}
			}
			break;
		default:
			return response;
		}

		response.add("emails", listEmails);

		System.out.println(response);
		return response;
	}

}
