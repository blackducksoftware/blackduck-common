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
package com.blackducksoftware.integration.hub.api.component;

import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.blackducksoftware.integration.hub.model.response.HubResponse;

public class Component extends HubResponse {
    // ****URL**** //
    private final String component;

    private final String componentName;

    private final String originId;

    // ****URL**** //
    private final String version;

    private final String versionName;

    public Component(String component, String componentName, String originId, String version, String versionName) {
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

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

}
