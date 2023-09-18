package org.epos.handler.enums;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public enum RequestDomainType {

	EXECUTE("execute"),
	//DETAILS("details"),
	WORKSPACES("workspaces"),
	GET_ORIGINAL_URL("getoriginalurl"),
	RESOURCES("resources"),
	TNA("tna"),
	INGESTOR("ingestor"),
	CONVERTER("converter"),
	CONVERTERPLUGINS("converter-plugins"),
	PROCESSING("processing"),
	BACKOFFICE("backoffice");
	
	private String value;
	
	RequestDomainType(String value) {
		this.setValue(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	public static Optional<RequestDomainType> getInstance(String value) {
		return StringUtils.isBlank(value) ? Optional.empty() : 
			Arrays.stream(RequestDomainType.values())
			.filter(v -> StringUtils.isNotBlank(v.getValue()) && v.getValue().equals(value))
			.findAny();
	}
}
