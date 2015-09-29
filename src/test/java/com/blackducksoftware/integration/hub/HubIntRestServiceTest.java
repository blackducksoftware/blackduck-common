package com.blackducksoftware.integration.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.response.AutoCompleteItem;
import com.blackducksoftware.integration.hub.response.DistributionEnum;
import com.blackducksoftware.integration.hub.response.PhaseEnum;
import com.blackducksoftware.integration.hub.response.ProjectItem;
import com.blackducksoftware.integration.hub.response.ReleaseItem;
import com.blackducksoftware.integration.hub.response.VersionComparison;
import com.blackducksoftware.integration.hub.util.HubIntTestHelper;
import com.blackducksoftware.integration.hub.util.TestLogger;

public class HubIntRestServiceTest {

    private static Properties testProperties;

    @BeforeClass
    public static void testInit() {
        testProperties = new Properties();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = classLoader.getResourceAsStream("test.properties");
        try {
            testProperties.load(is);
        } catch (IOException e) {
            System.err.println("reading test.properties failed!");
        }
        // p.load(new FileReader(new File("test.properties")));
        System.out.println(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        System.out.println(testProperties.getProperty("TEST_USERNAME"));
        System.out.println(testProperties.getProperty("TEST_PASSWORD"));

    }

    // List<String> paths = new ArrayList<String>();
    // paths.add("/Users/jrichard/Documents/Jenkins/Jenkins-Hub-Git/int-hub-jenkins/test-workspace/workspace");
    // Map<String, Boolean> response = null;
    // try {
    // response = restService.getScanLocationIds("jrichardMac", paths,
    // "5003bbc7-fc7a-4ba5-9070-d2c3a260b7b8");
    //
    // restService.mapScansToProjectVersion(response, "5003bbc7-fc7a-4ba5-9070-d2c3a260b7b8");
    // } finally {
    // System.out.println(logger.getOutputString());
    //

    @Test
    public void testSetCookies() throws Exception {
        TestLogger logger = new TestLogger();

        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setLogger(logger);
        restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

        assertNotNull(restService.getCookies());
        assertTrue(!restService.getCookies().isEmpty());
        assertTrue(logger.getErrorList().isEmpty());
    }

    @Test
    public void testGetProjectMatches() throws Exception {
        TestLogger logger = new TestLogger();

        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setLogger(logger);
        restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

        List<AutoCompleteItem> matches = restService.getProjectMatches(testProperties.getProperty("TEST_PROJECT"));

        assertNotNull(matches);
        assertTrue(!matches.isEmpty());
        assertTrue(logger.getErrorList().isEmpty());
    }

    @Test
    public void testGetProjectByName() throws Exception {
        TestLogger logger = new TestLogger();

        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setLogger(logger);
        restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

        ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_PROJECT"));

        assertNotNull(project);
        assertEquals(testProperties.getProperty("TEST_PROJECT"), project.getName());
        assertTrue(logger.getErrorList().isEmpty());
    }

    @Test
    public void testGetProjectById() throws Exception {
        TestLogger logger = new TestLogger();

        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setLogger(logger);
        restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

        ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_PROJECT"));

        assertNotNull(project);
        assertEquals(testProperties.getProperty("TEST_PROJECT"), project.getName());
        assertTrue(logger.getErrorList().isEmpty());

        String id = project.getId();
        project = restService.getProjectById(id);

        assertNotNull(project);
        assertEquals(testProperties.getProperty("TEST_PROJECT"), project.getName());
        assertEquals(id, project.getId());
        assertTrue(logger.getErrorList().isEmpty());
    }

    @Test
    public void testGetScanLocationIds() throws Exception {
        TestLogger logger = new TestLogger();

        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setLogger(logger);
        restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

        List<String> paths = new ArrayList<String>();
        paths.add(testProperties.getProperty("TEST_PATH"));
        Map<String, Boolean> response = null;
        try {
            response = restService.getScanLocationIds("jrichardMac", paths,
                    "5003bbc7-fc7a-4ba5-9070-d2c3a260b7b8");

            assertNotNull(response);
            assertTrue(!response.isEmpty());

            String output = logger.getOutputString();
            assertTrue(output, output.contains("Checking for the scan location with Host name:"));
            assertTrue(output, output.contains("Attempt # 1"));
            assertTrue(output, output.contains("Comparing target :"));
            assertTrue(output, output.contains("MATCHED!"));
            assertTrue(output, output.contains("The scan target :"));
        } finally {
            assertTrue(!logger.getOutputList().isEmpty());
            assertTrue(logger.getErrorList().isEmpty());
        }
    }

    @Test
    public void testGetVersionsForProject() throws Exception {
        TestLogger logger = new TestLogger();

        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setLogger(logger);
        restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

        ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_PROJECT"));

        assertNotNull(project);
        assertEquals(testProperties.getProperty("TEST_PROJECT"), project.getName());
        assertTrue(logger.getErrorList().isEmpty());

        String id = project.getId();
        List<ReleaseItem> releases = restService.getVersionsForProject(id);

        assertNotNull(releases);
        assertTrue(!releases.isEmpty());

        boolean foundRelease = false;
        for (ReleaseItem release : releases) {
            if (release.getVersion().equals(testProperties.getProperty("TEST_VERSION"))) {
                foundRelease = true;
            }
        }
        assertTrue(foundRelease);

        assertTrue(logger.getErrorList().isEmpty());
    }

    @Test
    public void testCreateHubProject() throws Exception {
        TestLogger logger = new TestLogger();

        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setLogger(logger);
        restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));
        // TEST_CREATE_PROJECT
        String projectId = null;
        try {

            projectId = restService.createHubProject(testProperties.getProperty("TEST_CREATE_PROJECT"));

            assertTrue(StringUtils.isNotBlank(projectId));
        } finally {
            if (StringUtils.isBlank(projectId)) {
                ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_CREATE_PROJECT"));
                projectId = project.getId();
            }
            if (StringUtils.isNotBlank(projectId)) {
                HubIntTestHelper helper = new HubIntTestHelper(restService);
                helper.deleteHubProject(projectId);
            }
        }

        assertTrue(logger.getErrorList().isEmpty());
    }

    @Test
    public void testCreateHubVersion() throws Exception {
        TestLogger logger = new TestLogger();

        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setLogger(logger);
        restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));
        // TEST_CREATE_PROJECT
        String projectId = null;
        try {

            projectId = restService.createHubProject(testProperties.getProperty("TEST_CREATE_PROJECT"));
            assertTrue(StringUtils.isNotBlank(projectId));

            String versionId = restService.createHubVersion(testProperties.getProperty("TEST_CREATE_VERSION"), projectId, PhaseEnum.DEVELOPMENT.name(),
                    DistributionEnum.INTERNAL.name());

            assertTrue(StringUtils.isNotBlank(versionId));
        } finally {
            if (StringUtils.isBlank(projectId)) {
                ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_CREATE_PROJECT"));
                projectId = project.getId();
            }
            if (StringUtils.isNotBlank(projectId)) {
                HubIntTestHelper helper = new HubIntTestHelper(restService);
                helper.deleteHubProject(projectId);
            }
        }

        assertTrue(logger.getErrorList().isEmpty());
    }

    @Test
    public void testGetHubVersion() throws Exception {
        TestLogger logger = new TestLogger();

        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setLogger(logger);
        restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

        String version = restService.getHubVersion();

        assertTrue(StringUtils.isNotBlank(version));
        assertTrue(logger.getErrorList().isEmpty());
    }

    @Test
    public void testCompareWithHubVersion() throws Exception {
        TestLogger logger = new TestLogger();

        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setLogger(logger);
        restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

        VersionComparison comparison = restService.compareWithHubVersion("1");

        assertNotNull(comparison);
        assertEquals("1", comparison.getConsumerVersion());
        assertEquals(Integer.valueOf(-1), comparison.getNumericResult());
        assertEquals("<", comparison.getOperatorResult());

        comparison = restService.compareWithHubVersion("9999999");

        assertNotNull(comparison);
        assertEquals("9999999", comparison.getConsumerVersion());
        assertEquals(Integer.valueOf(1), comparison.getNumericResult());
        assertEquals(">", comparison.getOperatorResult());

        assertTrue(logger.getErrorList().isEmpty());
    }
}
