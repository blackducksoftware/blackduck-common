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

import org.apache.commons.lang3.StringUtils;

public class ComponentQuery {
    private final String id;

    private final String groupId;

    private final String artifactId;

    private final String version;

    public ComponentQuery(final String id, final String groupId, final String artifactId, final String version) {
        this.id = id;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getQuery() {
        final String idSegment = StringUtils.join(new String[] { "id", id }, ':');
        return StringUtils.join(new String[] { idSegment, groupId, artifactId, version }, '|');
    }

}
