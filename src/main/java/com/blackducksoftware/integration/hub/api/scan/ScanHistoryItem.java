/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.api.scan;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.scan.status.ScanStatus;

public class ScanHistoryItem {
	private String scannerVersion;

	private String lastModifiedOn;

	private String createdOn;

	private String createdByUserName;

	private ScanStatus status;

	private String scanSourceType;

	private String numDirs;

	private String numNonDirFiles;

	public String getScannerVersion() {
		return scannerVersion;
	}

	public void setScannerVersion(final String scannerVersion) {
		this.scannerVersion = scannerVersion;
	}

	public String getLastModifiedOn() {
		return lastModifiedOn;
	}

	public void setLastModifiedOn(final String lastModifiedOn) {
		this.lastModifiedOn = lastModifiedOn;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(final String createdOn) {
		this.createdOn = createdOn;
	}

	public String getCreatedByUserName() {
		return createdByUserName;
	}

	public void setCreatedByUserName(final String createdByUserName) {
		this.createdByUserName = createdByUserName;
	}

	public ScanStatus getStatus() {
		return status;
	}

	public void setStatus(final ScanStatus status) {
		this.status = status;
	}

	public String getScanSourceType() {
		return scanSourceType;
	}

	public void setScanSourceType(final String scanSourceType) {
		this.scanSourceType = scanSourceType;
	}

	public String getNumDirs() {
		return numDirs;
	}

	public void setNumDirs(final String numDirs) {
		this.numDirs = numDirs;
	}

	public String getNumNonDirFiles() {
		return numNonDirFiles;
	}

	public void setNumNonDirFiles(final String numNonDirFiles) {
		this.numNonDirFiles = numNonDirFiles;
	}

	public DateTime getCreatedOnTime() {
		if (StringUtils.isBlank(createdOn)) {
			return null;
		}
		return new DateTime(createdOn);
	}

	public DateTime getLastModifiedOnTime() {
		if (StringUtils.isBlank(lastModifiedOn)) {
			return null;
		}
		return new DateTime(lastModifiedOn);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((createdByUserName == null) ? 0 : createdByUserName.hashCode());
		result = prime * result + ((createdOn == null) ? 0 : createdOn.hashCode());
		result = prime * result + ((lastModifiedOn == null) ? 0 : lastModifiedOn.hashCode());
		result = prime * result + ((numDirs == null) ? 0 : numDirs.hashCode());
		result = prime * result + ((numNonDirFiles == null) ? 0 : numNonDirFiles.hashCode());
		result = prime * result + ((scanSourceType == null) ? 0 : scanSourceType.hashCode());
		result = prime * result + ((scannerVersion == null) ? 0 : scannerVersion.hashCode());
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
		if (!(obj instanceof ScanHistoryItem)) {
			return false;
		}
		final ScanHistoryItem other = (ScanHistoryItem) obj;
		if (createdByUserName == null) {
			if (other.createdByUserName != null) {
				return false;
			}
		} else if (!createdByUserName.equals(other.createdByUserName)) {
			return false;
		}
		if (createdOn == null) {
			if (other.createdOn != null) {
				return false;
			}
		} else if (!createdOn.equals(other.createdOn)) {
			return false;
		}
		if (lastModifiedOn == null) {
			if (other.lastModifiedOn != null) {
				return false;
			}
		} else if (!lastModifiedOn.equals(other.lastModifiedOn)) {
			return false;
		}
		if (numDirs == null) {
			if (other.numDirs != null) {
				return false;
			}
		} else if (!numDirs.equals(other.numDirs)) {
			return false;
		}
		if (numNonDirFiles == null) {
			if (other.numNonDirFiles != null) {
				return false;
			}
		} else if (!numNonDirFiles.equals(other.numNonDirFiles)) {
			return false;
		}
		if (scanSourceType == null) {
			if (other.scanSourceType != null) {
				return false;
			}
		} else if (!scanSourceType.equals(other.scanSourceType)) {
			return false;
		}
		if (scannerVersion == null) {
			if (other.scannerVersion != null) {
				return false;
			}
		} else if (!scannerVersion.equals(other.scannerVersion)) {
			return false;
		}
		if (status != other.status) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ScanHistoryItem [scannerVersion=");
		builder.append(scannerVersion);
		builder.append(", lastModifiedOn=");
		builder.append(lastModifiedOn);
		builder.append(", createdOn=");
		builder.append(createdOn);
		builder.append(", createdByUserName=");
		builder.append(createdByUserName);
		builder.append(", status=");
		builder.append(status);
		builder.append(", scanSourceType=");
		builder.append(scanSourceType);
		builder.append(", numDirs=");
		builder.append(numDirs);
		builder.append(", numNonDirFiles=");
		builder.append(numNonDirFiles);
		builder.append("]");
		return builder.toString();
	}

}
