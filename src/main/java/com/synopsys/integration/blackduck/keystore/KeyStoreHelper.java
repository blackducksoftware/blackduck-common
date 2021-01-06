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

import javax.net.ssl.HttpsURLConnection;

import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class KeyStoreHelper {
    private static final char[] DEFAULT_JAVA_KEYSTORE_PASSWORD = new char[] { 'c', 'h', 'a', 'n', 'g', 'e', 'i', 't' };

    private final IntLogger logger;

    public KeyStoreHelper(final IntLogger logger) {
        this.logger = logger;
    }

    public void updateKeyStoreWithServerCertificate(HttpUrl httpsServer, String keyStoreFilePath) {
        HttpsURLConnection httpsConnection = null;
        try {
            if (!(httpsServer.url().openConnection() instanceof HttpsURLConnection)) {
                // if it isn't an https server, there's no certificate to worry about
                return;
            }
            httpsConnection = (HttpsURLConnection) httpsServer.url().openConnection();
            httpsConnection.connect();
            Certificate[] certificates = httpsConnection.getServerCertificates();
            httpsConnection.disconnect();

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (InputStream inputStream = new FileInputStream(keyStoreFilePath)) {
                keyStore.load(inputStream, DEFAULT_JAVA_KEYSTORE_PASSWORD);
            }

            String alias = httpsServer.url().getHost();
            keyStore.setCertificateEntry(alias, certificates[0]);

            try (OutputStream outputStream = new FileOutputStream(keyStoreFilePath)) {
                keyStore.store(outputStream, DEFAULT_JAVA_KEYSTORE_PASSWORD);
            }
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            logger.errorAndDebug("Could not manage the local keystore - communicating to the server will have to be configured manually: " + e.getMessage(), e);
        }
    }

}
