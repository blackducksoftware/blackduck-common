package com.blackducksoftware.integration.hub.project.api;

import com.blackducksoftware.integration.hub.meta.AbstractLinkedResource;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ProjectItem extends AbstractLinkedResource {

	public static final String VERSION_LINK = "versions";

	public static final String CANONICAL_VERSION_LINK = "canonicalVersion";

	private final String name;

	private final String source;


	public ProjectItem(final String name, final String source, final MetaInformation _meta) {
		super(_meta);
		this.name = name;
		this.source = source;
	}

	public String getName() {
		return name;
	}

	public String getSource() {
		return source;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((get_meta() == null) ? 0 : get_meta().hashCode());
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
		if (get_meta() == null) {
			if (other.get_meta() != null) {
				return false;
			}
		} else if (!get_meta().equals(other.get_meta())) {
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
		builder.append(get_meta());
		builder.append("]");
		return builder.toString();
	}

}
