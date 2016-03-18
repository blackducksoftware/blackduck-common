package com.blackducksoftware.integration.hub.report.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.polling.HubEventPolling;
import com.blackducksoftware.integration.hub.response.ReportFormatEnum;
import com.blackducksoftware.integration.hub.response.ReportMetaInformationItem;
import com.blackducksoftware.integration.hub.response.ReportMetaInformationItem.ReportMetaLinkItem;
import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;

public class BomReportGenerator {
    private final HubReportGenerationInfo hubReportGenerationInfo;

    private final HubSupportHelper supportHelper;

    /**
     * Make sure supportHelper.checkHubSupport() has already been run before passing in the supportHelper.
     *
     */
    public BomReportGenerator(HubReportGenerationInfo hubReportGenerationInfo, HubSupportHelper supportHelper) {
        this.hubReportGenerationInfo = hubReportGenerationInfo;
        this.supportHelper = supportHelper;
    }

    public HubBomReportData generateHubReport(IntLogger logger) throws IOException, BDRestException, URISyntaxException, InterruptedException,
            HubIntegrationException {
        logger.debug("Waiting for the bom to be updated with the scan results.");
        HubEventPolling hubEventPolling = new HubEventPolling(hubReportGenerationInfo.getService());

        if (supportHelper.isCliStatusDirOptionSupport()) {
            hubEventPolling.assertBomUpToDate(hubReportGenerationInfo, logger);
        } else {
            hubEventPolling.assertBomUpToDate(hubReportGenerationInfo);
        }

        logger.debug("The bom has been updated, generating the report.");
        String reportUrl = hubReportGenerationInfo.getService().generateHubReport(hubReportGenerationInfo.getVersionId(), ReportFormatEnum.JSON);

        DateTime timeFinished = null;
        ReportMetaInformationItem reportInfo = null;

        while (timeFinished == null) {
            // Wait until the report is done being generated and retry every 5 seconds
            Thread.sleep(5000);
            reportInfo = hubReportGenerationInfo.getService().getReportLinks(reportUrl);

            timeFinished = reportInfo.getTimeFinishedAt();
        }

        List<ReportMetaLinkItem> links = reportInfo.get_meta().getLinks();

        ReportMetaLinkItem contentLink = null;
        for (ReportMetaLinkItem link : links) {
            if (link.getRel().equalsIgnoreCase("content")) {
                contentLink = link;
                break;
            }
        }
        if (contentLink == null) {
            throw new HubIntegrationException("Could not find content link for the report at : " + reportUrl);
        }

        HubBomReportData hubBomReportData = new HubBomReportData();
        VersionReport report = hubReportGenerationInfo.getService().getReportContent(contentLink.getHref());
        hubBomReportData.setReport(report);
        logger.debug("Finished retrieving the report.");

        hubReportGenerationInfo.getService().deleteHubReport(hubReportGenerationInfo.getVersionId(),
                hubReportGenerationInfo.getService().getReportIdFromReportUrl(reportUrl));

        return hubBomReportData;
    }

}
