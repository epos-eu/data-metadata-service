package org.epos.handler.enums;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

public enum OperationType {
	
	GET("get"),
	POST("post"),
	PUT("put"),
	DELETE("delete");
	
	private String value;
	
	OperationType(String value) {
		this.setValue(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public static Optional<OperationType> getInstance(String value) {
		return StringUtils.isBlank(value) ? Optional.empty() : 
					Arrays.stream(OperationType.values())
						.filter(v -> StringUtils.isNotBlank(v.getValue()) && v.getValue().equals(value))
						.findAny();
	}

}
