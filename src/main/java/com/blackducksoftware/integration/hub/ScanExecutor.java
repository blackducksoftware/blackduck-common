package com.blackducksoftware.integration.hub;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;

public abstract class ScanExecutor {

    public static final int DEFAULT_MEMORY = 4096;

    public static enum Result {
        SUCCESS, FAILURE;
    }

    private final String hubUrl;

    private final String hubUsername;

    private final String hubPassword;

    private final List<String> scanTargets;

    private final int buildNumber;

    private int scanMemory;

    private IntLogger logger;

    private boolean hubSupportLogOption;

    private boolean cliSupportStatusOption;

    private boolean cliSupportsMapping;

    private boolean shouldParseStatus;

    private String project;

    private String version;

    private String workingDirectory;

    private String proxyHost;

    private int proxyPort;

    private List<Pattern> noProxyHosts;

    private String proxyUsername;

    private String proxyPassword;

    private boolean verboseRun;

    protected ScanExecutor(String hubUrl, String hubUsername, String hubPassword, List<String> scanTargets, Integer buildNumber) {

        if (StringUtils.isBlank(hubUrl)) {
            throw new IllegalArgumentException("No Hub URL provided.");
        }
        if (StringUtils.isBlank(hubUsername)) {
            throw new IllegalArgumentException("No Hub username provided.");
        }
        if (StringUtils.isBlank(hubPassword)) {
            throw new IllegalArgumentException("No Hub password provided.");
        }
        if (scanTargets == null || scanTargets.isEmpty()) {
            throw new IllegalArgumentException("No scan targets provided.");
        }
        if (buildNumber == null) {
            throw new IllegalArgumentException("No build number provided.");
        }
        this.hubUrl = hubUrl;
        this.hubUsername = hubUsername;
        this.hubPassword = hubPassword;
        this.scanTargets = scanTargets;
        this.buildNumber = buildNumber;
    }

    public IntLogger getLogger() {
        return logger;
    }

    public void setLogger(IntLogger logger) {
        this.logger = logger;
    }

    public List<String> getScanTargets() {
        return scanTargets;
    }

    public Integer getScanMemory() {
        return scanMemory;
    }

    public void setScanMemory(Integer scanMemory) {
        this.scanMemory = scanMemory;
    }

    public boolean doesHubSupportLogOption() {
        return hubSupportLogOption;
    }

    public void setHubSupportLogOption(boolean hubSupportLogOption) {
        this.hubSupportLogOption = hubSupportLogOption;
    }

    public boolean doesCliSupportStatusOption() {
        return cliSupportStatusOption;
    }

    public void setCliSupportStatusOption(boolean cliSupportStatusOption) {
        this.cliSupportStatusOption = cliSupportStatusOption;
    }

    public boolean doesCliSupportsMapping() {
        return cliSupportsMapping;
    }

    public void setCliSupportsMapping(boolean cliSupportsMapping) {
        this.cliSupportsMapping = cliSupportsMapping;
    }

    public boolean shouldParseStatus() {
        return shouldParseStatus;
    }

    public void setShouldParseStatus(boolean shouldParseStatus) {
        this.shouldParseStatus = shouldParseStatus;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getHubUrl() {
        return hubUrl;
    }

    public Integer getBuildNumber() {
        return buildNumber;
    }

    public String getHubUsername() {
        return hubUsername;
    }

    public String getHubPassword() {
        return hubPassword;
    }

    public boolean isVerboseRun() {
        return verboseRun;
    }

    public void setVerboseRun(boolean verboseRun) {
        this.verboseRun = verboseRun;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public List<Pattern> getNoProxyHosts() {
        return noProxyHosts;
    }

    public void setNoProxyHosts(List<Pattern> noProxyHosts) {
        this.noProxyHosts = noProxyHosts;
    }

    public String getProxyUsername() {
        return proxyUsername;
    }

    public void setProxyUsername(String proxyUsername) {
        this.proxyUsername = proxyUsername;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    protected boolean isConfiguredCorrectly(String scanExec, String oneJarPath, String javaExec) {
        if (getLogger() == null) {
            // Need to suppress the sonar rule here.
            // NOPMD SystemPrintln
            System.out.println("Could not find a logger");
            return false;
        }

        if (scanExec == null) {
            getLogger().error("Please provide the Hub scan CLI.");
            return false;
        }
        else {
            File scanExecFile = new File(scanExec);
            if (!scanExecFile.exists()) {

                getLogger().error("The Hub scan CLI provided does not exist.");
                return false;
            }
        }

        if (oneJarPath == null) {
            getLogger().error("Please provide the path for the CLI cache.");
            return false;
        }

        if (javaExec == null) {
            getLogger().error("Please provide the java home directory.");
            return false;
        }
        else {
            File javaExecFile = new File(javaExec);
            if (!javaExecFile.exists()) {
                getLogger().error("The Java home provided does not exist.");
                return false;
            }
        }

        if (scanMemory <= 0) {
            getLogger().error("No memory set for the HUB CLI. Will use the default memory, " + DEFAULT_MEMORY);
            setScanMemory(DEFAULT_MEMORY);
        }

        return true;
    }

    public Result setupAndRunScan(String scanExec, String oneJarPath, String javaExec) throws HubIntegrationException {
        if (isConfiguredCorrectly(scanExec, oneJarPath, javaExec)) {

            try {

                URL url = new URL(getHubUrl());
                List<String> cmd = new ArrayList<String>();

                String javaPath = javaExec;

                getLogger().debug("Using this java installation : " + javaPath);

                cmd.add(javaPath);
                cmd.add("-Done-jar.silent=true");
                cmd.add("-Done-jar.jar.path=" + oneJarPath);

                if (StringUtils.isNotBlank(getProxyHost()) && getProxyPort() != null) {
                    cmd.add("-Dhttp.proxyHost=" + getProxyHost());
                    cmd.add("-Dhttp.proxyPort=" + getProxyPort());
                    // cmd.add("-Dhttps.proxyHost=" + getProxyHost());
                    // cmd.add("-Dhttps.proxyPort=" + getProxyPort());

                    if (getNoProxyHosts() != null) {
                        StringBuilder noProxyHosts = new StringBuilder();

                        for (Pattern pattern : getNoProxyHosts()) {
                            if (noProxyHosts.length() > 0) {
                                noProxyHosts.append("|");
                            }
                            noProxyHosts.append(pattern.toString());
                        }
                        cmd.add("-Dhttp.nonProxyHosts=" + noProxyHosts.toString());
                    }
                    if (StringUtils.isNotBlank(getProxyUsername()) && StringUtils.isNotBlank(getProxyPassword())) {
                        cmd.add("-Dhttp.proxyUser=" + getProxyUsername());
                        cmd.add("-Dhttp.proxyPassword=" + getProxyPassword());
                        // cmd.add("-Dhttps.proxyUser=" + getProxyUsername());
                        // cmd.add("-Dhttps.proxyPassword=" + getProxyPassword());
                    }
                }

                cmd.add("-Xmx" + scanMemory + "m");

                cmd.add("-jar");
                cmd.add(scanExec);
                cmd.add("--scheme");
                cmd.add(url.getProtocol());
                cmd.add("--host");
                cmd.add(url.getHost());
                getLogger().debug("Using this Hub hostname : '" + url.getHost() + "'");
                cmd.add("--username");
                cmd.add(getHubUsername());
                cmd.add("--password");
                cmd.add(getHubPassword());

                if (url.getPort() != -1) {
                    cmd.add("--port");
                    cmd.add(Integer.toString(url.getPort()));
                } else {
                    if (url.getDefaultPort() != -1) {
                        cmd.add("--port");
                        cmd.add(Integer.toString(url.getDefaultPort()));
                    } else {
                        getLogger().warn("Could not find a port to use for the Server.");
                    }

                }

                if (isVerboseRun()) {
                    cmd.add("-v");
                }

                String logDirectoryPath = null;

                if (doesHubSupportLogOption()) {
                    // Only add the logDir option if the Hub supports the logDir option

                    logDirectoryPath = getLogDirectoryPath();
                    // Need to only add this option if version 2.0.1 or later,
                    // this is the pro-active approach to the log problem
                    cmd.add("--logDir");

                    cmd.add(logDirectoryPath);
                }

                if (doesCliSupportsMapping() && StringUtils.isNotBlank(getProject()) && StringUtils.isNotBlank(getVersion())) {
                    // Only add the project and release options if the Hub supports them

                    // Need to only add this option if version 2.2.? or later
                    cmd.add("--project");

                    cmd.add(getProject());

                    cmd.add("--release");

                    cmd.add(getVersion());
                }

                for (String target : scanTargets) {
                    cmd.add(target);
                }

                return executeScan(cmd, logDirectoryPath);

            } catch (MalformedURLException e) {
                throw new HubIntegrationException("The server URL provided was not a valid", e);
            } catch (IOException e) {
                throw new HubIntegrationException(e.getMessage(), e);
            } catch (InterruptedException e) {
                throw new HubIntegrationException(e.getMessage(), e);
            }
        } else {
            return Result.FAILURE;
        }
    }

    /**
     * Should determine the path to the log directory to pass into the CLI. If the directory does not exist it should be
     * created here.
     *
     * @return String
     * @throws IOException
     */
    protected String getLogDirectoryPath() throws IOException {
        File logDirectory = new File(new File(getWorkingDirectory(), "HubScanLogs"), String.valueOf(getBuildNumber()));
        // This log directory should never exist as a new one is created for each Build
        if (!logDirectory.exists() && !logDirectory.mkdirs()) {
            throw new IOException("Could not create the HubScanLogs directory!");
        }

        return logDirectory.getCanonicalPath();
    }

    protected abstract Result executeScan(List<String> cmd, String logDirectoryPath) throws HubIntegrationException, InterruptedException;
}
