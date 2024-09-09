/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation;

import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;

public abstract class CodeLocationCreationRequest<T extends CodeLocationBatchOutput> {
    public abstract T executeRequest() throws BlackDuckIntegrationException;

}
