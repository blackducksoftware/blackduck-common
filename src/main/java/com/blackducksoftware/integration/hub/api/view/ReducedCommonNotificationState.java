package com.blackducksoftware.integration.hub.api.view;

import com.blackducksoftware.integration.hub.api.generated.enumeration.NotificationType;

public interface ReducedCommonNotificationState {
    public String getContentType();

    public java.util.Date getCreatedAt();

    public NotificationType getType();

    public String getJson();

}
