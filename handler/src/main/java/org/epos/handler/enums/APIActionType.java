package org.epos.handler.enums;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public enum APIActionType {

	SEARCH("search"),
	DETAILS("details"),
	MONITORING("monitoring"),
	PLUGINS("plugins"),
	SENDEMAIL("send-email"),
	VALIDATIONS("validations"),
	CONTACTPOINT("contactpoints"),
	DATAPRODUCT("dataproducts"),
	DISTRIBUTION("distributions"),
	EQUIPMENT("equipments"),
	FACILITY("facilities"),
	OPERATION("operations"),
	ORGANISATION("organisations"),
	PERSON("persons"),
	SERVICE("services"),
	SOFTWARE("softwares"),
	WEBSERVICE("webservices"),
	CONVERTERPLUGINS("converter-plugins"),
	SHOWEQUIPMENTS("show-equipments"),
	SHOWFACILITIES("show-facilities");
	
	private String value;
	
	APIActionType(String value) {
		this.setValue(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	public static Optional<APIActionType> getInstance(String value) {
		return StringUtils.isBlank(value) ? Optional.empty() : 
			Arrays.stream(APIActionType.values())
			.filter(v -> StringUtils.isNotBlank(v.getValue()) && v.getValue().equals(value))
			.findAny();
	}
}
