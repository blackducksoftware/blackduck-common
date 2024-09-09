package com.blackduck.integration.blackduck.http.transform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.blackduck.integration.blackduck.TimingExtension;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.synopsys.integration.blackduck.api.core.BlackDuckView;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectView;
import com.blackduck.integration.blackduck.http.BlackDuckPageResponse;
import com.blackduck.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.log.BufferedIntLogger;
import com.synopsys.integration.log.IntLogger;

@ExtendWith(TimingExtension.class)
public class BlackDuckJsonTransformerTest {
    private static Gson gson;
    private static ObjectMapper objectMapper;
    private static BlackDuckResponseResolver blackDuckResponseResolver;
    private static IntLogger logger;
    private static BlackDuckJsonTransformer blackDuckJsonTransformer;

    @BeforeAll
    public static void setup() {
        BlackDuckJsonTransformerTest.gson = BlackDuckServicesFactory.createDefaultGson();
        BlackDuckJsonTransformerTest.objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();
        BlackDuckJsonTransformerTest.blackDuckResponseResolver = new BlackDuckResponseResolver(gson);
        BlackDuckJsonTransformerTest.logger = new BufferedIntLogger();
        BlackDuckJsonTransformerTest.blackDuckJsonTransformer = new BlackDuckJsonTransformer(gson, objectMapper, blackDuckResponseResolver, logger);
    }

    @Test
    public void testConstructionHappyPath() throws Exception {
        InputStream jsonInputStream = getClass().getResourceAsStream("/projectViewResponse.json");
        String json = IOUtils.toString(jsonInputStream, StandardCharsets.UTF_8);

        ProjectView projectView = BlackDuckJsonTransformerTest.blackDuckJsonTransformer.getResponseAs(json, ProjectView.class);
        assertFalse(StringUtils.isBlank(projectView.getDescription()));
        assertObjectValid(projectView);
        assertJsonValid(json, projectView);
    }

    @Test
    public void testJsonPreservedWithMissingField() throws Exception {
        InputStream jsonInputStream = getClass().getResourceAsStream("/projectViewResponse.json");
        String json = IOUtils.toString(jsonInputStream, StandardCharsets.UTF_8);

        ProjectViewWithoutDescription projectView = BlackDuckJsonTransformerTest.blackDuckJsonTransformer.getResponseAs(json, ProjectViewWithoutDescription.class);
        assertFalse(StringUtils.isBlank(projectView.getJsonElement().getAsJsonObject().get("description").getAsString()));
        assertObjectValid(projectView);
        assertJsonValid(json, projectView);
    }

    @Test
    public void testPageResults() throws Exception {
        InputStream jsonInputStream = getClass().getResourceAsStream("/projectsResponse.json");
        String json = IOUtils.toString(jsonInputStream, StandardCharsets.UTF_8);

        BlackDuckPageResponse<ProjectView> blackDuckPageResponse = BlackDuckJsonTransformerTest.blackDuckJsonTransformer.getResponses(json, ProjectView.class);
        assertEquals(10, blackDuckPageResponse.getItems().size());
        assertEquals(84, blackDuckPageResponse.getTotalCount());

        JsonElement jsonElement = BlackDuckJsonTransformerTest.gson.fromJson(json, JsonElement.class);
        JsonArray items = jsonElement.getAsJsonObject().get("items").getAsJsonArray();
        Iterator<JsonElement> itemsIterator = items.iterator();
        for (ProjectView projectView : blackDuckPageResponse.getItems()) {
            assertObjectValid(projectView);
            assertTrue(StringUtils.isNotBlank(projectView.getName()));

            JsonElement item = itemsIterator.next();
            assertJsonValid(BlackDuckJsonTransformerTest.gson.toJson(item), projectView);
        }
    }

    @Test
    public void testArbitraryJsonDifference() throws Exception {
        InputStream jsonInputStream = getClass().getResourceAsStream("/complex.json");
        String json = IOUtils.toString(jsonInputStream, StandardCharsets.UTF_8);
        SillyFruitResponse sillyFruitResponse = BlackDuckJsonTransformerTest.blackDuckJsonTransformer.getResponseAs(json, SillyFruitResponse.class);

        assertTrue(sillyFruitResponse.fruits.possibleFruits.contains(SillyFruitResponse.PossibleFruits.APPLE));
        assertTrue(sillyFruitResponse.fruits.possibleFruits.contains(SillyFruitResponse.PossibleFruits.BANANA));
        assertTrue(sillyFruitResponse.fruits.nestedList.get(0).apple);
        assertFalse(sillyFruitResponse.fruits.nestedList.get(0).banana);
        assertFalse(sillyFruitResponse.fruits.nestedList.get(1).apple);
        assertFalse(sillyFruitResponse.fruits.nestedList.get(1).banana);
        assertTrue(sillyFruitResponse.fruits.nestedList.get(2).apple);
        assertTrue(sillyFruitResponse.fruits.nestedList.get(2).banana);

        String patchedJson = BlackDuckJsonTransformerTest.blackDuckJsonTransformer.producePatchedJson(sillyFruitResponse);
        assertJsonStringsEqual(json, patchedJson);
    }

    @Test
    public void testSettingObjectToNull() throws Exception {
        InputStream jsonInputStream = getClass().getResourceAsStream("/json/ProjectVersionView_with_license.json");
        String json = IOUtils.toString(jsonInputStream, StandardCharsets.UTF_8);

        ProjectVersionView projectVersionView = BlackDuckJsonTransformerTest.blackDuckJsonTransformer.getResponseAs(json, ProjectVersionView.class);
        projectVersionView.setLicense(null);

        String patchedJson = BlackDuckJsonTransformerTest.blackDuckJsonTransformer.producePatchedJson(projectVersionView);
        System.out.println(patchedJson);
    }

    private void assertObjectValid(BlackDuckView blackDuckView) {
        assertNotNull(blackDuckView);
        assertNotNull(blackDuckView.getMeta());
        assertNotNull(blackDuckView.getGson());
        assertNotNull(blackDuckView.getJson());
        assertNotNull(blackDuckView.getJsonElement());
    }

    private void assertJsonValid(String json, BlackDuckView blackDuckView) {
        assertJsonStringsEqual(json, blackDuckView.getJson());
    }

    private void assertJsonStringsEqual(String expectedJson, String actualJson) {
        String expectedJsonWithoutWhitespace = expectedJson.replaceAll("\\s+", "");
        String actualJsonWithoutWhitespace = actualJson.replaceAll("\\s+", "");
        assertEquals(expectedJsonWithoutWhitespace, actualJsonWithoutWhitespace);
    }

    private Set<String> getStringsFromArray(JsonArray jsonArray) {
        Set<String> strings = new HashSet<>();
        Iterator<JsonElement> elements = jsonArray.iterator();
        while (elements.hasNext()) {
            strings.add(elements.next().getAsString());
        }

        return strings;
    }

}
