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

import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfigBuilder;

public abstract class HubServerConfigValidator<T> extends AbstractValidator<T> {

	public T validateServerUrl(final String serverUrl) throws IOException {
		ValidationResult result;
		try {
			final HubServerConfigBuilder builder = new HubServerConfigBuilder();
			builder.setHubUrl(serverUrl);
			builder.validateHubUrl(getValidationLogger());
			result = createResult();
		} catch (final IllegalArgumentException e) {
			result = handleValidationException(e);
		}
		return processResult(result);
	}

	public T validateServerUrl(final String serverUrl, final HubProxyInfo proxyInfo) throws IOException {
		ValidationResult result;
		try {
			final HubServerConfigBuilder builder = new HubServerConfigBuilder();
			builder.setHubUrl(serverUrl);
			builder.setProxyInfo(proxyInfo);
			builder.validateHubUrl(getValidationLogger());
			result = createResult();
		} catch (final IllegalArgumentException e) {
			result = handleValidationException(e);
		}
		return processResult(result);
	}

	public T validateTimeout(final String timeout) throws IOException {
		ValidationResult result;
		try {
			final HubServerConfigBuilder builder = new HubServerConfigBuilder();
			builder.setTimeout(timeout);
			builder.validateTimeout(getValidationLogger());
			result = createResult();
		} catch (final IllegalArgumentException e) {
			result = handleValidationException(e);
		}
		return processResult(result);
	}
}
