package org.epos.handler.enums;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public enum DataOriginType {

	WORKSPACES("workspaces-service"),
	RESOURCES("resources-service"),
	INGESTOR("ingestor"),
	CONVERTER("converter-service"),
	EXTERNALACCESS("external-access-service"),
	BACKOFFICE("backoffice"),
	PLUGINS("converter-plugins"); // TEMPORARY

	private String value;

	DataOriginType(String value) {
		this.setValue(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	public static Optional<DataOriginType> getInstance(String value) {
		return StringUtils.isBlank(value) ? Optional.empty() : 
			Arrays.stream(DataOriginType.values())
			.filter(v -> StringUtils.isNotBlank(v.getValue()) && v.getValue().equals(value))
			.findAny();
	}
}
