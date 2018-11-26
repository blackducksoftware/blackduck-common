package com.synopsys.integration.blackduck.service;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
    public void testMergedJson() throws Exception {
        final InputStream jsonInputStream = getClass().getResourceAsStream("/projectViewResponse.json");
        final String json = IOUtils.toString(jsonInputStream, StandardCharsets.UTF_8);
        final ProjectView projectView = blackDuckJsonTransformer.getResponseAs(json, ProjectView.class);

        final InputStream jsonToMergeInputStream = getClass().getResourceAsStream("/projectViewResponseToMerge.json");
        final String jsonToMerge = IOUtils.toString(jsonToMergeInputStream, StandardCharsets.UTF_8);

        final String mergedJson = blackDuckJsonTransformer.mergeJson(json, jsonToMerge);
        final ProjectView mergedProjectView = blackDuckJsonTransformer.getResponseAs(mergedJson, ProjectView.class);

        assertEquals(projectView.getName(), mergedProjectView.getName());
        assertEquals("a merged description", mergedProjectView.getDescription());

        assertEquals("https://int-hub02.dc1.lan/api/projects/e3c16059-f316-4dcc-9efe-0672cb0e6ddf/assignable-users", projectView.getFirstLink("assignable-users").get());
        assertEquals("Mairzy doats and dozy doats and liddle lamzy divey", mergedProjectView.getFirstLink("assignable-users").get());

        final Set<String> expectedOriginal = new HashSet<>(Arrays.asList("DELETE", "GET", "PUT"));
        final Set<String> expectedMerged = new HashSet<>(Arrays.asList("monkey"));
        assertEquals(expectedOriginal, new HashSet<>(projectView.getAllowedMethods()));
        assertEquals(expectedMerged, new HashSet<>(mergedProjectView.getAllowedMethods()));
    }

    @Test
    public void testSubObjectsPreserved() throws Exception {
        final InputStream jsonInputStream = getClass().getResourceAsStream("/misterFantasticWithAddress.json");
        final String json = IOUtils.toString(jsonInputStream, StandardCharsets.UTF_8);
        final SuperHero mrFantastic = blackDuckJsonTransformer.getResponseAs(json, SuperHero.class);

        final Address currentAddress = mrFantastic.getAddress();
        //they are renaming the Baxter Building...
        currentAddress.setFirstLine("Fantasic Four HQ");

        final String updatedJson = gson.toJson(mrFantastic);
        final String mergedJson = blackDuckJsonTransformer.mergeJson(json, updatedJson);
        final JsonObject jsonObject = gson.fromJson(mergedJson, JsonObject.class);
        assertEquals("Midtown", jsonObject.get("address").getAsJsonObject().get("third line"));
    }

    @Test
    public void testNewFieldsAreAdded() throws Exception {
        final InputStream jsonInputStream = getClass().getResourceAsStream("/misterFantastic.json");
        final String json = IOUtils.toString(jsonInputStream, StandardCharsets.UTF_8);
        final SuperHero mrFantastic = blackDuckJsonTransformer.getResponseAs(json, SuperHero.class);

        final Address address = new Address();
        address.setCity("New York");
        mrFantastic.setAddress(address);

        final String updatedJson = gson.toJson(mrFantastic);
        final String mergedJson = blackDuckJsonTransformer.mergeJson(json, updatedJson);
        final JsonObject jsonObject = gson.fromJson(mergedJson, JsonObject.class);
        assertEquals("New York", jsonObject.get("address").getAsJsonObject().get("city").getAsString());
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

    private Set<String> getStringsFromArray(final JsonArray jsonArray) {
        final Set<String> strings = new HashSet<>();
        final Iterator<JsonElement> elements = jsonArray.iterator();
        while (elements.hasNext()) {
            strings.add(elements.next().getAsString());
        }

        return strings;
    }

}
