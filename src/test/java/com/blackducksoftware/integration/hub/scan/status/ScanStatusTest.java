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
package com.blackducksoftware.integration.hub.scan.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ScanStatusTest {

	@Test
	public void testGetScanStatus() {
		assertEquals(ScanStatus.UNKNOWN, ScanStatus.getScanStatus("Fake"));
		assertEquals(ScanStatus.UNSTARTED, ScanStatus.getScanStatus(ScanStatus.UNSTARTED.toString().toLowerCase()));
		assertEquals(ScanStatus.UNSTARTED, ScanStatus.getScanStatus(ScanStatus.UNSTARTED.toString()));
		assertEquals(ScanStatus.SCANNING, ScanStatus.getScanStatus(ScanStatus.SCANNING.toString().toLowerCase()));
		assertEquals(ScanStatus.SCANNING, ScanStatus.getScanStatus(ScanStatus.SCANNING.toString()));
		assertEquals(ScanStatus.SAVING_SCAN_DATA,
				ScanStatus.getScanStatus(ScanStatus.SAVING_SCAN_DATA.toString().toLowerCase()));
		assertEquals(ScanStatus.SAVING_SCAN_DATA, ScanStatus.getScanStatus(ScanStatus.SAVING_SCAN_DATA.toString()));
		assertEquals(ScanStatus.SCAN_DATA_SAVE_COMPLETE,
				ScanStatus.getScanStatus(ScanStatus.SCAN_DATA_SAVE_COMPLETE.toString().toLowerCase()));
		assertEquals(ScanStatus.SCAN_DATA_SAVE_COMPLETE,
				ScanStatus.getScanStatus(ScanStatus.SCAN_DATA_SAVE_COMPLETE.toString()));
		assertEquals(ScanStatus.REQUESTED_MATCH_JOB,
				ScanStatus.getScanStatus(ScanStatus.REQUESTED_MATCH_JOB.toString().toLowerCase()));
		assertEquals(ScanStatus.REQUESTED_MATCH_JOB,
				ScanStatus.getScanStatus(ScanStatus.REQUESTED_MATCH_JOB.toString()));
		assertEquals(ScanStatus.MATCHING, ScanStatus.getScanStatus(ScanStatus.MATCHING.toString().toLowerCase()));
		assertEquals(ScanStatus.MATCHING, ScanStatus.getScanStatus(ScanStatus.MATCHING.toString()));
		assertEquals(ScanStatus.BOM_VERSION_CHECK,
				ScanStatus.getScanStatus(ScanStatus.BOM_VERSION_CHECK.toString().toLowerCase()));
		assertEquals(ScanStatus.BOM_VERSION_CHECK, ScanStatus.getScanStatus(ScanStatus.BOM_VERSION_CHECK.toString()));
		assertEquals(ScanStatus.BUILDING_BOM,
				ScanStatus.getScanStatus(ScanStatus.BUILDING_BOM.toString().toLowerCase()));
		assertEquals(ScanStatus.BUILDING_BOM, ScanStatus.getScanStatus(ScanStatus.BUILDING_BOM.toString()));
		assertEquals(ScanStatus.COMPLETE, ScanStatus.getScanStatus(ScanStatus.COMPLETE.toString().toLowerCase()));
		assertEquals(ScanStatus.COMPLETE, ScanStatus.getScanStatus(ScanStatus.COMPLETE.toString()));
		assertEquals(ScanStatus.CANCELLED, ScanStatus.getScanStatus(ScanStatus.CANCELLED.toString().toLowerCase()));
		assertEquals(ScanStatus.CANCELLED, ScanStatus.getScanStatus(ScanStatus.CANCELLED.toString()));
		assertEquals(ScanStatus.ERROR_SCANNING,
				ScanStatus.getScanStatus(ScanStatus.ERROR_SCANNING.toString().toLowerCase()));
		assertEquals(ScanStatus.ERROR_SCANNING, ScanStatus.getScanStatus(ScanStatus.ERROR_SCANNING.toString()));
		assertEquals(ScanStatus.ERROR_SAVING_SCAN_DATA,
				ScanStatus.getScanStatus(ScanStatus.ERROR_SAVING_SCAN_DATA.toString().toLowerCase()));
		assertEquals(ScanStatus.ERROR_SAVING_SCAN_DATA,
				ScanStatus.getScanStatus(ScanStatus.ERROR_SAVING_SCAN_DATA.toString()));
		assertEquals(ScanStatus.ERROR_MATCHING,
				ScanStatus.getScanStatus(ScanStatus.ERROR_MATCHING.toString().toLowerCase()));
		assertEquals(ScanStatus.ERROR_MATCHING, ScanStatus.getScanStatus(ScanStatus.ERROR_MATCHING.toString()));
		assertEquals(ScanStatus.ERROR_BUILDING_BOM,
				ScanStatus.getScanStatus(ScanStatus.ERROR_BUILDING_BOM.toString().toLowerCase()));
		assertEquals(ScanStatus.ERROR_BUILDING_BOM, ScanStatus.getScanStatus(ScanStatus.ERROR_BUILDING_BOM.toString()));
		assertEquals(ScanStatus.ERROR, ScanStatus.getScanStatus(ScanStatus.ERROR.toString().toLowerCase()));
		assertEquals(ScanStatus.ERROR, ScanStatus.getScanStatus(ScanStatus.ERROR.toString()));
		assertEquals(ScanStatus.UNKNOWN, ScanStatus.getScanStatus(ScanStatus.UNKNOWN.toString().toLowerCase()));
		assertEquals(ScanStatus.UNKNOWN, ScanStatus.getScanStatus(ScanStatus.UNKNOWN.toString()));
		assertEquals(ScanStatus.UNKNOWN, ScanStatus.getScanStatus(null));
	}

	@Test
	public void testIsDoneStatus() {
		assertFalse(ScanStatus.BOM_VERSION_CHECK.isDone());
		assertFalse(ScanStatus.BUILDING_BOM.isDone());
		assertFalse(ScanStatus.MATCHING.isDone());
		assertFalse(ScanStatus.REQUESTED_MATCH_JOB.isDone());
		assertFalse(ScanStatus.SAVING_SCAN_DATA.isDone());
		assertFalse(ScanStatus.SCANNING.isDone());
		assertFalse(ScanStatus.SCAN_DATA_SAVE_COMPLETE.isDone());
		assertFalse(ScanStatus.UNKNOWN.isDone());
		assertFalse(ScanStatus.UNSTARTED.isDone());

		assertTrue(ScanStatus.CANCELLED.isDone());
		assertTrue(ScanStatus.CLONED.isDone());
		assertTrue(ScanStatus.COMPLETE.isDone());
		assertTrue(ScanStatus.ERROR.isDone());
		assertTrue(ScanStatus.ERROR_BUILDING_BOM.isDone());
		assertTrue(ScanStatus.ERROR_MATCHING.isDone());
		assertTrue(ScanStatus.ERROR_SAVING_SCAN_DATA.isDone());
		assertTrue(ScanStatus.ERROR_SCANNING.isDone());
	}

	@Test
	public void testIsErrorStatus() {
		assertFalse(ScanStatus.BOM_VERSION_CHECK.isError());
		assertFalse(ScanStatus.BUILDING_BOM.isError());
		assertFalse(ScanStatus.COMPLETE.isError());
		assertFalse(ScanStatus.MATCHING.isError());
		assertFalse(ScanStatus.REQUESTED_MATCH_JOB.isError());
		assertFalse(ScanStatus.SAVING_SCAN_DATA.isError());
		assertFalse(ScanStatus.SCANNING.isError());
		assertFalse(ScanStatus.SCAN_DATA_SAVE_COMPLETE.isError());
		assertFalse(ScanStatus.UNKNOWN.isError());
		assertFalse(ScanStatus.UNSTARTED.isError());

		assertTrue(ScanStatus.CANCELLED.isError());
		assertTrue(ScanStatus.ERROR.isError());
		assertTrue(ScanStatus.ERROR_BUILDING_BOM.isError());
		assertTrue(ScanStatus.ERROR_MATCHING.isError());
		assertTrue(ScanStatus.ERROR_SAVING_SCAN_DATA.isError());
		assertTrue(ScanStatus.ERROR_SCANNING.isError());
	}

}
