package com.blackducksoftware.integration.hub.notification.content;

import java.util.List;

import com.blackducksoftware.integration.hub.api.core.HubComponent;

public abstract class NotificationContent extends HubComponent {
    public abstract boolean providesProjectComponentDetails();

    public abstract List<NotificationContentLinks> getNotificationContentLinks();

}
