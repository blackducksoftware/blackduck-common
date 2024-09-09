/*
 * blackduck-common
 *
 * Copyright (c) 2024 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.codelocation;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.exception.IntegrationException;

public class CodeLocationsRetriever {
    /*
    ejk - This is needed because codelocations are being created and retrieved
    at the same time. Here's an excellent summary from sbillings:

    "The results come back from Black Duck *sorted* by codelocation name, but of
    course the codelocations are being created and mapped by Black Duck in
    random order. So as new ones get added, they don't get added at the end, but
    at random positions in the sorted list bumping some codelocations out of
    their old positions. By the time the second GET happens, a codelocation that
    was in page 1 when the first GET happened could have been bumped to page 2,
    providing a duplicate across the two GETs. It would be very rare, possibly
    explaining why this is the first time we've seen this."
     */
    private static final BinaryOperator<String> COLLAPSE_DUPLICATES = (s1, s2) -> s1;

    private final BlackDuckApiClient blackDuckApiClient;

    public CodeLocationsRetriever(BlackDuckApiClient blackDuckApiClient) {
        this.blackDuckApiClient = blackDuckApiClient;
    }

    public Map<String, String> retrieveCodeLocations(ProjectVersionView projectVersionView, Set<String> codeLocationNames) throws IntegrationException {
        List<CodeLocationView> codeLocationViews = blackDuckApiClient.getAllResponses(projectVersionView.metaCodelocationsLink());
        return codeLocationViews
                   .stream()
                   .filter(codeLocationView -> codeLocationNames.contains(codeLocationView.getName()))
                   .collect(Collectors.toMap(codeLocationView -> codeLocationView.getHref().string(), CodeLocationView::getName, (href1, href2) -> href1));
    }

}
