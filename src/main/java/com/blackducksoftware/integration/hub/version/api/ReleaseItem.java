/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
package com.blackducksoftware.integration.hub.version.api;

import com.blackducksoftware.integration.hub.meta.AbstractLinkedResource;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ReleaseItem extends AbstractLinkedResource {

	public static final String VERSION_REPORT_LINK = "versionReport";

	public static final String RISK_PROFILE_LINK = "riskProfile";

	public static final String POLICY_STATUS_LINK = "policy-status";

	private final String versionName;

	private final String phase;

	private final String distribution;

	private final String source;


	public ReleaseItem(final String versionName, final String phase, final String distribution,
			final String source, final MetaInformation _meta) {
		super(_meta);
		this.versionName = versionName;
		this.phase = phase;
		this.distribution = distribution;
		this.source = source;
	}

	public String getVersionName() {
		return versionName;
	}

	public String getPhase() {
		return phase;
	}

	public PhaseEnum getPhaseEnum() {
		return PhaseEnum.valueOf(phase);
	}

	public String getDistribution() {
		return distribution;
	}

	public DistributionEnum getDistributionEnum() {
		return DistributionEnum.valueOf(distribution);
	}

	public String getSource() {
		return source;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((distribution == null) ? 0 : distribution.hashCode());
		result = prime * result + ((phase == null) ? 0 : phase.hashCode());
		result = prime * result + ((source == null) ? 0 : source.hashCode());
		result = prime * result + ((versionName == null) ? 0 : versionName.hashCode());
		result = prime * result + ((get_meta() == null) ? 0 : get_meta().hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ReleaseItem)) {
			return false;
		}
		final ReleaseItem other = (ReleaseItem) obj;
		if (get_meta() == null) {
			if (other.get_meta() != null) {
				return false;
			}
		} else if (!get_meta().equals(other.get_meta())) {
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
		builder.append(get_meta());
		builder.append("]");
		return builder.toString();
	}

}
