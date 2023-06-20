package org.epos.handler.beans;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MonitoringBean {
	
	private String identifier;
	private String name;
	private String originalURL;
	private HashSet<ValidationRule> validationRules;
	private HashSet<Contacts> contactPoints;
	private String TCSGroup;
	private String id;
	private String uid;
	
	
	public MonitoringBean(String identifier, String originalURL, HashSet<ValidationRule> validationRules,
			HashSet<Contacts> contactPoints, String tCSGroup) {
		this.identifier = identifier;
		this.originalURL = originalURL;
		this.validationRules = validationRules;
		this.contactPoints = contactPoints;
		this.TCSGroup = tCSGroup;
	}
	
	public MonitoringBean() {
	}

	public void createValidationRule(String type, String encodingFormat, String schemaVersion) {
		if(this.getValidationRules()==null)
			this.setValidationRules(new HashSet<MonitoringBean.ValidationRule>());
		this.getValidationRules().add(new ValidationRule(type, encodingFormat, schemaVersion));
	}
	
	public void createContacts(String name, String role, List<String> list) {
		if(this.getContactPoints()==null)
			this.setContactPoints(new HashSet<MonitoringBean.Contacts>());
		this.getContactPoints().add(new Contacts(name, role, list));
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

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getOriginalURL() {
		return originalURL;
	}

	public void setOriginalURL(String originalURL) {
		this.originalURL = originalURL;
	}

	public HashSet<ValidationRule> getValidationRules() {
		return validationRules;
	}

	public void setValidationRules(HashSet<ValidationRule> validationRules) {
		this.validationRules = validationRules;
	}

	public HashSet<Contacts> getContactPoints() {
		return contactPoints;
	}

	public void setContactPoints(HashSet<Contacts> contactPoints) {
		this.contactPoints = contactPoints;
	}

	public String getTCSGroup() {
		return TCSGroup;
	}

	public void setTCSGroup(String tCSGroup) {
		TCSGroup = tCSGroup;
	}


	@Override
	public String toString() {
		return "MonitoringBean [identifier=" + identifier + ", originalURL=" + originalURL + ", validationRules="
				+ validationRules + ", contactPoints=" + contactPoints + ", TCSGroup=" + TCSGroup + "]";
	}



	private class ValidationRule{
		private String type;
		private String encodingFormat;
		private String schemaVersion;
		
		
		public ValidationRule(String type, String encodingFormat, String schemaVersion) {
			this.type = type;
			this.encodingFormat = encodingFormat;
			this.schemaVersion = schemaVersion;
		}
		
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getEncodingFormat() {
			return encodingFormat;
		}
		public void setEncodingFormat(String encodingFormat) {
			this.encodingFormat = encodingFormat;
		}
		public String getSchemaVersion() {
			return schemaVersion;
		}
		public void setSchemaVersion(String schemaVersion) {
			this.schemaVersion = schemaVersion;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + ((encodingFormat == null) ? 0 : encodingFormat.hashCode());
			result = prime * result + ((schemaVersion == null) ? 0 : schemaVersion.hashCode());
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
			ValidationRule other = (ValidationRule) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (encodingFormat == null) {
				if (other.encodingFormat != null)
					return false;
			} else if (!encodingFormat.equals(other.encodingFormat))
				return false;
			if (schemaVersion == null) {
				if (other.schemaVersion != null)
					return false;
			} else if (!schemaVersion.equals(other.schemaVersion))
				return false;
			if (type == null) {
				if (other.type != null)
					return false;
			} else if (!type.equals(other.type))
				return false;
			return true;
		}

		private MonitoringBean getEnclosingInstance() {
			return MonitoringBean.this;
		}
		
	}
	
	private class Contacts{
		private String name;
		private String role;
		private List<String> email;
		
		public Contacts(String name, String role, List<String> list) {
			this.name = name;
			this.role = role;
			this.email = list;
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getRole() {
			return role;
		}
		public void setRole(String role) {
			this.role = role;
		}
		public List<String> getEmail() {
			return email;
		}
		public void setEmail(List<String> email) {
			this.email = email;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getEnclosingInstance().hashCode();
			result = prime * result + ((email == null) ? 0 : email.hashCode());
			result = prime * result + ((role == null) ? 0 : role.hashCode());
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
			Contacts other = (Contacts) obj;
			if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
				return false;
			if (email == null) {
				if (other.email != null)
					return false;
			} else if (!email.equals(other.email))
				return false;
			if (role == null) {
				if (other.role != null)
					return false;
			} else if (!role.equals(other.role))
				return false;
			return true;
		}

		private MonitoringBean getEnclosingInstance() {
			return MonitoringBean.this;
		}
		
	}

}
