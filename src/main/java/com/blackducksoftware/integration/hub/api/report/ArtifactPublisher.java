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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;

public class ArtifactPublisher {

    private final static String REPORT_RESOURCES_DIR = "riskreport/web";

    private File destinationDirectory;

    public ArtifactPublisher(String destinationDirectory) {
        this(new File(destinationDirectory));
    }

    public ArtifactPublisher(File destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

    public List<File> publish() throws IOException, URISyntaxException {
        List<File> fileList = findFileList();
        return writeFiles(fileList);
    }

    private List<File> findFileList() throws IOException, URISyntaxException {
        List<File> fileList = new LinkedList<>();
        URL rootDirectory = Thread.currentThread().getContextClassLoader().getResource(REPORT_RESOURCES_DIR);
        File riskReportDir = new File(rootDirectory.toURI());
        fileList = listFiles(riskReportDir);
        return fileList;
    }

    private List<File> writeFiles(List<File> fileList) throws IOException {
        List<File> writtenList = new LinkedList<>();
        for (File file : fileList) {
            String destFilePath = file.getCanonicalPath();
            int indexExcludingRootDir = destFilePath.lastIndexOf(REPORT_RESOURCES_DIR) + REPORT_RESOURCES_DIR.length();
            destFilePath = destFilePath.substring(indexExcludingRootDir);
            File destFile = new File(destinationDirectory, destFilePath);
            destFile.mkdirs();
            Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            writtenList.add(destFile);
        }
        return writtenList;
    }

    private List<File> listFiles(final File rootDirectory) {
        List<File> resultList = new LinkedList<>();
        File[] children = rootDirectory.listFiles();

        for (File file : children) {
            if (file.isFile()) {
                resultList.add(file);
            } else {
                resultList.addAll(listFiles(file));
            }
        }

        return resultList;
    }
}
