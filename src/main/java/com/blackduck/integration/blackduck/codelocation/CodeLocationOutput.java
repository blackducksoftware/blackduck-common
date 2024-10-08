/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation;

import com.blackduck.integration.util.NameVersion;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public abstract class CodeLocationOutput {
    private final Result result;
    @Nullable
    private final NameVersion projectAndVersion;
    private final String codeLocationName;
    private final int expectedNotificationCount;
    private final String errorMessage;
    private final Exception exception;

    public CodeLocationOutput(Result result, @Nullable NameVersion projectAndVersion, String codeLocationName, int expectedNotificationCount, String errorMessage, Exception exception) {
        this.result = result;
        this.projectAndVersion = projectAndVersion;
        this.codeLocationName = codeLocationName;
        this.expectedNotificationCount = expectedNotificationCount;
        this.errorMessage = errorMessage;
        this.exception = exception;
    }

    public Result getResult() {
        return result;
    }

    public Optional<NameVersion> getProjectAndVersion() {
        return Optional.ofNullable(projectAndVersion);
    }

    public String getCodeLocationName() {
        return codeLocationName;
    }

    public int getExpectedNotificationCount() {
        return expectedNotificationCount;
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

}
