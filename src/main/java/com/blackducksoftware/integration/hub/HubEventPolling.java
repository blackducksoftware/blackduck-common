package com.blackducksoftware.integration.hub;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.response.ReportMetaInformationItem;
import com.blackducksoftware.integration.hub.response.mapping.ScanHistoryItem;
import com.blackducksoftware.integration.hub.response.mapping.ScanLocationItem;
import com.blackducksoftware.integration.hub.response.mapping.ScanStatus;

public class HubEventPolling {

    /**
     * Check the code locations with the host specified and the paths provided. Check the history for the scan history
     * that falls between the times provided, if the status of that scan history for all code locations is complete then
     * the bom is up to date with these scan results. Otherwise we try again after 10 sec, and we keep trying until it
     * is up to date or until we hit the maximum wait time.
     * If we find a scan history object that has status cancelled or an error type then we throw an exception.
     *
     * @param service
     *            HubIntRestService
     * @param timeBeforeScan
     *            DateTime before the Cli was run
     * @param timeAfterScan
     *            DateTime after the Cli was run
     * @param hostname
     *            String hostname where the Cli was run
     * @param scanTargets
     *            List<<String>> the target paths that were scanned
     * @param maximumWait
     *            long, maximum time to wait for the Bom to be updated completely
     * @return True if the bom has been updated with the code locations from this scan
     * @throws InterruptedException
     * @throws BDRestException
     * @throws HubIntegrationException
     * @throws URISyntaxException
     * @throws IOException
     */
    public static boolean isBomUpToDate(HubIntRestService service, DateTime timeBeforeScan, DateTime timeAfterScan, String hostname, List<String>
            scanTargets, long maximumWait) throws InterruptedException, BDRestException, HubIntegrationException, URISyntaxException, IOException {
        long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        while (elapsedTime < maximumWait) {
            // logger.trace("CHECKING CODE LOCATIONS");
            List<ScanLocationItem> scanLocationsToCheck = service.getScanLocations(hostname, scanTargets);
            boolean upToDate = true;
            for (ScanLocationItem currentCodeLocation : scanLocationsToCheck) {
                for (ScanHistoryItem currentScanHistory : currentCodeLocation.getScanList()) {
                    DateTime scanHistoryCreationTime = currentScanHistory.getCreatedOnTime();
                    if (scanHistoryCreationTime != null && scanHistoryCreationTime.isAfter(timeBeforeScan) && scanHistoryCreationTime.isBefore(timeAfterScan)) {
                        // This scan history Item came from the scan we executed
                        if (ScanStatus.isFinishedStatus(currentScanHistory.getStatus())) {
                            if (ScanStatus.isErrorStatus(currentScanHistory.getStatus())) {
                                throw new HubIntegrationException("There was a problem with one of the code locations. Error Status : "
                                        + currentScanHistory.getStatus().name());
                            }
                        } else {
                            // The code location is still updating or matching, etc.
                            upToDate = false;
                        }
                    } else {
                        // This scan history Item did not come from the scan we executed
                        continue;
                    }
                }
            }
            if (upToDate) {
                // The code locations are all finished, so we know the bom has been updated with our scan results
                // So we break out of this loop
                return true;
            }
            // wait 10 seconds before checking the status's again
            Thread.sleep(10000);
            elapsedTime = System.currentTimeMillis() - startTime;
        }
        String formattedTime = String.format("%d minutes", TimeUnit.MILLISECONDS.toMinutes(maximumWait));
        throw new HubIntegrationException("The Bom has not finished updating from the scan within the specified wait time : " + formattedTime);

    }

    /**
     * Checks the report URL every 5 seconds until the report has a finished time available, then we know it is done
     * being generated. Throws HubIntegrationException after 30 minutes if the report has not been generated yet.
     *
     * @param service
     *            HubIntRestService
     * @param reportUrl
     *            String
     * @return
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws HubIntegrationException
     */
    public static boolean isReportFinishedGenerating(HubIntRestService service, String reportUrl) throws IOException, BDRestException, URISyntaxException,
            InterruptedException, HubIntegrationException {
        // maximum wait time of 30 minutes
        final long maximumWait = 1000 * 60 * 30;
        return isReportFinishedGenerating(service, reportUrl, maximumWait);
    }

    /**
     * Checks the report URL every 5 seconds until the report has a finished time available, then we know it is done
     * being generated. Throws HubIntegrationException after the maximum wait if the report has not been generated yet.
     *
     * @param service
     *            HubIntRestService
     * @param reportUrl
     *            String
     * @param maximumWait
     *            long
     * @return
     * @throws IOException
     * @throws BDRestException
     * @throws URISyntaxException
     * @throws InterruptedException
     * @throws HubIntegrationException
     */
    public static boolean isReportFinishedGenerating(HubIntRestService service, String reportUrl, final long maximumWait) throws IOException, BDRestException,
            URISyntaxException,
            InterruptedException, HubIntegrationException {
        final long startTime = System.currentTimeMillis();
        long elapsedTime = 0;
        String timeFinished = null;
        ReportMetaInformationItem reportInfo = null;

        while (timeFinished == null) {
            // Wait until the report is done being generated
            // Retry every 5 seconds
            Thread.sleep(5000);
            elapsedTime = System.currentTimeMillis() - startTime;
            reportInfo = service.getReportLinks(reportUrl);
            timeFinished = reportInfo.getFinishedAt();

            if (elapsedTime >= maximumWait) {
                String formattedTime = String.format("%d minutes", TimeUnit.MILLISECONDS.toMinutes(maximumWait));
                throw new HubIntegrationException("The Report has not finished generating in : " + formattedTime);
            }
        }
        return true;
    }

}
