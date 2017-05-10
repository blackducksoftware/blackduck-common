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
package com.blackducksoftware.integration.hub.dataservice.license;

import com.blackducksoftware.integration.hub.model.enumeration.ComplexLicenseEnum;
import com.blackducksoftware.integration.hub.model.view.ComplexLicenseView;

public class ComplexLicenseParser {

    private final ComplexLicenseView complexLicense;

    private String licenseString;

    public ComplexLicenseParser(final ComplexLicenseView complexLicense) {
        this.complexLicense = complexLicense;
    }

    public String parse() {
        if (licenseString == null) {
            licenseString = parse(this.complexLicense);
        }
        return licenseString;
    }

    private String parse(final ComplexLicenseView complexLicense) {

        if (complexLicense.licenses != null && complexLicense.licenses.isEmpty()) {
            return complexLicense.name;
        } else {
            final String operator = complexLicense.type == ComplexLicenseEnum.CONJUNCTIVE ? " AND " : " OR ";
            final StringBuilder licenseText = new StringBuilder();
            int i = 1;
            for (final ComplexLicenseView childLicense : complexLicense.licenses) {
                licenseText.append(this.parse(childLicense));
                if (i < complexLicense.licenses.size()) {
                    licenseText.append(operator);
                }
                i++;
            }
            return i > 2 ? "(" + licenseText.toString() + ")" : licenseText.toString();
        }
    }

}
