package org.epos.handler.enums;


public enum AvailableFormatType {
	
	ORIGINAL("original"),
	CONVERTED("converted");
	
	private String value;
	
	AvailableFormatType(String value) {
		this.setValue(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
