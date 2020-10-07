package com.synopsys.integration.blackduck.useragent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

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
