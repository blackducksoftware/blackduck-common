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
package com.blackducksoftware.integration.hub.builder;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

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
	private int maxWaitTimeForBomUpdate;
	private int scanMemory;
	private final Set<String> scanTargetPaths = new HashSet<String>();
	private boolean disableScanTargetPathExistenceCheck;

	public HubScanJobConfigBuilder(final boolean eatExceptionsOnSetters) {
		super(eatExceptionsOnSetters);
	}

	@Override
	public ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> build() {
		final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result = assertValid();
		final ImmutableList<String> immutableScanTargetPaths = new ImmutableList.Builder<String>()
				.addAll(scanTargetPaths).build();

		result.setConstructedObject(new HubScanJobConfig(projectName, version, phase, distribution, workingDirectory,
				scanMemory, shouldGenerateRiskReport, maxWaitTimeForBomUpdate, immutableScanTargetPaths));

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
			result.addResult(HubScanJobFieldEnum.PROJECT, new ValidationResult(ValidationResultEnum.WARN,
					"No Project name or Version were found. Any scans run will not be mapped to a Version."));
			result.addResult(HubScanJobFieldEnum.VERSION, new ValidationResult(ValidationResultEnum.WARN,
					"No Project name or Version were found. Any scans run will not be mapped to a Version."));
		} else {
			validateProject(result);
			validateVersion(result);
		}
	}

	public void validateProject(final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result)
	{
		if (null == projectName) {
			result.addResult(HubScanJobFieldEnum.PROJECT,
					new ValidationResult(ValidationResultEnum.ERROR, "No Project name was found."));
		} else {
			result.addResult(HubScanJobFieldEnum.PROJECT, new ValidationResult(ValidationResultEnum.OK, ""));
		}
	}

	public void validateVersion(final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result)
	{
		if (null == version) {
			result.addResult(HubScanJobFieldEnum.VERSION,
					new ValidationResult(ValidationResultEnum.ERROR, "No Version was found."));
		} else {
			result.addResult(HubScanJobFieldEnum.VERSION, new ValidationResult(ValidationResultEnum.OK, ""));
		}
	}

	public void validateScanMemory(final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result)
	{
		validateScanMemory(result, null);
	}

	private void validateScanMemory(final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result,
			final Integer defaultScanMemory) {
		if (scanMemory < MINIMUM_MEMORY_IN_MEGABYTES && defaultScanMemory != null) {
			scanMemory = defaultScanMemory;
		}
		if (scanMemory < MINIMUM_MEMORY_IN_MEGABYTES) {
			result.addResult(HubScanJobFieldEnum.SCANMEMORY, new ValidationResult(ValidationResultEnum.ERROR,
					"The minimum amount of memory for the scan is " + MINIMUM_MEMORY_IN_MEGABYTES + " MB."));
		}
	}

	public void validateShouldGenerateRiskReport(
			final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result) {
		if ((null == projectName || null == version) && shouldGenerateRiskReport) {
			result.addResult(HubScanJobFieldEnum.GENERATE_RISK_REPORT, new ValidationResult(ValidationResultEnum.ERROR,
					"To generate the Risk Report, you need to provide a Project name or version."));
		}
	}

	public void validateMaxWaitTimeForBomUpdate(
			final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result) {
		validateMaxWaitTimeForBomUpdate(result, null);
	}

	private void validateMaxWaitTimeForBomUpdate(
			final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result, final Integer defaultMaxWaitTime)
	{
		if (maxWaitTimeForBomUpdate <= 0) {
			if (defaultMaxWaitTime != null) {
				maxWaitTimeForBomUpdate = defaultMaxWaitTime;
			} else {
				result.addResult(HubScanJobFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE,
						new ValidationResult(ValidationResultEnum.ERROR,
								"The maximum wait time for the BOM Update must be greater than 0."));
			}
		} else if (maxWaitTimeForBomUpdate < 2) {
			result.addResult(HubScanJobFieldEnum.MAX_WAIT_TIME_FOR_BOM_UPDATE,
					new ValidationResult(ValidationResultEnum.WARN, "This wait time may be too short."));
		}
	}

	/**
	 * If running this validation outside of a Build, make sure you run
	 * disableScanTargetPathExistenceCheck() because the targets may not exist
	 * yet.
	 *
	 */
	public void validateScanTargetPaths(final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result)
	{
		validateScanTargetPaths(result, null);
	}

	private void validateScanTargetPaths(final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> result,
			final String defaultTargetPath) {
		if (scanTargetPaths.isEmpty() && defaultTargetPath != null) {
			scanTargetPaths.add(defaultTargetPath);
		}

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
					}

					String targetCanonicalPath;
					try {
						targetCanonicalPath = target.getCanonicalPath();
						if (!targetCanonicalPath.startsWith(workingDirectory)) {
							result.addResult(HubScanJobFieldEnum.TARGETS,
									new ValidationResult(ValidationResultEnum.ERROR,
									"Can not scan targets outside the working directory."));
						}
					} catch (final IOException e) {
						result.addResult(HubScanJobFieldEnum.TARGETS, new ValidationResult(ValidationResultEnum.ERROR,
								"Could not get the canonical path for Target : " + targetPath));
					}
				}
			}
		}
		scanTargetPaths.clear();
		scanTargetPaths.addAll(targetPaths);

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

	public void setMaxWaitTimeForBomUpdate(final int maxWaitTimeForRiskReport) {
		this.maxWaitTimeForBomUpdate = maxWaitTimeForRiskReport;
	}

	public void setMaxWaitTimeForBomUpdate(final String maxWaitTimeForBomUpdate) {
		setMaxWaitTimeForBomUpdate(stringToInteger(maxWaitTimeForBomUpdate));
	}

	public void setScanMemory(final int scanMemory) {
		this.scanMemory = scanMemory;
	}

	public void setScanMemory(final String scanMemory) {
		setScanMemory(stringToInteger(scanMemory));
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
