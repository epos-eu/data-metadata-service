package org.epos.handler.beans;


import java.io.Serializable;
import java.util.*;

public class Distribution implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private transient String productid;
	private transient String distributionid;
	private String operationid;
	
	private String href;

	private HashMap<String, ArrayList<String>> categories;
	
	private String id;
	private String uid;
	private String type;
	private String title;
	private String description;
	private String license;
	private String downloadURL;
	private List<String> keywords;
	private List<DataServiceProvider> dataProvider;
	private String frequencyUpdate;
	private List<String> internalID;
	private List<String> DOI;
	private SpatialInfo spatial;
	private TemporalCoverage temporalCoverage;	
	private List<String> scienceDomain;
	private String hasQualityAnnotation;
	private List<AvailableFormat> availableFormats;
	private List<AvailableContactPoints> availableContactPoints;
	
	// WEBSERVICE
	
	private String serviceName;
	private String serviceDescription;
	private DataServiceProvider serviceProvider;
	private SpatialInfo serviceSpatial;
	private TemporalCoverage serviceTemporalCoverage;
	private String serviceEndpoint;
	private String serviceDocumentation;
	private List<String> serviceType;
	private String endpoint;

	private List<ServiceParameter> serviceParameters;
	
	public Distribution() {
		keywords = new ArrayList<>();
		dataProvider = new ArrayList<>();
		internalID = new ArrayList<>();
		DOI = new ArrayList<>();
		availableFormats = new ArrayList<>();
		availableContactPoints = new ArrayList<>();
		serviceSpatial = new SpatialInfo();
		spatial = new SpatialInfo();
		temporalCoverage = new TemporalCoverage();
		serviceTemporalCoverage = new TemporalCoverage();
		scienceDomain = new ArrayList<>();
		serviceType = new ArrayList<>();
	}

	public Distribution(String productid, String distributionid, String operationid, String href, String id, String uid,
						String type, String title, String description, String license, String downloadURL, List<String> keywords,
						List<DataServiceProvider> dataProvider, String frequencyUpdate, List<String> internalID, List<String> DOI, SpatialInfo spatial,
						TemporalCoverage temporalCoverage, List<AvailableFormat> availableFormats, String serviceName,
						String serviceDescription, DataServiceProvider serviceProvider, SpatialInfo serviceSpatial,
						TemporalCoverage serviceTemporalCoverage, String serviceEndpoint, String serviceDocumentation,
						String endpoint, List<ServiceParameter> serviceParameters) {
		super();
		this.productid = productid;
		this.distributionid = distributionid;
		this.operationid = operationid;
		this.href = href;
		this.id = id;
		this.uid = uid;
		this.type = type;
		this.title = title;
		this.description = description;
		this.license = license;
		this.downloadURL = downloadURL;
		this.keywords = keywords;
		this.dataProvider = dataProvider;
		this.frequencyUpdate = frequencyUpdate;
		this.internalID = internalID;
		this.DOI = DOI;
		this.spatial = spatial;
		this.temporalCoverage = temporalCoverage;
		this.availableFormats = availableFormats;
		this.serviceName = serviceName;
		this.serviceDescription = serviceDescription;
		this.serviceProvider = serviceProvider;
		this.serviceSpatial = serviceSpatial;
		this.serviceTemporalCoverage = serviceTemporalCoverage;
		this.serviceEndpoint = serviceEndpoint;
		this.serviceDocumentation = serviceDocumentation;
		this.endpoint = endpoint;
		this.serviceParameters = serviceParameters;
	}



	public String getProductid() {
		return productid;
	}

	public HashMap<String, ArrayList<String>> getCategories() {
		return categories;
	}

	public void setCategories(HashMap<String, ArrayList<String>> categories) {
		this.categories = categories;
	}

	public void setProductid(String productid) {
		this.productid = productid;
	}

	public String getDistributionid() {
		return distributionid;
	}

	public void setDistributionid(String distributionid) {
		this.distributionid = distributionid;
	}

	public String getOperationid() {
		return operationid;
	}

	public void setOperationid(String operationid) {
		this.operationid = operationid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
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

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getDownloadURL() {
		return downloadURL;
	}

	public void setDownloadURL(String downloadURL) {
		this.downloadURL = downloadURL;
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public List<DataServiceProvider> getDataProvider() {
		return dataProvider;
	}

	public void setDataProvider(List<DataServiceProvider> dataServiceProvider) {
		this.dataProvider = dataServiceProvider;
	}

	public String getFrequencyUpdate() {
		return frequencyUpdate;
	}

	public void setFrequencyUpdate(String frequencyUpdate) {
		this.frequencyUpdate = frequencyUpdate;
	}

	public List<String> getInternalID() {
		return internalID;
	}

	public void setInternalID(List<String> internalID) {
		this.internalID = internalID;
	}

	public List<String> getDOI() {
		return DOI;
	}

	public void setDOI(List<String> dOI) {
		DOI = dOI;
	}

	public SpatialInfo getSpatial() {
		return spatial;
	}

	public void setSpatial(SpatialInfo spatial) {
		this.spatial = spatial;
	}

	public TemporalCoverage getTemporalCoverage() {
		return temporalCoverage;
	}

	public void setTemporalCoverage(TemporalCoverage temporalCoverage) {
		this.temporalCoverage = temporalCoverage;
	}

	public List<AvailableFormat> getAvailableFormats() {
		return availableFormats;
	}

	public void setAvailableFormats(List<AvailableFormat> availableFormats) {
		this.availableFormats = availableFormats;
	}

	public List<AvailableContactPoints> getAvailableContactPoints() {
		return availableContactPoints;
	}

	public void setAvailableContactPoints(List<AvailableContactPoints> availableContactPoints) {
		this.availableContactPoints = availableContactPoints;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceDescription() {
		return serviceDescription;
	}

	public void setServiceDescription(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}

	public DataServiceProvider getServiceProvider() {
		return serviceProvider;
	}

	public void setServiceProvider(DataServiceProvider serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	public SpatialInfo getServiceSpatial() {
		return serviceSpatial;
	}

	public void setServiceSpatial(SpatialInfo serviceSpatial) {
		this.serviceSpatial = serviceSpatial;
	}

	public TemporalCoverage getServiceTemporalCoverage() {
		return serviceTemporalCoverage;
	}

	public void setServiceTemporalCoverage(TemporalCoverage serviceTemporalCoverage) {
		this.serviceTemporalCoverage = serviceTemporalCoverage;
	}

	public String getServiceEndpoint() {
		return serviceEndpoint;
	}

	public void setServiceEndpoint(String serviceEndpoint) {
		this.serviceEndpoint = serviceEndpoint;
	}

	public String getServiceDocumentation() {
		return serviceDocumentation;
	}

	public void setServiceDocumentation(String serviceDocumentation) {
		this.serviceDocumentation = serviceDocumentation;
	}

	public List<ServiceParameter> getParameters() {
		return serviceParameters;
	}

	public void setParameters(List<ServiceParameter> parameters) {
		this.serviceParameters = parameters;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}
	
	public List<String> getScienceDomain() {
		return scienceDomain;
	}

	public void setScienceDomain(List<String> scienceDomain) {
		this.scienceDomain = scienceDomain;
	}

	public List<String> getServiceType() {
		return serviceType;
	}

	public void setServiceType(List<String> serviceType) {
		this.serviceType = serviceType;
	}


	public String getHasQualityAnnotation() {
		return hasQualityAnnotation;
	}

	public void setHasQualityAnnotation(String hasQualityAnnotation) {
		this.hasQualityAnnotation = hasQualityAnnotation;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((DOI == null) ? 0 : DOI.hashCode());
		result = prime * result + ((availableFormats == null) ? 0 : availableFormats.hashCode());
		result = prime * result + ((dataProvider == null) ? 0 : dataProvider.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((distributionid == null) ? 0 : distributionid.hashCode());
		result = prime * result + ((downloadURL == null) ? 0 : downloadURL.hashCode());
		result = prime * result + ((endpoint == null) ? 0 : endpoint.hashCode());
		result = prime * result + ((frequencyUpdate == null) ? 0 : frequencyUpdate.hashCode());
		result = prime * result + ((href == null) ? 0 : href.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((internalID == null) ? 0 : internalID.hashCode());
		result = prime * result + ((keywords == null) ? 0 : keywords.hashCode());
		result = prime * result + ((license == null) ? 0 : license.hashCode());
		result = prime * result + ((serviceDescription == null) ? 0 : serviceDescription.hashCode());
		result = prime * result + ((serviceDocumentation == null) ? 0 : serviceDocumentation.hashCode());
		result = prime * result + ((serviceEndpoint == null) ? 0 : serviceEndpoint.hashCode());
		result = prime * result + ((serviceName == null) ? 0 : serviceName.hashCode());
		result = prime * result + ((serviceParameters == null) ? 0 : serviceParameters.hashCode());
		result = prime * result + ((serviceProvider == null) ? 0 : serviceProvider.hashCode());
		result = prime * result + ((serviceSpatial == null) ? 0 : serviceSpatial.hashCode());
		result = prime * result + ((serviceTemporalCoverage == null) ? 0 : serviceTemporalCoverage.hashCode());
		result = prime * result + ((spatial == null) ? 0 : spatial.hashCode());
		result = prime * result + ((temporalCoverage == null) ? 0 : temporalCoverage.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Distribution other = (Distribution) obj;
		if (DOI == null) {
			if (other.DOI != null)
				return false;
		} else if (!DOI.equals(other.DOI))
			return false;
		if (availableFormats == null) {
			if (other.availableFormats != null)
				return false;
		} else if (!availableFormats.equals(other.availableFormats))
			return false;
		if (dataProvider == null) {
			if (other.dataProvider != null)
				return false;
		} else if (!dataProvider.equals(other.dataProvider))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (distributionid == null) {
			if (other.distributionid != null)
				return false;
		} else if (!distributionid.equals(other.distributionid))
			return false;
		if (downloadURL == null) {
			if (other.downloadURL != null)
				return false;
		} else if (!downloadURL.equals(other.downloadURL))
			return false;
		if (endpoint == null) {
			if (other.endpoint != null)
				return false;
		} else if (!endpoint.equals(other.endpoint))
			return false;
		if (frequencyUpdate == null) {
			if (other.frequencyUpdate != null)
				return false;
		} else if (!frequencyUpdate.equals(other.frequencyUpdate))
			return false;
		if (href == null) {
			if (other.href != null)
				return false;
		} else if (!href.equals(other.href))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (internalID == null) {
			if (other.internalID != null)
				return false;
		} else if (!internalID.equals(other.internalID))
			return false;
		if (keywords == null) {
			if (other.keywords != null)
				return false;
		} else if (!keywords.equals(other.keywords))
			return false;
		if (license == null) {
			if (other.license != null)
				return false;
		} else if (!license.equals(other.license))
			return false;
		if (serviceDescription == null) {
			if (other.serviceDescription != null)
				return false;
		} else if (!serviceDescription.equals(other.serviceDescription))
			return false;
		if (serviceDocumentation == null) {
			if (other.serviceDocumentation != null)
				return false;
		} else if (!serviceDocumentation.equals(other.serviceDocumentation))
			return false;
		if (serviceEndpoint == null) {
			if (other.serviceEndpoint != null)
				return false;
		} else if (!serviceEndpoint.equals(other.serviceEndpoint))
			return false;
		if (serviceName == null) {
			if (other.serviceName != null)
				return false;
		} else if (!serviceName.equals(other.serviceName))
			return false;
		if (serviceParameters == null) {
			if (other.serviceParameters != null)
				return false;
		} else if (!serviceParameters.equals(other.serviceParameters))
			return false;
		if (serviceProvider == null) {
			if (other.serviceProvider != null)
				return false;
		} else if (!serviceProvider.equals(other.serviceProvider))
			return false;
		if (serviceSpatial == null) {
			if (other.serviceSpatial != null)
				return false;
		} else if (!serviceSpatial.equals(other.serviceSpatial))
			return false;
		if (serviceTemporalCoverage == null) {
			if (other.serviceTemporalCoverage != null)
				return false;
		} else if (!serviceTemporalCoverage.equals(other.serviceTemporalCoverage))
			return false;
		if (spatial == null) {
			if (other.spatial != null)
				return false;
		} else if (!spatial.equals(other.spatial))
			return false;
		if (temporalCoverage == null) {
			if (other.temporalCoverage != null)
				return false;
		} else if (!temporalCoverage.equals(other.temporalCoverage))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Distribution [operationid=" + operationid + ", href=" + href + ", categories=" + categories + ", id="
				+ id + ", uid=" + uid + ", type=" + type + ", title=" + title + ", description=" + description
				+ ", license=" + license + ", downloadURL=" + downloadURL + ", keywords=" + keywords + ", dataProvider="
				+ dataProvider + ", frequencyUpdate=" + frequencyUpdate + ", internalID=" + internalID + ", DOI=" + DOI
				+ ", spatial=" + spatial + ", temporalCoverage=" + temporalCoverage + ", scienceDomain=" + scienceDomain
				+ ", hasQualityAnnotation=" + hasQualityAnnotation + ", availableFormats=" + availableFormats
				+ ", availableContactPoints=" + availableContactPoints + ", serviceName=" + serviceName
				+ ", serviceDescription=" + serviceDescription + ", serviceProvider=" + serviceProvider
				+ ", serviceSpatial=" + serviceSpatial + ", serviceTemporalCoverage=" + serviceTemporalCoverage
				+ ", serviceEndpoint=" + serviceEndpoint + ", serviceDocumentation=" + serviceDocumentation
				+ ", serviceType=" + serviceType + ", endpoint=" + endpoint + ", serviceParameters=" + serviceParameters
				+ "]";
	}
	
	

}
