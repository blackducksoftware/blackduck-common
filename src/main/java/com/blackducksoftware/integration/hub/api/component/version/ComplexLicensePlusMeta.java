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
package com.blackducksoftware.integration.hub.api.component.version;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

public class ComplexLicensePlusMeta {

    private static final String AND = " AND ";

    private static final String OR = " OR ";

    private static final String UNKNOWN = "UNKNOWN";

    private static final String MAPPING_PENDING = "Mapping Pending";

    private static final String OPEN_PARENTHESIS = "(";

    private static final String CLOSED_PARENTHESIS = ")";

    private final ComplexLicense complexLicense;

    private final String licenseDisplay;

    private final String textUrl;

    private final List<ComplexLicensePlusMeta> subLicensesPlusMeta;

    public ComplexLicensePlusMeta(ComplexLicense complexLicense, String textUrl, List<ComplexLicensePlusMeta> subLicensesPlusMeta) {
        this.complexLicense = complexLicense;
        this.licenseDisplay = this.toLicenseText(this.complexLicense);
        this.textUrl = textUrl;
        this.subLicensesPlusMeta = subLicensesPlusMeta;
    }

    public ComplexLicense getComplexLicense() {
        return this.complexLicense;
    }

    public String getTextUrl() {
        return this.textUrl;
    }

    public List<ComplexLicensePlusMeta> getSubLicensesPlusMeta() {
        return this.subLicensesPlusMeta;
    }

    private String toLicenseText(ComplexLicense complexLicense) {
        if (complexLicense.getLicenses() != null && complexLicense.getLicenses().isEmpty()) {
            return complexLicense.getName();
        } else {
            String operator = complexLicense.getType() == ComplexLicenseType.CONJUNCTIVE ? AND : OR;

            Collection<String> result = new LinkedList<>();
            for (ComplexLicense childLicense : complexLicense.getLicenses()) {
                result.add(this.toLicenseText(childLicense));
            }

            /**
             * AND 'Mapping Pending' => 'UNKNOWN'
             * OR 'Mapping Pending' => discard 'Mapping Pending' at all
             */
            // result.contains("") is needed for Mapping Pending OR Mapping Pending
            if (result.contains(MAPPING_PENDING) || result.contains("")) {
                LinkedList<String> removalCollection = new LinkedList<>();
                removalCollection.add(MAPPING_PENDING);
                removalCollection.add("");
                if (AND.equals(operator) && result.contains(MAPPING_PENDING)) {
                    result.removeAll(removalCollection);
                    result.add(UNKNOWN);
                } else {
                    result.removeAll(removalCollection);
                }
            }
            StringBuilder licenseText = new StringBuilder();
            int pos = 0;
            for (String text : result) {
                pos++;
                licenseText.append(text);
                if (pos != result.size())
                    licenseText.append(operator);
            }
            return result.size() > 1 ? OPEN_PARENTHESIS + licenseText.toString() + CLOSED_PARENTHESIS
                    : licenseText.toString();
        }
    }

    public String getLicenseDisplay() {
        return licenseDisplay;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }
}
