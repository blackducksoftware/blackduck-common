/**
 * blackduck-common
 * <p>
 * Copyright (c) 2020 Synopsys, Inc.
 * <p>
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.service;

import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.TagView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;

import java.util.List;
import java.util.Optional;

public class TagService extends DataService {
    public TagService(BlackDuckService blackDuckService, IntLogger logger) {
        super(blackDuckService, logger);
    }

    public List<TagView> getAllTags(ProjectView projectView) throws IntegrationException {
        return blackDuckService.getAllResponses(projectView, ProjectView.TAGS_LINK_RESPONSE);
    }

    public Optional<TagView> findMatchingTag(ProjectView projectView, String tagName) throws IntegrationException {
        return getAllTags(projectView)
                .stream()
                .filter(tagView -> tagView.getName().equals(tagName))
                .findFirst();
    }

    public void updateTag(TagView tag) throws IntegrationException {
        blackDuckService.put(tag);
    }

    public TagView createTag(ProjectView projectView, TagView tag) throws IntegrationException {
        if (!projectView.hasLink(ProjectView.TAGS_LINK)) {
            throw new BlackDuckIntegrationException(String.format("The supplied projectView does not have the link (%s) to create a tag.", ProjectView.TAGS_LINK));
        }
        HttpUrl tagsLink = projectView.getFirstLink(ProjectView.TAGS_LINK).get();
        HttpUrl tagLink = blackDuckService.post(tagsLink, tag);
        return blackDuckService.getResponse(tagLink, TagView.class);
    }

}
