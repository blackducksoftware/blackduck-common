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
package com.blackducksoftware.integration.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mockito.Mockito;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.blackducksoftware.integration.hub.api.VersionComparison;
import com.blackducksoftware.integration.hub.capabilities.HubCapabilitiesEnum;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.test.TestLogger;

public class HubSupportHelperTest {
	private HubIntRestService getMockedService(final String returnVersion) throws Exception {
		final HubIntRestService service = Mockito.mock(HubIntRestService.class);
		Mockito.when(service.getHubVersion()).thenReturn(returnVersion);
		return service;
	}

	private HubIntRestService getMockedServiceWithFallBack(final String returnVersion, final boolean compareSupported)
			throws Exception {
		final HubIntRestService service = getMockedService(returnVersion);
		VersionComparison compare;
		if (compareSupported) {
			compare = new VersionComparison("", "", -1, "");
		} else {
			compare = new VersionComparison("", "", 1, "");
		}
		Mockito.when(service.compareWithHubVersion(Mockito.anyString())).thenReturn(compare);
		return service;
	}

	@Test
	public void testJreProvided() throws Exception {
		HubIntRestService service = getMockedService("3.0.0");
		HubSupportHelper supportHelper = new HubSupportHelper();
		TestLogger logger = new TestLogger();
		supportHelper.checkHubSupport(service, logger);

		assertTrue(supportHelper.isHasBeenChecked());
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.JRE_PROVIDED));
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.POLICY_API));

		service = getMockedService("3.0.1");
		supportHelper = new HubSupportHelper();
		logger = new TestLogger();
		supportHelper.checkHubSupport(service, logger);

		assertTrue(supportHelper.isHasBeenChecked());
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.JRE_PROVIDED));
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.POLICY_API));
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.CLI_STATUS_DIRECTORY_OPTION));

		service = getMockedService("3.1.0");
		supportHelper = new HubSupportHelper();
		logger = new TestLogger();
		supportHelper.checkHubSupport(service, logger);

		assertTrue(supportHelper.isHasBeenChecked());
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.JRE_PROVIDED));
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.POLICY_API));
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.CLI_STATUS_DIRECTORY_OPTION));
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.CLI_PASSWORD_ENVIRONMENT_VARIABLE));

		service = getMockedService("3.4.0");
		supportHelper = new HubSupportHelper();
		logger = new TestLogger();
		supportHelper.checkHubSupport(service, logger);

		assertTrue(supportHelper.isHasBeenChecked());
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.JRE_PROVIDED));
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.POLICY_API));
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.CLI_STATUS_DIRECTORY_OPTION));
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.CLI_PASSWORD_ENVIRONMENT_VARIABLE));
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.BOM_FILE_UPLOAD));

		service = getMockedService("4.0.0");
		supportHelper = new HubSupportHelper();
		logger = new TestLogger();
		supportHelper.checkHubSupport(service, logger);

		assertTrue(supportHelper.isHasBeenChecked());
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.JRE_PROVIDED));
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.POLICY_API));
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.CLI_STATUS_DIRECTORY_OPTION));
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.CLI_PASSWORD_ENVIRONMENT_VARIABLE));
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.BOM_FILE_UPLOAD));

		service = getMockedService("3.0.0-SNAPSHOT");
		supportHelper = new HubSupportHelper();
		logger = new TestLogger();
		supportHelper.checkHubSupport(service, logger);

		assertTrue(supportHelper.isHasBeenChecked());
		for (final HubCapabilitiesEnum value : HubCapabilitiesEnum.values()) {
			assertFalse(supportHelper.hasCapability(value));
		}

		service = getMockedService("2.9.9");
		supportHelper = new HubSupportHelper();
		logger = new TestLogger();
		supportHelper.checkHubSupport(service, logger);

		assertTrue(supportHelper.isHasBeenChecked());
		for (final HubCapabilitiesEnum value : HubCapabilitiesEnum.values()) {
			assertFalse(supportHelper.hasCapability(value));
		}
	}

	@Test
	public void testCheckHubSupportFallback() throws Exception {
		HubIntRestService service = getMockedServiceWithFallBack("Two.one.zero", true);
		HubSupportHelper supportHelper = new HubSupportHelper();
		TestLogger logger = new TestLogger();
		supportHelper.checkHubSupport(service, logger);

		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.JRE_PROVIDED));
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.POLICY_API));

		service = getMockedServiceWithFallBack("3.0", true);
		supportHelper = new HubSupportHelper();
		logger = new TestLogger();
		supportHelper.checkHubSupport(service, logger);

		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.JRE_PROVIDED));
		assertTrue(supportHelper.hasCapability(HubCapabilitiesEnum.POLICY_API));
	}

	@Test
	public void testHubVersionApiMissing() throws Exception {
		final HubIntRestService service = getMockedService("2.0.1");
		final ResourceException cause = new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);
		final BDRestException exception = new BDRestException("", cause, null);
		Mockito.when(service.getHubVersion()).thenThrow(exception);

		final HubSupportHelper supportHelper = new HubSupportHelper();
		final TestLogger logger = new TestLogger();
		supportHelper.checkHubSupport(service, logger);

		for (final HubCapabilitiesEnum value : HubCapabilitiesEnum.values()) {
			assertFalse(supportHelper.hasCapability(value));
		}
	}

	@Test
	public void testHubVersionApiRestFailure() throws Exception {
		final HubIntRestService service = getMockedService("2.0.1");
		final ResourceException cause = new ResourceException(Status.CLIENT_ERROR_PAYMENT_REQUIRED);
		final String errorMessage = "error";
		final BDRestException exception = new BDRestException(errorMessage, cause, null);
		Mockito.when(service.getHubVersion()).thenThrow(exception);

		final HubSupportHelper supportHelper = new HubSupportHelper();
		final TestLogger logger = new TestLogger();
		supportHelper.checkHubSupport(service, logger);

		for (final HubCapabilitiesEnum value : HubCapabilitiesEnum.values()) {
			assertFalse(supportHelper.hasCapability(value));
		}

		assertTrue(logger.getOutputString().contains(errorMessage));
		assertTrue(logger.getOutputString().contains(Status.CLIENT_ERROR_PAYMENT_REQUIRED.getReasonPhrase()));
	}

	@Test
	public void testHubVersionApiRestFailureDifferentEror() throws Exception {
		final HubIntRestService service = getMockedService("2.0.1");
		final Exception cause = new Exception();
		final String errorMessage = "error";
		final BDRestException exception = new BDRestException(errorMessage, cause, null);
		Mockito.when(service.getHubVersion()).thenThrow(exception);

		final HubSupportHelper supportHelper = new HubSupportHelper();
		final TestLogger logger = new TestLogger();
		supportHelper.checkHubSupport(service, logger);

		for (final HubCapabilitiesEnum value : HubCapabilitiesEnum.values()) {
			assertFalse(supportHelper.hasCapability(value));
		}
		assertTrue(logger.getOutputString().contains(errorMessage));
	}

	@Test
	public void testGetLinuxCLIWrapperLink() throws Exception {
		assertEquals("testUrl/download/scan.cli.zip", HubSupportHelper.getLinuxCLIWrapperLink("testUrl"));
		assertEquals("testUrl/download/scan.cli.zip", HubSupportHelper.getLinuxCLIWrapperLink("testUrl/"));
		assertEquals("http://testSite/download/scan.cli.zip",
				HubSupportHelper.getLinuxCLIWrapperLink("http://testSite/"));
		assertEquals("http://testSite/download/scan.cli.zip",
				HubSupportHelper.getLinuxCLIWrapperLink("http://testSite"));

		try {
			HubSupportHelper.getLinuxCLIWrapperLink(" ");
		} catch (final Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
			assertEquals("You must provide a valid Hub URL in order to get the correct link.", e.getMessage());
		}

		try {
			HubSupportHelper.getLinuxCLIWrapperLink(null);
		} catch (final Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
			assertEquals("You must provide a valid Hub URL in order to get the correct link.", e.getMessage());
		}
	}

	@Test
	public void testGetWindowsCLIWrapperLink() throws Exception {
		assertEquals("testUrl/download/scan.cli-windows.zip", HubSupportHelper.getWindowsCLIWrapperLink("testUrl"));
		assertEquals("testUrl/download/scan.cli-windows.zip", HubSupportHelper.getWindowsCLIWrapperLink("testUrl/"));
		assertEquals("http://testSite/download/scan.cli-windows.zip",
				HubSupportHelper.getWindowsCLIWrapperLink("http://testSite/"));
		assertEquals("http://testSite/download/scan.cli-windows.zip",
				HubSupportHelper.getWindowsCLIWrapperLink("http://testSite"));

		try {
			HubSupportHelper.getWindowsCLIWrapperLink(" ");
		} catch (final Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
			assertEquals("You must provide a valid Hub URL in order to get the correct link.", e.getMessage());
		}

		try {
			HubSupportHelper.getWindowsCLIWrapperLink(null);
		} catch (final Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
			assertEquals("You must provide a valid Hub URL in order to get the correct link.", e.getMessage());
		}
	}

	@Test
	public void testGetOSXCLIWrapperLink() throws Exception {
		assertEquals("testUrl/download/scan.cli-macosx.zip", HubSupportHelper.getOSXCLIWrapperLink("testUrl"));
		assertEquals("testUrl/download/scan.cli-macosx.zip", HubSupportHelper.getOSXCLIWrapperLink("testUrl/"));
		assertEquals("http://testSite/download/scan.cli-macosx.zip",
				HubSupportHelper.getOSXCLIWrapperLink("http://testSite/"));
		assertEquals("http://testSite/download/scan.cli-macosx.zip",
				HubSupportHelper.getOSXCLIWrapperLink("http://testSite"));

		try {
			HubSupportHelper.getOSXCLIWrapperLink(" ");
		} catch (final Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
			assertEquals("You must provide a valid Hub URL in order to get the correct link.", e.getMessage());
		}

		try {
			HubSupportHelper.getOSXCLIWrapperLink(null);
		} catch (final Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
			assertEquals("You must provide a valid Hub URL in order to get the correct link.", e.getMessage());
		}
	}

}
