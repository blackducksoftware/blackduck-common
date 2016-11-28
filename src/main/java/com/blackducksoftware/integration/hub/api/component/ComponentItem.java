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
package com.blackducksoftware.integration.hub.api.component;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ComponentItem extends HubItem {
    private final String component; // ****URL**** //

    private final String componentName;

    private final String originId;

    private final String version; // ****URL**** //

    private final String versionName;

    public ComponentItem(MetaInformation meta, String component, String componentName, String originId, String version, String versionName) {
        super(meta);
        this.component = component;
        this.componentName = componentName;
        this.originId = originId;
        this.version = version;
        this.versionName = versionName;
    }

    public String getComponent() {
        return component;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getOriginId() {
        return originId;
    }

    public String getVersion() {
        return version;
    }

    public String getVersionName() {
        return versionName;
    }

}
