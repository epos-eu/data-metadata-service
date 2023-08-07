package org.epos.handler.enums;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public enum ProviderType {

	ALL("All"),
	DATAPROVIDERS("DataProviders"),
	SERVICEPROVIDERS("ServiceProviders");
	
	private String value;
	
	ProviderType(String value) {
		this.setValue(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	public static Optional<ProviderType> getInstance(String value) {
		return StringUtils.isBlank(value) ? Optional.empty() : 
			Arrays.stream(ProviderType.values())
			.filter(v -> StringUtils.isNotBlank(v.getValue()) && v.getValue().equals(value))
			.findAny();
	}
}
