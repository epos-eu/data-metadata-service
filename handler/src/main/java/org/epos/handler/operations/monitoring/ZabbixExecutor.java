package org.epos.handler.operations.monitoring;

import static org.epos.handler.support.Utils.gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.epos.handler.RequestHandler;
import org.epos.handler.constants.EnvironmentVariables;
import org.epos.handler.support.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ZabbixExecutor {

	private static ZabbixExecutor executor;
	private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	private JsonArray hostResults;

	private static final Logger LOGGER = LoggerFactory.getLogger(ZabbixExecutor.class); 

	public static ZabbixExecutor getInstance() {
		if(executor==null) executor = new ZabbixExecutor();
		return executor;
	}

	private ZabbixExecutor() {
		final Runnable updater = new Runnable() {
			public void run() { 
				LOGGER.info("Updating monitoring information");
				hostResults = new JsonArray();
				String auth;
				try {
					auth = ZabbixExecutor.auth();
					DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
					//String items = ZabbixExecutor.getItems(auth);
					ArrayList<String> listOfIds = new ArrayList<String>();
					gson.fromJson(ZabbixExecutor.getProblems(auth), JsonObject.class).get("result").getAsJsonArray()
					.forEach(e->listOfIds.add(getSubString(e.getAsJsonObject().get("name").getAsString(),'"','"')));
					
					for(JsonElement item : gson.fromJson(ZabbixExecutor.getItems(auth), JsonObject.class).get("result").getAsJsonArray()) {
						if(!item.getAsJsonObject().get("name").getAsString().contains("EPOS ICS-C")) {
							JsonObject singleResult = new JsonObject();
							
							String id =  StringUtils.substringBetween(item.getAsJsonObject().get("key_").getAsString(), "[", "]");
							singleResult.addProperty("name", item.getAsJsonObject().get("name").getAsString());
							singleResult.addProperty("itemid", item.getAsJsonObject().get("itemid").getAsString());
							singleResult.addProperty("key_", item.getAsJsonObject().get("key_").getAsString());
							singleResult.addProperty("lastclock",item.getAsJsonObject().get("lastclock").getAsString());
							singleResult.addProperty("lastvalue",item.getAsJsonObject().get("lastvalue").getAsString());
							singleResult.addProperty("id", id);
							singleResult.addProperty("status", (!listOfIds.contains(id))? 1 : 2);
							singleResult.addProperty("timestamp",df.format(new Date(item.getAsJsonObject().get("lastclock").getAsLong()*1000)).replace(" ", "T")+"Z");
							hostResults.add(singleResult);
						}
					}
					ZabbixExecutor.logout(auth);

				} catch (IOException | InterruptedException e) {
					LOGGER.error(e.getLocalizedMessage());
				}
				LOGGER.info("Monitoring information updated, host result: "+hostResults.toString());
			}
		};
		scheduler.scheduleAtFixedRate(updater, 0, 15, TimeUnit.MINUTES);

	}


	public static String auth() throws IOException, InterruptedException {

		String authRequest = "{\n"
				+ "    \"jsonrpc\": \"2.0\",\n"
				+ "    \"method\": \"user.login\",\n"
				+ "    \"params\": {\n"
				+ "        \"user\": \""+EnvironmentVariables.MONITORING_USER+"\",\n"
				+ "        \"password\": \""+EnvironmentVariables.MONITORING_PWD+"\"\n"
				+ "    },\n"
				+ "    \"id\": 1\n"
				+ "}";
		var values = Utils.gson.fromJson(authRequest, HashMap.class);

		var objectMapper = new ObjectMapper();
		String requestBody = objectMapper
				.writeValueAsString(values);

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(EnvironmentVariables.MONITORING_URL))
				.header("Content-Type", "application/json-rpc")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();

		HttpResponse<String> response = client.send(request,
				HttpResponse.BodyHandlers.ofString());


		return Utils.gson.fromJson(response.body(), JsonObject.class).get("result").getAsString();
	}

	public static String logout(String auth) throws IOException, InterruptedException {

		String retrieveItems = "{\"jsonrpc\":\"2.0\",\"method\":\"user.logout\",\"params\":[],\"auth\":\""+auth+"\",\"id\":6}\n";

		var values = Utils.gson.fromJson(retrieveItems, HashMap.class);
		var objectMapper = new ObjectMapper();
		String requestBody = objectMapper
				.writeValueAsString(values);


		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(EnvironmentVariables.MONITORING_URL))
				.header("Content-Type", "application/json-rpc")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();

		HttpClient client = HttpClient.newHttpClient();

		HttpResponse<String> response = client.send(request,
				HttpResponse.BodyHandlers.ofString());

		return response.body();
	}


	public static String retrieveHosts(String auth) throws IOException, InterruptedException {


		String retrieveHosts = "{\n"
				+ "    \"jsonrpc\": \"2.0\",\n"
				+ "    \"method\": \"host.get\",\n"
				+ "    \"params\": {\n"
				+ "        \"output\": [\n"
				+ "            \"hostid\",\n"
				+ "            \"host\"\n"
				+ "        ],\n"
				+ "        \"selectInterfaces\": [\n"
				+ "            \"interfaceid\",\n"
				+ "            \"ip\"\n"
				+ "        ]\n"
				+ "    },\n"
				+ "    \"id\": 2,\n"
				+ "    \"auth\": \""+ auth+"\"\n"
				+ "}";

		var values = Utils.gson.fromJson(retrieveHosts, HashMap.class);

		var objectMapper = new ObjectMapper();
		String requestBody = objectMapper
				.writeValueAsString(values);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(EnvironmentVariables.MONITORING_URL))
				.header("Content-Type", "application/json-rpc")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();

		HttpClient client = HttpClient.newHttpClient();

		HttpResponse<String> response = client.send(request,
				HttpResponse.BodyHandlers.ofString());

		return response.body();
	}

	public static String getItems(String auth) throws IOException, InterruptedException {

		String retrieveItems = "{\n"
				+ "    \"jsonrpc\": \"2.0\",\n"
				+ "    \"method\": \"item.get\",\n"
				+ "    \"params\": {\n"
				+ "        \"output\": [\n"
				+ "            \"itemid\",\n"
				+ "            \"name\",\n"
				+ "            \"key_\",\n"
				+ "            \"lastclock\",\n"
				+ "            \"lastvalue\"\n"
				+ "        ],\n"
				+ "        \"webitems\": true,\n"
				+ "        \"history\": \"0\",\n"
				+ "        \"filter\": {\n"
				+ "            \"hostid\": \"\"\n"
				+ "        },\n"
				+ "        \"selectHosts\": [\n"
				+ "            \"hostid\",\n"
				+ "            \"host\"\n"
				+ "        ],\n"
				+ "        \"search\": {\n"
				+ "            \"key_\": \"web.test.error\"\n"
				+ "        },\n"
				+ "        \"selectApplications\": [\n"
				+ "            \"applicationid\"\n"
				+ "        ]\n"
				+ "    },\n"
				+ "    \"auth\": \""+auth+"\",\n"
				+ "    \"id\": 2\n"
				+ "}";

		var values = Utils.gson.fromJson(retrieveItems, HashMap.class);
		var objectMapper = new ObjectMapper();
		String requestBody = objectMapper
				.writeValueAsString(values);


		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(EnvironmentVariables.MONITORING_URL))
				.header("Content-Type", "application/json-rpc")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();

		HttpClient client = HttpClient.newHttpClient();

		HttpResponse<String> response = client.send(request,
				HttpResponse.BodyHandlers.ofString());

		return response.body();
	}

	public static String getProblems(String auth) throws IOException, InterruptedException {

		String retrieveItems = "{\n"
				+ "    \"jsonrpc\": \"2.0\",\n"
				+ "    \"method\": \"problem.get\",\n"
				+ "    \"params\": {\n"
				+ "        \"output\": [\n"
				+ "            \"name\",\n"
				+ "            \"clock\"\n"
				+ "        ],\n"
				+ "        \"filter\": {\n"
				+ "            \"hostid\": \"\"\n"
				+ "        },\n"
				+ "        \"recent\": false\n"
				+ "    },\n"
				+ "    \"auth\": \""+auth+"\",\n"
				+ "    \"id\": 3\n"
				+ "}";

		var values = Utils.gson.fromJson(retrieveItems, HashMap.class);

		var objectMapper = new ObjectMapper();
		String requestBody = objectMapper
				.writeValueAsString(values);


		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(EnvironmentVariables.MONITORING_URL))
				.header("Content-Type", "application/json-rpc")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();

		HttpClient client = HttpClient.newHttpClient();

		HttpResponse<String> response = client.send(request,
				HttpResponse.BodyHandlers.ofString());

		return response.body();
	}

	public static String getHttpItems(String auth, String hostid) throws IOException, InterruptedException {

		String retrieveItems = "{\n"
				+ "        \"jsonrpc\": \"2.0\",\n"
				+ "        \"method\": \"httptest.get\",\n"
				+ "        \"params\": {\n"
				+ "          \"output\": [\"name\", \"httptestid\"],\n"
				+ "          \"recent\" : false,\n"
				+ "          \"filter\": {\n"
				+ "              \"hostid\" : \""+hostid+"\"\n"
				+ "          },\n"
				+ "          \"selectHosts\" : [\"hostid\", \"host\"]\n"
				+ "        },\n"
				+ "        \"auth\": \""+auth+"\",\n"
				+ "        \"id\": 1\n"
				+ "      }";

		var values = Utils.gson.fromJson(retrieveItems, HashMap.class);

		var objectMapper = new ObjectMapper();
		String requestBody = objectMapper
				.writeValueAsString(values);


		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(EnvironmentVariables.MONITORING_URL))
				.header("Content-Type", "application/json-rpc")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();

		HttpClient client = HttpClient.newHttpClient();

		HttpResponse<String> response = client.send(request,
				HttpResponse.BodyHandlers.ofString());

		return response.body();
	}

	protected static String getHttpTest(String auth) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		String httpTest = "{\n"
				+ "    \"jsonrpc\": \"2.0\",\n"
				+ "    \"method\": \"httptest.get\",\n"
				+ "    \"params\": {\n"
				+ "        \"output\": [\n"
				+ "            \"name\",\n"
				+ "            \"httptestid\"\n"
				+ "        ],\n"
				+ "        \"filter\": {\n"
				+ "            \"hostid\": \"\"\n"
				+ "        },\n"
				+ "        \"selectHosts\": [\n"
				+ "            \"hostid\",\n"
				+ "            \"host\"\n"
				+ "        ],\n"
				+ "        \"recent\": false\n"
				+ "    },\n"
				+ "    \"auth\": \""+auth+"\",\n"
				+ "    \"id\": 4\n"
				+ "}";
		var values = Utils.gson.fromJson(httpTest, HashMap.class);

		var objectMapper = new ObjectMapper();
		String requestBody = objectMapper
				.writeValueAsString(values);

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(EnvironmentVariables.MONITORING_URL))
				.header("Content-Type", "application/json-rpc")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();

		HttpResponse<String> response = client.send(request,
				HttpResponse.BodyHandlers.ofString());


		return Utils.gson.fromJson(response.body(), JsonObject.class).get("result").getAsString();
	}

	protected static String getGraphs(String auth) throws IOException, InterruptedException {
		// TODO Auto-generated method stub
		String httpTest = "{\n"
				+ "    \"jsonrpc\": \"2.0\",\n"
				+ "    \"method\": \"httptest.get\",\n"
				+ "    \"params\": {\n"
				+ "        \"output\": [\n"
				+ "            \"name\",\n"
				+ "            \"httptestid\"\n"
				+ "        ],\n"
				+ "        \"filter\": {\n"
				+ "            \"hostid\": \"\"\n"
				+ "        },\n"
				+ "        \"selectHosts\": [\n"
				+ "            \"hostid\",\n"
				+ "            \"host\"\n"
				+ "        ],\n"
				+ "        \"recent\": false\n"
				+ "    },\n"
				+ "    \"auth\": \""+auth+"\",\n"
				+ "    \"id\": 4\n"
				+ "}";
		var values = Utils.gson.fromJson(httpTest, HashMap.class);

		var objectMapper = new ObjectMapper();
		String requestBody = objectMapper
				.writeValueAsString(values);

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(EnvironmentVariables.MONITORING_URL))
				.header("Content-Type", "application/json-rpc")
				.POST(HttpRequest.BodyPublishers.ofString(requestBody))
				.build();

		HttpResponse<String> response = client.send(request,
				HttpResponse.BodyHandlers.ofString());


		return Utils.gson.fromJson(response.body(), JsonObject.class).get("result").getAsString();
	}



	public JsonArray getHostResults() {
		return hostResults;
	}

	public void setHostResults(JsonArray hostResults) {
		this.hostResults = hostResults;
	}

	private String getSubString(final String input, char characterStart, char characterEnd) {
	    if(input == null) {
	        return null;
	    }
	    
	    final int indexOfAt = input.indexOf(characterStart);
	    if(input.isEmpty() || indexOfAt < 0 || indexOfAt > input.length()-1) {
	        return null;
	    }

	    String suffix = input.substring(indexOfAt + 1);
	    
	    final int indexOfDot = suffix.indexOf(characterEnd);
	    
	    if(indexOfDot < 1) {
	        return null;
	    }

	    return suffix.substring(0, indexOfDot);
	}

}
