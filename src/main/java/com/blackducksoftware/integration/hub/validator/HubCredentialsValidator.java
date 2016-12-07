/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
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
