/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.builder;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.blackducksoftware.integration.builder.AbstractBuilder;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;
import com.blackducksoftware.integration.hub.validator.HubScanConfigValidator;
import com.blackducksoftware.integration.phone.home.enums.ThirdPartyName;
import com.blackducksoftware.integration.validator.AbstractValidator;
import com.google.common.collect.ImmutableList;

public class HubScanConfigBuilder extends AbstractBuilder<HubScanConfig> {

    private String projectName;

    private String version;

    private String phase;

    private String distribution;

    private File workingDirectory;

    private String scanMemory;

    private final Set<String> scanTargetPaths = new HashSet<>();

    private boolean dryRun;

    private File toolsDir;

    private ThirdPartyName thirdPartyName;

    private String thirdPartyVersion;

    private String pluginVersion;

    private boolean disableScanTargetPathExistenceCheck;

    @Override
    public HubScanConfig buildObject() {
        HubScanConfig config = null;
        final ImmutableList<String> immutableScanTargetPaths = new ImmutableList.Builder<String>()
                .addAll(scanTargetPaths).build();

        config = new HubScanConfig(projectName, version, phase, distribution, workingDirectory,
                NumberUtils.toInt(scanMemory), immutableScanTargetPaths, dryRun, toolsDir, thirdPartyName, thirdPartyVersion, pluginVersion);

        return config;
    }

    @Override
    public AbstractValidator createValidator() {
        final HubScanConfigValidator validator = new HubScanConfigValidator();
        validator.setDryRun(dryRun);
        validator.setProjectName(projectName);
        validator.setVersion(pluginVersion);
        validator.setScanMemory(scanMemory);
        validator.setWorkingDirectory(workingDirectory);
        if (disableScanTargetPathExistenceCheck) {
            validator.disableScanTargetPathExistenceCheck();
        }
        return validator;
    }

    public void setToolsDir(File toolsDir) {
        this.toolsDir = toolsDir;
    }

    public void setThirdPartyName(ThirdPartyName thirdPartyName) {
        this.thirdPartyName = thirdPartyName;
    }

    public void setThirdPartyVersion(String thirdPartyVersion) {
        this.thirdPartyVersion = thirdPartyVersion;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
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

    public void setScanMemory(final int scanMemory) {
        setScanMemory(String.valueOf(scanMemory));
    }

    public void setScanMemory(final String scanMemory) {
        this.scanMemory = scanMemory;
    }

    public void addScanTargetPath(final String scanTargetPath) {
        scanTargetPaths.add(scanTargetPath);
    }

    public void addAllScanTargetPaths(final List<String> scanTargetPaths) {
        this.scanTargetPaths.addAll(scanTargetPaths);
    }

    public void setDryRun(final boolean dryRun) {
        this.dryRun = dryRun;
    }

    public void setWorkingDirectory(final File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void disableScanTargetPathExistenceCheck() {
        disableScanTargetPathExistenceCheck = true;
    }
}
