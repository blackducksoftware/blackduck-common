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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.global.HubProxyInfoFieldEnum;
import com.blackducksoftware.integration.validator.AbstractValidator;
import com.blackducksoftware.integration.validator.ValidationResult;
import com.blackducksoftware.integration.validator.ValidationResultEnum;
import com.blackducksoftware.integration.validator.ValidationResults;

public class HubProxyValidator extends AbstractValidator {

    public static final String MSG_PROXY_INVALID_CONFIG = "The proxy information not valid - please check the log for the specific issues.";

    public static final String MSG_IGNORE_HOSTS_INVALID = "Proxy ignore hosts does not compile to a valid regular expression.";

    public static final String MSG_CREDENTIALS_INVALID = "Proxy username and password must both be populated or both be empty.";

    public static final String MSG_PROXY_PORT_INVALID = "Proxy port must be greater than 0.";

    public static final String MSG_PROXY_HOST_REQUIRED = "Proxy port specified, but proxy host not specified.";

    public static final String MSG_PROXY_PORT_REQUIRED = "Proxy host specified, but proxy port not specified.";

    public static final String MSG_PROXY_HOST_NOT_SPECIFIED = "Proxy host not specified.";

    private String host;

    private String port;

    private String username;

    private String password;

    private int passwordLength;

    private String ignoredProxyHosts;

    @Override
    public ValidationResults assertValid() {
        final ValidationResults result = new ValidationResults();
        if (hasProxySettings()) {
            validatePort(result);
            validateCredentials(result);
            validateIgnoreHosts(result);
        }
        return result;
    }

    public void validatePort(final ValidationResults result) {
        if (StringUtils.isBlank(host) && StringUtils.isBlank(port)) {
            return;
        } else if (StringUtils.isBlank(host) && StringUtils.isNotBlank(port)) {
            result.addResult(HubProxyInfoFieldEnum.PROXYHOST,
                    new ValidationResult(ValidationResultEnum.ERROR, MSG_PROXY_HOST_REQUIRED));
        } else if (StringUtils.isNotBlank(host) && StringUtils.isBlank(port)) {
            result.addResult(HubProxyInfoFieldEnum.PROXYPORT,
                    new ValidationResult(ValidationResultEnum.ERROR, MSG_PROXY_PORT_REQUIRED));
            return;
        }
        int portToValidate = 0;
        try {
            portToValidate = stringToInteger(port);
        } catch (final IllegalArgumentException e) {
            result.addResult(HubProxyInfoFieldEnum.PROXYPORT,
                    new ValidationResult(ValidationResultEnum.ERROR, e.getMessage(), e));
            return;
        }
        if (StringUtils.isNotBlank(host) && portToValidate < 0) {
            result.addResult(HubProxyInfoFieldEnum.PROXYPORT,
                    new ValidationResult(ValidationResultEnum.ERROR, MSG_PROXY_PORT_INVALID));
        }

    }

    public void validateCredentials(final ValidationResults result) {
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password) && StringUtils.isBlank(host)) {
            result.addResult(HubProxyInfoFieldEnum.PROXYHOST,
                    new ValidationResult(ValidationResultEnum.ERROR, MSG_PROXY_HOST_NOT_SPECIFIED));
        }
        if (StringUtils.isNotBlank(username) && StringUtils.isBlank(password) || StringUtils.isBlank(username) && StringUtils.isNotBlank(password)) {
            result.addResult(HubProxyInfoFieldEnum.PROXYUSERNAME,
                    new ValidationResult(ValidationResultEnum.ERROR, MSG_CREDENTIALS_INVALID));
            result.addResult(HubProxyInfoFieldEnum.PROXYPASSWORD,
                    new ValidationResult(ValidationResultEnum.ERROR, MSG_CREDENTIALS_INVALID));
        }
    }

    public void validateIgnoreHosts(final ValidationResults result) {
        if (StringUtils.isNotBlank(ignoredProxyHosts)) {
            if (StringUtils.isBlank(host)) {
                result.addResult(HubProxyInfoFieldEnum.PROXYHOST,
                        new ValidationResult(ValidationResultEnum.ERROR, MSG_PROXY_HOST_NOT_SPECIFIED));
            }
            try {
                if (ignoredProxyHosts.contains(",")) {
                    String[] ignoreHosts = null;
                    ignoreHosts = ignoredProxyHosts.split(",");
                    for (final String ignoreHost : ignoreHosts) {
                        Pattern.compile(ignoreHost.trim());
                    }
                } else {
                    Pattern.compile(ignoredProxyHosts);
                }
            } catch (final PatternSyntaxException ex) {
                result.addResult(HubProxyInfoFieldEnum.NOPROXYHOSTS,
                        new ValidationResult(ValidationResultEnum.ERROR, MSG_IGNORE_HOSTS_INVALID));
            }
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(final int port) {
        setPort(String.valueOf(port));
    }

    public void setPort(final String port) {
        this.port = port;
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

    public String getIgnoredProxyHosts() {
        return ignoredProxyHosts;
    }

    public void setIgnoredProxyHosts(final String ignoredProxyHosts) {
        this.ignoredProxyHosts = ignoredProxyHosts;
    }

    public boolean hasProxySettings() {
        return StringUtils.isNotBlank(host) || StringUtils.isNotBlank(port) || StringUtils.isNotBlank(username) || StringUtils.isNotBlank(password)
                || StringUtils.isNotBlank(ignoredProxyHosts);
    }

    public boolean hasAuthenticatedProxySettings() {
        return StringUtils.isNotBlank(password) && StringUtils.isNotBlank(username);
    }
}
