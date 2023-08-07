package org.epos.handler;

import java.io.Serializable;
import java.net.ConnectException;
import java.util.Map;

import com.zaxxer.hikari.pool.HikariPool;
import org.eclipse.persistence.exceptions.DatabaseException;
import org.epos.handler.HeaderParser.HeaderParserBuilder;
import org.epos.handler.operations.converter.ConverterGet;
import org.epos.handler.operations.externalaccess.ExternalAccessGet;
import org.epos.handler.operations.ingestor.IngestorPost;
import org.epos.handler.operations.resources.ResourcesGet;
import org.epos.handler.operations.sender.SenderGet;
import org.epos.router_framework.domain.Actor;
import org.epos.router_framework.domain.BuiltInActorType;
import org.epos.router_framework.exception.RoutingMessageHandlingException;
import org.epos.router_framework.handling.PlainTextRelayRouterHandler;
import org.epos.router_framework.types.ServiceType;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import javax.persistence.PersistenceException;

public class RequestHandler extends PlainTextRelayRouterHandler {
	

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class); 

	public RequestHandler(Actor defaultNextActor) {
		super(defaultNextActor);
	}

	@Override
	public Serializable handle(String payload, ServiceType service, Map<String, Object> headers)
			throws RoutingMessageHandlingException {
		String errorMessage = "";
		for (int attemptNumber = 1 ; attemptNumber <= 2 ; attemptNumber++ ) {
			try {
				return trueHandle(payload, headers);
			} catch (PSQLException | DatabaseException | ConnectException | PersistenceException | HikariPool.PoolInitializationException e){
				String msg = "Attempt to connect to the DB number failed.";
				LOGGER.warn(msg);
				System.err.println(msg);
				errorMessage = e.getMessage();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		String msg = "Connection to the DB failed. Error Message: \n" + errorMessage + "\nProcede to exit";
		LOGGER.warn(msg);
		System.err.println(msg);
		System.exit(1);
		return "{\"error\": \"failed to connect to the db\"}";
	}

	private String trueHandle(String payload, Map<String, Object> headers) throws RoutingMessageHandlingException, PSQLException, ConnectException {
		LOGGER.info("Received request from endpoint: "+ headers);

		HeaderParser headerParser = new HeaderParser(new HeaderParserBuilder(headers));
		
		System.out.println(headerParser.toString());

		JsonObject response = new JsonObject();


		switch(headerParser.getOperation()) {
		case DELETE:
			switch(headerParser.getOrigin()) {
			default:
				break;
			}
			break;
		case GET:
			switch(headerParser.getOrigin()) {
			case CONVERTER:
				break;
			case PLUGINS:
				this.setOverriddenNextActor(Actor.getInstance(BuiltInActorType.CONVERTER));
				return new ConverterGet().operationAction(headerParser, headers, payload, response);
			case EXTERNALACCESS:
				return new ExternalAccessGet().operationAction(headerParser, headers, payload, response);
			case INGESTOR:
				break;
			case RESOURCES:
				return new ResourcesGet().operationAction(headerParser, headers, payload, response);
			case WORKSPACES:
				break;
			default:
				break;
			}
			break;
		case POST:
			switch(headerParser.getOrigin()) {
			case INGESTOR:
				return new IngestorPost().operationAction(headerParser, headers, payload, response);
			case SENDER:
				return new SenderGet().operationAction(headerParser, headers, payload, response);
			default:
				break;
			}
			break;
		case PUT:
			switch(headerParser.getOrigin()) {
			default:
				break;

			}
			break;
		default:
			break;
		}

		return response.toString();
	}

}
