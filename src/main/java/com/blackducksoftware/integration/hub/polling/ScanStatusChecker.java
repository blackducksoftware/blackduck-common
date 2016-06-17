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
package com.blackducksoftware.integration.hub.polling;

import java.util.concurrent.CountDownLatch;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.scan.status.ScanStatus;
import com.blackducksoftware.integration.hub.scan.status.ScanStatusToPoll;

public class ScanStatusChecker implements Runnable {

	private ScanStatusToPoll currentStatus;

	private final CountDownLatch countDownLock;

	private final HubIntRestService service;

	private HubIntegrationException exception;

	private boolean running;

	public ScanStatusChecker(final HubIntRestService service, final ScanStatusToPoll currentStatus, final CountDownLatch countDownLock) {
		this.service = service;
		this.currentStatus = currentStatus;
		this.countDownLock = countDownLock;
		running = true;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(final boolean running) {
		this.running = running;
	}

	public boolean hasError() {
		return exception != null;
	}

	public HubIntegrationException getError() {
		return exception;
	}

	private boolean isScanFinished(final ScanStatusToPoll status) {
		if (ScanStatus.isFinishedStatus(currentStatus.getStatusEnum()) == false) {
			return false;
		} else {

			if (ScanStatus.isErrorStatus(currentStatus.getStatusEnum())) {
				exception = new HubIntegrationException("There was a problem with one of the scans. Error Status : "
						+ currentStatus.getStatusEnum().name());
			}
			countDownLock.countDown(); // finished so unlock the parent thread.
			setRunning(false);
			return true;
		}

	}

	@Override
	public void run() {
		while (isRunning() == true) {

			try {
				if (isScanFinished(currentStatus) == true) {
					break;
				} else {
					// The code location is still updating or matching, etc.
					currentStatus = service.checkScanStatus(currentStatus.getMeta().getHref());

					if (isScanFinished(currentStatus) == true) {
						break;
					}
				}

				Thread.sleep(10000);
			} catch (final Exception ex) {
				setRunning(false);
			}
		}
	}
}
