package com.blackduck.integration.blackduck.codelocation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blackduck.integration.blackduck.codelocation.CodeLocationsRetriever;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.api.core.ResourceLink;
import com.synopsys.integration.blackduck.api.core.ResourceMetadata;
import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.view.CodeLocationView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.blackduck.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class CodeLocationsRetrieverTest {
    private final HttpUrl url = new HttpUrl("https://www.synopsys.com");

    public CodeLocationsRetrieverTest() throws IntegrationException {}

    @Test
    public void testCodeLocationsBeingCreatedDuringRetrieve() {
        ProjectVersionView projectVersionView = buildProjectVersionView();

        BlackDuckApiClient blackDuckApiClient = Mockito.mock(BlackDuckApiClient.class);
        UrlMultipleResponses<CodeLocationView> expectedArg = new UrlMultipleResponses<>(url, CodeLocationView.class);
        try {
            Mockito.when(blackDuckApiClient.getAllResponses(Mockito.eq(expectedArg))).thenReturn(listWithDuplicates());
        } catch (IntegrationException e) {
            fail("No exception is expected when mocking.", e);
        }

        CodeLocationsRetriever codeLocationsRetriever = new CodeLocationsRetriever(blackDuckApiClient);
        try {
            Map<String, String> hrefToName = codeLocationsRetriever.retrieveCodeLocations(projectVersionView, expectedNames());
            assertEquals(expectedResults(), hrefToName);
        } catch (IntegrationException e) {
            fail("No exception is expected when retrieving.", e);
        }

        // TODO ejk 2021-06-18 it appears that a single invocation of verify
        //  causes all unverified code to error
        //        Mockito.verify(blackDuckApiClient);
    }

    private ProjectVersionView buildProjectVersionView() {
        ResourceLink codelocationsLink = new ResourceLink();
        codelocationsLink.setRel(ProjectVersionView.CODELOCATIONS_LINK);
        codelocationsLink.setHref(url);

        ResourceMetadata resourceMetadata = new ResourceMetadata();
        resourceMetadata.setLinks(Arrays.asList(codelocationsLink));

        ProjectVersionView projectVersionView = new ProjectVersionView();
        projectVersionView.setMeta(resourceMetadata);

        return projectVersionView;
    }

    private Map<String, String> expectedResults() {
        Map<String, String> results = new HashMap<>();

        results.put("https://www.synopsys.com/codelocations/1", "frodo");
        results.put("https://www.synopsys.com/codelocations/2", "sam");
        results.put("https://www.synopsys.com/codelocations/3", "aragorn");
        results.put("https://www.synopsys.com/codelocations/4", "legolas");
        results.put("https://www.synopsys.com/codelocations/5", "gimli");

        return results;
    }

    private Set<String> expectedNames() {
        return new HashSet<>(expectedResults().values());
    }

    private List<CodeLocationView> listWithDuplicates() {
        List<CodeLocationView> codeLocations = new LinkedList<>();

        for (Map.Entry<String, String> entry : expectedResults().entrySet()) {
            codeLocations.add(createCodeLocation(entry.getKey(), entry.getValue()));
        }

        CodeLocationView toDuplicate = codeLocations.get(2);
        codeLocations.add(createCodeLocation(toDuplicate.getHref().string(), toDuplicate.getName()));

        return codeLocations;
    }

    private CodeLocationView createCodeLocation(String href, String name) {
        ResourceMetadata resourceMetadata = new ResourceMetadata();
        try {
            resourceMetadata.setHref(new HttpUrl(href));
        } catch (IntegrationException e) {
            fail("The urls should all be valid.", e);
        }

        CodeLocationView codeLocationView = new CodeLocationView();
        codeLocationView.setMeta(resourceMetadata);
        codeLocationView.setName(name);

        return codeLocationView;
    }

}
