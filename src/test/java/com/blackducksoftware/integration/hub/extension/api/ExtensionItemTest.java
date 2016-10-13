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
package com.blackducksoftware.integration.hub.extension.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.extension.ExtensionItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ExtensionItemTest {
	private static final String INFO_URL = "infoUrl";
	private static final String DESCRIPTION = "description";
	private static final String NAME = "name";

	@Test
	public void testConstructor() {
		final MetaInformation meta = new MetaInformation(null, null, null);
		final ExtensionItem item = new ExtensionItem(meta, NAME, DESCRIPTION, INFO_URL, true);

		assertEquals(NAME, item.getName());
		assertEquals(DESCRIPTION, item.getDescription());
		assertEquals(INFO_URL, item.getInfoUrl());
		assertTrue(item.isAuthenticated());
	}
}
