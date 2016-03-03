package com.blackducksoftware.integration.hub.policy.api;

import java.util.List;

public class PolicyMeta {
    private final String allow;

    private final String href;

    private final List<String> links;

    public PolicyMeta(String allow, String href, List<String> links) {
        this.allow = allow;
        this.href = href;
        this.links = links;
    }

    public String getAllow() {
        return allow;
    }

    public String getHref() {
        return href;
    }

    public List<String> getLinks() {
        return links;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((allow == null) ? 0 : allow.hashCode());
        result = prime * result + ((href == null) ? 0 : href.hashCode());
        result = prime * result + ((links == null) ? 0 : links.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof PolicyMeta)) {
            return false;
        }
        PolicyMeta other = (PolicyMeta) obj;
        if (allow == null) {
            if (other.allow != null) {
                return false;
            }
        } else if (!allow.equals(other.allow)) {
            return false;
        }
        if (href == null) {
            if (other.href != null) {
                return false;
            }
        } else if (!href.equals(other.href)) {
            return false;
        }
        if (links == null) {
            if (other.links != null) {
                return false;
            }
        } else if (!links.equals(other.links)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PolicyMeta [allow=");
        builder.append(allow);
        builder.append(", href=");
        builder.append(href);
        builder.append(", links=");
        builder.append(links);
        builder.append("]");
        return builder.toString();
    }

}
