/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.http;

public class BlackDuckQuery {
    private final String q;

    public BlackDuckQuery(String prefix, String parameter) {
        this(prefix + ":" + parameter);
    }

    public BlackDuckQuery(String parameter) {
        q = parameter;
    }

    public String getParameter() {
        return q;
    }

}
