package com.blackducksoftware.integration.hub.notification.content;

public interface NotificationContentVariety {

    boolean hasComponentVersion();

    boolean hasOnlyComponent();

    boolean hasPolicy();

    boolean hasVulnerability();

}
