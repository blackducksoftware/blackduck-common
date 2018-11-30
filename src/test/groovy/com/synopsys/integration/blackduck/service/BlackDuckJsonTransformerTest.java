package com.synopsys.integration.blackduck.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectView;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;

public class BlackDuckJsonTransformerTest {
    private static Gson gson;
    private static ObjectMapper objectMapper;
    private static IntLogger logger;
    private static BlackDuckJsonTransformer blackDuckJsonTransformer;

    @BeforeAll
    public static void setup() {
        gson = BlackDuckServicesFactory.createDefaultGson();
        objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
        logger = new BufferedIntLogger();
        blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, logger);
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
        assertEquals(84, blackDuckPageResponse.getTotalCount());

        final JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
        final JsonArray items = jsonElement.getAsJsonObject().get("items").getAsJsonArray();
        final Iterator<JsonElement> itemsIterator = items.iterator();
        for (final ProjectView projectView : blackDuckPageResponse.getItems()) {
            assertObjectValid(projectView);
            assertTrue(StringUtils.isNotBlank(projectView.getName()));

            final JsonElement item = itemsIterator.next();
            assertJsonValid(gson.toJson(item), projectView);
        }
    }

    @Test
    public void testArbitraryJsonDifference() throws Exception {
        final InputStream jsonInputStream = getClass().getResourceAsStream("/complex.json");
        final String json = IOUtils.toString(jsonInputStream, StandardCharsets.UTF_8);
        final FruitTest fruitTest = blackDuckJsonTransformer.getResponseAs(json, FruitTest.class);

        assertTrue(fruitTest.fruits.possibleFruits.contains(FruitTest.PossibleFruits.APPLE));
        assertTrue(fruitTest.fruits.possibleFruits.contains(FruitTest.PossibleFruits.BANANA));
        assertTrue(fruitTest.fruits.nestedList.get(0).apple);
        assertFalse(fruitTest.fruits.nestedList.get(0).banana);
        assertFalse(fruitTest.fruits.nestedList.get(1).apple);
        assertFalse(fruitTest.fruits.nestedList.get(1).banana);
        assertTrue(fruitTest.fruits.nestedList.get(2).apple);
        assertTrue(fruitTest.fruits.nestedList.get(2).banana);

        final String patchedJson = blackDuckJsonTransformer.producePatchedJson(fruitTest);
        assertJsonStringsEqual(json, patchedJson);
    }

    private void assertObjectValid(final BlackDuckView blackDuckView) {
        assertNotNull(blackDuckView);
        assertNotNull(blackDuckView.getMeta());
        assertNotNull(blackDuckView.getGson());
        assertNotNull(blackDuckView.getJson());
        assertNotNull(blackDuckView.getJsonElement());
    }

    private void assertJsonValid(final String json, final BlackDuckView blackDuckView) {
        assertJsonStringsEqual(json, blackDuckView.getJson());
    }

    private void assertJsonStringsEqual(final String expectedJson, final String actualJson) {
        final String expectedJsonWithoutWhitespace = expectedJson.replaceAll("\\s+", "");
        final String actualJsonWithoutWhitespace = actualJson.replaceAll("\\s+", "");
        assertEquals(expectedJsonWithoutWhitespace, actualJsonWithoutWhitespace);
    }

    private Set<String> getStringsFromArray(final JsonArray jsonArray) {
        final Set<String> strings = new HashSet<>();
        final Iterator<JsonElement> elements = jsonArray.iterator();
        while (elements.hasNext()) {
            strings.add(elements.next().getAsString());
        }

        return strings;
    }

}
