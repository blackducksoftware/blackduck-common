/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.hub.cli;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.service.model.RequestFactory;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.rest.RestConstants;
import com.blackducksoftware.integration.rest.connection.RestConnection;
import com.blackducksoftware.integration.rest.request.Request;
import com.blackducksoftware.integration.rest.request.Response;
import com.blackducksoftware.integration.util.HostNameHelper;

public class CLIDownloadUtility {
    private final IntLogger logger;
    private final RestConnection restConnection;

    public CLIDownloadUtility(final IntLogger logger, final RestConnection restConnection) {
        this.logger = logger;
        this.restConnection = restConnection;
    }

    public void performInstallation(final File directoryToInstallTo, final String hubUrl, final String hubVersion) throws HubIntegrationException, EncryptionException {
        final CLILocation cliLocation = new CLILocation(this.logger, directoryToInstallTo);
        final String cliDownloadUrl = cliLocation.getCLIDownloadUrl(this.logger, hubUrl);
        if (StringUtils.isNotBlank(cliDownloadUrl)) {
            try {
                customInstall(cliLocation, new URL(cliDownloadUrl), hubVersion);
            } catch (final MalformedURLException e) {
                throw new HubIntegrationException(String.format("The cli could not be downloaded from %s: %s", cliDownloadUrl, e.getMessage()), e);
            }
        } else {
            this.logger.error("Could not find the correct Hub CLI download URL.");
        }
    }

    public void customInstall(final CLILocation cliLocation, final URL cliDownloadUrl, final String hubVersion) throws HubIntegrationException, EncryptionException {
        final String directoryToInstallTo;
        try {
            directoryToInstallTo = cliLocation.getCanonicalPath();
        } catch (final IOException e) {
            throw new HubIntegrationException("Could not get the path for the install directory for the cli - does it exist?", e);
        }

        try {
            boolean cliMismatch = true;
            final File hubVersionFile = cliLocation.createHubVersionFile();
            if (hubVersionFile.exists()) {
                final String storedHubVersion = IOUtils.toString(new FileReader(hubVersionFile));
                if (hubVersion.equals(storedHubVersion)) {
                    cliMismatch = false;
                } else {
                    hubVersionFile.delete();
                    hubVersionFile.createNewFile();
                }
            }
            final File cliInstallDirectory = cliLocation.getCLIInstallDir();
            if (!cliInstallDirectory.exists()) {
                cliMismatch = true;
            }

            if (cliMismatch) {
                this.logger.debug("Attempting to download the Hub CLI.");
                try (final FileWriter writer = new FileWriter(hubVersionFile)) {
                    writer.write(hubVersion);
                }
                hubVersionFile.setLastModified(0L);
            }
            final long cliTimestamp = hubVersionFile.lastModified();

            final Map<String, String> headers = new HashMap<>();
            headers.put("If-Modified-Since", String.valueOf(cliTimestamp));

            final Request.Builder requestBuilder = RequestFactory.createCommonGetRequestBuilder(cliDownloadUrl.toURI().toString());
            requestBuilder.additionalHeaders(headers);

            final Request request = requestBuilder.build();
            try (Response response = this.restConnection.executeRequest(request)) {
                if (RestConstants.NOT_MODIFIED_304 == response.getStatusCode()) {
                    // CLI has not been modified
                    return;
                }
                final String lastModified = response.getHeaderValue("Last-Modified");
                Long lastModifiedLong = 0L;

                if (StringUtils.isNotBlank(lastModified)) {
                    // Should parse the Date just like URLConnection did
                    try {
                        final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
                        format.setTimeZone(TimeZone.getTimeZone("UTC"));
                        final Date parsed = format.parse(lastModified);
                        lastModifiedLong = parsed.getTime();
                    } catch (final ParseException pe) {
                        throw new HubIntegrationException("Could not parse the last modified date : " + pe.getMessage());
                    }
                }

                if (cliInstallDirectory.exists() && cliInstallDirectory.listFiles().length > 0) {
                    if (!cliMismatch && lastModifiedLong == cliTimestamp) {
                        this.logger.debug("The current Hub CLI is up to date.");
                        return;
                    }
                    for (final File file : cliInstallDirectory.listFiles()) {
                        FileUtils.deleteDirectory(file);
                    }
                } else {
                    cliInstallDirectory.mkdir();
                }

                this.logger.debug("Updating the Hub CLI.");
                hubVersionFile.setLastModified(lastModifiedLong);

                final String localHostName = HostNameHelper.getMyHostName();
                this.logger.info("Unpacking " + cliDownloadUrl.toString() + " to " + directoryToInstallTo + " on " + localHostName);

                try (InputStream cliStream = response.getContent()) {
                    long byteCount = 0;
                    try (CountingInputStream cis = new CountingInputStream(cliStream)) {
                        byteCount = cis.getByteCount();
                        unzip(cliInstallDirectory, cis, this.logger);
                    } catch (final IOException e) {
                        throw new HubIntegrationException(String.format("Failed to unpack %s (%d bytes read of total %d)", cliDownloadUrl, byteCount, response.getContentLength()), e);
                    }
                }
            } catch (final IntegrationException e) {
                this.logger.error("Skipping installation of " + cliDownloadUrl + " to " + directoryToInstallTo + ": " + e.toString());
                return;
            }
        } catch (final IOException e) {
            throw new HubIntegrationException("Failed to install " + cliDownloadUrl + " to " + directoryToInstallTo, e);
        } catch (final URISyntaxException e) {
            throw new HubIntegrationException("Failed to convert " + cliDownloadUrl + " to a URI : " + e.getMessage(), e);
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
        final Enumeration<? extends ZipEntry> entries = zip.entries();
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
        try (FileOutputStream fos = new FileOutputStream(f)) {
            org.apache.commons.io.IOUtils.copy(in, fos);
        }
    }

}
