/**
 * Hub Common
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
package com.blackducksoftware.integration.hub.certificate;

import java.io.File;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.log.IntLogger;

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

    public void importHttpsCertificateForHubServer(final HubServerConfig hubServerConfig) throws IntegrationException {
        if (hubServerConfig == null || hubServerConfig.getHubUrl() == null || !hubServerConfig.getHubUrl().getProtocol().startsWith("https")) {
            return;
        }
        handler.timeout = hubServerConfig.getTimeout();
        if (hubServerConfig.getProxyInfo() != null) {
            handler.proxyHost = hubServerConfig.getProxyInfo().getHost();
            handler.proxyPort = hubServerConfig.getProxyInfo().getPort();
            handler.proxyNoHosts = hubServerConfig.getProxyInfo().getIgnoredProxyHosts();
            handler.proxyUsername = hubServerConfig.getProxyInfo().getUsername();
            handler.proxyPassword = hubServerConfig.getProxyInfo().getDecryptedPassword();
        }

        if (handler.isCertificateInTrustStore(hubServerConfig.getHubUrl())) {
            return;
        }
        handler.retrieveAndImportHttpsCertificate(hubServerConfig.getHubUrl());
    }
}
