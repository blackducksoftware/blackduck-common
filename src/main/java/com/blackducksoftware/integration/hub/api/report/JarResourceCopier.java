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
        List<String> fileList = findRelativePathFileList();
        return writeFiles(fileList, resourceDir, destinationDir);
    }

    public abstract List<String> findRelativePathFileList();

    private List<File> writeFiles(List<String> fileList, String resourceDir, String destinationDir) throws IOException {
        List<File> writtenList = new LinkedList<>();
        for (String relativePath : fileList) {
            String resourceFile = resourceDir + relativePath;
            String destFile = destinationDir + File.separator + relativePath;
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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private void copyFile(final InputStream resourceStream, final String destFile, List<File> writtenFileList) throws IOException {
        File filePath = new File(destFile);
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
