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
package com.blackducksoftware.integration.hub.builder;

public class ValidationResult {
	private final ValidationResultEnum resultType;
	private final String message;
	private final Throwable throwable;

	public ValidationResult(final ValidationResultEnum resultType, final String message) {
		this.resultType = resultType;
		this.message = message;
		this.throwable = null;
	}

	public ValidationResult(final ValidationResultEnum resultType, final String message, final Throwable throwable) {
		this.resultType = resultType;
		this.message = message;
		this.throwable = throwable;
	}

	public ValidationResultEnum getResultType() {
		return resultType;
	}

	public String getMessage() {
		return message;
	}

	public Throwable getThrowable() {
		return throwable;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
		result = prime * result + ((throwable == null) ? 0 : throwable.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ValidationResult)) {
			return false;
		}
		final ValidationResult other = (ValidationResult) obj;
		if (message == null) {
			if (other.message != null) {
				return false;
			}
		} else if (!message.equals(other.message)) {
			return false;
		}
		if (resultType != other.resultType) {
			return false;
		}
		if (throwable == null) {
			if (other.throwable != null) {
				return false;
			}
		} else if (!throwable.equals(other.throwable)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		builder.append("ValidationResult [resultType=");
		builder.append(resultType);
		builder.append(", message=");
		builder.append(message);
		builder.append(", throwable=");
		builder.append(throwable);
		builder.append("]");
		return builder.toString();
	}

}