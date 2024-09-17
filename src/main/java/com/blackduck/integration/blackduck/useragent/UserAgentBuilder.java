/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.useragent;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UserAgentBuilder {
    private List<UserAgentItem> userAgentItems = new ArrayList<>();

    public void addUserAgent(UserAgentItem userAgentItem) {
        if (null == userAgentItem || null == userAgentItem.getProduct() || StringUtils.isBlank(userAgentItem.getProduct().getName())) {
            return;
        }
        userAgentItems.add(userAgentItem);
    }

    public String createFullUserAgentString() {
        return userAgentItems
            .stream()
            .map(UserAgentItem::createUserAgentString)
            .collect(Collectors.joining(" "));
    }

}
