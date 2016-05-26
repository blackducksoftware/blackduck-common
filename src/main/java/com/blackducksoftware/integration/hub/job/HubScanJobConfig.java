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
package com.blackducksoftware.integration.hub.job;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class HubScanJobConfig {
	private final String projectName;

	private final String version;

	private final String phase;

	private final String distribution;

	private final String workingDirectory;

	private final boolean shouldGenerateRiskReport;

	private final int maxWaitTimeForBomUpdate;

	private final int scanMemory;

	private final ImmutableList<String> scanTargetPaths;

	public HubScanJobConfig(final String projectName, final String version, final String phase, final String distribution, final String workingDirectory, final int scanMemory,
			final boolean shouldGenerateRiskReport, final int maxWaitTimeForBomUpdate,
			final ImmutableList<String> scanTargetPaths) {
		this.projectName = projectName;
		this.version = version;
		this.phase = phase;
		this.distribution = distribution;
		this.workingDirectory = workingDirectory;
		this.shouldGenerateRiskReport = shouldGenerateRiskReport;
		this.maxWaitTimeForBomUpdate = maxWaitTimeForBomUpdate;
		this.scanMemory = scanMemory;
		this.scanTargetPaths = scanTargetPaths;
	}

	public String getProjectName() {
		return projectName;
	}

	public String getVersion() {
		return version;
	}

	public String getPhase() {
		return phase;
	}

	public String getDistribution() {
		return distribution;
	}

	public String getWorkingDirectory() {
		return workingDirectory;
	}

	public boolean isShouldGenerateRiskReport() {
		return shouldGenerateRiskReport;
	}

	public int getMaxWaitTimeForBomUpdate() {
		return maxWaitTimeForBomUpdate;
	}

	public long getMaxWaitTimeForBomUpdateInMilliseconds() {
		return maxWaitTimeForBomUpdate * 60 * 1000;
	}

	public int getScanMemory() {
		return scanMemory;
	}

	public List<String> getScanTargetPaths() {
		return scanTargetPaths;
	}


	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("HubScanJobConfig [projectName=");
		builder.append(projectName);
		builder.append(", version=");
		builder.append(version);
		builder.append(", phase=");
		builder.append(phase);
		builder.append(", distribution=");
		builder.append(distribution);
		builder.append(", workingDirectory=");
		builder.append(workingDirectory);
		builder.append(", shouldGenerateRiskReport=");
		builder.append(shouldGenerateRiskReport);
		builder.append(", maxWaitTimeForBomUpdate=");
		builder.append(maxWaitTimeForBomUpdate);
		builder.append(", scanMemory=");
		builder.append(scanMemory);
		builder.append(", scanTargetPaths=");
		builder.append(scanTargetPaths);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((distribution == null) ? 0 : distribution.hashCode());
		result = prime * result + maxWaitTimeForBomUpdate;
		result = prime * result + ((phase == null) ? 0 : phase.hashCode());
		result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
		result = prime * result + scanMemory;
		result = prime * result + ((scanTargetPaths == null) ? 0 : scanTargetPaths.hashCode());
		result = prime * result + (shouldGenerateRiskReport ? 1231 : 1237);
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		result = prime * result + ((workingDirectory == null) ? 0 : workingDirectory.hashCode());
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
		if (!(obj instanceof HubScanJobConfig)) {
			return false;
		}
		final HubScanJobConfig other = (HubScanJobConfig) obj;
		if (distribution == null) {
			if (other.distribution != null) {
				return false;
			}
		} else if (!distribution.equals(other.distribution)) {
			return false;
		}
		if (maxWaitTimeForBomUpdate != other.maxWaitTimeForBomUpdate) {
			return false;
		}
		if (phase == null) {
			if (other.phase != null) {
				return false;
			}
		} else if (!phase.equals(other.phase)) {
			return false;
		}
		if (projectName == null) {
			if (other.projectName != null) {
				return false;
			}
		} else if (!projectName.equals(other.projectName)) {
			return false;
		}
		if (scanMemory != other.scanMemory) {
			return false;
		}
		if (scanTargetPaths == null) {
			if (other.scanTargetPaths != null) {
				return false;
			}
		} else if (!scanTargetPaths.equals(other.scanTargetPaths)) {
			return false;
		}
		if (shouldGenerateRiskReport != other.shouldGenerateRiskReport) {
			return false;
		}
		if (version == null) {
			if (other.version != null) {
				return false;
			}
		} else if (!version.equals(other.version)) {
			return false;
		}
		if (workingDirectory == null) {
			if (other.workingDirectory != null) {
				return false;
			}
		} else if (!workingDirectory.equals(other.workingDirectory)) {
			return false;
		}
		return true;
	}

}
