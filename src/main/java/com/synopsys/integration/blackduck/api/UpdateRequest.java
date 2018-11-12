package com.synopsys.integration.blackduck.api;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;

public class UpdateRequest {
    public String resourceUrl;
    public Map<String, JsonElement> fieldsToUpdate;

    public UpdateRequest(final String resourceUrl) {
        this.resourceUrl = resourceUrl;
        this.fieldsToUpdate = new HashMap<>();
    }

    public UpdateRequest(final String resourceUrl, final Map<String, JsonElement> fieldsToUpdate) {
        this.resourceUrl = resourceUrl;
        this.fieldsToUpdate = new HashMap<>(fieldsToUpdate);
    }

    public void addFieldToUpdate(final String fieldName, final JsonElement newValue) {
        fieldsToUpdate.put(fieldName, newValue);
    }
}
