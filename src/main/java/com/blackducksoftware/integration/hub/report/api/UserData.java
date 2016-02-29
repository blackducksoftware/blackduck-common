package com.blackducksoftware.integration.hub.report.api;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

public class UserData {

    private final String id;

    private final String username;

    public UserData(String id,
            String username) {
        this.id = id;
        this.username = username;
    }

    public String getId() {
        return id;
    }

    public UUID getUUId() {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        try {
            return UUID.fromString(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public String getUsername() {
        return username;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((username == null) ? 0 : username.hashCode());
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
        if (!(obj instanceof UserData)) {
            return false;
        }
        UserData other = (UserData) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        if (username == null) {
            if (other.username != null) {
                return false;
            }
        } else if (!username.equals(other.username)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("UserData [id=");
        builder.append(id);
        builder.append(", username=");
        builder.append(username);
        builder.append("]");
        return builder.toString();
    }

}
