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
package com.blackducksoftware.integration.hub.report.api;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

public class LicenseDefinition {

	private final String licenseId;

	private final String discoveredAs;

	private final String name;

	private final String spdxId;

	private final String ownership;

	private final String codeSharing;

	private final String licenseDisplay;

	public LicenseDefinition(final String licenseId,
			final String discoveredAs, final String name, final String spdxId,
			final String ownership, final String codeSharing,
			final String licenseDisplay) {
		this.licenseId = licenseId;
		this.discoveredAs = discoveredAs;
		this.name = name;
		this.spdxId = spdxId;
		this.ownership = ownership;
		this.codeSharing = codeSharing;
		this.licenseDisplay = licenseDisplay;

	}

	public String getLicenseId() {
		return licenseId;
	}

	public UUID getLicenseUUId() {
		if (StringUtils.isBlank(licenseId)) {
			return null;
		}
		try {
			return UUID.fromString(licenseId);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	public String getDiscoveredAs() {
		return discoveredAs;
	}

	public String getName() {
		return name;
	}

	public String getSpdxId() {
		return spdxId;
	}

	public String getOwnership() {
		return ownership;
	}

	public String getCodeSharing() {
		return codeSharing;
	}

	/**
	 * This method is supposed to be called by JSON serializer only
	 */
	 public String getLicenseDisplay() {
		return licenseDisplay;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codeSharing == null) ? 0 : codeSharing.hashCode());
		result = prime * result + ((discoveredAs == null) ? 0 : discoveredAs.hashCode());
		result = prime * result + ((licenseId == null) ? 0 : licenseId.hashCode());
		result = prime * result + ((licenseDisplay == null) ? 0 : licenseDisplay.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((ownership == null) ? 0 : ownership.hashCode());
		result = prime * result + ((spdxId == null) ? 0 : spdxId.hashCode());
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
		if (!(obj instanceof LicenseDefinition)) {
			return false;
		}
		final LicenseDefinition other = (LicenseDefinition) obj;
		if (codeSharing == null) {
			if (other.codeSharing != null) {
				return false;
			}
		} else if (!codeSharing.equals(other.codeSharing)) {
			return false;
		}
		if (discoveredAs == null) {
			if (other.discoveredAs != null) {
				return false;
			}
		} else if (!discoveredAs.equals(other.discoveredAs)) {
			return false;
		}
		if (licenseId == null) {
			if (other.licenseId != null) {
				return false;
			}
		} else if (!licenseId.equals(other.licenseId)) {
			return false;
		}
		if (licenseDisplay == null) {
			if (other.licenseDisplay != null) {
				return false;
			}
		} else if (!licenseDisplay.equals(other.licenseDisplay)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		if (ownership == null) {
			if (other.ownership != null) {
				return false;
			}
		} else if (!ownership.equals(other.ownership)) {
			return false;
		}
		if (spdxId == null) {
			if (other.spdxId != null) {
				return false;
			}
		} else if (!spdxId.equals(other.spdxId)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("LicenseDefinition [licenseId=");
		builder.append(licenseId);
		builder.append(", discoveredAs=");
		builder.append(discoveredAs);
		builder.append(", name=");
		builder.append(name);
		builder.append(", spdxId=");
		builder.append(spdxId);
		builder.append(", ownership=");
		builder.append(ownership);
		builder.append(", codeSharing=");
		builder.append(codeSharing);
		builder.append(", licenseDisplay=");
		builder.append(licenseDisplay);
		builder.append("]");
		return builder.toString();
	}

}
