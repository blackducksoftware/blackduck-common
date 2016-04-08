package com.blackducksoftware.integration.hub.scan.status;

import com.blackducksoftware.integration.hub.meta.AbstractLinkedResource;
import com.blackducksoftware.integration.hub.meta.MetaInformation;


public class ScanStatusToPoll extends AbstractLinkedResource {
	private final String status;


	public ScanStatusToPoll(final String status, final MetaInformation _meta) {
		super(_meta);
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public ScanStatus getStatusEnum() {
		return ScanStatus.getScanStatus(status);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((get_meta() == null) ? 0 : get_meta().hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
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
		if (!(obj instanceof ScanStatusToPoll)) {
			return false;
		}
		final ScanStatusToPoll other = (ScanStatusToPoll) obj;
		if (get_meta() == null) {
			if (other.get_meta() != null) {
				return false;
			}
		} else if (!get_meta().equals(other.get_meta())) {
			return false;
		}
		if (status == null) {
			if (other.status != null) {
				return false;
			}
		} else if (!status.equals(other.status)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ScanStatusToPoll [status=");
		builder.append(status);
		builder.append(", _meta=");
		builder.append(get_meta());
		builder.append("]");
		return builder.toString();
	}

}
