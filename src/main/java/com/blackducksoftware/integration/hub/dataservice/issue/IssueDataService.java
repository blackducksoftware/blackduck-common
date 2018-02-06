package com.blackducksoftware.integration.hub.dataservice.issue;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.IssueView;
import com.blackducksoftware.integration.hub.request.HubRequest;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;

import okhttp3.Response;

public class IssueDataService extends HubService {

    public IssueDataService(final RestConnection restConnection) {
        super(restConnection);
    }

    public String createIssue(final IssueView issueItem, final String url) throws IntegrationException {
        final HubRequest request = getHubRequestFactory().createRequest(url);
        try (Response response = request.executePost(getGson().toJson(issueItem))) {
            return response.header("location");
        }
    }

    public void updateIssue(final IssueView issueItem, final String url) throws IntegrationException {
        final HubRequest request = getHubRequestFactory().createRequest(url);
        try (Response response = request.executePut(getGson().toJson(issueItem))) {
        }
    }

    public void deleteIssue(final IssueView issueItem) throws IntegrationException {
        final String codeLocationItemUrl = getHref(issueItem);
        deleteIssue(codeLocationItemUrl);
    }

    public void deleteIssue(final String issueItemUrl) throws IntegrationException {
        final HubRequest request = getHubRequestFactory().createRequest(issueItemUrl);
        request.executeDelete();
    }

}
