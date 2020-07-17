/**
 * blackduck-common
 * <p>
 * Copyright (c) 2020 Synopsys, Inc.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.service.model;

import com.synopsys.integration.blackduck.api.generated.enumeration.ProjectVersionLicenseType;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionLicenseLicensesView;

public class ComplexLicenseParser {
    private final ProjectVersionLicenseLicensesView complexLicense;
    private String licenseString;

    public ComplexLicenseParser(ProjectVersionLicenseLicensesView complexLicense) {
        this.complexLicense = complexLicense;
    }

    public String parse() {
        if (licenseString == null) {
            licenseString = parse(complexLicense);
        }
        return licenseString;
    }

    private String parse(ProjectVersionLicenseLicensesView complexLicense) {
        if (complexLicense.getLicenses() != null && complexLicense.getLicenses().isEmpty()) {
            return complexLicense.getName();
        } else {
            String operator = complexLicense.getType() == ProjectVersionLicenseType.CONJUNCTIVE ? " AND " : " OR ";
            StringBuilder licenseText = new StringBuilder();
            int i = 1;
            for (ProjectVersionLicenseLicensesView childLicense : complexLicense.getLicenses()) {
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
