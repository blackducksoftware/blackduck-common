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
package com.blackducksoftware.integration.hub.validator;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.global.HubCredentialsFieldEnum;
import com.blackducksoftware.integration.validator.AbstractValidator;
import com.blackducksoftware.integration.validator.ValidationResult;
import com.blackducksoftware.integration.validator.ValidationResultEnum;
import com.blackducksoftware.integration.validator.ValidationResults;

public class HubCredentialsValidator extends AbstractValidator {

    private String username;

    private String password;

    @Override
    public ValidationResults assertValid() {
        final ValidationResults result = new ValidationResults();

        validateCredentials(result);

        return result;
    }

    public void validateCredentials(final ValidationResults result) {

        validateUsername(result);
        validatePassword(result);
    }

    public void validateUsername(final ValidationResults result) {
        if (StringUtils.isBlank(username)) {
            result.addResult(HubCredentialsFieldEnum.USERNAME,
                    new ValidationResult(ValidationResultEnum.ERROR, "No Hub Username was found."));
        }
    }

    public void validatePassword(final ValidationResults result) {
        if (StringUtils.isBlank(password)) {
            result.addResult(HubCredentialsFieldEnum.PASSWORD,
                    new ValidationResult(ValidationResultEnum.ERROR, "No Hub Password was found."));
        }
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
}
