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

import java.util.List;

import com.blackducksoftware.integration.hub.api.item.HubResponse;

public class ComplexLicense extends HubResponse {
    private final CodeSharingEnum codeSharing;

    private final String license;

    private final List<ComplexLicense> licenses;

    private final String name;

    private final OwnershipEnum ownership;

    private final ComplexLicenseType type;

    public ComplexLicense(final CodeSharingEnum codeSharing, final String license, final List<ComplexLicense> licenses, final String name,
            final OwnershipEnum ownership,
            final ComplexLicenseType type) {
        this.codeSharing = codeSharing;
        this.license = license;
        this.licenses = licenses;
        this.name = name;
        this.ownership = ownership;
        this.type = type;
    }

    public CodeSharingEnum getCodeSharing() {
        return codeSharing;
    }

    public String getLicense() {
        return license;
    }

    public List<ComplexLicense> getLicenses() {
        return licenses;
    }

    public String getName() {
        return name;
    }

    public OwnershipEnum getOwnership() {
        return ownership;
    }

    public ComplexLicenseType getType() {
        return type;
    }

}
