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
package com.blackducksoftware.integration.hub.validate;

import java.io.IOException;

import com.blackducksoftware.integration.hub.global.HubProxyInfoBuilder;

public abstract class HubProxyInfoValidator<T> extends AbstractValidator<T> {

	public T validatePort(final String host, final String port) throws IOException {
		int portValue = 0; // invalid port value
		try {
			portValue = Integer.valueOf(port);
		} catch (final Exception ex) {
			// the port is invalid leave it at 0
		}
		return validatePort(host, portValue);
	}

	public T validatePort(final String host, final int port) throws IOException {
		ValidationResult result;
		try {
			final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
			builder.setHost(host);
			builder.setPort(port);
			builder.validatePort(getValidationLogger());
			result = createResult();
		} catch (final IllegalArgumentException e) {
			result = handleValidationException(e);
		}
		return processResult(result);
	}

	public T validateCredentials(final String host, final String username, final String password) throws IOException {
		ValidationResult result;
		try {
			final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
			builder.setHost(host);
			builder.setUsername(username);
			builder.setPassword(password);
			builder.validateCredentials(getValidationLogger());
			result = createResult();
		} catch (final IllegalArgumentException e) {
			result = handleValidationException(e);
		}
		return processResult(result);
	}

	public T validateIgnoreHosts(final String host, final String ignoreHosts) throws IOException {
		ValidationResult result;
		try {
			final HubProxyInfoBuilder builder = new HubProxyInfoBuilder();
			builder.setHost(host);
			builder.setIgnoredProxyHosts(ignoreHosts);
			builder.validateIgnoreHosts(getValidationLogger());
			result = createResult();
		} catch (final IllegalArgumentException e) {
			result = handleValidationException(e);
		}
		return processResult(result);
	}
}
