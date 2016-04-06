package com.blackducksoftware.integration.hub.meta;

public abstract class AbstractLinkedResource {

	private final MetaInformation _meta;

	public AbstractLinkedResource(final MetaInformation _meta) {
		this._meta = _meta;
	}

	public MetaInformation get_meta() {
		return _meta;
	}

	public String getLink(final String linkRel) {
		if (get_meta() != null && get_meta().getLinks() != null && !get_meta().getLinks().isEmpty()) {
			for (final MetaLink link : get_meta().getLinks()) {
				if (link.getRel().equalsIgnoreCase(linkRel)) {
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
		if (!(obj instanceof AbstractLinkedResource)) {
			return false;
		}
		final AbstractLinkedResource other = (AbstractLinkedResource) obj;
		if (_meta == null) {
			if (other._meta != null) {
				return false;
			}
		} else if (!_meta.equals(other._meta)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("AbstractLinkedResource [_meta=");
		builder.append(_meta);
		builder.append("]");
		return builder.toString();
	}

}
