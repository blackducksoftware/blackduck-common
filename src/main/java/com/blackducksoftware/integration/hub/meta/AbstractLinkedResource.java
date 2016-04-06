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
}
