package com.synopsys.integration.blackduck.service;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.blackduck.api.generated.view.TagView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class TagService extends DataService {

    public TagService(final BlackDuckService blackDuckService, final IntLogger logger) {
        super(blackDuckService, logger);
    }

    public List<TagView> getAllTags(final ProjectView projectView) throws IntegrationException {
        return blackDuckService.getAllResponses(projectView, ProjectView.TAGS_LINK_RESPONSE);
    }

    public Optional<TagView> findMatchingTag(final ProjectView projectView, final String tagName) throws IntegrationException {
        return getAllTags(projectView)
                   .stream()
                   .filter(tagView -> tagView.getName().equals(tagName))
                   .findFirst();

    }

    public void updateTag(final TagView tag) throws IntegrationException {
        blackDuckService.put(tag);
    }

    public TagView createTag(final ProjectView projectView, final TagView tag) throws IntegrationException {
        if (!projectView.hasLink(ProjectView.TAGS_LINK)) {
            throw new BlackDuckIntegrationException(String.format("The supplied projectView does not have the link (%s) to create a tag.", ProjectView.TAGS_LINK));
        }
        final String tagsLink = projectView.getFirstLink(ProjectView.TAGS_LINK).orElse(null);
        final String tagLink = blackDuckService.post(new BlackDuckPath(tagsLink), tag);
        return blackDuckService.getResponse(tagLink, TagView.class);
    }

}
