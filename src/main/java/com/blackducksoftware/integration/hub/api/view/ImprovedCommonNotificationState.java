package com.blackducksoftware.integration.hub.api.view;

import com.blackducksoftware.integration.hub.notification.content.NotificationContent;

public interface ImprovedCommonNotificationState extends ReducedCommonNotificationState {
    public NotificationContent getNotificationContent();

}
