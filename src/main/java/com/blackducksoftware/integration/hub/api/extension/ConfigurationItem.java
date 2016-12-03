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
package com.blackducksoftware.integration.hub.api.extension;

import java.util.List;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class ConfigurationItem extends HubItem {
    private final String name;

    private final OptionTypeEnum optionType;

    private final String title;

    private final boolean required;

    private final boolean singleValue;

    private final String description;

    private final List<OptionItem> options;

    private final List<String> value;

    public ConfigurationItem(final MetaInformation meta, final String name, final OptionTypeEnum optionType,
            final String title, final boolean required, final boolean singleValue, final String description,
            final List<OptionItem> options, final List<String> value) {
        super(meta);
        this.name = name;
        this.optionType = optionType;
        this.title = title;
        this.required = required;
        this.singleValue = singleValue;
        this.description = description;
        this.options = options;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public OptionTypeEnum getOptionType() {
        return optionType;
    }

    public String getTitle() {
        return title;
    }

    public boolean isRequired() {
        return required;
    }

    public boolean isSingleValue() {
        return singleValue;
    }

    public String getDescription() {
        return description;
    }

    public List<OptionItem> getOptions() {
        return options;
    }

    public List<String> getValue() {
        return value;
    }

}
