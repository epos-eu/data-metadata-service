package org.epos.handler.beans;

import java.util.List;


public class OrganizationBean {
	private String uid;
    private String logo;
    private String url;
    private String name;
    
	public OrganizationBean(String uid, String logo, String url, String object) {
		super();
		this.uid = uid;
		this.logo = logo;
		this.url = url;
		this.name = object;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
    
    
}
