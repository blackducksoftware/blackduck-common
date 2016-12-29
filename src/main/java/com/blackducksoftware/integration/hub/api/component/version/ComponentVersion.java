/**
 * Hub Common
 *
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
 */
package com.blackducksoftware.integration.hub.api.component.version;

import java.util.Date;

import com.blackducksoftware.integration.hub.api.item.HubItem;

public class ComponentVersion extends HubItem {
    private final ComplexLicense license;

    private final Date releasedOn;

    private final SourceEnum source;

    private final String versionName;

    public ComponentVersion(final ComplexLicense license, final Date releasedOn, final SourceEnum source, final String versionName) {
        this.license = license;
        this.releasedOn = releasedOn;
        this.source = source;
        this.versionName = versionName;
    }

    public ComplexLicense getLicense() {
        return license;
    }

    public Date getReleasedOn() {
        return releasedOn;
    }

    public SourceEnum getSource() {
        return source;
    }

    public String getVersionName() {
        return versionName;
    }

}
