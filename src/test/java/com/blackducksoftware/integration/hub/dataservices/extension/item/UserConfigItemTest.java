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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.extension.ConfigurationItem;
import com.blackducksoftware.integration.hub.api.extension.OptionItem;
import com.blackducksoftware.integration.hub.api.extension.OptionTypeEnum;
import com.blackducksoftware.integration.hub.api.user.UserItem;
import com.blackducksoftware.integration.hub.api.user.UserType;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class UserConfigItemTest {

    private UserItem createUserItem() {
        final String id = UUID.randomUUID().toString();
        final MetaInformation meta = new MetaInformation(null, "http://localhost/api/users/" + id, null);
        return new UserItem(meta, "username", "firstName", "lastName", "user@blackducksoftware.com", UserType.INTERNAL,
                true);
    }

    private Map<String, ConfigurationItem> createConfigItemMap() {
        final MetaInformation meta = new MetaInformation(null, null, null);
        List<String> valueList = new ArrayList<>();
        valueList.add("a value");
        final List<OptionItem> options = new ArrayList<>();
        final ConfigurationItem item1 = new ConfigurationItem(meta, "item1", OptionTypeEnum.STRING, "itemTitle", true,
                true, "a description", options, valueList);
        valueList = new ArrayList<>();
        valueList.add("another value");
        final ConfigurationItem item2 = new ConfigurationItem(meta, "item2", OptionTypeEnum.STRING, "itemTitle", true,
                true, "another description", options, valueList);
        final Map<String, ConfigurationItem> configMap = new HashMap<>();
        configMap.put(item1.getName(), item1);
        configMap.put(item2.getName(), item2);
        return configMap;
    }

    @Test
    public void testConstructor() {
        final UserItem user = createUserItem();
        final Map<String, ConfigurationItem> configItemMap = createConfigItemMap();
        final UserConfigItem item = new UserConfigItem(user, configItemMap);

        assertNotNull(item);
        assertEquals(user, item.getUser());
        assertEquals(configItemMap, item.getConfigMap());
    }
}
