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
package com.blackducksoftware.integration.hub.job;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.google.common.collect.ImmutableList;

public class HubScanJobConfigBuilder {
	public static final int DEFAULT_MEMORY_IN_MEGABYTES = 4096;

	public static final int DEFAULT_REPORT_WAIT_TIME_IN_MINUTES = 5;

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

	public HubScanJobConfig build(final IntLogger logger) throws HubIntegrationException, IOException {
		assertValid(logger);

		final ImmutableList<String> immutableScanTargetPaths = new ImmutableList.Builder<String>()
				.addAll(scanTargetPaths).build();

		return new HubScanJobConfig(projectName, version, phase, distribution, workingDirectory, scanMemory,
				shouldGenerateRiskReport, maxWaitTimeForBomUpdate, immutableScanTargetPaths);
	}

	public void assertValid(final IntLogger logger) throws HubIntegrationException, IOException {
		boolean valid = true;

		if (!validateProjectAndVersion(logger)) {
			valid = false;
		}

		if (!validateScanMemory(logger, DEFAULT_MEMORY_IN_MEGABYTES)) {
			valid = false;
		}

		if (!validateShouldGenerateRiskReport(logger)) {
			valid = false;
		}

		if (!validateMaxWaitTimeForBomUpdate(logger, DEFAULT_REPORT_WAIT_TIME_IN_MINUTES)) {
			valid = false;
		}

		if (!validateScanTargetPaths(logger, workingDirectory)) {
			valid = false;
		}

		if (!valid) {
			throw new HubIntegrationException(
					"The configuration is not valid - please check the log for the specific issues.");
		}
	}

	public boolean validateProjectAndVersion(final IntLogger logger) throws IOException {
		boolean valid = true;

		if (null == projectName && null == version) {
			logger.warn("No Project name or Version were found. Any scans run will not be mapped to a Version.");
		} else if (!validateProject(logger)) {
			valid = false;
		} else if (!validateVersion(logger)) {
			valid = false;
		}

		return valid;
	}

	public boolean validateProject(final IntLogger logger) throws IOException {
		boolean valid = true;

		if (null == projectName) {
			valid = false;
			logger.error("No Project name was found.");
		}

		return valid;
	}

	public boolean validateVersion(final IntLogger logger) throws IOException {
		boolean valid = true;

		if (null == version) {
			valid = false;
			logger.error("No Version was found.");
		}

		return valid;
	}

	public boolean validateScanMemory(final IntLogger logger) throws IOException {
		return validateScanMemory(logger, null);
	}

	private boolean validateScanMemory(final IntLogger logger, final Integer defaultScanMemory) throws IOException {
		boolean scanMemoryValid = true;
		if (scanMemory < MINIMUM_MEMORY_IN_MEGABYTES && defaultScanMemory != null) {
			scanMemory = defaultScanMemory;
		}
		if (scanMemory < MINIMUM_MEMORY_IN_MEGABYTES) {
			scanMemoryValid = false;
			logger.error("The minimum amount of memory for the scan is " + MINIMUM_MEMORY_IN_MEGABYTES + " MB.");
		}
		return scanMemoryValid;
	}

	public boolean validateShouldGenerateRiskReport(final IntLogger logger) throws IOException {
		boolean shouldGenerateReportValid = true;
		if ((null == projectName || null == version) && shouldGenerateRiskReport) {
			shouldGenerateReportValid = false;
			logger.error("To generate the Risk Report, you need to provide a Project name or version.");
		}
		return shouldGenerateReportValid;
	}

	public boolean validateMaxWaitTimeForBomUpdate(final IntLogger logger) throws IOException {
		return validateMaxWaitTimeForBomUpdate(logger, null);
	}

	private boolean validateMaxWaitTimeForBomUpdate(final IntLogger logger, final Integer defaultMaxWaitTime)
			throws IOException {
		boolean waitTimeValid = true;
		if (maxWaitTimeForBomUpdate <= 0) {
			if (defaultMaxWaitTime != null) {
				maxWaitTimeForBomUpdate = defaultMaxWaitTime;
			} else {
				waitTimeValid = false;
				logger.error("The maximum wait time for the BOM Update must be greater than 0.");
			}
		} else if (maxWaitTimeForBomUpdate < 2) {
			logger.warn("This wait time may be too short.");
		}
		return waitTimeValid;
	}

	/**
	 * If running this validation outside of a Build, make sure you run
	 * disableScanTargetPathExistenceCheck() because the targets may not exist
	 * yet.
	 *
	 */
	public boolean validateScanTargetPaths(final IntLogger logger) throws IOException {
		return validateScanTargetPaths(logger, null);
	}

	private boolean validateScanTargetPaths(final IntLogger logger, final String defaultTargetPath) throws IOException {
		boolean scanTargetPathsValid = true;
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
						logger.error("The scan target '" + target.getAbsolutePath() + "' does not exist.");
						scanTargetPathsValid = false;
					}

					final String targetCanonicalPath = target.getCanonicalPath();
					if (!targetCanonicalPath.startsWith(workingDirectory)) {
						logger.error("Can not scan targets outside the working directory.");
						scanTargetPathsValid = false;
					}
				}
			}
		}
		scanTargetPaths.clear();
		scanTargetPaths.addAll(targetPaths);

		return scanTargetPathsValid;
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

	private Integer stringToInteger(final String integer) {
		final String integerString = StringUtils.trimToNull(integer);
		try {
			return Integer.valueOf(integerString);
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("The String : " + integer + " , is not an Integer.", e);
		}
	}

}
