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

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractBuilder<Key, Type> {

	private final boolean shouldUseDefaultValues;

	public AbstractBuilder(final boolean shouldUseDefaultValues) {
		this.shouldUseDefaultValues = shouldUseDefaultValues;
	}

	public abstract ValidationResults<Key, Type> build();

	public abstract ValidationResults<Key, Type> assertValid();

	protected int stringToInteger(final String integer) throws IllegalArgumentException {
		final String integerString = StringUtils.trimToNull(integer);
		if (integerString != null) {
			try {
				return Integer.valueOf(integerString);
			} catch (final NumberFormatException e) {
				throw new IllegalArgumentException("The String : " + integer + " , is not an Integer.", e);
			}
		} else {
			throw new IllegalArgumentException("The String : " + integer + " , is not an Integer.");
		}
	}


	public boolean shouldUseDefaultValues() {
		return shouldUseDefaultValues;
	}
}
