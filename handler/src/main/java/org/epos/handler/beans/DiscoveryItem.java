package org.epos.handler.beans;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DiscoveryItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String href;
	private String id;
	private String uid;
	private transient String sha256id;
	private transient Set<String> dataprovider;
	private transient Set<String> serviceprovider;
	private transient List<String> dataproductCategories;
	private String title;
	private String description;
	private int status = 0;
	private String statusTimestamp;
	private List<AvailableFormat> availableFormats;
	
	@Override
	public int hashCode() {
		return Objects.hash(availableFormats, description, href, id, status, statusTimestamp, title, uid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiscoveryItem other = (DiscoveryItem) obj;
		return Objects.equals(availableFormats, other.availableFormats)
				&& Objects.equals(description, other.description) && Objects.equals(href, other.href)
				&& Objects.equals(id, other.id) && status == other.status
				&& Objects.equals(statusTimestamp, other.statusTimestamp) && Objects.equals(title, other.title)
				&& Objects.equals(uid, other.uid);
	}

	@Override
	public String toString() {
		return "DiscoveryItem [href=" + href + ", id=" + id + ", uid=" + uid + ", title=" + title
				+ ", description=" + description + ", status=" + status + ", statusTimestamp=" + statusTimestamp
				+ ", availableFormats=" + availableFormats + "]";
	}

	public DiscoveryItem(DiscoveryItemBuilder builder) {
		this.href = builder.href;
		this.id = builder.id;
		this.uid = builder.uid;
		this.sha256id = builder.sha256id;
		this.dataprovider = builder.dataprovider;
		this.serviceprovider = builder.serviceprovider;
		this.dataproductCategories = builder.dataproductCategories;
		this.title = builder.title;
		this.description = builder.description;
		this.status = builder.status;
		this.statusTimestamp = builder.statusTimestamp;
		this.availableFormats = builder.availableFormats;
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

	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public String getUid() {
		return uid;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSha256id() {
		return sha256id;
	}

	public void setSha256id(String sha256id) {
		this.sha256id = sha256id;
	}

	public Set<String> getDataprovider() {
		return dataprovider;
	}

	public void setDataprovider(Set<String> dataprovider) {
		this.dataprovider = dataprovider;
	}

	public Set<String> getServiceprovider() {
		return serviceprovider;
	}

	public void setServiceprovider(Set<String> serviceprovider) {
		this.serviceprovider = serviceprovider;
	}

	public List<String> getDataproductCategories() {
		return dataproductCategories;
	}

	public void setDataproductCategories(List<String> dataproductCategories) {
		this.dataproductCategories = dataproductCategories;
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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getStatusTimestamp() {
		return statusTimestamp;
	}

	public void setStatusTimestamp(String statusTimestamp) {
		this.statusTimestamp = statusTimestamp;
	}

	public List<AvailableFormat> getAvailableFormats() {
		return availableFormats;
	}

	public void setAvailableFormats(List<AvailableFormat> availableFormats) {
		this.availableFormats = availableFormats;
	}

	public static class DiscoveryItemBuilder{
		
		private String href;
		private String id;
		private String uid;
		private transient String sha256id;
		private transient Set<String> dataprovider;
		private transient Set<String> serviceprovider;
		private transient List<String> dataproductCategories;
		private String title;
		private String description;
		private int status = 0;
		private String statusTimestamp;
		private List<AvailableFormat> availableFormats;
		
		public DiscoveryItemBuilder(String id, String href) {
			this.id = id;
			this.href = href;
		}

		public DiscoveryItemBuilder uid(String uid) {
			this.uid = uid;
			return this;
		}

		public DiscoveryItemBuilder setSha256id(String sha256id) {
			this.sha256id = sha256id;
			return this;
		}
		
		public DiscoveryItemBuilder setDataprovider(Set<String> facetsDataProviders) {
			this.dataprovider = facetsDataProviders;
			return this;
		}
		
		public DiscoveryItemBuilder setServiceProvider(Set<String> serviceprovider) {
			this.serviceprovider = serviceprovider;
			return this;
		}
		
		public DiscoveryItemBuilder setDataproductCategories(List<String> dataproductCategories) {
			this.dataproductCategories = dataproductCategories;
			return this;
		}

		public DiscoveryItemBuilder title(String title) {
			this.title = title;
			return this;
		}
		
		public DiscoveryItemBuilder description(String description) {
			this.description = description;
			return this;
		}

		public DiscoveryItemBuilder setStatus(int status) {
			this.status = status;
			return this;
		}
		
		public DiscoveryItemBuilder setStatusTimestamp(String statusTimestamp) {
			this.statusTimestamp = statusTimestamp;
			return this;
		}

		public DiscoveryItemBuilder availableFormats(List<AvailableFormat> availableFormats) {
			this.availableFormats = availableFormats;
			return this;
		}
		
		public DiscoveryItem build() {
            return new DiscoveryItem(this);
        }
	}
	
}
