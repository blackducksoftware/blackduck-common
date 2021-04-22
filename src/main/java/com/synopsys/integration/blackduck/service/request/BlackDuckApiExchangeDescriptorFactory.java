package com.synopsys.integration.blackduck.service.request;

import java.util.function.BiFunction;

import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.core.response.ApiResponse;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathResponse;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathSingleResponse;
import com.synopsys.integration.blackduck.api.core.response.LinkMultipleResponses;
import com.synopsys.integration.blackduck.api.core.response.LinkResponse;
import com.synopsys.integration.blackduck.api.core.response.LinkSingleResponse;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilderFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class BlackDuckApiExchangeDescriptorFactory {
    private final BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory;

    public BlackDuckApiExchangeDescriptorFactory(BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory) {
        this.blackDuckRequestBuilderFactory = blackDuckRequestBuilderFactory;
    }

    public <T extends BlackDuckResponse> BlackDuckApiExchangeDescriptorSingle<T> fromBlackDuckView(BlackDuckView blackDuckView, LinkSingleResponse<T> linkSingleResponse) {
        return descriptorFromView(blackDuckView, linkSingleResponse, BlackDuckApiExchangeDescriptorSingle::new);
    }

    public <T extends BlackDuckResponse> BlackDuckApiExchangeDescriptorMultiple<T> fromBlackDuckView(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses) {
        return descriptorFromView(blackDuckView, linkMultipleResponses, BlackDuckApiExchangeDescriptorMultiple::new);
    }

    public <T extends BlackDuckResponse> BlackDuckApiExchangeDescriptorSingle<T> fromBlackDuckView(BlackDuckView blackDuckView, LinkSingleResponse<T> linkSingleResponse, BlackDuckRequestBuilder blackDuckRequestBuilder) {
        return descriptorFromViewAndBuilder(blackDuckView, linkSingleResponse, blackDuckRequestBuilder, BlackDuckApiExchangeDescriptorSingle::new);
    }

    public <T extends BlackDuckResponse> BlackDuckApiExchangeDescriptorMultiple<T> fromBlackDuckView(BlackDuckView blackDuckView, LinkMultipleResponses<T> linkMultipleResponses, BlackDuckRequestBuilder blackDuckRequestBuilder) {
        return descriptorFromViewAndBuilder(blackDuckView, linkMultipleResponses, blackDuckRequestBuilder, BlackDuckApiExchangeDescriptorMultiple::new);
    }

    public <T extends BlackDuckResponse> BlackDuckApiExchangeDescriptorSingle<T> fromBlackDuckPath(BlackDuckPathSingleResponse<T> blackDuckPathSingleResponse) throws IntegrationException {
        return descriptorFromPath(blackDuckPathSingleResponse, BlackDuckApiExchangeDescriptorSingle::new);
    }

    public <T extends BlackDuckResponse> BlackDuckApiExchangeDescriptorMultiple<T> fromBlackDuckPath(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses) throws IntegrationException {
        return descriptorFromPath(blackDuckPathMultipleResponses, BlackDuckApiExchangeDescriptorMultiple::new);
    }

    public <T extends BlackDuckResponse> BlackDuckApiExchangeDescriptorSingle<T> fromBlackDuckPath(BlackDuckPathSingleResponse<T> blackDuckPathSingleResponse, BlackDuckRequestBuilder blackDuckRequestBuilder) throws IntegrationException {
        return descriptorFromPathAndBuilder(blackDuckPathSingleResponse, blackDuckRequestBuilder, BlackDuckApiExchangeDescriptorSingle::new);
    }

    public <T extends BlackDuckResponse> BlackDuckApiExchangeDescriptorMultiple<T> fromBlackDuckPath(BlackDuckPathMultipleResponses<T> blackDuckPathMultipleResponses, BlackDuckRequestBuilder blackDuckRequestBuilder) throws IntegrationException {
        return descriptorFromPathAndBuilder(blackDuckPathMultipleResponses, blackDuckRequestBuilder, BlackDuckApiExchangeDescriptorMultiple::new);
    }

    private <T extends BlackDuckResponse, D extends BlackDuckApiExchangeDescriptor<T>> D descriptorFromView(BlackDuckView blackDuckView, LinkResponse<T> linkResponse, BiFunction<BlackDuckRequestBuilder, Class<T>, D> descriptorCreator) {
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestBuilderFactory.createBlackDuckRequestBuilder();
        return descriptorFromViewAndBuilder(blackDuckView, linkResponse, blackDuckRequestBuilder, descriptorCreator);
    }

    private <T extends BlackDuckResponse, D extends BlackDuckApiExchangeDescriptor<T>> D descriptorFromViewAndBuilder(BlackDuckView blackDuckView, LinkResponse<T> linkResponse, BlackDuckRequestBuilder blackDuckRequestBuilder, BiFunction<BlackDuckRequestBuilder, Class<T>, D> descriptorCreator) {
        HttpUrl url = blackDuckView.getFirstLink(linkResponse.getLink());
        blackDuckRequestBuilder.url(url);
        return descriptor(blackDuckRequestBuilder, linkResponse, descriptorCreator);
    }

    private <T extends BlackDuckResponse, D extends BlackDuckApiExchangeDescriptor<T>> D descriptorFromPath(BlackDuckPathResponse<T> blackDuckPathResponse, BiFunction<BlackDuckRequestBuilder, Class<T>, D> descriptorCreator)
        throws IntegrationException {
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestBuilderFactory.createBlackDuckRequestBuilder();
        return descriptorFromPathAndBuilder(blackDuckPathResponse, blackDuckRequestBuilder, descriptorCreator);
    }

    private <T extends BlackDuckResponse, D extends BlackDuckApiExchangeDescriptor<T>> D descriptorFromPathAndBuilder(BlackDuckPathResponse<T> blackDuckPathResponse, BlackDuckRequestBuilder blackDuckRequestBuilder, BiFunction<BlackDuckRequestBuilder, Class<T>, D> descriptorCreator)
        throws IntegrationException {
        BlackDuckPath blackDuckPath = blackDuckPathResponse.getBlackDuckPath();
        blackDuckRequestBuilderFactory.populateUrl(blackDuckRequestBuilder, blackDuckPath);
        return descriptor(blackDuckRequestBuilder, blackDuckPathResponse, descriptorCreator);
    }

    private <T extends BlackDuckResponse, D extends BlackDuckApiExchangeDescriptor<T>> D descriptor(BlackDuckRequestBuilder blackDuckRequestBuilder, ApiResponse<T> apiResponse,
        BiFunction<BlackDuckRequestBuilder, Class<T>, D> descriptorCreator) {
        blackDuckRequestBuilder.commonGet();
        return descriptorCreator.apply(blackDuckRequestBuilder, apiResponse.getResponseClass());
    }

}
