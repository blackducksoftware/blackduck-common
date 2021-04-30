/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.UserView;
import com.synopsys.integration.blackduck.api.manual.temporary.component.UserRequest;
import com.synopsys.integration.blackduck.http.BlackDuckPageDefinition;
import com.synopsys.integration.blackduck.http.BlackDuckPageResponse;
import com.synopsys.integration.blackduck.http.BlackDuckQuery;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilderFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

public class UserService extends DataService {
    public UserService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory, IntLogger logger) {
        super(blackDuckApiClient, apiDiscovery, blackDuckRequestBuilderFactory, logger);
    }

    public UserRequest createUserRequest(String username, String password, String firstName, String lastName) {
        UserRequest userRequest = new UserRequest();
        userRequest.setUserName(username);
        userRequest.setPassword(password);
        userRequest.setFirstName(firstName);
        userRequest.setLastName(lastName);
        userRequest.setActive(true);
        return userRequest;
    }

    public UserView createUser(UserRequest userRequest) throws IntegrationException {
        HttpUrl createUserUrl = apiDiscovery.getUrl(ApiDiscovery.USERS_PATH);
        HttpUrl userUrl = blackDuckApiClient.post(createUserUrl, userRequest);
        return blackDuckApiClient.getResponse(userUrl, UserView.class);
    }

    public UserView findCurrentUser() throws IntegrationException {
        UrlSingleResponse<UserView> currentUserResponse = apiDiscovery.metaSingleResponse(ApiDiscovery.CURRENT_USER_PATH);
        return blackDuckApiClient.getResponse(currentUserResponse);
    }

    public BlackDuckPageResponse<UserView> findUsersByEmail(String emailSearchTerm, BlackDuckPageDefinition blackDuckPageDefinition) throws IntegrationException {
        UrlMultipleResponses<UserView> usersResponse = apiDiscovery.metaMultipleResponses(ApiDiscovery.USERS_PATH);

        Optional<BlackDuckQuery> usernameQuery = BlackDuckQuery.createQuery("email", emailSearchTerm);
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestBuilderFactory
                                                              .createCommonGetRequestBuilder(usernameQuery)
                                                              .setBlackDuckPageDefinition(blackDuckPageDefinition);

        return blackDuckApiClient.getPageResponse(usersResponse, blackDuckRequestBuilder);
    }

    public Optional<UserView> findUserByUsername(String username) throws IntegrationException {
        UrlMultipleResponses<UserView> usersResponse = apiDiscovery.metaMultipleResponses(ApiDiscovery.USERS_PATH);

        Optional<BlackDuckQuery> usernameQuery = BlackDuckQuery.createQuery("userName", username);
        BlackDuckRequestBuilder blackDuckRequestBuilder = blackDuckRequestBuilderFactory.createCommonGetRequestBuilder(usernameQuery);

        List<UserView> foundUsers = blackDuckApiClient.getSomeResponses(usersResponse, blackDuckRequestBuilder, 1);
        return foundUsers.stream().findFirst();
    }

    public List<UserView> getAllUsers() throws IntegrationException {
        UrlMultipleResponses<UserView> usersResponse = apiDiscovery.metaMultipleResponses(ApiDiscovery.USERS_PATH);
        return blackDuckApiClient.getAllResponses(usersResponse);
    }

    public BlackDuckPageResponse<UserView> getPageOfUsers(BlackDuckPageDefinition blackDuckPageDefinition) throws IntegrationException {
        UrlMultipleResponses<UserView> usersResponse = apiDiscovery.metaMultipleResponses(ApiDiscovery.USERS_PATH);
        return blackDuckApiClient.getPageResponse(usersResponse, blackDuckPageDefinition);
    }

}
