package com.blackducksoftware.integration.hub.api.component;

import org.apache.commons.lang3.StringUtils;

public class ComponentQuery {
    private final String id;

    private final String groupId;

    private final String artifactId;

    private final String version;

    public ComponentQuery(final String id, final String groupId, final String artifactId, final String version) {
        this.id = id;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    public String getQuery() {
        final String idSegment = StringUtils.join(new String[] { "id", id }, ':');
        return StringUtils.join(new String[] { idSegment, groupId, artifactId, version }, '|');
    }

}
