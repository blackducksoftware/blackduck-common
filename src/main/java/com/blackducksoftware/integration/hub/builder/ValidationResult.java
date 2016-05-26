/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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