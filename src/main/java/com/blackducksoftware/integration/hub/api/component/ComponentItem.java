package com.blackducksoftware.integration.hub.api.component;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ComponentItem extends HubItem {

	private final String component;
	private final String componentName;
	private final String originId;
	private final String version;
	private final String versionName;

	public ComponentItem(final MetaInformation meta, final String component, final String componentName,
			final String originId, final String version, final String versionName) {
		super(meta);
		this.component = component;
		this.componentName = componentName;
		this.originId = originId;
		this.version = version;
		this.versionName = versionName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((component == null) ? 0 : component.hashCode());
		result = prime * result + ((componentName == null) ? 0 : componentName.hashCode());
		result = prime * result + ((originId == null) ? 0 : originId.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		result = prime * result + ((versionName == null) ? 0 : versionName.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final ComponentItem other = (ComponentItem) obj;
		if (component == null) {
			if (other.component != null) {
				return false;
			}
		} else if (!component.equals(other.component)) {
			return false;
		}
		if (componentName == null) {
			if (other.componentName != null) {
				return false;
			}
		} else if (!componentName.equals(other.componentName)) {
			return false;
		}
		if (originId == null) {
			if (other.originId != null) {
				return false;
			}
		} else if (!originId.equals(other.originId)) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		if (versionName == null) {
			if (other.versionName != null) {
				return false;
			}
		} else if (!versionName.equals(other.versionName)) {
			return false;
		}
		return true;
	}

	public String getComponent() {
		return component;
	}

	public String getComponentName() {
		return componentName;
	}

	public String getOriginId() {
		return originId;
	}

	public String getVersion() {
		return version;
	}

	public String getVersionName() {
		return versionName;
	}

}
