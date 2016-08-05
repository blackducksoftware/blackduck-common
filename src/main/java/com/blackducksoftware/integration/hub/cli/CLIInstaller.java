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
package com.blackducksoftware.integration.hub.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;

import com.blackducksoftware.integration.hub.CIEnvironmentVariables;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.util.AuthenticatorUtil;

public class CLIInstaller {
	private String proxyHost;
	private Integer proxyPort;
	private String proxyUserName;
	private String proxyPassword;
	private final CLILocation cliLocation;
	private final CIEnvironmentVariables ciEnvironmentVariables;

	public CLIInstaller(final CLILocation cliLocation, final CIEnvironmentVariables ciEnvironmentVariables) {
		this.cliLocation = cliLocation;
		this.ciEnvironmentVariables = ciEnvironmentVariables;
	}

	public void performInstallation(final IntLogger logger, final HubIntRestService restService,
			final String localHostName)
			throws IOException, InterruptedException, BDRestException, URISyntaxException, HubIntegrationException {
		if (StringUtils.isBlank(localHostName)) {
			throw new IllegalArgumentException("You must provided the hostName of the machine this is running on.");
		}

		final String cliDownloadUrl = cliLocation.getCLIDownloadUrl(logger, restService);
		if (StringUtils.isNotBlank(cliDownloadUrl)) {
			customInstall(new URL(cliDownloadUrl), restService.getHubVersion(), localHostName, logger);
		} else {
			logger.error("Could not find the correct Hub CLI download URL.");
		}
	}

	public void customInstall(final URL archive, String hubVersion, final String localHostName, final IntLogger logger)
			throws IOException, InterruptedException, HubIntegrationException {
		boolean cliMismatch = true;
		// For some reason the Hub returns the Version inside quotes
		hubVersion = hubVersion.replace("\"", "");

		try {
			final File hubVersionFile = cliLocation.createHubVersionFile();
			if (hubVersionFile.exists()) {
				final String storedHubVersion = IOUtils.toString(new FileInputStream(hubVersionFile));
				if (hubVersion.equals(storedHubVersion)) {
					cliMismatch = false;
				} else {
					hubVersionFile.delete();
				}
			}
			if (cliMismatch) {
				hubVersionFile.createNewFile();
				final FileWriter writer = new FileWriter(hubVersionFile);
				writer.write(hubVersion);
				writer.close();
				hubVersionFile.setLastModified(0L);
			}
			final long cliTimestamp = hubVersionFile.lastModified();

			URLConnection connection = null;
			try {
				Proxy proxy = null;

				if (StringUtils.isNotBlank(proxyHost)) {
					proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
				}
				if (proxy != null) {
					if (StringUtils.isNotBlank(proxyUserName) && StringUtils.isNotBlank(proxyPassword)) {
						AuthenticatorUtil.setAuthenticator(proxyUserName, proxyPassword);
					} else {
						AuthenticatorUtil.resetAuthenticator();
					}
				}
				if (proxy != null) {
					connection = archive.openConnection(proxy);
				} else {
					connection = archive.openConnection();
				}
				connection.setIfModifiedSince(cliTimestamp);
				connection.connect();
			} catch (final IOException ioe) {
				logger.error("Skipping installation of " + archive + " to " + cliLocation.getCanonicalPath() + ": "
						+ ioe.toString());
				return;
			}

			if (connection instanceof HttpURLConnection
					&& ((HttpURLConnection) connection).getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
				// CLI has not been modified
				return;
			}

			final long sourceTimestamp = connection.getLastModified();

			final File cliInstallDirectory = cliLocation.getCLIInstallDir();
			if (cliInstallDirectory.exists() && cliInstallDirectory.listFiles().length > 0) {
				if (!cliMismatch && sourceTimestamp == cliTimestamp) {
					// already up to date
					return;
				}

				for (final File file : cliInstallDirectory.listFiles()) {
					FileUtils.deleteDirectory(file);
				}
			} else {
				cliInstallDirectory.mkdir();
			}
			hubVersionFile.setLastModified(sourceTimestamp);

			logger.info("Unpacking " + archive.toString() + " to " + cliInstallDirectory.getCanonicalPath() + " on "
					+ localHostName);

			final CountingInputStream cis = new CountingInputStream(connection.getInputStream());
			try {
				unzip(cliInstallDirectory, cis, logger);
				updateJreSecurity(logger);
			} catch (final IOException e) {
				throw new IOException(String.format("Failed to unpack %s (%d bytes read of total %d)", archive,
						cis.getByteCount(), connection.getContentLength()), e);
			}
		} catch (final IOException e) {
			throw new IOException("Failed to install " + archive + " to " + cliLocation.getCanonicalPath(), e);
		}
	}

	private void updateJreSecurity(final IntLogger logger) throws IOException {
		final String cacertsFilename = "cacerts";
		if (ciEnvironmentVariables.containsKey(CIEnvironmentVariables.BDS_CACERTS_OVERRIDE)) {
			final File securityDirectory = cliLocation.getJreSecurityDirectory();
			if (null == securityDirectory) {
				// the cli might not have the jre included
				return;
			}
			final String customCacertsPath = ciEnvironmentVariables
					.getValue(CIEnvironmentVariables.BDS_CACERTS_OVERRIDE);
			final File customCacerts = new File(customCacertsPath);

			final File cacerts = new File(securityDirectory, cacertsFilename);
			final File cacertsBackup = new File(securityDirectory, cacertsFilename + System.currentTimeMillis());

			try {
				FileUtils.moveFile(cacerts, cacertsBackup);
				FileUtils.copyFile(customCacerts, cacerts);
			} catch (final IOException e) {
				logger.error("Could not copy the custom cacerts file from: " + customCacertsPath + " to: "
						+ cacerts.getAbsolutePath() + " msg: " + e.getMessage());
				throw e;
			}
		}
	}

	private void unzip(final File dir, final InputStream in, final IntLogger logger) throws IOException {
		// uses java.io.tmpdir
		final File tmpFile = File.createTempFile("tmpzip", null);

		try {
			copyInputStreamToFile(in, tmpFile);
			unzip(dir, tmpFile, logger);
		} finally {
			tmpFile.delete();
		}
	}

	private void unzip(File dir, final File zipFile, final IntLogger logger) throws IOException {
		// without getAbsoluteFile, getParentFile below seems to fail
		dir = dir.getAbsoluteFile();
		final ZipFile zip = new ZipFile(zipFile);
		final Enumeration<ZipEntry> entries = zip.getEntries();
		try {
			while (entries.hasMoreElements()) {
				final ZipEntry e = entries.nextElement();
				final File f = new File(dir, e.getName());
				if (e.isDirectory()) {
					f.mkdirs();
				} else {
					final File p = f.getParentFile();
					if (p != null) {
						p.mkdirs();
					}
					final InputStream input = zip.getInputStream(e);
					try {
						copyInputStreamToFile(input, f);
					} finally {
						input.close();
					}
					f.setLastModified(e.getTime());
				}
			}
		} finally {
			zip.close();
		}
	}

	private void copyInputStreamToFile(final InputStream in, final File f) throws IOException {
		final FileOutputStream fos = new FileOutputStream(f);
		try {
			org.apache.commons.io.IOUtils.copy(in, fos);
		} finally {
			org.apache.commons.io.IOUtils.closeQuietly(fos);
		}
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(final String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public Integer getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(final Integer proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getProxyUserName() {
		return proxyUserName;
	}

	public void setProxyUserName(final String proxyUserName) {
		this.proxyUserName = proxyUserName;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(final String proxyPassword) {
		this.proxyPassword = proxyPassword;
	}

}
