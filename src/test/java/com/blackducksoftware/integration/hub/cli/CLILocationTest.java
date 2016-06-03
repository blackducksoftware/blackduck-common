package com.blackducksoftware.integration.hub.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;

public class CLILocationTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testConstructorNull() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("You must provided a directory to install the CLI to.");
		new CLILocation(null);
	}

	@Test
	public void testConstructor() throws Exception {
		final File directoryToInstallTo = folder.newFolder();
		final CLILocation cliLocation = new CLILocation(directoryToInstallTo);

		assertEquals(new File(directoryToInstallTo, CLILocation.CLI_UNZIP_DIR), cliLocation.getCLIInstallDir());
		assertNull(cliLocation.getCLIHome());
		assertNull(cliLocation.getProvidedJavaExec());
		assertFalse(cliLocation.getCLIExists(null));
		assertNull(cliLocation.getCLI(null));
		assertNull(cliLocation.getOneJarFile());
	}

	@Test
	public void testGetCLIDownloadUrlJreSupported() throws Exception {
		final String baseUrl = "http://test-hub-server";

		final File directoryToInstallTo = folder.newFolder();
		final CLILocation cliLocation = new CLILocation(directoryToInstallTo);
		HubIntRestService restService = new HubIntRestService(baseUrl);
		restService = Mockito.spy(restService);
		Mockito.doReturn("3.0.1").when(restService).getHubVersion();

		final String downloadUrl = cliLocation.getCLIDownloadUrl(null, restService);

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
		final CLILocation cliLocation = new CLILocation(directoryToInstallTo);
		HubIntRestService restService = new HubIntRestService(baseUrl);
		restService = Mockito.spy(restService);
		Mockito.doReturn("2.4.0").when(restService).getHubVersion();

		final String downloadUrl = cliLocation.getCLIDownloadUrl(null, restService);

		final StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(baseUrl + "/download/");
		urlBuilder.append(HubSupportHelper.DEFAULT_CLI_DOWNLOAD);
		assertEquals(urlBuilder.toString(), downloadUrl);
	}

}
