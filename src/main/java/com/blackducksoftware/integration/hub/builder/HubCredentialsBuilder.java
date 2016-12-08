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
package com.blackducksoftware.integration.hub.builder;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.builder.AbstractBuilder;
import com.blackducksoftware.integration.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.global.HubCredentials;
import com.blackducksoftware.integration.hub.validator.HubCredentialsValidator;
import com.blackducksoftware.integration.validator.AbstractValidator;

public class HubCredentialsBuilder extends AbstractBuilder<HubCredentials> {

    private String username;

    private String password;

    private int passwordLength;

    @Override
    public HubCredentials buildObject() {

        HubCredentials creds = null;
        if (StringUtils.isNotBlank(password) && passwordLength == 0) {
            // Password needs to be encrypted
            String encryptedPassword = null;
            try {
                encryptedPassword = PasswordEncrypter.encrypt(password);
            } catch (final EncryptionException e) {
                throw new IllegalArgumentException(e);
            }
            creds = new HubCredentials(username, encryptedPassword, password.length());
        } else {
            // password is blank or already encrypted so we just pass in the
            // values given to us
            creds = new HubCredentials(username, password, passwordLength);
        }
        return creds;
    }

    @Override
    public AbstractValidator createValidator() {
        final HubCredentialsValidator validator = new HubCredentialsValidator();
        validator.setUsername(getUsername());
        validator.setPassword(getPassword());
        return validator;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public int getPasswordLength() {
        return passwordLength;
    }

    /**
     * IMPORTANT : The password length should only be set if the password is
     * already encrypted
     */
    public void setPasswordLength(final int passwordLength) {
        this.passwordLength = passwordLength;
    }

}
