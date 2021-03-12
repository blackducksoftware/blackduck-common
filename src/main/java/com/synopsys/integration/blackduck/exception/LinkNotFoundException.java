/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.exception;

import com.synopsys.integration.exception.IntegrationException;

public class LinkNotFoundException extends IntegrationException {
    public LinkNotFoundException(String message) {
        super(message);
    }
}
