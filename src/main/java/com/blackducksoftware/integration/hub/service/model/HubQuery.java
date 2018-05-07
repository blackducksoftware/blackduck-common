package com.blackducksoftware.integration.hub.service.model;

public class HubQuery {
    private final String q;

    public HubQuery(final String parameter) {
        this.q = parameter;
    }

    public String getParameter() {
        return q;
    }

}
