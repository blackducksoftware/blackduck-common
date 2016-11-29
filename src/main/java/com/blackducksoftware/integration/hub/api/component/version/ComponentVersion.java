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
package com.blackducksoftware.integration.hub.api.component.version;

import java.util.UUID;

import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.util.HubUrlParser;

public class ComponentVersion extends HubItem {
    public static final String COMPONENT_URL_IDENTIFIER = "components";

    public static final String VERSION_URL_IDENTIFIER = "versions";

    private String versionName;

    private String releasedOn;

    public ComponentVersion(final MetaInformation meta) {
        super(meta);
    }

    // License goes here

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(final String versionName) {
        this.versionName = versionName;
    }

    public String getReleasedOn() {
        return releasedOn;
    }

    public DateTime getReleasedOnDate() {
        return getDateTime(releasedOn);
    }

    public void setReleasedOn(final String releasedOn) {
        this.releasedOn = releasedOn;
    }

    @Deprecated
    public UUID getComponentId() throws MissingUUIDException {
        if (getMeta() == null || getMeta().getHref() == null) {
            return null;
        }
        return HubUrlParser.getUUIDFromURLString(COMPONENT_URL_IDENTIFIER, getMeta().getHref());
    }

    @Deprecated
    public UUID getVersionId() throws MissingUUIDException {
        if (getMeta() == null || getMeta().getHref() == null) {
            return null;
        }
        return HubUrlParser.getUUIDFromURLString(VERSION_URL_IDENTIFIER, getMeta().getHref());
    }

}
