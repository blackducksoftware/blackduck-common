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
package com.blackducksoftware.integration.hub.api.user;

import java.util.UUID;

import com.blackducksoftware.integration.hub.api.item.HubItem;
import com.blackducksoftware.integration.hub.exception.MissingUUIDException;
import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.util.HubUrlParser;

public class UserItem extends HubItem {
    public static final String USER_URL_IDENTIFIER = "users";

    private final String userName;

    private final String firstName;

    private final String lastName;

    private final String email;

    private final UserType type;

    private final boolean active;

    public UserItem(final MetaInformation meta, final String userName, final String firstName, final String lastName,
            final String email, final UserType type, final boolean active) {
        super(meta);

        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.type = type;
        this.active = active;
    }

    public String getUserName() {
        return userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public UserType getType() {
        return type;
    }

    public boolean isActive() {
        return active;
    }

    @Deprecated
    public UUID getUserId() throws MissingUUIDException {
        if (getMeta() == null || getMeta().getHref() == null) {
            return null;
        }
        return HubUrlParser.getUUIDFromURLString(USER_URL_IDENTIFIER, getMeta().getHref());
    }

    @Override
    public String toString() {
        return "UserItem [userName=" + userName + ", firstName=" + firstName + ", lastName=" + lastName + ", email="
                + email + ", type=" + type + ", active=" + active + "]";
    }

}
