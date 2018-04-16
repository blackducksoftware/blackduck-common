package com.blackducksoftware.integration.hub.api.view;

import java.util.Date;

import com.blackducksoftware.integration.hub.api.core.HubView;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationStateRequestStateType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;

public class ReducedUserNotificationView extends HubView implements ReducedCommonNotificationState {
    public String contentType;
    public java.util.Date createdAt;
    public NotificationStateRequestStateType notificationState;
    public NotificationType type;

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Override
    public NotificationType getType() {
        return type;
    }

    @Override
    public String getJson() {
        return json;
    }

    @Override
    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    @Override
    public void setCreatedAt(final Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void setType(final NotificationType type) {
        this.type = type;
    }

    @Override
    public void setJson(final String json) {
        this.json = json;
    }

}
