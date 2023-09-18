package org.epos.handler.beans;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;

public class ProcessingServiceSimple  {
	private String id = null;

	private String name = null;

	private String description = null;

	private List<Object> serviceproviders = null;

	private List<String> dependecyServices = null;


	public ProcessingServiceSimple id(String id) {
		this.id = id;
		return this;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ProcessingServiceSimple name(String name) {
		this.name = name;
		return this;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ProcessingServiceSimple description(String description) {
		this.description = description;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ProcessingServiceSimple serviceproviders(List<Object> serviceproviders) {
		this.serviceproviders = serviceproviders;
		return this;
	}

	public ProcessingServiceSimple addServiceprovidersItem(Object serviceprovidersItem) {
		if (this.serviceproviders == null) {
			this.serviceproviders = new ArrayList<Object>();
		}
		this.serviceproviders.add(serviceprovidersItem);
		return this;
	}


	public List<Object> getServiceproviders() {
		return serviceproviders;
	}

	public void setServiceproviders(List<Object> serviceproviders) {
		this.serviceproviders = serviceproviders;
	}
	

	public ProcessingServiceSimple dependecyServices(List<String> dependecyServices) {
		this.dependecyServices = dependecyServices;
		return this;
	}

	public ProcessingServiceSimple addDependencyServicesItem(String dependecyServicesItem) {
		if (this.dependecyServices == null) {
			this.dependecyServices = new ArrayList<String>();
		}
		this.dependecyServices.add(dependecyServicesItem);
		return this;
	}


	public List<String> getDependencyServices() {
		return dependecyServices;
	}

	public void setDependencyServices(List<String> dependecyServices) {
		this.dependecyServices = dependecyServices;
	}


	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		ProcessingServiceSimple listOfServices = (ProcessingServiceSimple) o;
		return Objects.equals(this.id, listOfServices.id) &&
				Objects.equals(this.name, listOfServices.name) &&
				Objects.equals(this.description, listOfServices.description) &&
				Objects.equals(this.serviceproviders, listOfServices.serviceproviders);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, name, description, serviceproviders);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class ProcessingServiceSimple {\n");

		sb.append("    id: ").append(toIndentedString(id)).append("\n");
		sb.append("    name: ").append(toIndentedString(name)).append("\n");
		sb.append("    description: ").append(toIndentedString(description)).append("\n");
		sb.append("    serviceproviders: ").append(toIndentedString(serviceproviders)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
