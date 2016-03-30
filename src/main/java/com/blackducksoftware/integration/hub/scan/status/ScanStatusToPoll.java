package com.blackducksoftware.integration.hub.scan.status;


public class ScanStatusToPoll {
	private final String status;

	private final ScanStatusMeta _meta;

	public ScanStatusToPoll(final String status, final ScanStatusMeta _meta) {
		this.status = status;
		this._meta = _meta;
	}

	public String getStatus() {
		return status;
	}

	public ScanStatus getStatusEnum() {
		return ScanStatus.getScanStatus(status);
	}

	public ScanStatusMeta get_meta() {
		return _meta;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_meta == null) ? 0 : _meta.hashCode());
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
		if (_meta == null) {
			if (other._meta != null) {
				return false;
			}
		} else if (!_meta.equals(other._meta)) {
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
		builder.append("ScanStatusCliOutput [status=");
		builder.append(status);
		builder.append(", _meta=");
		builder.append(_meta);
		builder.append("]");
		return builder.toString();
	}

}
