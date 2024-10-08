/*
 * blackduck-common
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.blackduck.service.dataservice;

import com.blackduck.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.blackduck.integration.blackduck.api.generated.view.TagView;
import com.blackduck.integration.blackduck.api.manual.view.ProjectView;
import com.blackduck.integration.blackduck.exception.BlackDuckIntegrationException;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.blackduck.integration.blackduck.service.DataService;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.rest.HttpUrl;

import java.util.List;
import java.util.Optional;

public class TagService extends DataService {
    public TagService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger) {
        super(blackDuckApiClient, apiDiscovery, logger);
    }

    public List<TagView> getAllTags(ProjectView projectView) throws IntegrationException {
        return blackDuckApiClient.getAllResponses(projectView.metaTagsLink());
    }

    public Optional<TagView> findMatchingTag(ProjectView projectView, String tagName) throws IntegrationException {
        return getAllTags(projectView)
                   .stream()
                   .filter(tagView -> tagView.getName().equals(tagName))
                   .findFirst();
    }

    public void updateTag(TagView tag) throws IntegrationException {
        blackDuckApiClient.put(tag);
    }

    public TagView createTag(ProjectView projectView, TagView tag) throws IntegrationException {
        if (!projectView.hasLink(ProjectView.TAGS_LINK)) {
            throw new BlackDuckIntegrationException(String.format("The supplied projectView does not have the link (%s) to create a tag.", ProjectView.TAGS_LINK));
        }
        HttpUrl tagsLink = projectView.getFirstLink(ProjectView.TAGS_LINK);
        HttpUrl tagLink = blackDuckApiClient.post(tagsLink, tag);
        return blackDuckApiClient.getResponse(tagLink, TagView.class);
    }

}
