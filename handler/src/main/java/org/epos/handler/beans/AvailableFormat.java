package org.epos.handler.beans;


import java.io.Serializable;
import org.epos.handler.enums.AvailableFormatType;

public class AvailableFormat implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String label;
	private String format;

	private String originalFormat;
	private String href;
	private AvailableFormatType type;
	
	public AvailableFormat(AvailableFormatBuilder builder) {
		this.label = builder.label;
		this.originalFormat = builder.originalFormat;
		this.format = builder.format;
		this.href = builder.href;
		this.type = builder.type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String originalFormat) {
		this.format = format;
	}

	public String getOriginalFormat() {
		return originalFormat;
	}

	public void setOriginalFormat(String originalFormat) {
		this.originalFormat = originalFormat;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public AvailableFormatType getType() {
		return type;
	}

	public void setType(AvailableFormatType type) {
		this.type = type;
	}

	public static class AvailableFormatBuilder{

		private String label;
		private String originalFormat;
		private String format;
		private String href;
		private AvailableFormatType type = AvailableFormatType.ORIGINAL;
		
		public AvailableFormatBuilder() {}
		
		public AvailableFormatBuilder label(String label) {
			this.label = label;
			return this;
		}

		public AvailableFormatBuilder originalFormat(String originalFormat) {
			this.originalFormat = originalFormat;
			return this;
		}

		public AvailableFormatBuilder format(String format) {
			this.format = format;
			return this;
		}

		public AvailableFormatBuilder href(String href) {
			this.href = href;
			return this;
		}

		public AvailableFormatBuilder description(AvailableFormatType type) {
			this.type = type;
			return this;
		}

		public AvailableFormat build() {
			return new AvailableFormat(this);
		}
	}

}
