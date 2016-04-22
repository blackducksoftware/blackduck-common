/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
package com.blackducksoftware.integration.hub.job;

import java.io.IOException;

import com.blackducksoftware.integration.hub.exception.ValidationException;
import com.blackducksoftware.integration.hub.logging.IntExceptionLogger;

public abstract class HubScanJobConfigValidator<T> {
	public abstract T handleValidationException(ValidationException e);

	public abstract T handleSuccess();

	public T validateScanMemory(final String scanMemory) throws IOException {
		try {
			final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();
			builder.setScanMemory(scanMemory);
			builder.validateScanMemory(IntExceptionLogger.LOGGER);
		} catch (final ValidationException e) {
			return handleValidationException(e);
		}

		return handleSuccess();
	}

	public T validateMaxWaitTimeForBomUpdate(final String bomUpdateMaxiumWaitTime) throws IOException {
		try {
			final HubScanJobConfigBuilder builder = new HubScanJobConfigBuilder();
			builder.setMaxWaitTimeForBomUpdate(bomUpdateMaxiumWaitTime);
			builder.validateMaxWaitTimeForBomUpdate(IntExceptionLogger.LOGGER);
		} catch (final ValidationException e) {
			return handleValidationException(e);
		}

		return handleSuccess();
	}

}
