/**
 * blackduck-common
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.proxy.ProxyInfo;

public class ScanCommand {
    private final File installDirectory;
    private final File outputDirectory;
    private final boolean dryRun;
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
    private final boolean snippetMatchingOnly;
    private final boolean fullSnippetScan;
    private final Set<String> excludePatterns;
    private final String additionalArguments;
    private final String targetPath;
    private final boolean verbose;
    private final boolean debug;
    private final String projectName;
    private final String versionName;

    public ScanCommand(final File installDirectory, final File outputDirectory, final boolean dryRun, final ProxyInfo proxyInfo, final String scanCliOpts, final int scanMemoryInMegabytes, final String scheme,
            final String host, final String apiToken, final String username, final String password, final int port, final boolean runInsecure, final String name, final boolean snippetMatching, final boolean snippetMatchingOnly,
            final boolean fullSnippetScan, final Set<String> excludePatterns, final String additionalArguments, final String targetPath, final boolean verbose, final boolean debug, final String projectName, final String versionName) {
        this.installDirectory = installDirectory;
        this.outputDirectory = outputDirectory;
        this.dryRun = dryRun;
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
        this.snippetMatchingOnly = snippetMatchingOnly;
        this.fullSnippetScan = fullSnippetScan;
        this.excludePatterns = excludePatterns;
        this.additionalArguments = additionalArguments;
        this.targetPath = targetPath;
        this.verbose = verbose;
        this.debug = debug;
        this.projectName = projectName;
        this.versionName = versionName;
    }

    public List<String> createCommandForProcessBuilder(final IntLogger logger, final ScanPaths scannerPaths, final String specificRunOutputDirectoryPath) throws IllegalArgumentException {
        final List<String> cmd = new ArrayList<>();
        logger.debug("Using this java installation : " + scannerPaths.getPathToJavaExecutable());

        scannerPaths.addJavaAndOnePathArguments(cmd);
        if (proxyInfo.shouldUseProxy()) {
            final ProxyInfo blackDuckProxyInfo = proxyInfo;
            final String proxyHost = blackDuckProxyInfo.getHost().orElse(null);
            final int proxyPort = blackDuckProxyInfo.getPort();
            final String proxyUsername = blackDuckProxyInfo.getUsername().orElse(null);
            final String proxyPassword = blackDuckProxyInfo.getPassword().orElse(null);
            final String proxyNtlmDomain = blackDuckProxyInfo.getNtlmDomain().orElse(null);
            final String proxyNtlmWorkstation = blackDuckProxyInfo.getNtlmWorkstation().orElse(null);
            cmd.add("-Dhttp.proxyHost=" + proxyHost);
            cmd.add("-Dhttp.proxyPort=" + Integer.toString(proxyPort));
            if (StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {
                cmd.add("-Dhttp.proxyUser=" + proxyUsername);
                cmd.add("-Dhttp.proxyPassword=" + proxyPassword);
            } else {
                // CLI will ignore the proxy host and port if there are no credentials
                cmd.add("-Dhttp.proxyUser=user");
                cmd.add("-Dhttp.proxyPassword=password");
            }
            if (StringUtils.isNotBlank(proxyNtlmDomain)) {
                cmd.add("-Dhttp.auth.ntlm.domain=" + proxyNtlmDomain);
            }
            if (StringUtils.isNotBlank(proxyNtlmWorkstation)) {
                cmd.add("-Dblackduck.http.auth.ntlm.workstation=" + proxyNtlmWorkstation);
            }
        }
        if (StringUtils.isNotBlank(scanCliOpts)) {
            for (final String scanOpt : scanCliOpts.split(" ")) {
                if (StringUtils.isNotBlank(scanOpt)) {
                    cmd.add(scanOpt);
                }
            }
        }
        cmd.add("-Xmx" + scanMemoryInMegabytes + "m");
        scannerPaths.addScanExecutableArguments(cmd);

        cmd.add("--no-prompt");

        if (!dryRun) {
            cmd.add("--scheme");
            cmd.add(scheme);
            cmd.add("--host");
            cmd.add(host);
            logger.debug("Using the Black Duck hostname : '" + host + "'");

            if (StringUtils.isEmpty(apiToken)) {
                cmd.add("--username");
                cmd.add(username);
            }

            final int blackDuckPort = port;
            if (blackDuckPort > 0) {
                cmd.add("--port");
                cmd.add(Integer.toString(blackDuckPort));
            } else {
                logger.warn("Could not find a port to use for the Server.");
            }

            if (runInsecure) {
                cmd.add("--insecure");
            }
        }

        if (verbose) {
            cmd.add("-v");
        }
        if (debug) {
            cmd.add("--debug");
        }

        cmd.add("--logDir");
        cmd.add(specificRunOutputDirectoryPath);

        if (dryRun) {
            // The dryRunWriteDir is the same as the log directory path
            // The CLI will create a subdirectory for the json files
            cmd.add("--dryRunWriteDir");
            cmd.add(specificRunOutputDirectoryPath);
        }

        // Only add the statusWriteDir option if Black Duck supports the statusWriteDir option
        // The scanStatusDirectoryPath is the same as the log directory path
        // The CLI will create a subdirectory for the status files
        cmd.add("--statusWriteDir");
        cmd.add(specificRunOutputDirectoryPath);

        if (StringUtils.isNotBlank(projectName) && StringUtils.isNotBlank(versionName)) {
            cmd.add("--project");
            cmd.add(projectName);
            cmd.add("--release");
            cmd.add(versionName);
        }

        if (StringUtils.isNotBlank(name)) {
            cmd.add("--name");
            cmd.add(name);
        }

        if (snippetMatching) {
            cmd.add("--snippet-matching");
        }

        if (excludePatterns != null) {
            for (final String exclusionPattern : excludePatterns) {
                if (StringUtils.isNotBlank(exclusionPattern)) {
                    cmd.add("--exclude");
                    cmd.add(exclusionPattern);
                }
            }
        }
        final String additionalScanArguments = additionalArguments;
        if (StringUtils.isNotBlank(additionalScanArguments)) {
            for (final String additionalArgument : additionalScanArguments.split(" ")) {
                if (StringUtils.isNotBlank(additionalArgument)) {
                    cmd.add(additionalArgument);
                }
            }
        }

        return cmd;
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

    public boolean isSnippetMatchingOnly() {
        return snippetMatchingOnly;
    }

    public boolean isFullSnippetScan() {
        return fullSnippetScan;
    }

    public Set<String> getExcludePatterns() {
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

    public String getProjectName() {
        return projectName;
    }

    public String getVersionName() {
        return versionName;
    }

}
