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
package com.blackducksoftware.integration.hub.global;

import java.io.Serializable;
import java.util.Arrays;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.blackducksoftware.integration.encryption.PasswordDecrypter;
import com.blackducksoftware.integration.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.exception.EncryptionException;

public class HubCredentials implements Serializable {
    private static final long serialVersionUID = 7924589951692259151L;

    private final String username;

    private final String encryptedPassword;

    private final int actualPasswordLength;

    public HubCredentials(final String username, final String password) throws EncryptionException {
        this.username = username;
        this.actualPasswordLength = (password == null ? 0 : password.length());
        this.encryptedPassword = PasswordEncrypter.encrypt(password);
    }

    public HubCredentials(final String username, final String encryptedPassword, final int actualPasswordLength) {
        this.username = username;
        this.actualPasswordLength = actualPasswordLength;
        this.encryptedPassword = encryptedPassword;
    }

    public HubCredentials(final String username, final String password, final boolean isEncrypted) throws EncryptionException {
        this.username = username;
        this.encryptedPassword = isEncrypted ? password : PasswordEncrypter.encrypt(password);
        this.actualPasswordLength = isEncrypted ? PasswordDecrypter.decrypt(password).length() : password.length();
    }

    public String getMaskedPassword() {
        final char[] array = new char[actualPasswordLength];
        Arrays.fill(array, '*');
        return new String(array);
    }

    public String getDecryptedPassword() throws EncryptionException {
        return PasswordDecrypter.decrypt(encryptedPassword);
    }

    public String getUsername() {
        return username;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public int getActualPasswordLength() {
        return actualPasswordLength;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
    }

}
