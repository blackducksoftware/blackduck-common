package com.blackducksoftware.integration.hub;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;
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

    private int maxWaitTimeForRiskReport;

    private int scanMemory;

    private final List<String> scanTargetPaths = new ArrayList<String>();

    private boolean disableScanTargetPathExistenceCheck;

    public HubScanJobConfig build(final IntLogger logger) throws HubIntegrationException, IOException {
        assertValid(logger);

        final ImmutableList<String> immutableScanTargetPaths = new ImmutableList.Builder<String>().addAll(scanTargetPaths).build();

        return new HubScanJobConfig(projectName, version, phase, distribution, workingDirectory, scanMemory, shouldGenerateRiskReport,
                maxWaitTimeForRiskReport, immutableScanTargetPaths);
    }

    public void assertValid(final IntLogger logger) throws HubIntegrationException, IOException {
        boolean valid = true;

        if (null == projectName && null == version) {
            logger.warn("No Project name or Version were found. Any scans run will not be mapped to a Version.");
        } else if (null == projectName) {
            valid = false;
            logger.error("No Project name was found.");
        } else if (null == version) {
            valid = false;
            logger.error("No Version was found.");
        }

        if (scanMemory < MINIMUM_MEMORY_IN_MEGABYTES) {
            valid = false;
            logger.error("The minimum amount of memory for the scan is " + MINIMUM_MEMORY_IN_MEGABYTES + " MB.");
        }

        if ((null == projectName || null == version) && shouldGenerateRiskReport) {
            valid = false;
            logger.error("To generate the Risk Report, you need to provide a Project name or version.");
        }

        if (shouldGenerateRiskReport && maxWaitTimeForRiskReport <= 0) {
            valid = false;
            logger.error("The maximum wait time for the Risk Report must be greater than 0.");
        }

        if (!validateScanTargetPaths(logger, scanTargetPaths)) {
            valid = false;
        }

        if (!valid) {
            throw new HubIntegrationException("The configuration is not valid - please check the log for the specific issues.");
        }
    }

    public boolean validateScanTargetPaths(final IntLogger logger, final List<String> scanTargetPaths) throws IOException {
        return validateScanTargetPaths(logger, scanTargetPaths, false);
    }

    private boolean validateScanTargetPaths(final IntLogger logger, final List<String> scanTargetPaths, final boolean shouldUseDefault) throws IOException {
        boolean scanTargetPathsValid = true;
        if (scanTargetPaths.isEmpty()) {
            if (shouldUseDefault) {
                scanTargetPathsValid = false;
                logger.error("No scan targets configured.");
            } else {
                scanTargetPaths.add(workingDirectory);
            }
        } else {
            for (final String targetAbsolutePath : scanTargetPaths) {
                if (StringUtils.isBlank(targetAbsolutePath)) {
                    if (shouldUseDefault) {
                        // ??????
                    } else {
                        logger.error("Can not scan null target.");
                        scanTargetPathsValid = false;
                    }
                }

                if (!disableScanTargetPathExistenceCheck) {
                    final File target = new File(targetAbsolutePath);
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

    public void setMaxWaitTimeForRiskReport(final int maxWaitTimeForRiskReport) {
        this.maxWaitTimeForRiskReport = maxWaitTimeForRiskReport;
    }

    public void setMaxWaitTimeForRiskReport(final String maxWaitTimeForRiskReport) {
        final int maxWaitTimeForRiskReportValue = NumberUtils.toInt(maxWaitTimeForRiskReport);
        setMaxWaitTimeForRiskReport(maxWaitTimeForRiskReportValue);
    }

    public void setScanMemory(final int scanMemory) {
        this.scanMemory = scanMemory;
    }

    public void setScanMemory(final String scanMemory) {
        final int scanMemoryValue = NumberUtils.toInt(scanMemory);
        setScanMemory(scanMemoryValue);
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
