/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.http.transform.subclass;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;

public abstract class BlackDuckResponseSubclassResolver<T extends BlackDuckResponse> {
    protected Gson gson;
    protected Class<T> parentClass;

    public BlackDuckResponseSubclassResolver(Gson gson, Class<T> parentClass) {
        this.gson = gson;
        this.parentClass = parentClass;
    }

    public abstract Class<? extends BlackDuckResponse> resolveSubclass(JsonElement jsonElement);

}
