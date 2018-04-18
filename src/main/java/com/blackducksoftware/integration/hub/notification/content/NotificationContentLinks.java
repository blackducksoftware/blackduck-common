package com.blackducksoftware.integration.hub.notification.content;

/**
 * For Notification types [] there are 1 or more related project/version and component/version links
 */
public class NotificationContentLinks {
    private final String projectVersionLink;
    private final String componentLink;
    private final String componentVersionLink;

    public static NotificationContentLinks createLinksWithComponentOnly(final String projectVersionLink, final String componentLink) {
        return new NotificationContentLinks(projectVersionLink, componentLink, null);
    }

    public static NotificationContentLinks createLinksWithComponentVersion(final String projectVersionLink, final String componentVersionLink) {
        return new NotificationContentLinks(projectVersionLink, null, componentVersionLink);
    }

    private NotificationContentLinks(final String projectVersionLink, final String componentLink, final String componentVersionLink) {
        this.projectVersionLink = projectVersionLink;
        this.componentLink = componentLink;
        this.componentVersionLink = componentVersionLink;
    }

    public boolean hasComponentVersion() {
        return componentVersionLink != null;
    }

    public boolean hasOnlyComponent() {
        return componentLink != null;
    }

    public String getProjectVersionLink() {
        return projectVersionLink;
    }

    public String getComponentLink() {
        return componentLink;
    }

    public String getComponentVersionLink() {
        return componentVersionLink;
    }

}
