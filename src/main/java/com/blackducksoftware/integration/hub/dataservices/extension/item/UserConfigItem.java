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
package com.blackducksoftware.integration.hub.dataservices.extension.item;

import java.util.Map;

import com.blackducksoftware.integration.hub.api.extension.ConfigurationItem;
import com.blackducksoftware.integration.hub.api.user.UserItem;

public class UserConfigItem {
	private final UserItem user;
	private final Map<String, ConfigurationItem> configMap;

	public UserConfigItem(final UserItem user, final Map<String, ConfigurationItem> configItems) {
		this.user = user;
		this.configMap = configItems;
	}

	public UserItem getUser() {
		return user;
	}

	public Map<String, ConfigurationItem> getConfigMap() {
		return configMap;
	}
}
