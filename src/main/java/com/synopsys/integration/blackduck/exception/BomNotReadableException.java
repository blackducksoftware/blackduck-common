/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.exception;

import com.synopsys.integration.exception.IntegrationException;

public class BomNotReadableException extends IntegrationException {
    public BomNotReadableException() {
        super("BOM could not be read.  This is likely because you lack sufficient permissions.  Please check your permissions.");
    }
}
