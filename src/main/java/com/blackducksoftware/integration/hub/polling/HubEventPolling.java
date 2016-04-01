package com.blackducksoftware.integration.hub.polling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.report.api.HubReportGenerationInfo;
import com.blackducksoftware.integration.hub.report.api.ReportMetaInformationItem;
import com.blackducksoftware.integration.hub.scan.api.ScanHistoryItem;
import com.blackducksoftware.integration.hub.scan.api.ScanLocationItem;
import com.blackducksoftware.integration.hub.scan.status.ScanStatus;
import com.blackducksoftware.integration.hub.scan.status.ScanStatusToPoll;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HubEventPolling {
	private final HubIntRestService service;

	public HubEventPolling(final HubIntRestService service) {
		this.service = service;
	}

	public HubIntRestService getService() {
		return service;
	}

	/**
	 * Check the code locations with the host specified and the paths provided. Check the history for the scan history
	 * that falls between the times provided, if the status of that scan history for all code locations is complete then
	 * the bom is up to date with these scan results. Otherwise we try again after 10 sec, and we keep trying until it
	 * is up to date or until we hit the maximum wait time.
	 * If we find a scan history object that has status cancelled or an error type then we throw an exception.
	 */
	public void assertBomUpToDate(final HubReportGenerationInfo hubReportGenerationInfo) throws InterruptedException, BDRestException, HubIntegrationException,
	URISyntaxException, IOException {
		final long maximumWait = hubReportGenerationInfo.getMaximumWaitTime();
		final DateTime timeBeforeScan = hubReportGenerationInfo.getBeforeScanTime();
		final DateTime timeAfterScan = hubReportGenerationInfo.getAfterScanTime();
		final String hostname = hubReportGenerationInfo.getHostname();
		final List<String> scanTargets = hubReportGenerationInfo.getScanTargets();

		final long startTime = System.currentTimeMillis();
		long elapsedTime = 0;
		while (elapsedTime < maximumWait) {
			// logger.trace("CHECKING CODE LOCATIONS");
			final List<ScanLocationItem> scanLocationsToCheck = getService().getScanLocations(hostname, scanTargets);
			boolean upToDate = true;
			for (final ScanLocationItem currentCodeLocation : scanLocationsToCheck) {
				if (!upToDate) {
					break;
				}
				for (final ScanHistoryItem currentScanHistory : currentCodeLocation.getScanList()) {
					final DateTime scanHistoryCreationTime = currentScanHistory.getCreatedOnTime();
					if (scanHistoryItemWithinOurScanBoundaries(scanHistoryCreationTime, timeBeforeScan, timeAfterScan)) {
						if (ScanStatus.isFinishedStatus(currentScanHistory.getStatus())) {
							if (ScanStatus.isErrorStatus(currentScanHistory.getStatus())) {
								throw new HubIntegrationException("There was a problem with one of the code locations. Error Status : "
										+ currentScanHistory.getStatus().name());
							}
						} else {
							// The code location is still updating or matching, etc.
							upToDate = false;
							break;
						}
					}
				}
			}
			if (upToDate) {
				// The code locations are all finished, so we know the bom has been updated with our scan results
				// So we break out of this loop
				return;
			}
			// wait 10 seconds before checking the status's again
			Thread.sleep(10000);
			elapsedTime = System.currentTimeMillis() - startTime;
		}

		final String formattedTime = String.format("%d minutes", TimeUnit.MILLISECONDS.toMinutes(maximumWait));
		throw new HubIntegrationException("The Bom has not finished updating from the scan within the specified wait time : " + formattedTime);
	}

	private boolean scanHistoryItemWithinOurScanBoundaries(final DateTime scanHistoryCreationTime, final DateTime timeBeforeScan, final DateTime timeAfterScan) {
		return (scanHistoryCreationTime != null && scanHistoryCreationTime.isAfter(timeBeforeScan) && scanHistoryCreationTime.isBefore(timeAfterScan));
	}

	/**
	 * Checks the status's in the scan files and polls their URL's, every 10 seconds,
	 * until they have all have status COMPLETE. We keep trying until we hit the maximum wait time.
	 * If we find a scan history object that has status cancelled or an error type then we throw an exception.
	 */
	public void assertBomUpToDate(final HubReportGenerationInfo hubReportGenerationInfo, final IntLogger logger) throws InterruptedException,
	BDRestException,
	HubIntegrationException,
	URISyntaxException,
	IOException {
		if (StringUtils.isBlank(hubReportGenerationInfo.getScanStatusDirectory())) {
			throw new HubIntegrationException("The scan status directory must be a non empty value.");
		}
		final File statusDirectory = new File(hubReportGenerationInfo.getScanStatusDirectory());
		if (!statusDirectory.exists()) {
			throw new HubIntegrationException("The scan status directory does not exist.");
		}
		if (!statusDirectory.isDirectory()) {
			throw new HubIntegrationException("The scan status directory provided is not a directory.");
		}
		final File[] statusFiles = statusDirectory.listFiles();
		if (statusFiles == null || statusFiles.length == 0) {
			throw new HubIntegrationException("Can not find the scan status files in the directory provided.");
		}
		int expectedNumScans = 0;
		if (hubReportGenerationInfo.getScanTargets() != null && !hubReportGenerationInfo.getScanTargets().isEmpty()) {
			expectedNumScans = hubReportGenerationInfo.getScanTargets().size();
		}
		if (statusFiles.length != expectedNumScans) {
			throw new HubIntegrationException("There were " + expectedNumScans + " scans configured and we found " + statusFiles.length + " status files.");
		}
		logger.info("Checking the directory : " + statusDirectory.getCanonicalPath() + " for the scan status's.");
		final CountDownLatch lock = new CountDownLatch(expectedNumScans);

		final List<ScanStatusChecker> scanStatusList = new ArrayList<ScanStatusChecker>();
		for (final File currentStatusFile : statusFiles) {
			final String fileContent = readFileAsString(currentStatusFile.getCanonicalPath());
			final Gson gson = new GsonBuilder().create();
			final ScanStatusToPoll status = gson.fromJson(fileContent, ScanStatusToPoll.class);
			if (status.get_meta() == null || status.getStatus() == null) {
				throw new HubIntegrationException("The scan status file : " + currentStatusFile.getCanonicalPath()
						+ " does not contain valid scan status json.");
			}
			final ScanStatusChecker checker = new ScanStatusChecker(service, status, lock);
			scanStatusList.add(checker);
		}
		// Now that we have read in all of the scan status's without error lets start polling them.
		final ThreadFactory threadFactory = Executors.defaultThreadFactory();
		final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), threadFactory);
		for (final ScanStatusChecker statusChecker : scanStatusList) {
			pool.submit(statusChecker);
		}
		pool.shutdown();

		logger.debug("Cleaning up the scan status files at : " + statusDirectory.getCanonicalPath());
		// We delete the files in a second loop to ensure we have all the scan status's in memory before we start
		// deleting the files. This way, if there is an exception thrown, the User can go look at the files to see what
		// went wrong.
		for (final File currentStatusFile : statusFiles) {
			currentStatusFile.delete();
		}
		statusDirectory.delete();

		final boolean finished = lock.await(hubReportGenerationInfo.getMaximumWaitTime(), TimeUnit.MILLISECONDS);

		for (final ScanStatusChecker statusChecker : scanStatusList) {
			statusChecker.setRunning(false); // force the thread to stop executing because we have timed out or gotten
			// all responses.
		}
		for (final ScanStatusChecker statusChecker : scanStatusList) {
			if (statusChecker.hasError() == true) {
				throw statusChecker.getError();
			}
		}

		// check if finished is false then the timeout occurred and we didn't finish processing.
		// if you get here then you have finished.
		if (!finished) {
			final String formattedTime = String.format("%d minutes", TimeUnit.MILLISECONDS.toMinutes(hubReportGenerationInfo.getMaximumWaitTime()));
			throw new HubIntegrationException("The Bom has not finished updating from the scan within the specified wait time : " + formattedTime);
		}
	}

	private String readFileAsString(final String file) throws IOException {
		final StringBuilder sb = new StringBuilder();
		final BufferedReader bufReader = new BufferedReader(new FileReader(file));
		try {
			String line;
			while ((line = bufReader.readLine()) != null) {
				sb.append(line);
				sb.append("\n");
			}
		} finally {
			bufReader.close();
		}
		return sb.toString();
	}

	/**
	 * Checks the report URL every 5 seconds until the report has a finished time available, then we know it is done
	 * being generated. Throws HubIntegrationException after 30 minutes if the report has not been generated yet.
	 *
	 */
	public ReportMetaInformationItem isReportFinishedGenerating(final String reportUrl) throws IOException, BDRestException, URISyntaxException,
	InterruptedException,
	HubIntegrationException {
		// maximum wait time of 30 minutes
		final long maximumWait = 1000 * 60 * 30;
		return isReportFinishedGenerating(reportUrl, maximumWait);
	}

	/**
	 * Checks the report URL every 5 seconds until the report has a finished time available, then we know it is done
	 * being generated. Throws HubIntegrationException after the maximum wait if the report has not been generated yet.
	 *
	 */
	public ReportMetaInformationItem isReportFinishedGenerating(final String reportUrl, final long maximumWait) throws IOException, BDRestException,
	URISyntaxException,
	InterruptedException, HubIntegrationException {
		final long startTime = System.currentTimeMillis();
		long elapsedTime = 0;
		String timeFinished = null;
		ReportMetaInformationItem reportInfo = null;

		while (timeFinished == null) {
			reportInfo = getService().getReportLinks(reportUrl);
			timeFinished = reportInfo.getFinishedAt();
			if (timeFinished != null) {
				break;
			}
			if (elapsedTime >= maximumWait) {
				final String formattedTime = String.format("%d minutes", TimeUnit.MILLISECONDS.toMinutes(maximumWait));
				throw new HubIntegrationException("The Report has not finished generating in : " + formattedTime);
			}
			// Retry every 5 seconds
			Thread.sleep(5000);
			elapsedTime = System.currentTimeMillis() - startTime;
		}
		return reportInfo;
	}

}
