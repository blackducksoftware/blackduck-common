package com.blackducksoftware.integration.hub.notification;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationStateRequestStateType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.notification.content.NotificationContent;
import com.blackducksoftware.integration.hub.notification.content.detail.SimpleNotificationContentDetail;

public class SimpleNotificationResult {
    private final NotificationContent notificationContent;
    private final String contentType;
    private final Date createdAt;
    private final NotificationType type;
    private final String notificationGroup;
    private final String contentDetailKey;
    private final Optional<NotificationStateRequestStateType> notificationState;

    private final List<SimpleNotificationContentDetail> simpleNotificationContentDetails;

    public SimpleNotificationResult(final NotificationContent notificationContent, final String contentType, final Date createdAt, final NotificationType type, final String notificationGroup, final String contentDetailKey,
            final Optional<NotificationStateRequestStateType> notificationState, final List<SimpleNotificationContentDetail> simpleNotificationContentDetails) {
        this.notificationContent = notificationContent;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.type = type;
        this.notificationGroup = notificationGroup;
        this.contentDetailKey = contentDetailKey;
        this.notificationState = notificationState;
        this.simpleNotificationContentDetails = simpleNotificationContentDetails;
    }

    public NotificationContent getNotificationContent() {
        return notificationContent;
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

    public String getNotificationGroup() {
        return notificationGroup;
    }

    public String getContentDetailKey() {
        return contentDetailKey;
    }

    public Optional<NotificationStateRequestStateType> getNotificationState() {
        return notificationState;
    }

    public List<SimpleNotificationContentDetail> getSimpleNotificationContentDetails() {
        return simpleNotificationContentDetails;
    }

}
