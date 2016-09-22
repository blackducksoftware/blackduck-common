package com.blackducksoftware.integration.hub.api.notification;

import com.blackducksoftware.integration.hub.meta.MetaInformation;

public class RuleViolationClearedNotificationItem extends NotificationItem {
	public RuleViolationClearedNotificationContent content;

	public RuleViolationClearedNotificationItem(final MetaInformation meta) {
		super(meta);
	}

	public RuleViolationClearedNotificationContent getContent() {
		return content;
	}

	public void setContent(final RuleViolationClearedNotificationContent content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "RuleViolationClearedNotificationItem [content=" + content + ", contentType=" + contentType + ", type="
				+ type
				+ ", createdAt=" + createdAt + ", Meta=" + getMeta() + "]";
	}
}
