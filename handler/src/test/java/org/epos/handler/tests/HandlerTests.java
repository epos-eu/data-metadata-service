package org.epos.handler.tests;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

import org.epos.handler.HeaderParser;
import org.epos.handler.HeaderParser.HeaderParserBuilder;
import org.epos.handler.enums.DataOriginType;
import org.epos.handler.enums.OperationType;
import org.epos.handler.enums.RequestDomainType;
import org.junit.jupiter.api.Test;

class HandlerTests {

	@Test
	void testHeaderComplex() {
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("epos_operation-type", "get");
		Map<String, Object> requestheader = new HashMap<String, Object>();
		requestheader.put("origin", "backoffice");
		requestheader.put("domain", "backoffice");
		requestheader.put("object", "Person");
		header.put("epos_request-type", requestheader);
		
		HeaderParser hp = new HeaderParser(new HeaderParserBuilder(header));
	    assertEquals(hp.getDomain(), RequestDomainType.BACKOFFICE);
	    assertEquals(hp.getObject(), "Person");
	    assertEquals(hp.getOperation(), OperationType.GET);
	    assertEquals(hp.getOrigin(), DataOriginType.BACKOFFICE);
	    
	}
	
	@Test
	void testHeaderSimple() {
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("epos_operation-type", "get");
		header.put("epos_request-type", "resources.get.testing.k8s-epos-deploy.new-environment.api.resources-service.v1.resources.search");
		    
		HeaderParser hp = new HeaderParser(new HeaderParserBuilder(header));
	    assertEquals(hp.getDomain(), RequestDomainType.RESOURCES);
	    assertEquals(hp.getObject(), "SEARCH");
	    assertEquals(hp.getOperation(), OperationType.GET);
	    assertEquals(hp.getOrigin(), DataOriginType.RESOURCES);
	    
	}
	
	@Test
	void testConverter() {
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("epos_operation-type", "get");
		header.put("epos_request-type", "converter-plugins");
		    
		HeaderParser hp = new HeaderParser(new HeaderParserBuilder(header));
	    assertEquals(hp.getDomain(), RequestDomainType.CONVERTERPLUGINS);
	    assertEquals(hp.getObject(), "CONVERTERPLUGINS");
	    assertEquals(hp.getOperation(), OperationType.GET);
	    assertEquals(hp.getOrigin(), DataOriginType.CONVERTERPLUGINS);
	    
	}
	
	@Test
	void testIngestor() {
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("epos_operation-type", "post");
		Map<String, Object> requestheader = new HashMap<String, Object>();
		requestheader.put("origin", "ingestor");
		requestheader.put("domain", "ingestor");
		requestheader.put("object", "null");
		header.put("epos_request-type", requestheader);
		
		HeaderParser hp = new HeaderParser(new HeaderParserBuilder(header));
	    assertEquals(hp.getDomain(), RequestDomainType.INGESTOR);
	    assertEquals(hp.getObject(), "null");
	    assertEquals(hp.getOperation(), OperationType.POST);
	    assertEquals(hp.getOrigin(), DataOriginType.INGESTOR);
	    
	}
	
	@Test
	void testResourcesPlugins() {
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("epos_operation-type", "get");
		header.put("epos_request-type", "testing.k8s-epos-deploy.new-environment.api.resources-service.v1.resources.plugins");
		    
		HeaderParser hp = new HeaderParser(new HeaderParserBuilder(header));
	    assertEquals(hp.getDomain(), RequestDomainType.RESOURCES);
	    assertEquals(hp.getObject(), "PLUGINS");
	    assertEquals(hp.getOperation(), OperationType.GET);
	    assertEquals(hp.getOrigin(), DataOriginType.RESOURCES);
	    
	}

	
	@Test
	void testProcessing() {
		Map<String, Object> header = new HashMap<String, Object>();
		header.put("epos_operation-type", "get");
		header.put("epos_request-type", "test.api.distributed-processing-service.v1.processing.serviceslist");
		
		HeaderParser hp = new HeaderParser(new HeaderParserBuilder(header));
	    assertEquals(hp.getDomain(), RequestDomainType.PROCESSING);
	    assertEquals(hp.getObject(), "SERVICESLIST");
	    assertEquals(hp.getOperation(), OperationType.GET);
	    assertEquals(hp.getOrigin(), DataOriginType.PROCESSING);
	    
	}
}
