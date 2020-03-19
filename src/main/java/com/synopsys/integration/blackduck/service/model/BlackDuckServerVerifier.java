/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.service.model;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.synopsys.integration.blackduck.codelocation.signaturescanner.command.ScannerZipInstaller;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

@Deprecated
/**
 * @deprecated BlackDuckServerConfig should be used to verify a Black Duck server.
 */
public class BlackDuckServerVerifier {

    public static final String INVALID_BLACK_DUCK_SERVER_URL = "The Url does not appear to be a Black Duck server :";
    public static final String BECAUSE = ", because: ";

    public void verifyIsBlackDuckServer(URL blackDuckUrl, ProxyInfo blackDuckProxyInfo, boolean alwaysTrustServerCertificate, int timeoutSeconds) throws IntegrationException {
        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        ProxyInfo proxyInfo = blackDuckProxyInfo != null ? blackDuckProxyInfo : ProxyInfo.NO_PROXY_INFO;
        IntHttpClient intHttpClient = new IntHttpClient(logger, timeoutSeconds, alwaysTrustServerCertificate, proxyInfo);

        try {
            Request request = new Request.Builder(blackDuckUrl.toURI().toString()).build();
            try (Response response = intHttpClient.execute(request)) {
                response.throwExceptionForError();
            } catch (IntegrationRestException e) {
                if (e.getHttpStatusCode() == RestConstants.UNAUTHORIZED_401 || e.getHttpStatusCode() == RestConstants.FORBIDDEN_403) {
                    // This could be a Black Duck server
                } else {
                    throw e;
                }
            } catch (IOException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
            URL downloadURL;
            try {
                downloadURL = new URL(blackDuckUrl, ScannerZipInstaller.DEFAULT_SIGNATURE_SCANNER_DOWNLOAD_URL_SUFFIX);
            } catch (MalformedURLException e) {
                throw new BlackDuckIntegrationException("Error constructing the download URL : " + e.getMessage(), e);
            }
            String downloadUri = downloadURL.toString();
            request = RequestFactory.createCommonGetRequest(downloadUri);
            try (Response response = intHttpClient.execute(request)) {
                response.throwExceptionForError();
            } catch (IntegrationRestException e) {
                throw new BlackDuckIntegrationException(INVALID_BLACK_DUCK_SERVER_URL + downloadUri + BECAUSE + e.getHttpStatusCode() + " : " + e.getHttpStatusMessage(), e);
            } catch (IntegrationException e) {
                throw new BlackDuckIntegrationException(INVALID_BLACK_DUCK_SERVER_URL + downloadUri + BECAUSE + e.getMessage(), e);
            } catch (IOException e) {
                throw new IntegrationException(e.getMessage(), e);
            }
        } catch (URISyntaxException e) {
            throw new IntegrationException(INVALID_BLACK_DUCK_SERVER_URL + blackDuckUrl.toString() + BECAUSE + e.getMessage(), e);
        }
    }

}
