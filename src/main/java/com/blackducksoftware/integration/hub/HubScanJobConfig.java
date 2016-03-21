package com.blackducksoftware.integration.hub;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;

public class HubScanJobConfig {
    public static final int DEFAULT_MEMORY_IN_MEGABYTES = 4096;

    public static final int DEFAULT_REPORT_WAIT_TIME_IN_MINUTES = 5;

    public static final int MINIMUM_MEMORY_IN_MEGABYTES = 256;

    private final String projectName;

    private final String version;

    private final String phase;

    private final String distribution;

    private final String workingDirectory;

    private final boolean shouldGenerateRiskReport;

    private final int maxWaitTimeForRiskReport;

    private final int scanMemory;

    private final List<String> scanTargetPaths;

    /**
     * We don't want to instantiate this from anything but the HubScanJobConfigBuilder.
     */
    HubScanJobConfig(String projectName, String version, String phase, String distribution, String workingDirectory, int scanMemory,
            boolean shouldGenerateRiskReport, int maxWaitTimeForRiskReport, List<String> scanTargetPaths) {
        this.projectName = projectName;
        this.version = version;
        this.phase = phase;
        this.distribution = distribution;
        this.workingDirectory = workingDirectory;
        this.shouldGenerateRiskReport = shouldGenerateRiskReport;
        this.maxWaitTimeForRiskReport = maxWaitTimeForRiskReport;
        this.scanMemory = scanMemory;
        this.scanTargetPaths = scanTargetPaths;
    }

    public void assertValid(IntLogger logger) throws HubIntegrationException, IOException {
        boolean valid = true;
        // scan paths
        // name
        // version
        // scanmemory
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

        if (null == projectName || null == version && shouldGenerateRiskReport) {
            valid = false;
            logger.error("To generate the Risk Report, you need to provide a Project name or version.");
        }

        if (shouldGenerateRiskReport && maxWaitTimeForRiskReport <= 0) {
            valid = false;
            logger.error("The maximum wait time for the Risk Report must be greater than 0.");
        }

        if (scanTargetPaths.isEmpty()) {
            valid = false;
            logger.error("No scan targets configured.");
        } else {
            if (!validateScanTargetPaths(logger)) {
                valid = false;
            }
        }

        if (!valid) {
            throw new HubIntegrationException("The configuration is not valid - please check the log for the specific issues.");
        }
    }

    private boolean validateScanTargetPaths(IntLogger logger) throws IOException {
        boolean scanTargetPathsValid = true;
        for (String targetAbsolutePath : scanTargetPaths) {
            if (null == targetAbsolutePath) {
                logger.error("Can not scan null target.");
                scanTargetPathsValid = false;
            }

            File target = new File(targetAbsolutePath);
            if (null == target || !target.exists()) {
                logger.error("The scan target '" + target.getAbsolutePath() + "' does not exist.");
                scanTargetPathsValid = false;
            }

            String targetCanonicalPath = target.getCanonicalPath();
            if (!targetCanonicalPath.startsWith(workingDirectory)) {
                logger.error("Can not scan targets outside the working directory.");
                scanTargetPathsValid = false;
            }
        }

        return scanTargetPathsValid;
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

    public int getMaxWaitTimeForRiskReport() {
        return maxWaitTimeForRiskReport;
    }

    public long getMaxWaitTimeForRiskReportInMilliseconds() {
        return maxWaitTimeForRiskReport * 60 * 1000;
    }

    public int getScanMemory() {
        return scanMemory;
    }

    public List<String> getScanTargetPaths() {
        return scanTargetPaths;
    }

}
