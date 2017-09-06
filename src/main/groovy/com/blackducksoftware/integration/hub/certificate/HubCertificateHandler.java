/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.certificate;

import java.io.File;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.cli.CLILocation;
import com.blackducksoftware.integration.log.IntLogger;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HubCertificateHandler {
    private final IntLogger logger;
    private final CertificateHandler handler;

    public HubCertificateHandler(final IntLogger logger) {
        this.logger = logger;
        handler = new CertificateHandler(logger);
    }

    public HubCertificateHandler(final IntLogger logger, final File javaHomeOverride) {
        this.logger = logger;
        handler = new CertificateHandler(logger, javaHomeOverride);
    }

    public void importHttpsCertificateForHubServer(final URL hubUrl, final int timeout) throws IntegrationException {
        if (hubUrl == null || !hubUrl.getProtocol().startsWith("https")) {
            return;
        }
        if (handler.isCertificateInTrustStore(hubUrl)) {
            return;
        }
        handler.retrieveAndImportHttpsCertificate(hubUrl);
        if (!isHubServer(hubUrl, timeout)) {
            // If we imported a certificate for a non Hub server we want to remove it again
            handler.removeHttpsCertificate(hubUrl);
        }
    }

    private boolean isHubServer(final URL hubUrl, final int timeout) {
        // We assume that a successful connection to the CLI download end point means this is a Hub Server
        final HttpUrl.Builder urlBuilder = HttpUrl.get(hubUrl).newBuilder();
        urlBuilder.addPathSegment("download");
        urlBuilder.addPathSegment(CLILocation.DEFAULT_CLI_DOWNLOAD);
        final HttpUrl url = urlBuilder.build();
        final OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(timeout, TimeUnit.SECONDS);
        builder.writeTimeout(timeout, TimeUnit.SECONDS);
        builder.readTimeout(timeout, TimeUnit.SECONDS);
        try {
            final OkHttpClient client = builder.build();
            final Request request = new Request.Builder().url(url).get().build();
            Response response = null;
            try {
                response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    return false;
                }
                return true;
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        } catch (final Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

}
