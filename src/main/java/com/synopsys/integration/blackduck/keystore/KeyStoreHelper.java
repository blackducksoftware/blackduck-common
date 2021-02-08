/**
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.keystore;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.http.client.SignatureScannerCertificateClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class KeyStoreHelper {
    private static final char[] DEFAULT_JAVA_KEYSTORE_PASSWORD = new char[] { 'c', 'h', 'a', 'n', 'g', 'e', 'i', 't' };

    private final IntLogger logger;
    private final SignatureScannerCertificateClient certificateClient;
    private final BlackDuckRequestFactory blackDuckRequestFactory;

    public KeyStoreHelper(final IntLogger logger, SignatureScannerCertificateClient certificateClient, BlackDuckRequestFactory blackDuckRequestFactory) {
        this.logger = logger;
        this.certificateClient = certificateClient;
        this.blackDuckRequestFactory = blackDuckRequestFactory;
    }

    public void updateKeyStoreWithServerCertificate(String alias, Certificate serverCertificate, String keyStoreFilePath) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (InputStream inputStream = new FileInputStream(keyStoreFilePath)) {
                keyStore.load(inputStream, DEFAULT_JAVA_KEYSTORE_PASSWORD);
            }

            keyStore.setCertificateEntry(alias, serverCertificate);

            try (OutputStream outputStream = new FileOutputStream(keyStoreFilePath)) {
                keyStore.store(outputStream, DEFAULT_JAVA_KEYSTORE_PASSWORD);
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException | IntegrationException e) {
            logger.errorAndDebug("Could not manage the local keystore - communicating to the server will have to be configured manually: " + e.getMessage(), e);
        }
    }

}
