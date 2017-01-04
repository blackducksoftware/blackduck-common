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
package com.blackducksoftware.integration.hub.buildtool.bdio;

import com.blackducksoftware.bdio.model.ExternalIdentifier;
import com.blackducksoftware.bdio.model.ExternalIdentifierBuilder;
import com.blackducksoftware.integration.hub.buildtool.Gav;

public class BdioIdCreator {
    private final ExternalIdentifierBuilder externalIdentifierBuilder = ExternalIdentifierBuilder.create();

    public ExternalIdentifier createExternalIdentifier(final Gav gav) {
        final String groupId = gav.getGroupId();
        final String artifactId = gav.getArtifactId();
        final String version = gav.getVersion();

        return externalIdentifierBuilder.maven(groupId, artifactId, version).build().get();
    }

    public String createMavenId(final Gav gav) {
        return String.format("mvn:%s/%s/%s", gav.getGroupId(), gav.getArtifactId(), gav.getVersion());
    }

}
