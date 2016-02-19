package com.blackducksoftware.integration.hub.report.api;

import java.util.UUID;

public class UserData {

    private final UUID id;

    private final String username;

    public UserData(UUID id,
            String username) {
        this.id = id;
        this.username = username;
    }

    public UUID getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

}
