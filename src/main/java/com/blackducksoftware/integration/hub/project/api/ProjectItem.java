package com.blackducksoftware.integration.hub.project.api;

import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.meta.MetaLink;

public class ProjectItem {

	public static final String VERSION_LINK = "versions";

	public static final String CANONICAL_VERSION_LINK = "canonicalVersion";

	private final String name;

	private final String source;

	private final MetaInformation _meta;

	public ProjectItem(final String name, final String source, final MetaInformation _meta) {
		this.name = name;
		this.source = source;
		this._meta = _meta;
	}

	public String getName() {
		return name;
	}

	public String getSource() {
		return source;
	}

	public MetaInformation get_meta() {
		return _meta;
	}

	public String getLink(final String linkRel){
		if(get_meta() != null && get_meta().getLinks() != null && !get_meta().getLinks().isEmpty()){
			for(final MetaLink link : get_meta().getLinks()){
				if(link.getRel().equalsIgnoreCase(linkRel)){
					return link.getHref();
				}
			}
		}
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_meta == null) ? 0 : _meta.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
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
		if (!(obj instanceof ProjectItem)) {
			return false;
		}
		final ProjectItem other = (ProjectItem) obj;
		if (_meta == null) {
			if (other._meta != null) {
				return false;
			}
		} else if (!_meta.equals(other._meta)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (source == null) {
			if (other.source != null) {
				return false;
			}
		} else if (!source.equals(other.source)) {
			return false;
		}
		return true;
	}


	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ProjectItem [name=");
		builder.append(name);
		builder.append(", source=");
		builder.append(source);
		builder.append(", _meta=");
		builder.append(_meta);
		builder.append("]");
		return builder.toString();
	}

}
