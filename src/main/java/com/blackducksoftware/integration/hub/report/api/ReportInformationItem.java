package com.blackducksoftware.integration.hub.report.api;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ReportInformationItem {

	private final String reportFormat;

	private final String locale;

	private final String fileName;

	private final int fileSize;

	private final String createdAt;

	private final String updatedAt;

	private final String finishedAt;

	private final String createdBy;

	private final MetaInformation _meta;

	public ReportInformationItem(final String reportFormat, final String locale, final String fileName, final int fileSize, final String createdAt, final String updatedAt, final String finishedAt,
			final String createdBy, final MetaInformation _meta) {
		this.reportFormat = reportFormat;
		this.locale = locale;
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.finishedAt = finishedAt;
		this.createdBy = createdBy;
		this._meta = _meta;
	}

	public String getReportFormat() {
		return reportFormat;
	}

	public String getLocale() {
		return locale;
	}

	public String getFileName() {
		return fileName;
	}

	public int getFileSize() {
		return fileSize;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public String getFinishedAt() {
		return finishedAt;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public MetaInformation get_meta() {
		return _meta;
	}

	private DateTime stringToDateTime(final String dateString) {
		if (StringUtils.isBlank(dateString)) {
			return null;
		}
		try {
			return new DateTime(dateString);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	public DateTime getTimeCreatedAt() {
		return stringToDateTime(createdAt);
	}

	public DateTime getTimeUpdatedAt() {
		return stringToDateTime(updatedAt);
	}

	public DateTime getTimeFinishedAt() {
		return stringToDateTime(finishedAt);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_meta == null) ? 0 : _meta.hashCode());
		result = prime * result + ((createdAt == null) ? 0 : createdAt.hashCode());
		result = prime * result + ((createdBy == null) ? 0 : createdBy.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		result = prime * result + fileSize;
		result = prime * result + ((finishedAt == null) ? 0 : finishedAt.hashCode());
		result = prime * result + ((locale == null) ? 0 : locale.hashCode());
		result = prime * result + ((reportFormat == null) ? 0 : reportFormat.hashCode());
		result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
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
		if (!(obj instanceof ReportInformationItem)) {
			return false;
		}
		final ReportInformationItem other = (ReportInformationItem) obj;
		if (_meta == null) {
			if (other._meta != null) {
				return false;
			}
		} else if (!_meta.equals(other._meta)) {
			return false;
		}
		if (createdAt == null) {
			if (other.createdAt != null) {
				return false;
			}
		} else if (!createdAt.equals(other.createdAt)) {
			return false;
		}
		if (createdBy == null) {
			if (other.createdBy != null) {
				return false;
			}
		} else if (!createdBy.equals(other.createdBy)) {
			return false;
		}
		if (fileName == null) {
			if (other.fileName != null) {
				return false;
			}
		} else if (!fileName.equals(other.fileName)) {
			return false;
		}
		if (fileSize != other.fileSize) {
			return false;
		}
		if (finishedAt == null) {
			if (other.finishedAt != null) {
				return false;
			}
		} else if (!finishedAt.equals(other.finishedAt)) {
			return false;
		}
		if (locale == null) {
			if (other.locale != null) {
				return false;
			}
		} else if (!locale.equals(other.locale)) {
			return false;
		}
		if (reportFormat == null) {
			if (other.reportFormat != null) {
				return false;
			}
		} else if (!reportFormat.equals(other.reportFormat)) {
			return false;
		}
		if (updatedAt == null) {
			if (other.updatedAt != null) {
				return false;
			}
		} else if (!updatedAt.equals(other.updatedAt)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ReportMetaInformationItem [reportFormat=");
		builder.append(reportFormat);
		builder.append(", locale=");
		builder.append(locale);
		builder.append(", fileName=");
		builder.append(fileName);
		builder.append(", fileSize=");
		builder.append(fileSize);
		builder.append(", createdAt=");
		builder.append(createdAt);
		builder.append(", updatedAt=");
		builder.append(updatedAt);
		builder.append(", finishedAt=");
		builder.append(finishedAt);
		builder.append(", createdBy=");
		builder.append(createdBy);
		builder.append(", _meta=");
		builder.append(_meta);
		builder.append("]");
		return builder.toString();
	}
}
