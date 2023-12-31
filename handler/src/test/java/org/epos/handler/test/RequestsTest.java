package org.epos.handler.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.epos.handler.RequestHandler;
import org.epos.handler.facets.Facets;
import org.epos.handler.facets.FacetsGeneration;
import org.epos.handler.facets.FacetsNodeTree;
import org.epos.router_framework.domain.Actor;
import org.epos.router_framework.domain.BuiltInActorType;
import org.epos.router_framework.exception.RoutingMessageHandlingException;
import org.epos.router_framework.types.ServiceType;
import org.springframework.boot.system.SystemProperties;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class RequestsTest {
	
	public static void main(String[] args) throws RoutingMessageHandlingException, IOException {
		RequestHandler rh = new RequestHandler(Actor.getInstance(BuiltInActorType.DB_CONNECTOR.verbLabel()).get());
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("epos_operation-type", "get");
		//headers.put("epos_request-type", "test.api.distributed-processing-service.v1.processing.details");
		
		headers.put("epos_request-type", "demo.k8s-epos-deploy.operational-testing.api.resources-service.v1.resources.monitoring");
		//headers.put("epos_request-type", "converter-plugins");
		//headers.put("epos_request-type", "api.external-access-service.v1.execute");
		//headers.put("epos_request-type", "testing.k8s-epos-deploy.new-environment.api.external-access-service.v1.execute");
		
		//headers.put("kind", "get.testing.k8s-epos-deploy.new-environment.api.resources-service.v1.resources.datasets");
		String payloadSearchBBox = "{\"epos:easternmostLongitude\":\"16.47452\",\"epos:westernmostLongitude\":\"-31.33065\",\"epos:southernmostLatitude\":\"35.68377\",\"epos:northernmostLatitude\":\"66.62856\",\"facets\":\"true\"}";
		String payloadSearchTemporal = "{\"schema:endDate\":\"2021-11-12T08:59:18Z\",\"schema:startDate\":\"1981-11-11T09:58:48Z\",\"facets\":\"true\"}";
		String payloadSearchTemporalOnlyEnd = "{\"schema:endDate\":\"1896-11-12T07:49:04Z\",\"facets\":\"true\"}";
		String payloadSearchTemporalOnlyStart = "{\"schema:startDate\":\"1872-11-20T08:58:57Z\",\"facets\":\"true\"}";
		String payloadDetails = "{\"id\":\"3939a716-2de4-407c-ae1c-f57ea017c55c\"}";
		String payloadSoftwaresPlugin = "{\"type\":\"plugins\"}";
		String payloadResourcesPlugin = "{}";
		String payloadSoftwaresEmpty = "{\"type\":\"plugins\", \"operation\" : \"anthropogenic_hazards/webservice/is-epos_platform/apps\"}";
		String payloadEmptySearch = "{\"facets\":\"true\",\"facetstype\":\"categories\"}";
		String payloadQSearch = "{\"facets\":\"true\",\"facetstype\":\"categories\",\"q\":\"Geological Feature Download Service\"}";
		String payloadMonitoring = "{}";
		String payloadEmptyResponse = "{\"q\":\"Acceleration\",\"facets\":\"false\"}";
		String payloadSearchOrganizations = "{\"organisations\":\"86b9d420-d626-4720-a6c9-0649f30cc2ac\",\"facets\":\"true\"}";
		String payloadExecute = "{\"format\":\"application/epos.geo+json\",\"id\":\"0b4d5b77-87a8-4c75-90c1-086f3d695e7a\",\"params\":\"{\\\"volcano_name\\\":\\\"Etna\\\",\\\"publication_date\\\":\\\"2022-03-01T10:00\\\"}\",\"useDefaults\":\"false\"}";
		String payloadSimpleExecute = "{\"format\":\"application/json\",\"id\":\"https://www.epos-eu.org/epos-dcat-ap/Seismology/Dataset/006/EMSC/Distribution\",\"params\":\"{\\\"maxradius\\\":\\\"\\\",\\\"minradius\\\":\\\"\\\",\\\"starttime\\\":\\\"\\\",\\\"eventid\\\":\\\"\\\",\\\"offset\\\":\\\"\\\",\\\"minnbtestimonies\\\":\\\"100\\\",\\\"lat\\\":\\\"\\\",\\\"maxdepth\\\":\\\"\\\",\\\"format\\\":\\\"geojson\\\",\\\"mindepth\\\":\\\"\\\",\\\"downloadAsFile\\\":\\\"false\\\",\\\"maxnbtestimonies\\\":\\\"\\\",\\\"minlat\\\":\\\"\\\",\\\"maxmag\\\":\\\"\\\",\\\"maxlon\\\":\\\"\\\",\\\"dayafter\\\":\\\"\\\",\\\"minmag\\\":\\\"5\\\",\\\"limit\\\":\\\"250\\\",\\\"lon\\\":\\\"\\\",\\\"orderby\\\":\\\"\\\",\\\"minlon\\\":\\\"\\\",\\\"maxlat\\\":\\\"\\\",\\\"endtime\\\":\\\"\\\"}\"}";
		//rh.handle(payloadEmptySearch, ServiceType.METADATA, headers);
		//FileWriter file = new FileWriter("output.json");
		//file.write(rh.handle(payloadDetails, ServiceType.METADATA, headers).toString());
		//file.close();
		//ZabbixExecutor.getInstance();
		//rh.handle(payloadEmptySearch, ServiceType.METADATA, headers);
		Facets.getInstance();
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println(FacetsGeneration.generateOnlyFacetsTree(new JsonObject(), null));
		//rh.handle(payloadResourcesPlugin, ServiceType.PROCESS, headers);
		System.out.println(rh.handle(payloadResourcesPlugin, ServiceType.METADATA, headers));
		//rh.handle(payloadEmptySearch, ServiceType.METADATA, headers);
	}

}
