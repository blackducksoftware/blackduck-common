/**
 * blackduck-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.synopsys.integration.blackduck.service.model.pdf;

import static java.awt.Color.decode;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import com.synopsys.integration.blackduck.exception.RiskReportException;
import com.synopsys.integration.blackduck.service.model.BomComponent;
import com.synopsys.integration.blackduck.service.model.ReportData;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.pdf.PDFBoxManager;
import com.synopsys.integration.pdf.StringManager;
import com.synopsys.integration.util.IntegrationEscapeUtil;

public class RiskReportPdfWriter {
    private final IntLogger logger;

    private final String HIGH_RISK = "High Risk";
    private final String MED_RISK = "Medium Risk";
    private final String LOW_RISK = "Low Risk";
    private final String NO_RISK = "No Risk";

    private PDFBoxManager pdfManager;

    public RiskReportPdfWriter(final IntLogger logger) {
        this.logger = logger;
    }

    public File createPDFReportFile(final File outputDirectory, final ReportData report) throws RiskReportException {
        final IntegrationEscapeUtil escapeUtil = new IntegrationEscapeUtil();
        final String escapedProjectName = escapeUtil.escapeForUri(report.getProjectName());
        final String escapedProjectVersionName = escapeUtil.escapeForUri(report.getProjectVersion());
        final File pdfFile = new File(outputDirectory, escapedProjectName + "_" + escapedProjectVersionName + "_BlackDuck_RiskReport.pdf");
        if (pdfFile.exists()) {
            pdfFile.delete();
        }
        final PDDocument document = new PDDocument();
        document.getDocumentInformation().setAuthor("Black Duck Software");
        document.getDocumentInformation().setCreator("Integrations");
        document.getDocumentInformation().setSubject("Black Duck Risk Report");

        try (PDFBoxManager pdfManager = new PDFBoxManager(pdfFile, document)) {
            this.pdfManager = pdfManager;
            final PDRectangle pageBox = pdfManager.currentPage.getMediaBox();
            final float pageWidth = pageBox.getWidth();
            final float pageHeight = pageBox.getHeight();

            final PDRectangle headerRectangle = writeHeader(pageWidth, pageHeight);
            final PDRectangle bottomOfProjectInfoRectangle = writeProjectInformation(pageWidth, headerRectangle.getLowerLeftY(), report);
            final PDRectangle bottomOfSummaryTableRectangle = writeSummaryTables(pageWidth, bottomOfProjectInfoRectangle.getLowerLeftY(), report);
            final PDRectangle bottomOfComponentTableRectangle = writeComponentTable(pageWidth, bottomOfSummaryTableRectangle.getLowerLeftY(), report);

            return pdfFile;
        } catch (final IOException | URISyntaxException e) {
            final String errorString = "Couldn't post the report: ";
            logger.trace(errorString + e.getMessage(), e);
            throw new RiskReportException(errorString + e.getMessage(), e);
        }
    }

    private PDRectangle writeHeader(final float pageWidth, final float startingHeight) throws IOException, URISyntaxException {
        final PDRectangle rectangle = pdfManager.drawRectangle(0, startingHeight - 100, pageWidth, 100, Color.BLACK);
        pdfManager.drawImage(pageWidth - 220, rectangle.getLowerLeftY() + 27.5F, 203, 45, "/riskreport/web/images/Black_Duck_BD_logo.png");
        pdfManager.writeText(5, rectangle.getLowerLeftY() + 40F, "Black Duck Risk Report", PDFBoxManager.DEFAULT_FONT_BOLD, 20, Color.WHITE);
        logger.trace("Finished writing the pdf header.");
        return rectangle;
    }

    private PDRectangle writeProjectInformation(final float pageWidth, final float startingHeight, final ReportData reportData) throws IOException {
        final float height = startingHeight - 18;
        PDRectangle rectangle = pdfManager.writeWrappedLink(5, height, 280, reportData.getProjectName(), reportData.getProjectURL(), PDFBoxManager.DEFAULT_FONT, 18);
        final String dash = " - ";
        rectangle = pdfManager.writeText(5 + rectangle.getUpperRightX(), height, dash, PDFBoxManager.DEFAULT_FONT, 18, Color.BLACK);
        rectangle = pdfManager.writeWrappedLink(5 + rectangle.getUpperRightX(), height, 280 - rectangle.getWidth(), reportData.getProjectVersion(), reportData.getProjectVersionURL(), PDFBoxManager.DEFAULT_FONT, 18);

        final String projectAttributesString = "Phase:  " + reportData.getPhase() + "    |    Distribution:  " + reportData.getDistribution();
        rectangle = pdfManager.writeWrappedText(5, rectangle.getLowerLeftY() - 18, 300, projectAttributesString);
        logger.trace("Finished writing the project information.");
        return rectangle;
    }

    private PDRectangle writeSummaryTables(final float pageWidth, final float startingHeight, final ReportData reportData) throws IOException {

        final float center = pageWidth / 2;

        final float height = startingHeight - 20;
        writeSummaryTable(center - 180, height, "Security Risk", reportData.getVulnerabilityRiskHighCount(), reportData.getVulnerabilityRiskMediumCount(), reportData.getVulnerabilityRiskLowCount(),
                reportData.getVulnerabilityRiskNoneCount(), reportData.getTotalComponents());
        writeSummaryTable(center, height, "License Risk", reportData.getLicenseRiskHighCount(), reportData.getLicenseRiskMediumCount(), reportData.getLicenseRiskLowCount(), reportData.getLicenseRiskNoneCount(),
                reportData.getTotalComponents());
        final PDRectangle rectangle = writeSummaryTable(center + 180, height, "Operational Risk", reportData.getOperationalRiskHighCount(), reportData.getOperationalRiskMediumCount(), reportData.getOperationalRiskLowCount(),
                reportData.getOperationalRiskNoneCount(), reportData.getTotalComponents());
        logger.trace("Finished writing the sumary tables.");
        return rectangle;
    }

    private PDRectangle writeSummaryTable(final float centerX, final float y, final String title, final int highCount, final int mediumCount, final int lowCount, final int noneCount, final int totalCount) throws IOException {
        PDRectangle rectangle = pdfManager.writeTextCentered(centerX, y, title, PDFBoxManager.DEFAULT_FONT_BOLD, 14, Color.BLACK);

        rectangle = writeSummaryTableRow(centerX, rectangle.getLowerLeftY() - 14, HIGH_RISK, highCount, totalCount, decode("#b52b24"));
        rectangle = writeSummaryTableRow(centerX, rectangle.getLowerLeftY() - 14, MED_RISK, mediumCount, totalCount, decode("#eca4a0"));
        rectangle = writeSummaryTableRow(centerX, rectangle.getLowerLeftY() - 14, LOW_RISK, lowCount, totalCount, new Color(153, 153, 153));
        return writeSummaryTableRow(centerX, rectangle.getLowerLeftY() - 14, NO_RISK, noneCount, totalCount, new Color(221, 221, 221));
    }

    private PDRectangle writeSummaryTableRow(final float centerX, final float rowY, final String rowTitle, final int count, final float totalCount, final Color barColor) throws IOException {
        final float rowTitleX = centerX - 80;
        final PDRectangle rectangle = pdfManager.writeText(rowTitleX, rowY, rowTitle);

        final String countString = String.valueOf(count);
        pdfManager.writeTextCentered(centerX, rowY, countString);

        final float barX = centerX + 20;
        if (count > 0) {
            pdfManager.drawRectangle(barX, rowY, (count / totalCount) * 60, 10, barColor);
        }
        return rectangle;
    }

    private PDRectangle writeComponentTable(final float pageWidth, final float startingHeight, final ReportData reportData) throws IOException, URISyntaxException {
        // new Color(221, 221, 221)
        final float height = startingHeight - 20;

        final PDRectangle rectangle = pdfManager.writeText(30, height, "BOM Entries " + reportData.getTotalComponents());

        // header row
        PDRectangle rowRectangle = pdfManager.drawRectangle(10, rectangle.getLowerLeftY() - 22, pageWidth - 20, 18, new Color(221, 221, 221));
        final float rowY = rowRectangle.getLowerLeftY() + 5;
        pdfManager.writeText(50, rowY, "Component", PDFBoxManager.DEFAULT_FONT_BOLD, 12, PDFBoxManager.DEFAULT_COLOR);
        pdfManager.writeText(190, rowY, "Version", PDFBoxManager.DEFAULT_FONT_BOLD, 12, PDFBoxManager.DEFAULT_COLOR);
        pdfManager.writeText(310, rowY, "License", PDFBoxManager.DEFAULT_FONT_BOLD, 12, PDFBoxManager.DEFAULT_COLOR);
        pdfManager.writeText(430, rowY, "H", PDFBoxManager.DEFAULT_FONT_BOLD, 12, PDFBoxManager.DEFAULT_COLOR);
        pdfManager.writeText(470, rowY, "M", PDFBoxManager.DEFAULT_FONT_BOLD, 12, PDFBoxManager.DEFAULT_COLOR);
        pdfManager.writeText(510, rowY, "L", PDFBoxManager.DEFAULT_FONT_BOLD, 12, PDFBoxManager.DEFAULT_COLOR);
        pdfManager.writeText(550, rowY, "Opt R", PDFBoxManager.DEFAULT_FONT_BOLD, 12, PDFBoxManager.DEFAULT_COLOR);

        boolean isOdd = false;
        for (final BomComponent component : reportData.getComponents()) {
            if (null != component) {
                rowRectangle = writeComponentRow(pageWidth, rowRectangle.getLowerLeftY(), component, isOdd);
                isOdd = !isOdd;
            }
        }
        logger.trace("Finished writing the component table.");
        return rowRectangle;
    }

    private PDRectangle writeComponentRow(final float pageWidth, final float y, final BomComponent component, final boolean isOdd) throws IOException, URISyntaxException {
        final float componentNameWidth = 125F;
        final float componentVersionWidth = 115F;
        final float componentLicenseWidth = 150F;

        List<String> componentNameTextLines = new ArrayList<>();
        List<String> componentVersionTextLines = new ArrayList<>();
        List<String> componentLicenseTextLines = new ArrayList<>();

        if (StringUtils.isNotBlank(component.getComponentName())) {
            componentNameTextLines = StringManager.wrapToCombinedList(component.getComponentName(), Math.round(componentNameWidth));
        }
        if (StringUtils.isNotBlank(component.getComponentVersion())) {
            componentVersionTextLines = StringManager.wrapToCombinedList(component.getComponentVersion(), Math.round(componentNameWidth));
        }
        if (StringUtils.isNotBlank(component.getLicense())) {
            componentLicenseTextLines = StringManager.wrapToCombinedList(component.getLicense(), Math.round(componentNameWidth));
        }

        float rowHeight = pdfManager.getApproximateWrappedStringHeight(componentNameTextLines.size(), PDFBoxManager.DEFAULT_FONT_SIZE);
        final float componentVersionHeight = pdfManager.getApproximateWrappedStringHeight(componentVersionTextLines.size(), PDFBoxManager.DEFAULT_FONT_SIZE);
        final float componentLicenseHeight = pdfManager.getApproximateWrappedStringHeight(componentLicenseTextLines.size(), PDFBoxManager.DEFAULT_FONT_SIZE);
        if (componentVersionHeight > rowHeight) {
            rowHeight = componentVersionHeight;
        }
        if (componentLicenseHeight > rowHeight) {
            rowHeight = componentLicenseHeight;
        }

        PDRectangle rowRectangle = null;
        Color rowColor = Color.WHITE;
        if (isOdd) {
            rowColor = new Color(221, 221, 221);
            rowRectangle = pdfManager.drawRectangle(10, y - rowHeight, pageWidth - 20, rowHeight, rowColor);
        } else {
            rowRectangle = pdfManager.drawRectangle(10, y - rowHeight, pageWidth - 20, rowHeight, rowColor);
        }

        final float rowUpperY = rowRectangle.getUpperRightY();
        if (StringUtils.isNotBlank(component.getPolicyStatus()) && component.getPolicyStatus().equalsIgnoreCase("IN_VIOLATION")) {
            pdfManager.drawImageCentered(15, rowUpperY, 8, 8, 0, rowHeight, "/riskreport/web/images/cross_through_circle.png");
        }
        String componentURL = "";
        if (StringUtils.isNotBlank(component.getComponentURL())) {
            componentURL = component.getComponentURL();
        }
        String componentVersionURL = "";
        if (StringUtils.isNotBlank(component.getComponentVersionURL())) {
            componentVersionURL = component.getComponentVersionURL();
        }
        pdfManager.writeWrappedVerticalCenteredLink(30F, rowUpperY, componentNameWidth, rowHeight, componentNameTextLines, componentURL, PDFBoxManager.DEFAULT_COLOR);
        pdfManager.writeWrappedCenteredLink(210, rowUpperY, componentVersionWidth, rowHeight, componentVersionTextLines, componentVersionURL, PDFBoxManager.DEFAULT_COLOR);

        final Risk licenseRisk = getLicenseRisk(component, rowColor);

        if (StringUtils.isNotBlank(licenseRisk.riskShortString)) {
            pdfManager.drawRectangleCentered(282, rowUpperY - 1, 12, 12, rowHeight, licenseRisk.riskColor);
            pdfManager.writeTextCentered(282, rowUpperY, rowHeight, licenseRisk.riskShortString);
        }

        pdfManager.writeWrappedVerticalCenteredText(290, rowUpperY, componentLicenseWidth, rowHeight, componentLicenseTextLines);

        pdfManager.writeTextCentered(434, rowUpperY, rowHeight, String.valueOf(component.getSecurityRiskHighCount()));
        pdfManager.writeTextCentered(477, rowUpperY, rowHeight, String.valueOf(component.getSecurityRiskMediumCount()));
        pdfManager.writeTextCentered(520, rowUpperY, rowHeight, String.valueOf(component.getSecurityRiskLowCount()));

        final Risk operationalRisk = getOperationalRisk(component, rowColor);

        pdfManager.drawRectangle(545, rowRectangle.getLowerLeftY(), 60, rowHeight, operationalRisk.riskColor);
        pdfManager.writeTextCentered(575, rowUpperY, rowHeight, operationalRisk.riskShortString, PDFBoxManager.DEFAULT_FONT_BOLD, 12, PDFBoxManager.DEFAULT_COLOR);

        return rowRectangle;
    }

    public Risk getLicenseRisk(final BomComponent component, final Color noColor) {
        final Risk risk = new Risk();
        risk.riskShortString = "";
        risk.riskColor = noColor;
        if (component.getLicenseRiskHighCount() > 0) {
            risk.riskShortString = "H";
            risk.riskColor = decode("#b52b24");
        } else if (component.getLicenseRiskMediumCount() > 0) {
            risk.riskShortString = "M";
            risk.riskColor = decode("#eca4a0");
        } else if (component.getLicenseRiskLowCount() > 0) {
            risk.riskShortString = "L";
            risk.riskColor = new Color(153, 153, 153);
        }
        return risk;
    }

    public Risk getOperationalRisk(final BomComponent component, final Color noColor) {
        final Risk risk = new Risk();
        risk.riskShortString = "-";
        risk.riskColor = noColor;
        if (component.getOperationalRiskHighCount() > 0) {
            risk.riskShortString = "H";
            risk.riskColor = decode("#b52b24");
        } else if (component.getOperationalRiskMediumCount() > 0) {
            risk.riskShortString = "M";
            risk.riskColor = decode("#eca4a0");
        } else if (component.getOperationalRiskLowCount() > 0) {
            risk.riskShortString = "L";
            risk.riskColor = new Color(153, 153, 153);
        }
        return risk;
    }

    private class Risk {
        public String riskShortString;
        public Color riskColor;

    }

}
