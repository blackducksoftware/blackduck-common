package com.synopsys.integration.hub.cli.simple;

import java.io.File;
import java.util.Map;

import com.synopsys.integration.rest.proxy.ProxyInfo;

public class SimpleScanData {
    private final String installDirectoryPath;
    private final File installDirectory;
    private final File outputDirectory;
    private final boolean dryRun;
    private final boolean shouldUseProxy;
    private final ProxyInfo proxyInfo;
    private final String scanCliOpts;
    private final int scanMemoryInMegabytes;
    private final String scheme;
    private final String host;
    private final String apiToken;
    private final String username;
    private final String password;
    private final int port;
    private final boolean runInsecure;
    private final String name;
    private final boolean snippetMatching;
    private final String[] excludePatterns;
    private final String additionalArguments;
    private final String targetPath;
    private final boolean verbose;
    private final boolean debug;
    private final Map<String, String> allEnvironmentVariables;
    private final String projectName;
    private final String versionName;

    public SimpleScanData(final String installDirectoryPath, final File installDirectory, final File outputDirectory, final boolean dryRun, final boolean shouldUseProxy, final ProxyInfo proxyInfo, final String scanCliOpts,
            final int scanMemoryInMegabytes, final String scheme, final String host, final String apiToken, final String username, final String password, final int port, final boolean runInsecure, final String name,
            final boolean snippetMatching, final String[] excludePatterns, final String additionalArguments, final String targetPath, final boolean verbose, final boolean debug, final Map<String, String> allEnvironmentVariables,
            final String projectName, final String versionName) {
        this.installDirectoryPath = installDirectoryPath;
        this.installDirectory = installDirectory;
        this.outputDirectory = outputDirectory;
        this.dryRun = dryRun;
        this.shouldUseProxy = shouldUseProxy;
        this.proxyInfo = proxyInfo;
        this.scanCliOpts = scanCliOpts;
        this.scanMemoryInMegabytes = scanMemoryInMegabytes;
        this.scheme = scheme;
        this.host = host;
        this.apiToken = apiToken;
        this.username = username;
        this.password = password;
        this.port = port;
        this.runInsecure = runInsecure;
        this.name = name;
        this.snippetMatching = snippetMatching;
        this.excludePatterns = excludePatterns;
        this.additionalArguments = additionalArguments;
        this.targetPath = targetPath;
        this.verbose = verbose;
        this.debug = debug;
        this.allEnvironmentVariables = allEnvironmentVariables;
        this.projectName = projectName;
        this.versionName = versionName;
    }

    public String getInstallDirectoryPath() {
        return installDirectoryPath;
    }

    public File getInstallDirectory() {
        return installDirectory;
    }

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public boolean shouldUseProxy() {
        return shouldUseProxy;
    }

    public ProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public String getScanCliOpts() {
        return scanCliOpts;
    }

    public int getScanMemoryInMegabytes() {
        return scanMemoryInMegabytes;
    }

    public String getScheme() {
        return scheme;
    }

    public String getHost() {
        return host;
    }

    public String getApiToken() {
        return apiToken;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public boolean isRunInsecure() {
        return runInsecure;
    }

    public String getName() {
        return name;
    }

    public boolean isSnippetMatching() {
        return snippetMatching;
    }

    public String[] getExcludePatterns() {
        return excludePatterns;
    }

    public String getAdditionalArguments() {
        return additionalArguments;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isDebug() {
        return debug;
    }

    public Map<String, String> getAllEnvironmentVariables() {
        return allEnvironmentVariables;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getVersionName() {
        return versionName;
    }

}
