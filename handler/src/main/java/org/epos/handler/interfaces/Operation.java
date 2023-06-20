package org.epos.handler.interfaces;

import java.util.Map;

import org.epos.handler.HeaderParser;
import org.epos.router_framework.exception.RoutingMessageHandlingException;

import com.google.gson.JsonObject;

public interface Operation {

	public String operationAction(HeaderParser header, Map<String, Object> headers, String payload, JsonObject response) throws RoutingMessageHandlingException;
}
