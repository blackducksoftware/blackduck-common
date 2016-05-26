/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.util.TestLogger;

public class CLIInstallerTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testConstructorNull() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("You must provided a directory to install the CLI to.");
		new CLIInstaller(null);
	}

	@Test
	public void testConstructor() throws Exception {
		final File directoryToInstallTo = folder.newFolder();
		final CLIInstaller installer = new CLIInstaller(directoryToInstallTo);

		assertEquals(directoryToInstallTo, installer.getDirectoryToInstallTo());
		assertEquals(new File(directoryToInstallTo, CLIInstaller.CLI_UNZIP_DIR), installer.getCLIInstallDir());
		assertNull(installer.getCLIHome());
		assertNull(installer.getProvidedJavaExec());
		assertFalse(installer.getCLIExists(null));
		assertNull(installer.getCLI());
		assertNull(installer.getOneJarFile());

	}

	@Test
	public void testGetCLIDownloadUrlJreSupported() throws Exception {
		final String baseUrl = "http://test-hub-server";

		final File directoryToInstallTo = folder.newFolder();
		final CLIInstaller installer = new CLIInstaller(directoryToInstallTo);
		HubIntRestService restService = new HubIntRestService(baseUrl);
		restService = Mockito.spy(restService);
		Mockito.doReturn("3.0.1").when(restService).getHubVersion();

		final String downloadUrl = installer.getCLIDownloadUrl(null, restService);

		final StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(baseUrl + "/download/");

		if (SystemUtils.IS_OS_MAC_OSX) {
			urlBuilder.append(HubSupportHelper.MAC_CLI_DOWNLOAD);
			assertEquals(urlBuilder.toString(), downloadUrl);
		} else if (SystemUtils.IS_OS_WINDOWS) {
			urlBuilder.append(HubSupportHelper.WINDOWS_CLI_DOWNLOAD);
			assertEquals(urlBuilder.toString(), downloadUrl);
		} else {
			urlBuilder.append(HubSupportHelper.DEFAULT_CLI_DOWNLOAD);
			assertEquals(urlBuilder.toString(), downloadUrl);
		}
	}

	@Test
	public void testGetCLIDownloadUrlJreNotSupported() throws Exception {
		final String baseUrl = "http://test-hub-server";

		final File directoryToInstallTo = folder.newFolder();
		final CLIInstaller installer = new CLIInstaller(directoryToInstallTo);
		HubIntRestService restService = new HubIntRestService(baseUrl);
		restService = Mockito.spy(restService);
		Mockito.doReturn("2.4.0").when(restService).getHubVersion();

		final String downloadUrl = installer.getCLIDownloadUrl(null, restService);

		final StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(baseUrl + "/download/");
		urlBuilder.append(HubSupportHelper.DEFAULT_CLI_DOWNLOAD);
		assertEquals(urlBuilder.toString(), downloadUrl);
	}

	@Test
	public void testCustomInstall_2_4_2() throws Exception {
		final File directoryToInstallTo = folder.newFolder();
		final CLIInstaller installer = new CLIInstaller(directoryToInstallTo);

		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final URL cliZip = classLoader.getResource("scan.cli-2.4.2.zip");
		final TestLogger logger = new TestLogger();

		installer.customInstall(cliZip, "2.4.2", "localHost", logger);

		assertNotNull(installer.getCLIHome());
		assertNull(installer.getProvidedJavaExec());
		assertTrue(installer.getCLIExists(logger));
		assertNotNull(installer.getCLI());
		assertNotNull(installer.getOneJarFile());

		assertTrue(logger.getErrorList().isEmpty());
		assertTrue(logger.getOutputString(), logger.getOutputString().contains("Unpacking file:"));
	}

	@Test
	public void testCustomInstall_2_4_2_To_3_1_0() throws Exception {
		final File directoryToInstallTo = folder.newFolder();
		final CLIInstaller installer = new CLIInstaller(directoryToInstallTo);

		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL cliZip = classLoader.getResource("scan.cli-2.4.2.zip");
		TestLogger logger = new TestLogger();

		installer.customInstall(cliZip, "2.4.2", "localHost", logger);

		assertNotNull(installer.getCLIHome());
		assertNull(installer.getProvidedJavaExec());
		assertTrue(installer.getCLIExists(logger));
		assertNotNull(installer.getCLI());
		assertNotNull(installer.getOneJarFile());

		assertTrue(logger.getErrorList().isEmpty());
		assertTrue(logger.getOutputString(), logger.getOutputString().contains("Unpacking file:"));

		cliZip = classLoader.getResource("scan.cli-3.1.0.zip");
		logger = new TestLogger();

		installer.customInstall(cliZip, "3.1.0", "localHost", logger);

		assertNotNull(installer.getCLIHome());
		assertNull(installer.getProvidedJavaExec());
		assertTrue(installer.getCLIExists(logger));
		assertNotNull(installer.getCLI());
		assertNotNull(installer.getOneJarFile());

		assertTrue(logger.getErrorList().isEmpty());
		assertTrue(logger.getOutputString(), logger.getOutputString().contains("Unpacking file:"));
	}

	@Test
	public void testCustomInstall_3_1_0() throws Exception {
		final File directoryToInstallTo = folder.newFolder();
		final CLIInstaller installer = new CLIInstaller(directoryToInstallTo);

		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final URL cliZip = classLoader.getResource("scan.cli-3.1.0.zip");
		final TestLogger logger = new TestLogger();

		installer.customInstall(cliZip, "3.1.0", "localHost", logger);

		assertNotNull(installer.getCLIHome());
		assertTrue(installer.getCLIExists(logger));
		assertNotNull(installer.getCLI());
		assertNotNull(installer.getOneJarFile());

		assertTrue(logger.getErrorList().isEmpty());
		assertTrue(logger.getOutputString(), logger.getOutputString().contains("Unpacking file:"));
	}

	@Test
	public void testCustomInstall_3_1_0_Modified() throws Exception {
		final File directoryToInstallTo = folder.newFolder();
		final CLIInstaller installer = new CLIInstaller(directoryToInstallTo);

		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final URL cliZip = classLoader.getResource("scan.cli-3.1.0.zip");
		TestLogger logger = new TestLogger();

		installer.customInstall(cliZip, "3.1.0", "localHost", logger);

		assertNotNull(installer.getCLIHome());
		assertTrue(installer.getCLIExists(logger));
		assertNotNull(installer.getCLI());
		assertNotNull(installer.getOneJarFile());

		assertTrue(logger.getErrorList().isEmpty());
		assertTrue(logger.getOutputString(), logger.getOutputString().contains("Unpacking file:"));

		final File hubVersionFile = new File(directoryToInstallTo, CLIInstaller.VERSION_FILE_NAME);
		hubVersionFile.setLastModified(0L);

		logger = new TestLogger();

		installer.customInstall(cliZip, "3.1.0", "localHost", logger);

		assertNotNull(installer.getCLIHome());
		assertNull(installer.getProvidedJavaExec());
		assertTrue(installer.getCLIExists(logger));
		assertNotNull(installer.getCLI());
		assertNotNull(installer.getOneJarFile());

		assertTrue(logger.getErrorList().isEmpty());
		assertTrue(logger.getOutputString(), logger.getOutputString().contains("Unpacking file:"));
	}

	@Test
	public void testCustomInstall_3_1_0_NotModified() throws Exception {
		final File directoryToInstallTo = folder.newFolder();
		final CLIInstaller installer = new CLIInstaller(directoryToInstallTo);

		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final URL cliZip = classLoader.getResource("scan.cli-3.1.0.zip");
		TestLogger logger = new TestLogger();

		installer.customInstall(cliZip, "3.1.0", "localHost", logger);

		assertNotNull(installer.getCLIHome());
		assertTrue(installer.getCLIExists(logger));
		assertNotNull(installer.getCLI());
		assertNotNull(installer.getOneJarFile());

		assertTrue(logger.getErrorList().isEmpty());
		assertTrue(logger.getOutputString(), logger.getOutputString().contains("Unpacking file:"));

		logger = new TestLogger();

		installer.customInstall(cliZip, "3.1.0", "localHost", logger);

		assertNotNull(installer.getCLIHome());
		assertNull(installer.getProvidedJavaExec());
		assertTrue(installer.getCLIExists(logger));
		assertNotNull(installer.getCLI());
		assertNotNull(installer.getOneJarFile());

		assertTrue(logger.getErrorList().isEmpty());
		assertTrue(logger.getOutputString(), !logger.getOutputString().contains("Unpacking file:"));
	}

}
