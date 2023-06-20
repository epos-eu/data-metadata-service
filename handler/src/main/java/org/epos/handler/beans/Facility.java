package org.epos.handler.beans;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Facility implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List<String> equipments;
	
	private String href;
	
	private String id;
	private String type;
	private String title;
	private String description;
	private List<String> categories;
	private List<String> pageurl;
	private List<String> services;
	private SpatialInfo spatial;
	private List<AvailableFormat> availableFormats;
	
	
	public Facility() {
		equipments = new ArrayList<>();
		categories = new ArrayList<>();
		pageurl = new ArrayList<>();
		services = new ArrayList<>();
		availableFormats = new ArrayList<>();
		spatial = new SpatialInfo();
	}


	public List<String> getEquipments() {
		return equipments;
	}


	public void setEquipments(List<String> equipmentsid) {
		this.equipments = equipmentsid;
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


	public String getType() {
		return type;
	}


	public void setType(String type) {
		this.type = type;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getCategories() {
		return categories;
	}


	public void setCategories(List<String> categories) {
		this.categories = categories;
	}


	public List<String> getPageurl() {
		return pageurl;
	}


	public void setPageurl(List<String> pageurl) {
		this.pageurl = pageurl;
	}


	public List<String> getServices() {
		return services;
	}


	public void setServices(List<String> services) {
		this.services = services;
	}


	public SpatialInfo getSpatial() {
		return spatial;
	}


	public void setSpatial(SpatialInfo spatial) {
		this.spatial = spatial;
	}


	public List<AvailableFormat> getAvailableFormats() {
		return availableFormats;
	}


	public void setAvailableFormats(List<AvailableFormat> availableFormats) {
		this.availableFormats = availableFormats;
	}


	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	

}
