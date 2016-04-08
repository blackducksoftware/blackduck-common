package com.blackducksoftware.integration.hub.meta;


public class MetaLink {
	private final String rel;

	private final String href;

	public MetaLink(final String rel, final String href) {
		this.rel = rel;
		this.href = href;
	}

	public String getRel() {
		return rel;
	}

	public String getHref() {
		return href;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((href == null) ? 0 : href.hashCode());
		result = prime * result + ((rel == null) ? 0 : rel.hashCode());
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
		if (!(obj instanceof MetaLink)) {
			return false;
		}
		final MetaLink other = (MetaLink) obj;
		if (href == null) {
			if (other.href != null) {
				return false;
			}
		} else if (!href.equals(other.href)) {
			return false;
		}
		if (rel == null) {
			if (other.rel != null) {
				return false;
			}
		} else if (!rel.equals(other.rel)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("MetaLink [rel=");
		builder.append(rel);
		builder.append(", href=");
		builder.append(href);
		builder.append("]");
		return builder.toString();
	}

}
