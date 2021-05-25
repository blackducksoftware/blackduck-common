/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation;

import java.util.function.Function;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.exception.IntegrationTimeoutException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.wait.WaitJobCompleter;
import com.synopsys.integration.wait.WaitJobConfig;

public class CodeLocationWaitJobCompleter implements WaitJobCompleter<CodeLocationWaitResult> {
    public static final Function<Long, String> ERROR_MESSAGE =
        (timeoutInSeconds) -> String.format("It was not possible to verify the code locations were added to the BOM within the timeout (%ds) provided.", timeoutInSeconds);

    private final IntLogger logger;
    private final CodeLocationWaitJobCondition codeLocationWaitJobCondition;
    private final WaitJobConfig waitJobConfig;

    public CodeLocationWaitJobCompleter(IntLogger logger, CodeLocationWaitJobCondition codeLocationWaitJobCondition, WaitJobConfig waitJobConfig) {
        this.logger = logger;
        this.codeLocationWaitJobCondition = codeLocationWaitJobCondition;
        this.waitJobConfig = waitJobConfig;
    }

    @Override
    public CodeLocationWaitResult complete() throws IntegrationException {
        logger.info("All code locations have been added to the BOM.");
        return CodeLocationWaitResult.COMPLETE(codeLocationWaitJobCondition.getFoundCodeLocationNames());
    }

    @Override
    public CodeLocationWaitResult handleTimeout() throws IntegrationTimeoutException {
        String errorMessage = ERROR_MESSAGE.apply(waitJobConfig.getTimeoutInSeconds());
        return CodeLocationWaitResult.PARTIAL(codeLocationWaitJobCondition.getFoundCodeLocationNames(), errorMessage);
    }

}