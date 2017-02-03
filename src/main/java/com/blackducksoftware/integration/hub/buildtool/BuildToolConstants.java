/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.buildtool;

public class BuildToolConstants {
    public static final String BDIO_FILE_SUFFIX = "_bdio.jsonld";

    public static final String FLAT_FILE_SUFFIX = "_flat.txt";

    public static final String HUB_PROJECT_NAME_FILE_NAME = "blackDuckHubProjectName.txt";

    public static final String HUB_PROJECT_VERSION_NAME_FILE_NAME = "blackDuckHubProjectVersionName.txt";

    public static final String BDIO_FILE_MEDIA_TYPE = "application/ld+json";

    public static final String BUILD_TOOL_STEP = "build-bom";

    public static final String BUILD_TOOL_STEP_CAMEL = "buildBom";

    public static final String BUILD_TOOL_CONFIGURATION_ERROR = "Configuration Error : %s";

    public static final String CREATE_FLAT_DEPENDENCY_LIST_STARTING = "Creating flat dependency list file for: %s starting.";

    public static final String CREATE_FLAT_DEPENDENCY_LIST_ERROR = "Error creating the output file: %s";

    public static final String CREATE_FLAT_DEPENDENCY_LIST_FINISHED = "Creating flat dependency list file for: %s finished.";

    public static final String CREATE_HUB_OUTPUT_STARTING = "Creating output file for: %s starting.";

    public static final String CREATE_HUB_OUTPUT_ERROR = "Error creating the output file: %s";

    public static final String CREATE_HUB_OUTPUT_FINISHED = "Creating output file for: %s finished.";

    public static final String DEPLOY_HUB_OUTPUT_STARTING = "Deploying: %s starting.";

    public static final String DEPLOY_HUB_OUTPUT_ERROR = "Could not deploy the file to the Hub, check the logs for specific issues: %s";

    public static final String DEPLOY_HUB_OUTPUT_FINISHED = "Deploying: %s finished.";

    public static final String CHECK_POLICIES_STARTING = "Checking policies for: %s starting.";

    public static final String CHECK_POLICIES_ERROR = "Could not check Hub policies, check the logs for specific issues: %s";

    public static final String CHECK_POLICIES_FINISHED = "Checking policies for: %s finished.";

    public static final String FAILED_TO_CREATE_REPORT = "Could not create Hub Risk Report, check the logs for specific issues: %s";

    public static final String CREATE_REPORT_STARTING = "Starting to create Hub Risk Report.";

    public static final String CREATE_REPORT_FINISHED = "Finished creating Hub Risk Report.";

    public static final String DEPLOY_HUB_OUTPUT_AND_CHECK_POLICIES_STARTING = "Deploying: %s for checking latest policy status starting.";

    public static final String DEPLOY_HUB_OUTPUT_AND_CHECK_POLICIES_FINISHED = "Deploying: %s for checking latest policy status finished.";

    public static final String UPLOAD_FILE_MESSAGE = "Uploaded the file %s to %s";

    public static final String SCAN_ERROR_MESSAGE = "There was an error waiting for the scans: %s";

}
