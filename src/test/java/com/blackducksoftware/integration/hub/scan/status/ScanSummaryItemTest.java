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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.scan.ScanSummaryItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ScanSummaryItemTest {
	@Test
	public void testScanSummaryItem() {
		final String href1 = "href1";
		final MetaInformation meta1 = new MetaInformation(null, href1, null);

		final String href2 = "href2";
		final MetaInformation meta2 = new MetaInformation(null, href2, null);

		final ScanSummaryItem item1 = new ScanSummaryItem(ScanStatus.UNKNOWN, null, null, null, meta1);
		final ScanSummaryItem item2 = new ScanSummaryItem(ScanStatus.COMPLETE, null, null, null, meta2);
		final ScanSummaryItem item3 = new ScanSummaryItem(ScanStatus.UNKNOWN, null, null, null, meta1);

		assertEquals(ScanStatus.UNKNOWN, item1.getStatus());
		assertEquals(meta1, item1.getMeta());

		assertEquals(ScanStatus.COMPLETE, item2.getStatus());
		assertEquals(meta2, item2.getMeta());

		assertTrue(!item1.equals(item2));
		assertTrue(item1.equals(item3));

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());
	}

}
