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
package com.blackducksoftware.integration.hub.global;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.logging.IntLogger;

public class HubCredentialsBuilder {
	private String username;
	private String password;

	public HubCredentials build(final IntLogger logger)
			throws HubIntegrationException, IllegalArgumentException, EncryptionException {
		assertValid(logger);

		return new HubCredentials(username, password);
	}

	public void assertValid(final IntLogger logger) throws HubIntegrationException {
		boolean valid = true;

		if (!validateCredentials(logger)) {
			valid = false;
		}

		if (!valid) {
			throw new HubIntegrationException(
					"The credentials are not valid - please check the log for the specific issues.");
		}
	}

	public boolean validateCredentials(final IntLogger logger) {
		boolean valid = true;

		if (!validateUsername(logger)) {
			valid = false;
		}

		if (!validatePassword(logger)) {
			valid = false;
		}

		return valid;
	}

	public boolean validateUsername(final IntLogger logger) {
		boolean valid = true;
		if (StringUtils.isBlank(username)) {
			valid = false;
			logger.error("No Hub Username was found.");
		}
		return valid;
	}

	public boolean validatePassword(final IntLogger logger) {
		boolean valid = true;
		if (StringUtils.isBlank(password)) {
			valid = false;
			logger.error("No Hub Password was found.");
		}
		return valid;
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
