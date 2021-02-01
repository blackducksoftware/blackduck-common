/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.util.List;

public class ScanPaths {
    private final String pathToJavaExecutable;
    private final String pathToCacerts;
    private final String pathToOneJar;
    private final String pathToScanExecutable;
    private final boolean managedByLibrary;

    public ScanPaths(final String pathToJavaExecutable, final String pathToCacerts, final String pathToOneJar, final String pathToScanExecutable, final boolean managedByLibrary) {
        this.pathToJavaExecutable = pathToJavaExecutable;
        this.pathToCacerts = pathToCacerts;
        this.pathToOneJar = pathToOneJar;
        this.pathToScanExecutable = pathToScanExecutable;
        this.managedByLibrary = managedByLibrary;
    }

    public void addJavaAndOnePathArguments(final List<String> cmd) {
        cmd.add(getPathToJavaExecutable());
        cmd.add("-Done-jar.silent=true");
        cmd.add("-Done-jar.jar.path=" + getPathToOneJar());
    }

    public void addScanExecutableArguments(final List<String> cmd) {
        cmd.add("-jar");
        cmd.add(getPathToScanExecutable());
    }

    public String getPathToJavaExecutable() {
        return pathToJavaExecutable;
    }

    public String getPathToCacerts() {
        return pathToCacerts;
    }

    public String getPathToOneJar() {
        return pathToOneJar;
    }

    public String getPathToScanExecutable() {
        return pathToScanExecutable;
    }

    public boolean isManagedByLibrary() {
        return managedByLibrary;
    }

}
