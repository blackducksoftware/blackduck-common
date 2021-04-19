package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathSingleResponse;
import com.synopsys.integration.blackduck.api.core.response.LinkMultipleResponses;
import com.synopsys.integration.blackduck.api.core.response.LinkSingleResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckApiExchangeDescriptorFactory {
    private final BlackDuckRequestFactory blackDuckRequestFactory;
    private final HttpUrl blackDuckBaseUrl;

    public BlackDuckApiExchangeDescriptorFactory(BlackDuckRequestFactory blackDuckRequestFactory, HttpUrl blackDuckBaseUrl) {
        this.blackDuckRequestFactory = blackDuckRequestFactory;
        this.blackDuckBaseUrl = blackDuckBaseUrl;
    }

    public <T extends BlackDuckResponse> BlackDuckApiExchangeDescriptorSingle<T> fromBlackDuckView(BlackDuckView blackDuckView, LinkSingleResponse<T> linkSingleResponse) {
        HttpUrl url = blackDuckView.getFirstLink(linkSingleResponse.getLink());
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestFactory.createCommonGetRequestBuilder(url);
        return new BlackDuckApiExchangeDescriptorSingle<>(blackDuckRequestBuilder, linkSingleResponse.getResponseClass());
    }

    public <T extends BlackDuckResponse> BlackDuckApiExchangeDescriptorMultiple<T> fromBlackDuckView(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses) {
        HttpUrl url = blackDuckView.getFirstLink(linkMultipleResponses.getLink());
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestFactory.createCommonGetRequestBuilder(url);
        return new BlackDuckApiExchangeDescriptorMultiple<>(blackDuckRequestBuilder, linkMultipleResponses.getResponseClass());
    }

    public <T extends BlackDuckResponse> BlackDuckApiExchangeDescriptorSingle<T> fromBlackDuckView(BlackDuckView blackDuckView, LinkSingleResponse<T> linkSingleResponse, BlackDuckRequestBuilder blackDuckRequestBuilder) {
        HttpUrl url = blackDuckView.getFirstLink(linkSingleResponse.getLink());
        blackDuckRequestBuilder.url(url);
        return new BlackDuckApiExchangeDescriptorSingle<>(blackDuckRequestBuilder, linkSingleResponse.getResponseClass());
    }

    public <T extends BlackDuckResponse> BlackDuckApiExchangeDescriptorMultiple<T> fromBlackDuckView(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, BlackDuckRequestBuilder blackDuckRequestBuilder) {
        HttpUrl url = blackDuckView.getFirstLink(linkMultipleResponses.getLink());
        blackDuckRequestBuilder.url(url);
        return new BlackDuckApiExchangeDescriptorMultiple<>(blackDuckRequestBuilder, linkMultipleResponses.getResponseClass());
    }

    public <T extends BlackDuckResponse> BlackDuckApiExchangeDescriptorSingle<T> fromBlackDuckPath(BlackDuckPathSingleResponse<T> blackDuckPathSingleResponse) throws IntegrationException {
        HttpUrl url = blackDuckPathSingleResponse.getBlackDuckPath().getFullBlackDuckUrl(blackDuckBaseUrl);
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestFactory.createCommonGetRequestBuilder(url);
        return new BlackDuckApiExchangeDescriptorSingle<>(blackDuckRequestBuilder, blackDuckPathSingleResponse.getResponseClass());
    }

    public <T extends BlackDuckResponse> BlackDuckApiExchangeDescriptorMultiple<T> fromBlackDuckPath(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses) throws IntegrationException {
        HttpUrl url = blackDuckPathMultipleResponses.getBlackDuckPath().getFullBlackDuckUrl(blackDuckBaseUrl);
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestFactory.createCommonGetRequestBuilder(url);
        return new BlackDuckApiExchangeDescriptorMultiple<>(blackDuckRequestBuilder, blackDuckPathMultipleResponses.getResponseClass());
    }

    public <T extends BlackDuckResponse> BlackDuckApiExchangeDescriptorSingle<T> fromBlackDuckPath(BlackDuckPathSingleResponse<T> blackDuckPathSingleResponse, BlackDuckRequestBuilder blackDuckRequestBuilder) throws IntegrationException {
        HttpUrl url = blackDuckPathSingleResponse.getBlackDuckPath().getFullBlackDuckUrl(blackDuckBaseUrl);
        blackDuckRequestBuilder.url(url);
        return new BlackDuckApiExchangeDescriptorSingle<>(blackDuckRequestBuilder, blackDuckPathSingleResponse.getResponseClass());
    }

    public <T extends BlackDuckResponse> BlackDuckApiExchangeDescriptorMultiple<T> fromBlackDuckPath(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, BlackDuckRequestBuilder blackDuckRequestBuilder)
        throws IntegrationException {
        HttpUrl url = blackDuckPathMultipleResponses.getBlackDuckPath().getFullBlackDuckUrl(blackDuckBaseUrl);
        blackDuckRequestBuilder.url(url);
        return new BlackDuckApiExchangeDescriptorMultiple<>(blackDuckRequestBuilder, blackDuckPathMultipleResponses.getResponseClass());
    }

}
