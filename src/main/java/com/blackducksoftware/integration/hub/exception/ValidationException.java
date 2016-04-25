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
package com.blackducksoftware.integration.hub.exception;

import com.blackducksoftware.integration.hub.ValidationExceptionEnum;

public class ValidationException extends RuntimeException {
	private static final long serialVersionUID = 9001308081326471943L;

	private final ValidationExceptionEnum validationMessageType;

	public ValidationException(final ValidationExceptionEnum validationMessageType) {
		this.validationMessageType = validationMessageType;
	}

	public ValidationException(final ValidationExceptionEnum validationMessageType, final String message) {
		super(message);
		this.validationMessageType = validationMessageType;
	}

	public ValidationException(final ValidationExceptionEnum validationMessageType, final Throwable cause) {
		super(cause);
		this.validationMessageType = validationMessageType;
	}

	public ValidationException(final ValidationExceptionEnum validationMessageType, final String message,
			final Throwable cause) {
		super(message, cause);
		this.validationMessageType = validationMessageType;
	}

	public ValidationExceptionEnum getValidationMessage() {
		return validationMessageType;
	}

}
