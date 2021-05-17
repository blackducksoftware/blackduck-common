/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http;

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
