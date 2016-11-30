/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
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
        List<File> fileList = findRelativePathFileList();
        return writeFiles(fileList, resourceDir, destinationDir);
    }

    public abstract List<File> findRelativePathFileList();

    private List<File> writeFiles(List<File> fileList, String resourceDir, String destinationDir) throws IOException {
        List<File> writtenList = new LinkedList<>();
        for (File file : fileList) {
            String relativePath = file.getPath();
            File resourceFile = new File(resourceDir, relativePath);
            File destFile = new File(destinationDir, relativePath);
            String resourcePath = resourceFile.getPath();
            System.out.println("Resource path: " + resourcePath);
            if (!copyFileViaClass(resourcePath, destFile, writtenList)) {
                copyFileViaClassLoader(resourcePath, destFile, writtenList);
            }
        }
        return writtenList;
    }

    public boolean copyFileViaClass(String resourcePath, File destFile, List<File> writtenFileList) throws IOException {
        try (InputStream resourceStream = getClassInputStream(resourcePath)) {
            if (resourceStream == null) {
                return false;
            } else {
                copyFile(resourceStream, destFile, writtenFileList);
                return true;
            }
        }
    }

    public boolean copyFileViaClassLoader(String resourcePath, File destFile, List<File> writtenFileList) throws IOException {
        try (InputStream resourceStream = getClassLoaderInputStream(resourcePath)) {
            if (resourceStream == null) {
                return false;
            } else {
                copyFile(resourceStream, destFile, writtenFileList);
                return true;
            }
        }
    }

    public void copyFile(final InputStream resourceStream, final File destFile, List<File> writtenFileList) throws IOException {
        destFile.mkdirs();
        System.out.println("Resource Stream: " + resourceStream);
        System.out.println("destFile: " + destFile);
        Files.copy(resourceStream, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        writtenFileList.add(destFile);
    }

    public InputStream getClassLoaderInputStream(String resourcePath) {
        return this.getClass().getClassLoader().getResourceAsStream(resourcePath);
    }

    public InputStream getClassInputStream(String resourcePath) {
        return this.getClass().getResourceAsStream(resourcePath);
    }
}
