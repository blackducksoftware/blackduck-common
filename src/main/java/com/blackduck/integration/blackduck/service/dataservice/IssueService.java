/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.dataservice;

import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.generated.view.*;
import com.blackduck.integration.blackduck.api.manual.temporary.component.IssueRequest;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.DataService;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpUrl;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class IssueService extends DataService {
    public IssueService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger) {
        super(blackDuckApiClient, apiDiscovery, logger);
    }

    public List<ProjectVersionIssuesView> getIssuesForProjectVersion(ProjectVersionView projectVersionView) throws IntegrationException {
        return blackDuckApiClient.getAllResponses(projectVersionView.metaIssuesLink());
    }

    public Optional<IssueView> getIssueByKey(ProjectVersionView projectVersionView, String issueKey) throws IntegrationException {
        Predicate<ProjectVersionIssuesView> issueKeyEquals = (issue -> issue.getIssueId().equals(issueKey));

        List<ProjectVersionIssuesView> bomComponentIssues = blackDuckApiClient.getSomeMatchingResponses(projectVersionView.metaIssuesLink(), issueKeyEquals, 1);
        Optional<ProjectVersionIssuesView> projectVersionIssuesViewOptional = bomComponentIssues.stream().findAny();

        if (projectVersionIssuesViewOptional.isPresent()) {
            return Optional.ofNullable(getIssueView(projectVersionIssuesViewOptional.get()));
        }

        return Optional.empty();
    }

    public IssueView getIssueView(ProjectVersionIssuesView projectVersionIssuesView) throws IntegrationException {
        HttpUrl issueUrl = projectVersionIssuesView.getHref();
        return blackDuckApiClient.getResponse(issueUrl, IssueView.class);
    }

    public void createIssueForComponent(ProjectVersionComponentVersionView projectVersionComponentView, IssueRequest issueRequest) throws IntegrationException {
        HttpUrl createIssueUrl = projectVersionComponentView.getFirstLink(ProjectVersionComponentView.COMPONENT_ISSUES_LINK);
        blackDuckApiClient.post(createIssueUrl, issueRequest);
    }

    public void updateIssue(IssueView issueView) throws IntegrationException {
        blackDuckApiClient.put(issueView);
    }

}
