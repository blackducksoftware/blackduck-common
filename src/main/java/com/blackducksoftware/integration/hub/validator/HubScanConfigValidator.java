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
package com.blackducksoftware.integration.hub.validator;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.scan.HubScanConfigFieldEnum;
import com.blackducksoftware.integration.validator.AbstractValidator;
import com.blackducksoftware.integration.validator.ValidationResult;
import com.blackducksoftware.integration.validator.ValidationResultEnum;
import com.blackducksoftware.integration.validator.ValidationResults;

public class HubScanConfigValidator extends AbstractValidator {

    public static final int DEFAULT_MEMORY_IN_MEGABYTES = 4096;

    public static final int DEFAULT_BOM_UPDATE_WAIT_TIME_IN_MINUTES = 5;

    public static final int MINIMUM_MEMORY_IN_MEGABYTES = 256;

    private String projectName;

    private String version;

    private File workingDirectory;

    private String scanMemory;

    private final Set<String> scanTargetPaths = new HashSet<>();

    private boolean dryRun;

    private boolean disableScanTargetPathExistenceCheck;

    @Override
    public ValidationResults assertValid() {
        final ValidationResults result = new ValidationResults();

        validateProjectAndVersion(result);

        validateScanMemory(result, DEFAULT_MEMORY_IN_MEGABYTES);

        validateScanTargetPaths(result, workingDirectory);

        return result;
    }

    public void validateProjectAndVersion(final ValidationResults result) {
        if (!dryRun && projectName == null && version == null) {
            result.addResult(HubScanConfigFieldEnum.VERSION, new ValidationResult(ValidationResultEnum.WARN,
                    "No Project name or Version were found. Any scans run will not be mapped to a Version."));
        } else {
            validateProject(result);
            validateVersion(result);
        }
    }

    public void validateProject(final ValidationResults result) {
        if (projectName == null) {
            result.addResult(HubScanConfigFieldEnum.PROJECT,
                    new ValidationResult(ValidationResultEnum.ERROR, "No Project name was found."));
        }
    }

    public void validateVersion(final ValidationResults result) {
        if (version == null) {
            result.addResult(HubScanConfigFieldEnum.VERSION,
                    new ValidationResult(ValidationResultEnum.ERROR, "No Version was found."));
        }
    }

    public void validateScanMemory(final ValidationResults result) {
        validateScanMemory(result, null);
    }

    private void validateScanMemory(final ValidationResults result,
            final Integer defaultScanMemory) {
        if (StringUtils.isBlank(scanMemory)) {
            result.addResult(HubScanConfigFieldEnum.SCANMEMORY,
                    new ValidationResult(ValidationResultEnum.ERROR, "No scan memory was specified."));
            return;
        }
        int scanMemoryInt = 0;
        try {
            scanMemoryInt = stringToInteger(scanMemory);
        } catch (final IllegalArgumentException e) {
            result.addResult(HubScanConfigFieldEnum.SCANMEMORY,
                    new ValidationResult(ValidationResultEnum.ERROR, e.getMessage(), e));
            return;
        }
        if (scanMemoryInt < MINIMUM_MEMORY_IN_MEGABYTES) {
            result.addResult(HubScanConfigFieldEnum.SCANMEMORY, new ValidationResult(ValidationResultEnum.ERROR,
                    "The minimum amount of memory for the scan is " + MINIMUM_MEMORY_IN_MEGABYTES + " MB."));
        }
    }

    /**
     * If running this validation outside of a Build, make sure you run
     * disableScanTargetPathExistenceCheck() because the targets may not exist
     * yet.
     *
     */
    public void validateScanTargetPaths(final ValidationResults result) {
        validateScanTargetPaths(result, null);
    }

    private void validateScanTargetPaths(final ValidationResults result,
            final File defaultTargetPath) {
        if (scanTargetPaths.isEmpty() && defaultTargetPath != null) {
            scanTargetPaths.add(defaultTargetPath.getAbsolutePath());
        }

        final Set<String> targetPaths = new HashSet<>();
        for (final String currentTargetPath : scanTargetPaths) {
            String targetPath;
            if (StringUtils.isBlank(currentTargetPath) && defaultTargetPath != null) {
                targetPath = defaultTargetPath.getAbsolutePath();
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
                    if (target == null || !target.exists()) {
                        result.addResult(HubScanConfigFieldEnum.TARGETS, new ValidationResult(ValidationResultEnum.ERROR,
                                "The scan target '" + target.getAbsolutePath() + "' does not exist."));
                    }

                    String targetCanonicalPath;
                    try {
                        targetCanonicalPath = target.getCanonicalPath();
                        if (!targetCanonicalPath.startsWith(workingDirectory.getCanonicalPath())) {
                            result.addResult(HubScanConfigFieldEnum.TARGETS, new ValidationResult(
                                    ValidationResultEnum.ERROR, "Can not scan targets outside the working directory."));
                        }
                    } catch (final IOException e) {
                        result.addResult(HubScanConfigFieldEnum.TARGETS, new ValidationResult(ValidationResultEnum.ERROR,
                                "Could not get the canonical path for Target : " + targetPath));
                    }
                }
            }
        }
        scanTargetPaths.clear();
        scanTargetPaths.addAll(targetPaths);
    }

    public void setProjectName(final String projectName) {
        this.projectName = StringUtils.trimToNull(projectName);
    }

    public void setVersion(final String version) {
        this.version = StringUtils.trimToNull(version);
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
