/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
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
