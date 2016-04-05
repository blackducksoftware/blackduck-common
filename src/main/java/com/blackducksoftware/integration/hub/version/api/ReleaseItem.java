package com.blackducksoftware.integration.hub.version.api;

import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.meta.MetaLink;

public class ReleaseItem {

	public static final String VERSION_REPORT_LINK = "versionReport";

	public static final String RISK_PROFILE_LINK = "riskProfile";

	public static final String POLICY_STATUS_LINK = "policy-status";

	private final String versionName;

	private final String phase;

	private final String distribution;

	private final String source;

	private final MetaInformation _meta;

	public ReleaseItem(final String versionName, final String phase, final String distribution,
			final String source, final MetaInformation _meta) {
		this.versionName = versionName;
		this.phase = phase;
		this.distribution = distribution;
		this.source = source;
		this._meta = _meta;
	}

	public String getVersionName() {
		return versionName;
	}

	public String getPhase() {
		return phase;
	}

	public String getDistribution() {
		return distribution;
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
		result = prime * result
				+ ((distribution == null) ? 0 : distribution.hashCode());
		result = prime * result + ((phase == null) ? 0 : phase.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result
				+ ((versionName == null) ? 0 : versionName.hashCode());
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
		if (!(obj instanceof ReleaseItem)) {
			return false;
		}
		final ReleaseItem other = (ReleaseItem) obj;
		if (_meta == null) {
			if (other._meta != null) {
				return false;
			}
		} else if (!_meta.equals(other._meta)) {
			return false;
		}
		if (distribution == null) {
			if (other.distribution != null) {
				return false;
			}
		} else if (!distribution.equals(other.distribution)) {
			return false;
		}
		if (phase == null) {
			if (other.phase != null) {
				return false;
			}
		} else if (!phase.equals(other.phase)) {
			return false;
		}
		if (source == null) {
			if (other.source != null) {
				return false;
			}
		} else if (!source.equals(other.source)) {
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

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ReleaseItem [versionName=");
		builder.append(versionName);
		builder.append(", phase=");
		builder.append(phase);
		builder.append(", distribution=");
		builder.append(distribution);
		builder.append(", source=");
		builder.append(source);
		builder.append(", _meta=");
		builder.append(_meta);
		builder.append("]");
		return builder.toString();
	}

}
