package com.blackducksoftware.integration.hub.cli;

import java.io.File;
import java.util.Map;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.rest.proxy.ProxyInfo;
import com.blackducksoftware.integration.util.IntEnvironmentVariables;

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

    public SimpleScanData(final HubServerConfig hubServerConfig, final SignatureScanConfig signatureScanConfig, final IntEnvironmentVariables intEnvironmentVariables, final String projectName, final String versionName)
            throws EncryptionException {
        if (null == hubServerConfig) {
            dryRun = true;
            shouldUseProxy = false;
            proxyInfo = ProxyInfo.NO_PROXY_INFO;
            scheme = null;
            host = null;
            apiToken = null;
            username = null;
            password = null;
            port = 0;
            runInsecure = false;
        } else {
            dryRun = signatureScanConfig.getCommonScanConfig().isDryRun();
            shouldUseProxy = !dryRun && hubServerConfig.shouldUseProxyForHub();
            proxyInfo = hubServerConfig.getProxyInfo();
            scheme = hubServerConfig.getHubUrl().getProtocol();
            host = hubServerConfig.getHubUrl().getHost();
            apiToken = hubServerConfig.getApiToken();
            username = hubServerConfig.getGlobalCredentials().getUsername();
            password = hubServerConfig.getGlobalCredentials().getDecryptedPassword();
            if (hubServerConfig.getHubUrl().getPort() > 0) {
                port = hubServerConfig.getHubUrl().getPort();
            } else if (hubServerConfig.getHubUrl().getDefaultPort() > 0) {
                port = hubServerConfig.getHubUrl().getDefaultPort();
            } else {
                port = 0;
            }
            runInsecure = hubServerConfig.isAlwaysTrustServerCertificate();
        }
        scanCliOpts = intEnvironmentVariables.getValue("SCAN_CLI_OPTS");
        allEnvironmentVariables = intEnvironmentVariables.getVariables();
        installDirectoryPath = signatureScanConfig.getCommonScanConfig().getInstallDirectory().getAbsolutePath();
        installDirectory = signatureScanConfig.getCommonScanConfig().getInstallDirectory();
        outputDirectory = signatureScanConfig.getCommonScanConfig().getOutputDirectory();
        scanMemoryInMegabytes = signatureScanConfig.getCommonScanConfig().getScanMemory();
        name = signatureScanConfig.getCodeLocationAlias();
        snippetMatching = signatureScanConfig.getCommonScanConfig().isSnippetModeEnabled();
        excludePatterns = signatureScanConfig.getExcludePatterns();
        additionalArguments = signatureScanConfig.getCommonScanConfig().getAdditionalScanArguments();
        targetPath = signatureScanConfig.getScanTarget();
        verbose = signatureScanConfig.getCommonScanConfig().isVerbose();
        debug = signatureScanConfig.getCommonScanConfig().isDebug();
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
