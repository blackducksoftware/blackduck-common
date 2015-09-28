package com.blackducksoftware.integration.hub;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

    private final List<File> scanTargets;

    private final Integer buildNumber;

    private Integer scanMemory;

    private IntLogger logger;

    private String hubVersion;

    private File workingDirectory;

    private Boolean isTest;

    protected ScanExecutor(String hubUrl, String hubUsername, String hubPassword, List<File> scanTargets, Integer buildNumber) {

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

    public List<File> getScanTargets() {
        return scanTargets;
    }

    public Integer getScanMemory() {
        return scanMemory;
    }

    public void setScanMemory(Integer scanMemory) {
        this.scanMemory = scanMemory;
    }

    public String getHubVersion() {
        return hubVersion;
    }

    public void setHubVersion(String hubVersion) {
        this.hubVersion = hubVersion;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getHubUrl() {
        return hubUrl;
    }

    public String getHubUsername() {
        return hubUsername;
    }

    public String getHubPassword() {
        return hubPassword;
    }

    public Boolean getIsTest() {
        return isTest;
    }

    protected Boolean isTest() {
        return isTest;
    }

    protected void setIsTest(Boolean isTest) {
        this.isTest = isTest;
    }

    private boolean isConfiguredCorrectly(File scanExec, File oneJarPath, File javaExec) {
        if (logger == null) {
            System.out.println("Could not find a logger");
            return false;
        }

        if (scanExec == null) {
            logger.error("Please provide the Hub scan CLI.");
            return false;
        }
        else if (!scanExec.exists()) {
            logger.error("The Hub scan CLI provided does not exist.");
            return false;
        }

        if (oneJarPath == null) {
            logger.error("Please provide the path for the CLI cache.");
            return false;
        }

        if (javaExec == null) {
            logger.error("Please provide the java home directory.");
            return false;
        }
        else if (!javaExec.exists()) {
            logger.error("The Java home provided does not exist.");
            return false;
        }

        if (scanMemory == null) {
            logger.error("No memory set for the HUB CLI. Will use the default memory, " + DEFAULT_MEMORY);
            setScanMemory(DEFAULT_MEMORY);
        }

        return true;
    }

    public Result setupAndRunScan(File scanExec, File oneJarPath, File javaExec) throws HubIntegrationException {
        try {
            URL url = new URL(getHubUrl());
            List<String> cmd = new ArrayList<String>();

            String javaPath = javaExec.getCanonicalPath();

            logger.debug("Using this java installation : " + javaPath);

            cmd.add(javaPath);
            cmd.add("-Done-jar.silent=true");
            cmd.add("-Done-jar.jar.path=" + oneJarPath.getCanonicalPath());

            // TODO add proxy configuration for the CLI as soon as the CLI has proxy support
            // Jenkins jenkins = Jenkins.getInstance();
            // if (jenkins != null) {
            // ProxyConfiguration proxy = jenkins.proxy;
            // if (proxy != null && proxy.getNoProxyHostPatterns() != null) {
            // if (!JenkinsHubIntRestService.getMatchingNoProxyHostPatterns(url.getHost(),
            // proxy.getNoProxyHostPatterns()))
            // {
            // if (StringUtils.isNotBlank(proxy.name) && proxy.port != 0) {
            // // System.setProperty("http.proxyHost", proxy.name);
            // // System.setProperty("http.proxyPort", Integer.toString(proxy.port));
            // // cmd.add("-Dhttp.useProxy=true");
            // cmd.add("-Dblackduck.hub.proxy.host=" + proxy.name);
            // cmd.add("-Dblackduck.hub.proxy.port=" + proxy.port);
            // System.setProperty("blackduck.hub.proxy.host", proxy.name);
            // System.setProperty("blackduck.hub.proxy.port", Integer.toString(proxy.port));
            // }
            // }
            // }
            // }

            cmd.add("-Xmx" + scanMemory + "m");

            cmd.add("-jar");
            cmd.add(scanExec.getCanonicalPath());
            cmd.add("--scheme");
            cmd.add(url.getProtocol());
            cmd.add("--host");
            cmd.add(url.getHost());
            logger.debug("Using this Hub hostname : '" + url.getHost() + "'");
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
                    logger.warn("Could not find a port to use for the Server.");
                }

            }

            if (isTest()) {
                // The new dry run option
                cmd.add("--selfTest");
            }
            // cmd.add("-v");
            File logDirectory = null;
            Boolean oldCLi = false;

            if (hubVersion != null && !hubVersion.equals("2.0.0")) {
                logDirectory = new File(new File(getWorkingDirectory(), "HubScanLogs"), String.valueOf(buildNumber));
                // This log directory should never exist as a new one is created for each Build
                logDirectory.mkdirs();
                // Need to only add this option if version 2.0.1 or later,
                // this is the pro-active approach to the log problem
                cmd.add("--logDir");

                cmd.add(logDirectory.getCanonicalPath());
            }

            for (File target : scanTargets) {
                String targetPath = target.getCanonicalPath();
                // targetPath = PostBuildHubScan.correctSeparatorInPath(targetPath, separator);
                cmd.add(targetPath);
            }

            executeScan(cmd);
        } catch (MalformedURLException e) {
            throw new HubIntegrationException("The server URL provided was not a valid", e);
        } catch (IOException e) {
            throw new HubIntegrationException(e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new HubIntegrationException(e.getMessage(), e);
        }
        return Result.SUCCESS;
    }

    protected abstract Result executeScan(List<String> cmd) throws HubIntegrationException, InterruptedException;
}
