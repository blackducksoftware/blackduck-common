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
package com.blackducksoftware.integration.hub.dataservices.scan;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.blackducksoftware.integration.hub.api.ScanSummaryRestService;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryItem;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.logging.IntLogger;

public class ScanStatusChecker {
	private static final long FIVE_SECONDS = 5 * 1000;

	private final ScanSummaryRestService scanSummaryRestService;
	private List<ScanSummaryItem> scanSummaryItems;
	private final long timeoutInMilliseconds;
	private final IntLogger logger;

	public ScanStatusChecker(final IntLogger logger, final ScanSummaryRestService scanSummaryRestService,
			final List<ScanSummaryItem> scanSummaryItems, final long timeoutInMilliseconds) {
		this.logger = logger;
		this.scanSummaryRestService = scanSummaryRestService;
		this.scanSummaryItems = scanSummaryItems;
		this.timeoutInMilliseconds = timeoutInMilliseconds;
	}

	public void waitForCompleteScans()
			throws InterruptedException, IOException, BDRestException, URISyntaxException, HubIntegrationException {
		final long startedTime = System.currentTimeMillis();

		scanSummaryItems = getPendingScans(scanSummaryItems);

		while (!done(scanSummaryItems, timeoutInMilliseconds, startedTime)) {
			Thread.sleep(FIVE_SECONDS);
			scanSummaryItems = getPendingScans(scanSummaryItems);
		}
	}

	private List<ScanSummaryItem> getPendingScans(final List<ScanSummaryItem> scanSummaries)
			throws InterruptedException, IOException, BDRestException, URISyntaxException, HubIntegrationException {
		final List<ScanSummaryItem> pendingScans = new ArrayList<>();
		for (final ScanSummaryItem scanSummaryItem : scanSummaries) {
			logger.info("waiting for scan: " + scanSummaryItem);
			final String scanSummaryLink = scanSummaryItem.getMeta().getHref();
			final ScanSummaryItem currentScanSummaryItem = scanSummaryRestService.getItem(scanSummaryLink);
			if (currentScanSummaryItem.getStatus().isPending()) {
				pendingScans.add(currentScanSummaryItem);
			} else if (currentScanSummaryItem.getStatus().isError()) {
				throw new HubIntegrationException("There was a problem with one of the scans. Error Status : "
						+ currentScanSummaryItem.getStatus().toString());
			}
		}

		return pendingScans;
	}

	private boolean done(final List<ScanSummaryItem> scanSummaryItems, final long timeoutInMilliseconds,
			final long startedTime) throws HubIntegrationException {
		if (scanSummaryItems.isEmpty()) {
			return true;
		}

		if (takenTooLong(timeoutInMilliseconds, startedTime)) {
			logger.info("the hub processing took too long...");
			final String formattedTime = String.format("%d minutes",
					TimeUnit.MILLISECONDS.toMinutes(timeoutInMilliseconds));
			throw new HubIntegrationException(
					"The Bom has not finished updating from the scan within the specified wait time : "
							+ formattedTime);
		}

		return false;
	}

	private boolean takenTooLong(final long timeoutInMilliseconds, final long startedTime) {
		final long elapsed = System.currentTimeMillis() - startedTime;
		return elapsed > timeoutInMilliseconds;
	}

}
