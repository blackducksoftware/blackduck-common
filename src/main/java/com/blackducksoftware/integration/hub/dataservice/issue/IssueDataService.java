package com.blackducksoftware.integration.hub.dataservice.issue;

import java.io.IOException;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.generated.view.IssueView;
import com.blackducksoftware.integration.hub.request.Request;
import com.blackducksoftware.integration.hub.request.Response;
import com.blackducksoftware.integration.hub.rest.HttpMethod;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;

public class IssueDataService extends HubService {

    public IssueDataService(final RestConnection restConnection) {
        super(restConnection);
    }

    public String createIssue(final IssueView issueItem, final String uri) throws IntegrationException {
        final Request request = getHubRequestFactory().createRequest(uri, HttpMethod.POST);
        request.setBodyContent(getGson().toJson(issueItem));
        try (Response response = getRestConnection().executeRequest(request)) {
            return response.getHeaderValue("location");
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public void updateIssue(final IssueView issueItem, final String uri) throws IntegrationException {
        final Request request = getHubRequestFactory().createRequest(uri, HttpMethod.PUT);
        request.setBodyContent(getGson().toJson(issueItem));
        try (Response response = getRestConnection().executeRequest(request)) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public void deleteIssue(final IssueView issueItem) throws IntegrationException {
        final String codeLocationItemUrl = getHref(issueItem);
        deleteIssue(codeLocationItemUrl);
    }

    public void deleteIssue(final String issueItemUri) throws IntegrationException {
        final Request request = getHubRequestFactory().createRequest(issueItemUri, HttpMethod.DELETE);
        try (Response response = getRestConnection().executeRequest(request)) {
        } catch (final IOException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

}
