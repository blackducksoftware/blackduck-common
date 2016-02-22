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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.response.AutoCompleteItem;
import com.blackducksoftware.integration.hub.response.DistributionEnum;
import com.blackducksoftware.integration.hub.response.PhaseEnum;
import com.blackducksoftware.integration.hub.response.ProjectItem;
import com.blackducksoftware.integration.hub.response.ReleaseItem;
import com.blackducksoftware.integration.hub.response.ReportFormatEnum;
import com.blackducksoftware.integration.hub.response.ReportMetaInformationItem;
import com.blackducksoftware.integration.hub.response.ReportMetaInformationItem.ReportMetaLinkItem;
import com.blackducksoftware.integration.hub.response.VersionComparison;
import com.blackducksoftware.integration.hub.util.HubIntTestHelper;
import com.blackducksoftware.integration.hub.util.TestLogger;

public class HubIntRestServiceTest {

    private static Properties testProperties;

    private static HubIntTestHelper helper;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @BeforeClass
    public static void testInit() throws Exception {
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

        helper = new HubIntTestHelper(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        helper.setLogger(new TestLogger());
        helper.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

        try {
            ProjectItem project = helper.getProjectByName(testProperties.getProperty("TEST_PROJECT"));

            List<ReleaseItem> projectVersions = helper.getVersionsForProject(project.getId());
            boolean versionExists = false;
            for (ReleaseItem release : projectVersions) {
                if (testProperties.getProperty("TEST_VERSION").equals(release.getVersion())) {
                    versionExists = true;
                    break;
                }
            }
            if (!versionExists) {
                helper.createHubVersion(testProperties.getProperty("TEST_VERSION"), project.getId(),
                        testProperties.getProperty("TEST_PHASE"),
                        testProperties.getProperty("TEST_DISTRIBUTION"));
            }

        } catch (ProjectDoesNotExistException e) {
            helper.createHubProjectAndVersion(testProperties.getProperty("TEST_PROJECT"), testProperties.getProperty("TEST_VERSION"),
                    testProperties.getProperty("TEST_PHASE"), testProperties.getProperty("TEST_DISTRIBUTION"));
        }
    }

    @AfterClass
    public static void testTeardown() {
        try {
            ProjectItem project = helper.getProjectByName(testProperties.getProperty("TEST_PROJECT"));
            helper.deleteHubProject(project.getId());
        } catch (Exception e) {

        }

    }

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
    public void testSetTimeoutZero() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Can not set the timeout to zero.");
        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setTimeout(0);
    }

    @Test
    public void testSetTimeout() throws Exception {
        TestLogger logger = new TestLogger();
        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setTimeout(120);
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
        String testProjectName = "TESTNAME";
        String projectId = null;
        try {

            projectId = restService.createHubProject(testProjectName);

            // Sleep for 1 second, server takes a second before you can start using projects
            Thread.sleep(2000);

            List<AutoCompleteItem> matches = restService.getProjectMatches(testProjectName);

            assertNotNull("matches must be not null", matches);
            assertTrue(!matches.isEmpty());
            assertTrue("error log expected to be empty", logger.getErrorList().isEmpty());
        } finally {
            if (StringUtils.isNotBlank(projectId)) {
                helper.deleteHubProject(projectId);
            }
        }
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
    public void testGetProjectByNameSpecialCharacters() throws Exception {
        TestLogger logger = new TestLogger();

        String projectName = "CItest!@#$^&";

        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setLogger(logger);
        restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));
        String projectId = null;
        try {

            projectId = restService.createHubProject(projectName);

            assertTrue(StringUtils.isNotBlank(projectId));

            ProjectItem project = restService.getProjectByName(projectName);

            assertNotNull(project);
            assertEquals(projectName, project.getName());
            assertTrue(logger.getErrorList().isEmpty());

        } finally {
            if (StringUtils.isBlank(projectId)) {
                try {
                    ProjectItem project = restService.getProjectByName(projectName);
                    projectId = project.getId();
                } catch (Exception e) {
                    // ignore exception
                }
            }
            if (StringUtils.isNotBlank(projectId)) {
                helper.deleteHubProject(projectId);
            }
        }

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
                try {
                    ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_CREATE_PROJECT"));
                    projectId = project.getId();
                } catch (Exception e) {
                    // ignore exception
                }
            }
            if (StringUtils.isNotBlank(projectId)) {
                helper.deleteHubProject(projectId);
            }
        }

        assertTrue(logger.getErrorList().isEmpty());
    }

    @Test
    public void testCreateHubProjectSpecialCharacters() throws Exception {
        TestLogger logger = new TestLogger();

        String projectName = "CItest!@#$^&";

        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setLogger(logger);
        restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));
        String projectId = null;
        try {

            projectId = restService.createHubProject(projectName);

            assertTrue(StringUtils.isNotBlank(projectId));
        } finally {
            if (StringUtils.isBlank(projectId)) {
                try {
                    ProjectItem project = restService.getProjectByName(projectName);
                    projectId = project.getId();
                } catch (Exception e) {
                    // ignore exception
                }
            }
            if (StringUtils.isNotBlank(projectId)) {
                helper.deleteHubProject(projectId);
            }
        }

        assertTrue(logger.getErrorList().isEmpty());
    }

    @Test
    public void testCreateHubProjectAndVersion() throws Exception {
        TestLogger logger = new TestLogger();

        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setLogger(logger);
        restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));
        // TEST_CREATE_PROJECT
        String projectId = null;
        try {

            projectId = restService.createHubProjectAndVersion(testProperties.getProperty("TEST_CREATE_PROJECT"),
                    testProperties.getProperty("TEST_CREATE_VERSION"), PhaseEnum.DEVELOPMENT.name(),
                    DistributionEnum.INTERNAL.name());

            assertTrue(StringUtils.isNotBlank(projectId));
        } finally {
            if (StringUtils.isBlank(projectId)) {
                try {
                    ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_CREATE_PROJECT"));
                    projectId = project.getId();
                } catch (Exception e) {
                    // ignore exception
                }
            }
            if (StringUtils.isNotBlank(projectId)) {
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
                try {
                    ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_CREATE_PROJECT"));
                    projectId = project.getId();
                } catch (Exception e) {
                    // ignore exception
                }
            }
            if (StringUtils.isNotBlank(projectId)) {
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

    @Test
    public void testGenerateHubReport() throws Exception {
        TestLogger logger = new TestLogger();

        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setLogger(logger);
        restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

        ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_PROJECT"));
        List<ReleaseItem> releases = restService.getVersionsForProject(project.getId());
        ReleaseItem release = null;
        for (ReleaseItem currentRelease : releases) {
            if (testProperties.getProperty("TEST_VERSION").equals(currentRelease.getVersion())) {
                release = currentRelease;
                break;
            }
        }
        assertNotNull(
                "In project : " + testProperties.getProperty("TEST_PROJECT") + " , could not find the version : " + testProperties.getProperty("TEST_VERSION"),
                release);
        String reportUrl = null;
        reportUrl = restService.generateHubReport(release.getId(), ReportFormatEnum.JSON);

        assertNotNull(reportUrl, reportUrl);
        // The project specified in the test properties file will be deleted at the end of the tests
        // So we dont need to worry about cleaning up the reports
    }

    @Test
    public void testGenerateHubReportFormatUNKNOWN() throws Exception {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Can not generate a report of format : ");
        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));

        restService.generateHubReport(null, ReportFormatEnum.UNKNOWN);
    }

    @Test
    public void testGenerateHubReportAndGetReportLinks() throws Exception {
        TestLogger logger = new TestLogger();

        HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restService.setLogger(logger);
        restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

        ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_PROJECT"));

        String versionId = restService.createHubVersion("Report Version", project.getId(), PhaseEnum.DEVELOPMENT.name(), DistributionEnum.INTERNAL.name());

        String reportUrl = null;
        System.err.println(versionId);

        // Give the server a second to recognize the new version
        Thread.sleep(1000);

        reportUrl = restService.generateHubReport(versionId, ReportFormatEnum.JSON);

        assertNotNull(reportUrl, reportUrl);

        ReportMetaInformationItem reportInfo = restService.getReportLinks(reportUrl);

        List<ReportMetaLinkItem> links = reportInfo.get_meta().getLinks();

        ReportMetaLinkItem contentLink = null;
        for (ReportMetaLinkItem link : links) {
            if (link.getRel().equalsIgnoreCase("content")) {
                contentLink = link;
                break;
            }
        }
        assertNotNull("Could not find the content link for the report at : " + reportUrl, contentLink);
        // The project specified in the test properties file will be deleted at the end of the tests
        // So we dont need to worry about cleaning up the reports
    }

}
