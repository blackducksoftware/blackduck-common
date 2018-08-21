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
package com.synopsys.integration.blackduck.service;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.request.Response;

public class BinaryScannerService extends DataService {
    public BinaryScannerService(final HubService hubService, final IntLogger logger) {
        super(hubService, logger);
    }

    public void scanBinary(final File binaryFile, final String projectName, final String projectVersion, final String codeLocatioName) throws IntegrationException, MalformedURLException, URISyntaxException {
        final RequestBuilder builder = hubService.getRestConnection().createRequestBuilder(HttpMethod.POST);
        final URL uploadUrl = new URL(hubService.getRestConnection().getBaseUrl(), "/api/uploads");
        builder.setUri(uploadUrl.toURI());
        builder.setEntity(createEntity(binaryFile, projectName, projectVersion, codeLocatioName));

        final Response response = hubService.getRestConnection().executeRequest(builder.build());
        logger.debug("Response: " + response.toString());
        logger.debug("Response: " + response.getStatusMessage().toString());
        logger.debug("Response: " + response.getStatusCode().toString());
        logger.debug("Response: " + response.getContentString());
        if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
            logger.info("Status code OK");
        } else {
            logger.error("Unknown status code: " + response.getStatusCode());
            throw new IntegrationException("Unkown status code when uploading binary scan: " + response.getStatusCode() + ", " + response.getStatusMessage());
        }
    }

    private HttpEntity createEntity(final File file, final String projectName, final String projectVersion, final String codeLocatioName) {
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        addPart(builder, "projectName", projectName);
        addPart(builder, "version", projectVersion);
        addPart(builder, "codeLocationName", codeLocatioName);
        builder.addBinaryBody("fileupload", file);

        return builder.build();
    }

    private void addPart(final MultipartEntityBuilder builder, final String name, final String value) {
        final StringBody body = new StringBody(value, ContentType.DEFAULT_TEXT);
        final FormBodyPart part = FormBodyPartBuilder.create(name, body).build();
        // part.getHeader().removeFields("Content-Type");
        builder.addPart(part);
    }

}
