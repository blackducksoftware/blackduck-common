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

    public void addFieldToUpdate(final String fieldName, final JsonElement newValue) {
        fieldsToUpdate.put(fieldName, newValue);
    }
}
