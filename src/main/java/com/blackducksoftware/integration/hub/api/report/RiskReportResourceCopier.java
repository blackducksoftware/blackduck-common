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
import java.util.LinkedList;
import java.util.List;

public class RiskReportResourceCopier extends JarResourceCopier {

    public final static String JSON_TOKEN_TO_REPLACE = "TOKEN_RISK_REPORT_JSON_TOKEN";

    public final static String RESOURCE_DIRECTORY = "riskreport/web";

    private String destinationDirectory;

    public RiskReportResourceCopier(String destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

    public List<File> copy() throws IOException, URISyntaxException {
        return copy(RESOURCE_DIRECTORY, destinationDirectory);
    }

    @Override
    public List<File> findRelativePathFileList() {
        List<File> relativePathList = new LinkedList<>();
        relativePathList.add(new File("css/HubBomReport.css"));
        relativePathList.add(new File("images/Hub_BD_logo.png"));
        relativePathList.add(new File("riskreport.html"));
        relativePathList.addAll(findJavascriptFileList());
        relativePathList.addAll(findFontAwesomeFileList());
        return relativePathList;
    }

    private List<File> findJavascriptFileList() {
        List<File> fileList = new LinkedList<>();
        File parentDir = new File("js");
        fileList.add(new File(parentDir, "HubBomReportFunctions.js"));
        fileList.add(new File(parentDir, "HubRiskReport.js"));
        fileList.add(new File(parentDir, "jquery-3.1.1.min.js"));
        fileList.add(new File(parentDir, "Sortable.js"));
        return fileList;
    }

    private List<File> findFontAwesomeFileList() {
        List<File> fileList = new LinkedList<>();
        File parentDir = new File("font-awesome-4.5.0");
        File cssDir = new File(parentDir, "css");
        File fontsDir = new File(parentDir, "fonts");
        File lessDir = new File(parentDir, "less");
        File scssDir = new File(parentDir, "scss");
        // css
        fileList.add(new File(cssDir, "font-awesome.css"));
        fileList.add(new File(cssDir, "font-awesome.min.css"));
        // fonts
        fileList.add(new File(fontsDir, "fontawesome-webfont.eot"));
        fileList.add(new File(fontsDir, "fontawesome-webfont.svg"));
        fileList.add(new File(fontsDir, "fontawesome-webfont.ttf"));
        fileList.add(new File(fontsDir, "fontawesome-webfont.woff"));
        fileList.add(new File(fontsDir, "fontawesome-webfont.woff2"));
        fileList.add(new File(fontsDir, "FontAwesome.otf"));
        // less
        fileList.add(new File(lessDir, "animated.less"));
        fileList.add(new File(lessDir, "bordered-pulled.less"));
        fileList.add(new File(lessDir, "core.less"));
        fileList.add(new File(lessDir, "fixed-width.less"));
        fileList.add(new File(lessDir, "font-awesome.less"));
        fileList.add(new File(lessDir, "icons.less"));
        fileList.add(new File(lessDir, "larger.less"));
        fileList.add(new File(lessDir, "list.less"));
        fileList.add(new File(lessDir, "mixins.less"));
        fileList.add(new File(lessDir, "path.less"));
        fileList.add(new File(lessDir, "rotated-flipped.less"));
        fileList.add(new File(lessDir, "stacked.less"));
        fileList.add(new File(lessDir, "variables.less"));
        // scss
        fileList.add(new File(scssDir, "_animated.scss"));
        fileList.add(new File(scssDir, "_bordered-pulled.scss"));
        fileList.add(new File(scssDir, "_core.scss"));
        fileList.add(new File(scssDir, "_fixed-width.scss"));
        fileList.add(new File(scssDir, "_icons.scss"));
        fileList.add(new File(scssDir, "_larger.scss"));
        fileList.add(new File(scssDir, "_list.scss"));
        fileList.add(new File(scssDir, "_mixins.scss"));
        fileList.add(new File(scssDir, "_path.scss"));
        fileList.add(new File(scssDir, "_rotated-flipped.scss"));
        fileList.add(new File(scssDir, "_stacked.scss"));
        fileList.add(new File(scssDir, "_variables.scss"));
        fileList.add(new File(scssDir, "font-awesome.scss"));
        return fileList;
    }
}
