package org.epos.handler.beans;

import java.util.List;
import java.util.Objects;

public class DataServiceProvider {

    private String dataProviderLegalName;
    private String dataProviderUrl;
    private String country;
    private List<DataServiceProvider> relatedDataServiceProvider;

    public String getDataProviderLegalName() {
        return dataProviderLegalName;
    }

    public void setDataProviderLegalName(String dataProviderLegalName) {
        this.dataProviderLegalName = dataProviderLegalName;
    }

    public List<DataServiceProvider> getRelatedDataProvider() {
        return relatedDataServiceProvider;
    }

    public void setRelatedDataProvider(List<DataServiceProvider> relatedDataServiceProvider) {
        this.relatedDataServiceProvider = relatedDataServiceProvider;
    }

    public String getDataProviderUrl() {
        return dataProviderUrl;
    }

    public void setDataProviderUrl(String dataProviderUrl) {
        this.dataProviderUrl = dataProviderUrl;
    }

    public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public int hashCode() {
		return Objects.hash(country, dataProviderLegalName, dataProviderUrl, relatedDataServiceProvider);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataServiceProvider other = (DataServiceProvider) obj;
		return Objects.equals(country, other.country)
				&& Objects.equals(dataProviderLegalName, other.dataProviderLegalName)
				&& Objects.equals(dataProviderUrl, other.dataProviderUrl)
				&& Objects.equals(relatedDataServiceProvider, other.relatedDataServiceProvider);
	}

	
}
