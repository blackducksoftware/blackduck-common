/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation;

import com.blackduck.integration.util.NameVersion;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

public abstract class CodeLocationBatchOutput<T extends CodeLocationOutput> implements Iterable<T> {
    private final Map<String, Integer> successfulCodeLocationNamesToExpectedNotificationCounts;
    private final List<T> outputs = new ArrayList<>();

    public CodeLocationBatchOutput(List<T> outputs) {
        successfulCodeLocationNamesToExpectedNotificationCounts =
            outputs
                .stream()
                .peek(this.outputs::add)
                .filter(output -> Result.SUCCESS == output.getResult())
                .filter(output -> StringUtils.isNotBlank(output.getCodeLocationName()))
                .collect(Collectors.toMap(CodeLocationOutput::getCodeLocationName, CodeLocationOutput::getExpectedNotificationCount));
    }

    public List<T> getOutputs() {
        return outputs;
    }

    public Optional<NameVersion> getProjectAndVersion() {
        return outputs
                   .stream()
                   .map(CodeLocationOutput::getProjectAndVersion)
                   .filter(Optional::isPresent)
                   .map(Optional::get)
                   .findFirst();
    }

    public Set<String> getSuccessfulCodeLocationNames() {
        return successfulCodeLocationNamesToExpectedNotificationCounts.keySet();
    }

    public int getExpectedNotificationCount() {
        return successfulCodeLocationNamesToExpectedNotificationCounts
                   .values()
                   .stream()
                   .mapToInt(Integer::intValue)
                   .sum();
    }

    public boolean hasAnyFailures() {
        return outputs
                   .stream()
                   .map(CodeLocationOutput::getResult)
                   .anyMatch(Result.FAILURE::equals);
    }

    @Override
    public Iterator<T> iterator() {
        return outputs.iterator();
    }

}
