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
package com.blackducksoftware.integration.hub.api.notification;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.google.gson.annotations.SerializedName;

public class PolicyOverrideNotificationContent {
    private String projectName;

    private String projectVersionName;

    private String componentName;

    private String componentVersionName;

    private String firstName;

    private String lastName;

    @SerializedName("projectVersion")
    private String projectVersionLink;

    // If version is specified, componentVersionLink will be populated
    // otherwise it will be null
    @SerializedName("componentVersion")
    private String componentVersionLink;

    // If version is not specified, componentLink will be populated
    // otherwise it will be null
    @SerializedName("component")
    private String componentLink;

    @SerializedName("bomComponentVersionPolicyStatus")
    private String bomComponentVersionPolicyStatusLink;

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersionName() {
        return projectVersionName;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentVersionName() {
        return componentVersionName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProjectVersionLink() {
        return projectVersionLink;
    }

    public String getComponentLink() {
        return componentLink;
    }

    public void setComponentLink(final String componentLink) {
        this.componentLink = componentLink;
    }

    public String getComponentVersionLink() {
        return componentVersionLink;
    }

    public String getBomComponentVersionPolicyStatusLink() {
        return bomComponentVersionPolicyStatusLink;
    }

    public void setProjectName(final String projectName) {
        this.projectName = projectName;
    }

    public void setProjectVersionName(final String projectVersionName) {
        this.projectVersionName = projectVersionName;
    }

    public void setComponentName(final String componentName) {
        this.componentName = componentName;
    }

    public void setComponentVersionName(final String componentVersionName) {
        this.componentVersionName = componentVersionName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public void setProjectVersionLink(final String projectVersionLink) {
        this.projectVersionLink = projectVersionLink;
    }

    public void setComponentVersionLink(final String componentVersionLink) {
        this.componentVersionLink = componentVersionLink;
    }

    public void setBomComponentVersionPolicyStatusLink(final String bomComponentVersionPolicyStatusLink) {
        this.bomComponentVersionPolicyStatusLink = bomComponentVersionPolicyStatusLink;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

}
