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
package com.blackducksoftware.integration.hub.dataservice.model;

import java.util.List;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.blackducksoftware.integration.hub.api.component.version.ComplexLicenseItem;

public class ComplexLicenseModel {

    private final ComplexLicenseItem complexLicenseItem;

    // private final String licenseDisplay;

    private final String textUrl;

    private final List<ComplexLicenseModel> wrappedComplexLicenseModels;

    // TODO: Revisit in 3.6?
    // public ComplexLicenseModel(ComplexLicenseItem complexLicense, List<ComplexLicenseModel>
    // wrappedComplexLicenseModels, String textUrl,
    // String licenseDisplay) {
    public ComplexLicenseModel(ComplexLicenseItem complexLicenseItem, String textUrl, List<ComplexLicenseModel> wrappedComplexLicenseModels) {
        this.complexLicenseItem = complexLicenseItem;
        // this.licenseDisplay = licenseDisplay;
        this.textUrl = textUrl;
        this.wrappedComplexLicenseModels = wrappedComplexLicenseModels;
    }

    public ComplexLicenseItem getComplexLicenseItem() {
        return this.complexLicenseItem;
    }

    public String getTextUrl() {
        return this.textUrl;
    }

    // public String getLicenseDisplay() {
    // return licenseDisplay;
    // }

    @Deprecated
    public List<ComplexLicenseModel> getWrappedComplexLicenseModels() {
        return this.wrappedComplexLicenseModels;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }
}
