package com.synopsys.integration.blackduck.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;

public class BlackDuckJsonTransformerTest {
    private static Gson gson;
    private static IntLogger logger;
    private static BlackDuckJsonTransformer blackDuckJsonTransformer;

    @BeforeAll
    public static void setup() {
        gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapterFactory(new BlackDuckTypeAdapterFactory()).create();
        logger = new BufferedIntLogger();
        blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, logger);
    }

    @Test
    public void testConstructionHappyPath() throws Exception {
        final InputStream jsonInputStream = getClass().getResourceAsStream("/projectViewResponse.json");
        final String json = IOUtils.toString(jsonInputStream, StandardCharsets.UTF_8);

        final ProjectView projectView = blackDuckJsonTransformer.getResponseAs(json, ProjectView.class);
        assertFalse(StringUtils.isBlank(projectView.getDescription()));
        assertObjectValid(projectView);
        assertJsonValid(json, projectView);
    }

    @Test
    public void testJsonPreservedWithMissingField() throws Exception {
        final InputStream jsonInputStream = getClass().getResourceAsStream("/projectViewResponse.json");
        final String json = IOUtils.toString(jsonInputStream, StandardCharsets.UTF_8);

        final ProjectViewWithoutDescription projectView = blackDuckJsonTransformer.getResponseAs(json, ProjectViewWithoutDescription.class);
        assertFalse(StringUtils.isBlank(projectView.getJsonElement().getAsJsonObject().get("description").getAsString()));
        assertObjectValid(projectView);
        assertJsonValid(json, projectView);
    }

    @Test
    public void testPageResults() throws Exception {
        final InputStream jsonInputStream = getClass().getResourceAsStream("/projectsResponse.json");
        final String json = IOUtils.toString(jsonInputStream, StandardCharsets.UTF_8);

        final BlackDuckPageResponse<ProjectView> blackDuckPageResponse = blackDuckJsonTransformer.getResponses(json, ProjectView.class);
        assertEquals(10, blackDuckPageResponse.getItems().size());
        assertEquals(72, blackDuckPageResponse.getTotalCount());
        for (final ProjectView projectView : blackDuckPageResponse.getItems()) {
            assertObjectValid(projectView);
            assertTrue(StringUtils.isNotBlank(projectView.getName()));
        }
    }

    private void assertObjectValid(final BlackDuckView blackDuckView) {
        assertNotNull(blackDuckView);
        assertNotNull(blackDuckView.getMeta());
        assertNotNull(blackDuckView.getGson());
        assertNotNull(blackDuckView.getJson());
        assertNotNull(blackDuckView.getJsonElement());
    }

    private void assertJsonValid(final String json, final BlackDuckView blackDuckView) {
        final String expectedJsonWithoutWhitespace = json.replaceAll("\\s+", "");
        final String actualJsonWithoutWhitespace = blackDuckView.getJson().replaceAll("\\s+", "");
        assertEquals(expectedJsonWithoutWhitespace, actualJsonWithoutWhitespace);
    }
}
