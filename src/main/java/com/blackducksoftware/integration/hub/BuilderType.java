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
package com.blackducksoftware.integration.hub;

import java.util.Arrays;
import java.util.List;

public enum BuilderType {

	MAVEN,

	GRADLE,

	ANT,

	UNKNOWN_BUILDER;

	public static BuilderType getBuilderType(final String builder) {
		BuilderType entityTypeEnum;
		try {
			entityTypeEnum = BuilderType.valueOf(builder.toUpperCase());
		} catch (final IllegalArgumentException e) {
			// ignore expection
			entityTypeEnum = UNKNOWN_BUILDER;
		} catch (final NullPointerException e) {
			// ignore expection
			entityTypeEnum = UNKNOWN_BUILDER;
		}
		return entityTypeEnum;
	}

	public static List<BuilderType> getBuilderTypes() {
		return Arrays.asList(BuilderType.values());
	}

}
