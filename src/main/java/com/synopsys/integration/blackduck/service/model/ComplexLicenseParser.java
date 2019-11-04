/**
 * blackduck-common
 *
 * Copyright (c) 2019 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.service.model;

import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionComponentLicensesLicenseTypeType;
import com.synopsys.integration.blackduck.api.generated.component.ProjectVersionLicenseLicensesView;

public class ComplexLicenseParser {
    private final ProjectVersionLicenseLicensesView complexLicense;
    private String licenseString;

    public ComplexLicenseParser(final ProjectVersionLicenseLicensesView complexLicense) {
        this.complexLicense = complexLicense;
    }

    public String parse() {
        if (licenseString == null) {
            licenseString = parse(complexLicense);
        }
        return licenseString;
    }

    private String parse(final ProjectVersionLicenseLicensesView complexLicense) {
        if (complexLicense.getLicenses() != null && complexLicense.getLicenses().isEmpty()) {
            return complexLicense.getName();
        } else {
            final String operator = complexLicense.getType() == ProjectVersionComponentLicensesLicenseTypeType.CONJUNCTIVE ? " AND " : " OR ";
            final StringBuilder licenseText = new StringBuilder();
            int i = 1;
            for (final ProjectVersionLicenseLicensesView childLicense : complexLicense.getLicenses()) {
                licenseText.append(parse(childLicense));
                if (i < complexLicense.getLicenses().size()) {
                    licenseText.append(operator);
                }
                i++;
            }
            return i > 2 ? "(" + licenseText.toString() + ")" : licenseText.toString();
        }
    }

}
