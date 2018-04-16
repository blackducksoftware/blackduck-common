package com.blackducksoftware.integration.hub.api.view;

import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;
import com.blackducksoftware.integration.hub.notification.content.NotificationContent;

public interface CommonNotificationState {
    public NotificationContent getNotificationContent();

    public String getContentType();

    public java.util.Date getCreatedAt();

    public NotificationType getType();

    public String getJson();

    public void setNotificationContent(NotificationContent notificationContent);

    public void setContentType(String contentType);

    public void setCreatedAt(java.util.Date createdAt);

    public void setType(NotificationType type);

    public void setJson(String json);

}
