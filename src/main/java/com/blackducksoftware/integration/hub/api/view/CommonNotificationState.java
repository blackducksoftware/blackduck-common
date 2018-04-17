package com.blackducksoftware.integration.hub.api.view;

import java.util.Date;

import com.blackducksoftware.integration.hub.api.core.HubView;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationStateRequestStateType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationUserView;
import com.blackducksoftware.integration.hub.api.generated.view.NotificationView;
import com.blackducksoftware.integration.hub.notification.content.NotificationContent;
import com.blackducksoftware.integration.util.Stringable;

/**
 * This is a flattened view of both NotificationView and NotificationUserView and must be manually maintained to support both views and their api. The only common class between both views is HubView which is why sourceView is of that type,
 * but it should only ever be either NotificationView or NotificationUserView.
 */
public class CommonNotificationState extends Stringable {
    private final HubView sourceView;
    private final String contentType;
    private final Date createdAt;
    private final NotificationType type;
    private final NotificationContent content;
    public NotificationStateRequestStateType notificationState;

    public CommonNotificationState(final NotificationView notificationView, final NotificationContent content) {
        this.sourceView = notificationView;
        this.contentType = notificationView.contentType;
        this.createdAt = notificationView.createdAt;
        this.type = notificationView.type;
        this.content = content;
    }

    public CommonNotificationState(final NotificationUserView notificationUserView, final NotificationContent content) {
        this.sourceView = notificationUserView;
        this.contentType = notificationUserView.contentType;
        this.createdAt = notificationUserView.createdAt;
        this.type = notificationUserView.type;
        this.content = content;
        this.notificationState = notificationUserView.notificationState;
    }

    public HubView getSourceView() {
        return sourceView;
    }

    public String getContentType() {
        return contentType;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public NotificationType getType() {
        return type;
    }

    public NotificationContent getContent() {
        return content;
    }

}
