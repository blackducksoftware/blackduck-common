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

import com.blackducksoftware.integration.hub.job.HubScanJobConfigBuilder;

public abstract class HubScanJobConfigValidator<T> extends AbstractValidator<T> {

	public T validateScanMemory(final String scanMemory) throws IOException {
		ValidationResult result;
		try {
			final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();
			builder.setScanMemory(scanMemory);
			builder.validateScanMemory(getValidationLogger());
			result = createResult();
		} catch (final IllegalArgumentException e) {
			result = handleValidationException(e);
		}
		return processResult(result);
	}

	public T validateMaxWaitTimeForBomUpdate(final String bomUpdateMaxiumWaitTime) throws IOException {
		ValidationResult result;
		try {
			final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();
			builder.setMaxWaitTimeForBomUpdate(bomUpdateMaxiumWaitTime);
			builder.validateMaxWaitTimeForBomUpdate(getValidationLogger());
			result = createResult();
		} catch (final IllegalArgumentException e) {
			result = handleValidationException(e);
		}
		return processResult(result);
	}
}
