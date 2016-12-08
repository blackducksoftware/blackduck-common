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
package com.blackducksoftware.integration.hub.api.report;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;

public abstract class JarResourceCopier {
    public List<File> copy(String resourceDir, String destinationDir) throws IOException, URISyntaxException {
        final List<String> fileList = findRelativePathFileList();
        return writeFiles(fileList, resourceDir, destinationDir);
    }

    public abstract List<String> findRelativePathFileList();

    private List<File> writeFiles(List<String> fileList, String resourceDir, String destinationDir) throws IOException {
        final List<File> writtenList = new LinkedList<>();
        for (final String relativePath : fileList) {
            final String resourceFile = resourceDir + relativePath;
            final String destFile = destinationDir + File.separator + relativePath;
            if (!copyFileViaClass(resourceFile, destFile, writtenList)) {
                copyFileViaClassLoader(resourceFile, destFile, writtenList);
            }
        }
        return writtenList;
    }

    private boolean copyFileViaClass(String resourcePath, String destFile, List<File> writtenFileList) throws IOException {
        try (InputStream resourceStream = getClassInputStream(resourcePath)) {
            if (resourceStream == null) {
                return false;
            } else {
                copyFile(resourceStream, destFile, writtenFileList);
                return true;
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private boolean copyFileViaClassLoader(String resourcePath, String destFile, List<File> writtenFileList) throws IOException {
        try (InputStream resourceStream = getClassLoaderInputStream(resourcePath)) {
            if (resourceStream == null) {
                return false;
            } else {
                copyFile(resourceStream, destFile, writtenFileList);
                return true;
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void copyFile(final InputStream resourceStream, final String destFile, List<File> writtenFileList) throws IOException {
        final File filePath = new File(destFile);
        filePath.getParentFile().mkdirs();
        Files.copy(resourceStream, filePath.toPath(), StandardCopyOption.REPLACE_EXISTING);
        writtenFileList.add(filePath);
    }

    private InputStream getClassLoaderInputStream(String resourcePath) {
        return this.getClass().getClassLoader().getResourceAsStream(resourcePath);
    }

    private InputStream getClassInputStream(String resourcePath) {
        return this.getClass().getResourceAsStream(resourcePath);
    }

}
