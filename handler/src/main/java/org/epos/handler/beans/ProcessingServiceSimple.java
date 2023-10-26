package org.epos.handler.beans;

import java.util.Objects;

public class ProcessingServiceSimple  {
	
	private String href = null;

	private String id = null;

	private String name = null;

	private String description = null;

	private String provider = null;
	
	public ProcessingServiceSimple() {}

	public ProcessingServiceSimple(String href, String id, String name, String description, String provider) {
		super();
		this.href = href;
		this.id = id;
		this.name = name;
		this.description = description;
		this.provider = provider;
	}	
	
	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	@Override
	public int hashCode() {
		return Objects.hash(description, id, name, provider);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProcessingServiceSimple other = (ProcessingServiceSimple) obj;
		return Objects.equals(description, other.description) && Objects.equals(id, other.id)
				&& Objects.equals(name, other.name) && Objects.equals(provider, other.provider);
	}

	@Override
	public String toString() {
		return "ProcessingServiceSimple [id=" + id + ", name=" + name + ", description=" + description + ", provider="
				+ provider + "]";
	}
	
}
