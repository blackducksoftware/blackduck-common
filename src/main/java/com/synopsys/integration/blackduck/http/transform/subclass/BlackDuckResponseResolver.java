/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http.transform.subclass;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.manual.view.NotificationUserView;
import com.synopsys.integration.blackduck.api.manual.view.NotificationView;

public class BlackDuckResponseResolver {
    private final Gson gson;
    private final Map<Class<? extends BlackDuckResponse>, BlackDuckResponseSubclassResolver<? extends BlackDuckResponse>> subclassResolvers = new HashMap<>();

    public BlackDuckResponseResolver(Gson gson) {
        this.gson = gson;

        addSubclassResolver(NotificationView.class, new NotificationViewSubclassResolver(gson));
        addSubclassResolver(NotificationUserView.class, new NotificationUserViewSubclassResolver(gson));
    }

    public <T extends BlackDuckResponse> T resolve(JsonElement jsonElement, Class<T> clazz) {
        if (subclassResolvers.containsKey(clazz)) {
            BlackDuckResponseSubclassResolver subclassResolver = subclassResolvers.get(clazz);
            Class<? extends BlackDuckResponse> subclass = subclassResolver.resolveSubclass(jsonElement);
            return (T) gson.fromJson(jsonElement, subclass);
        }

        return gson.fromJson(jsonElement, clazz);
    }

    private <T extends BlackDuckResponse> void addSubclassResolver(Class<T> clazz, BlackDuckResponseSubclassResolver<T> resolver) {
        subclassResolvers.put(clazz, resolver);
    }

}
