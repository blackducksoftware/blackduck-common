package com.blackducksoftware.integration.hub.api.view;

import com.blackducksoftware.integration.hub.api.core.HubView;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationStateRequestStateType;
import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;

public class ReducedUserNotificationView extends HubView {
    public String contentType;
    public java.util.Date createdAt;
    public NotificationStateRequestStateType notificationState;
    public NotificationType type;

}
