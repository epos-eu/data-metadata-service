package org.epos.handler.beans;


import java.io.Serializable;

import org.epos.handler.enums.ProviderType;

public class AvailableContactPoints implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String href;
	private ProviderType type;
	
	public AvailableContactPoints(AvailableContactPointsBuilder builder) {
		this.href = builder.href;
		this.type = builder.type;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public ProviderType getType() {
		return type;
	}

	public void setType(ProviderType type) {
		this.type = type;
	}

	public static class AvailableContactPointsBuilder{

		private String href;
		private ProviderType type = ProviderType.ALL;
		
		public AvailableContactPointsBuilder() {}
		
		public AvailableContactPointsBuilder href(String href) {
			this.href = href;
			return this;
		}

		public AvailableContactPointsBuilder type(ProviderType type) {
			this.type = type;
			return this;
		}

		public AvailableContactPoints build() {
			return new AvailableContactPoints(this);
		}
	}

}
