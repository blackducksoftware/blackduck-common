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
package com.blackducksoftware.integration.hub.builder;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.blackducksoftware.integration.hub.job.HubScanJobConfig;
import com.blackducksoftware.integration.hub.job.HubScanJobFieldEnum;
import com.google.common.collect.ImmutableList;

public class HubScanJobConfigBuilder extends AbstractBuilder<HubScanJobFieldEnum, HubScanJobConfig> {
	public static final int DEFAULT_MEMORY_IN_MEGABYTES = 4096;

	public static final int DEFAULT_BOM_UPDATE_WAIT_TIME_IN_MINUTES = 5;

	public static final int MINIMUM_MEMORY_IN_MEGABYTES = 256;

	private String projectName;
	private String version;
	private String phase;
	private String distribution;
	private String workingDirectory;
	private boolean shouldGenerateRiskReport;
	private String maxWaitTimeForBomUpdate;
	private String scanMemory;
	private final Set<String> scanTargetPaths = new HashSet<String>();
	private boolean disableScanTargetPathExistenceCheck;

	public HubScanJobConfigBuilder(final boolean shouldUseDefaultValues) {
		super(shouldUseDefaultValues);
	}

	@Override
	public ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> build() {
		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = assertValid();
		final ImmutableList<String> immutableScanTargetPaths = new ImmutableList.Builder<String>()
				.addAll(scanTargetPaths).build();

		result.setConstructedObject(new HubScanJobConfig(projectName, version, phase, distribution, workingDirectory,
				NumberUtils.toInt(scanMemory), shouldGenerateRiskReport, NumberUtils.toInt(maxWaitTimeForBomUpdate),
				immutableScanTargetPaths));

		return result;
	}

	@Override
	public ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> assertValid() {
		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = new ValidationResults<HubScanJobFieldEnum, HubScanJobConfig>();

		validateProjectAndVersion(result);

		validateScanMemory(result, DEFAULT_MEMORY_IN_MEGABYTES);

		validateShouldGenerateRiskReport(result);

		validateMaxWaitTimeForBomUpdate(result, DEFAULT_BOM_UPDATE_WAIT_TIME_IN_MINUTES);

		validateScanTargetPaths(result, workingDirectory);

		return result;
	}

	public void validateProjectAndVersion(final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result) {

		if (null == projectName && null == version) {
			result.addResult(HubScanJobFieldEnum.VERSION, new ValidationResult(ValidationResultEnum.WARN,
					"No Project name or Version were found. Any scans run will not be mapped to a Version."));
		} else {
			validateProject(result);
			validateVersion(result);
		}
	}

	public void validateProject(final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result) {
		if (null == projectName) {
			result.addResult(HubScanJobFieldEnum.PROJECT,
					new ValidationResult(ValidationResultEnum.ERROR, "No Project name was found."));
		} else {
			result.addResult(HubScanJobFieldEnum.PROJECT, new ValidationResult(ValidationResultEnum.OK, ""));
		}
	}

	public void validateVersion(final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result) {
		if (null == version) {
			result.addResult(HubScanJobFieldEnum.VERSION,
					new ValidationResult(ValidationResultEnum.ERROR, "No Version was found."));
		} else {
			result.addResult(HubScanJobFieldEnum.VERSION, new ValidationResult(ValidationResultEnum.OK, ""));
		}
	}

	public void validateScanMemory(final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result) {
		validateScanMemory(result, null);
	}

	private void validateScanMemory(final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result,
			final Integer defaultScanMemory) {
		if (shouldUseDefaultValues() && defaultScanMemory != null) {
			final int scanMemoryInt = NumberUtils.toInt(scanMemory);
			if (scanMemoryInt < MINIMUM_MEMORY_IN_MEGABYTES && defaultScanMemory != null) {
				scanMemory = String.valueOf(defaultScanMemory);
			}
			return;
		}
		if (StringUtils.isBlank(scanMemory)) {
			result.addResult(HubScanJobFieldEnum.SCANMEMORY,
					new ValidationResult(ValidationResultEnum.ERROR, "No scan memory was specified."));
			return;
		}
		int scanMemoryInt = 0;
		try {
			scanMemoryInt = stringToInteger(scanMemory);
		} catch (final IllegalArgumentException e) {
			result.addResult(HubScanJobFieldEnum.SCANMEMORY,
					new ValidationResult(ValidationResultEnum.ERROR, e.getMessage(), e));
			return;
		}
		if (scanMemoryInt < MINIMUM_MEMORY_IN_MEGABYTES) {
			result.addResult(HubScanJobFieldEnum.SCANMEMORY, new ValidationResult(ValidationResultEnum.ERROR,
					"The minimum amount of memory for the scan is " + MINIMUM_MEMORY_IN_MEGABYTES + " MB."));
		} else {
			result.addResult(HubScanJobFieldEnum.SCANMEMORY, new ValidationResult(ValidationResultEnum.OK, ""));
		}
	}

	public void validateShouldGenerateRiskReport(
			final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result) {
		if ((null == projectName || null == version) && shouldGenerateRiskReport) {
			result.addResult(HubScanJobFieldEnum.GENERATE_RISK_REPORT, new ValidationResult(ValidationResultEnum.ERROR,
					"To generate the Risk Report, you need to provide a Project name or version."));
		} else {
			result.addResult(HubScanJobFieldEnum.GENERATE_RISK_REPORT,
					new ValidationResult(ValidationResultEnum.OK, ""));
		}
	}

	public void validateMaxWaitTimeForBomUpdate(final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result) {
		validateMaxWaitTimeForBomUpdate(result, null);
	}

	private void validateMaxWaitTimeForBomUpdate(final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result,
			final Integer defaultMaxWaitTime) {
		if (shouldUseDefaultValues() && defaultMaxWaitTime != null) {
			final int maxWaitTime = NumberUtils.toInt(maxWaitTimeForBomUpdate);
			if (maxWaitTime <= 0) {
				maxWaitTimeForBomUpdate = String.valueOf(defaultMaxWaitTime);
			}
			return;
		}
		if (StringUtils.isBlank(maxWaitTimeForBomUpdate)) {
			result.addResult(HubScanJobFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE,
					new ValidationResult(ValidationResultEnum.ERROR, "No maximum wait time for the Bom Update found."));
			return;
		}
		int maxWaitTime = -1;
		try {
			maxWaitTime = stringToInteger(maxWaitTimeForBomUpdate);
		} catch (final IllegalArgumentException e) {
			result.addResult(HubScanJobFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE,
					new ValidationResult(ValidationResultEnum.ERROR, e.getMessage(), e));
			return;
		}
		if (maxWaitTime <= 0) {
			result.addResult(HubScanJobFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE,
					new ValidationResult(ValidationResultEnum.ERROR,
							"The maximum wait time for the BOM Update must be greater than 0."));
		} else if (maxWaitTime < 2) {
			result.addResult(HubScanJobFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE,
					new ValidationResult(ValidationResultEnum.WARN, "This wait time may be too short."));
		} else {
			result.addResult(HubScanJobFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE,
					new ValidationResult(ValidationResultEnum.OK, ""));
		}
	}

	/**
	 * If running this validation outside of a Build, make sure you run
	 * disableScanTargetPathExistenceCheck() because the targets may not exist
	 * yet.
	 *
	 */
	public void validateScanTargetPaths(final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result) {
		validateScanTargetPaths(result, null);
	}

	private void validateScanTargetPaths(final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result,
			final String defaultTargetPath) {
		if (scanTargetPaths.isEmpty() && defaultTargetPath != null) {
			scanTargetPaths.add(defaultTargetPath);
		}

		boolean valid = true;
		final Set<String> targetPaths = new HashSet<String>();
		for (final String currentTargetPath : scanTargetPaths) {
			String targetPath;
			if (StringUtils.isBlank(currentTargetPath) && defaultTargetPath != null) {
				targetPath = defaultTargetPath;
			} else {
				targetPath = currentTargetPath;
			}
			targetPaths.add(targetPath);

			if (!disableScanTargetPathExistenceCheck) {
				if (StringUtils.isNotBlank(targetPath)) {
					// If the targetPath is blank then it will be set to the
					// defaultTargetPath during the build
					// Since we dont know the defaultTargetPath at this point we
					// only validate non blank entries
					final File target = new File(targetPath);
					if (null == target || !target.exists()) {
						result.addResult(HubScanJobFieldEnum.TARGETS, new ValidationResult(ValidationResultEnum.ERROR,
								"The scan target '" + target.getAbsolutePath() + "' does not exist."));
						valid = false;
					}

					String targetCanonicalPath;
					try {
						targetCanonicalPath = target.getCanonicalPath();
						if (!targetCanonicalPath.startsWith(workingDirectory)) {
							result.addResult(HubScanJobFieldEnum.TARGETS, new ValidationResult(
									ValidationResultEnum.ERROR, "Can not scan targets outside the working directory."));
							valid = false;
						}
					} catch (final IOException e) {
						result.addResult(HubScanJobFieldEnum.TARGETS, new ValidationResult(ValidationResultEnum.ERROR,
								"Could not get the canonical path for Target : " + targetPath));
						valid = false;
					}
				}
			}
		}
		scanTargetPaths.clear();
		scanTargetPaths.addAll(targetPaths);

		if (valid) {
			result.addResult(HubScanJobFieldEnum.TARGETS, new ValidationResult(ValidationResultEnum.OK, ""));
		}
	}

	public void setProjectName(final String projectName) {
		this.projectName = StringUtils.trimToNull(projectName);
	}

	public void setVersion(final String version) {
		this.version = StringUtils.trimToNull(version);
	}

	public void setPhase(final String phase) {
		this.phase = phase;
	}

	public void setDistribution(final String distribution) {
		this.distribution = distribution;
	}

	public void setShouldGenerateRiskReport(final boolean shouldGenerateRiskReport) {
		this.shouldGenerateRiskReport = shouldGenerateRiskReport;
	}

	public void setShouldGenerateRiskReport(final String shouldGenerateRiskReport) {
		final boolean shouldGenerateRiskReportValue = Boolean.valueOf(shouldGenerateRiskReport);
		setShouldGenerateRiskReport(shouldGenerateRiskReportValue);
	}

	public void setMaxWaitTimeForBomUpdate(final int maxWaitTimeForBomUpdate) {
		setMaxWaitTimeForBomUpdate(String.valueOf(maxWaitTimeForBomUpdate));
	}

	public void setMaxWaitTimeForBomUpdate(final String maxWaitTimeForBomUpdate) {
		this.maxWaitTimeForBomUpdate = maxWaitTimeForBomUpdate;
	}

	public void setScanMemory(final int scanMemory) {
		setScanMemory(String.valueOf(scanMemory));
	}

	public void setScanMemory(final String scanMemory) {
		this.scanMemory = scanMemory;
	}

	public void addScanTargetPath(final String scanTargetPath) {
		scanTargetPaths.add(scanTargetPath);
	}

	public void addAllScanTargetPaths(final List<String> scanTargetPaths) {
		this.scanTargetPaths.addAll(scanTargetPaths);
	}

	public void setWorkingDirectory(final String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public void disableScanTargetPathExistenceCheck() {
		disableScanTargetPathExistenceCheck = true;
	}

}
