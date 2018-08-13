package com.synopsys.integration.hub.cli.simple;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.exception.EncryptionException;
import com.synopsys.integration.hub.cli.SignatureScanConfig;
import com.synopsys.integration.hub.configuration.HubServerConfig;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.util.IntEnvironmentVariables;

public class SimpleScanDataBuilder {
    private final String installDirectoryPath;
    private final File installDirectory;
    private final File outputDirectory;
    private boolean hubServerConfigOffline;
    private final boolean signatureScanConfigDryRun;
    private boolean hubServerConfigShouldUseProxy;
    private ProxyInfo proxyInfo;
    private final String scanCliOpts;
    private final int scanMemoryInMegabytes;
    private String scheme;
    private String host;
    private String apiToken;
    private String username;
    private String password;
    private int port;
    private boolean runInsecure;
    private final String name;
    private final boolean snippetMatching;
    private final String[] excludePatterns;
    private final String additionalArguments;
    private final String targetPath;
    private final boolean verbose;
    private final boolean debug;
    private final Map<String, String> allEnvironmentVariables;
    private String projectName;
    private String projectVersionName;

    public SimpleScanDataBuilder(final SignatureScanConfig signatureScanConfig, final IntEnvironmentVariables intEnvironmentVariables) {
        signatureScanConfigDryRun = signatureScanConfig.getCommonScanConfig().isDryRun();
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
    }

    public void setHubServerConfig(final HubServerConfig hubServerConfig) throws EncryptionException {
        if (null == hubServerConfig) {
            hubServerConfigOffline = true;
            hubServerConfigShouldUseProxy = false;
            proxyInfo = ProxyInfo.NO_PROXY_INFO;
            scheme = null;
            host = null;
            apiToken = null;
            username = null;
            password = null;
            port = 0;
            runInsecure = false;
        } else {
            hubServerConfigShouldUseProxy = hubServerConfig.shouldUseProxyForHub();
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
    }

    public void setProjectAndVersionNames(final String projectName, final String projectVersionName) {
        if (StringUtils.isNotBlank(projectName) && StringUtils.isNotBlank(projectVersionName)) {
            this.projectName = projectName;
            this.projectVersionName = projectVersionName;
        }
    }

    public SimpleScanData build() throws IllegalArgumentException {
        final boolean dryRun = hubServerConfigOffline || signatureScanConfigDryRun;
        final boolean shouldUseProxy = !dryRun && hubServerConfigShouldUseProxy;

        return new SimpleScanData(installDirectoryPath, installDirectory, outputDirectory, dryRun, shouldUseProxy, proxyInfo, scanCliOpts, scanMemoryInMegabytes, scheme, host, apiToken, username, password, port, runInsecure, name,
                snippetMatching, excludePatterns, additionalArguments, targetPath, verbose, debug, allEnvironmentVariables, projectName, projectVersionName);
    }

}
