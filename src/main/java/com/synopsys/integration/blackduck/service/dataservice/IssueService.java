/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.synopsys.integration.blackduck.api.generated.view.IssueView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionComponentView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionIssuesView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.IssueRequest;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class IssueService extends DataService {
    public IssueService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, IntLogger logger) {
        super(blackDuckApiClient, blackDuckRequestFactory, logger);
    }

    public List<ProjectVersionIssuesView> getIssuesForProjectVersion(ProjectVersionView projectVersionView) throws IntegrationException {
        return blackDuckApiClient.getAllResponses(projectVersionView, ProjectVersionView.ISSUES_LINK_RESPONSE);
    }

    public Optional<IssueView> getIssueByKey(ProjectVersionView projectVersionView, String issueKey) throws IntegrationException {
        Predicate<ProjectVersionIssuesView> issueKeyEquals = (issue -> issue.getIssueId().equals(issueKey));

        List<ProjectVersionIssuesView> bomComponentIssues = blackDuckApiClient.getSomeMatchingResponses(projectVersionView, ProjectVersionView.ISSUES_LINK_RESPONSE, issueKeyEquals, 1);
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

    public void createIssueForComponent(ProjectVersionComponentView projectVersionComponentView, IssueRequest issueRequest) throws IntegrationException {
        HttpUrl createIssueUrl = projectVersionComponentView.getFirstLink(ProjectVersionComponentView.COMPONENT_ISSUES_LINK);
        blackDuckApiClient.post(createIssueUrl, issueRequest);
    }

    public void updateIssue(IssueView issueView) throws IntegrationException {
        blackDuckApiClient.put(issueView);
    }

}
