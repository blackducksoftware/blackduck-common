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
package com.blackducksoftware.integration.hub.buildtool;

import java.net.URL;

public class FilePathGavExtractor {
    public Gav getMavenPathGav(final URL filePath, final URL localMavenRepoPath) {
        if (filePath == null || localMavenRepoPath == null) {
            return null;
        }
        String stringPath = filePath.getFile();
        stringPath = stringPath.replace(localMavenRepoPath.getFile(), "");
        String[] pathArray = stringPath.split("/");
        StringBuilder groupIdBuilder = new StringBuilder();
        for(int i = 0; i<pathArray.length-3; i++){
        	groupIdBuilder.append(pathArray[i]);
        	if(i!= pathArray.length-4){
        		groupIdBuilder.append(".");
        	}
        }
        final String groupId = groupIdBuilder.toString();
        final String artifactId = pathArray[pathArray.length - 3];
        final String version = pathArray[pathArray.length - 2];

        return new Gav(groupId, artifactId, version);

    }

    public Gav getGradlePathGav(final URL filePath) {
        if (filePath == null) {
            return null;
        }
        String[] pathArray = filePath.getFile().split("/");
        
        if (pathArray.length < 5) {
            return null;
        }

        final String groupId = pathArray[pathArray.length - 5];
        final String artifactId = pathArray[pathArray.length - 4];
        final String version = pathArray[pathArray.length - 3];

        return new Gav(groupId, artifactId, version);
    }

}
