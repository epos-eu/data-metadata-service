package org.epos.handler.test;

import java.util.List;

import org.epos.eposdatamodel.ContactPoint;

public class ProviderObject {
	
	private String serviceName;
	private List<ContactPoint> dataproviders;
	private List<ContactPoint> serviceproviders;
	

	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public List<ContactPoint> getDataproviders() {
		return dataproviders;
	}
	public void setDataproviders(List<ContactPoint> dataproviders) {
		this.dataproviders = dataproviders;
	}
	public List<ContactPoint> getServiceproviders() {
		return serviceproviders;
	}
	public void setServiceproviders(List<ContactPoint> serviceproviders) {
		this.serviceproviders = serviceproviders;
	}
	
	

}
