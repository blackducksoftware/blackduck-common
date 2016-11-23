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
        ClassLoader classLoader = this.getClass().getClassLoader();
        for (File file : fileList) {
            String relativePath = file.getPath();
            File resourceFile = new File(resourceDir, relativePath);
            File destFile = new File(destinationDir, relativePath);
            String resourcePath = resourceFile.getPath();
            try (InputStream resourceStream = classLoader.getResourceAsStream(resourcePath)) {
                destFile.mkdirs();
                Files.copy(resourceStream, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                writtenList.add(destFile);
            }
        }
        return writtenList;
    }
}
