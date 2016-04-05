package com.blackducksoftware.integration.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.restlet.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

import com.blackducksoftware.integration.hub.api.VersionComparison;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.meta.MetaLink;
import com.blackducksoftware.integration.hub.policy.api.PolicyStatus;
import com.blackducksoftware.integration.hub.policy.api.PolicyStatusEnum;
import com.blackducksoftware.integration.hub.project.api.AutoCompleteItem;
import com.blackducksoftware.integration.hub.project.api.ProjectItem;
import com.blackducksoftware.integration.hub.report.api.ReportFormatEnum;
import com.blackducksoftware.integration.hub.report.api.ReportInformationItem;
import com.blackducksoftware.integration.hub.report.api.VersionReport;
import com.blackducksoftware.integration.hub.scan.api.ScanLocationItem;
import com.blackducksoftware.integration.hub.scan.api.ScanLocationResults;
import com.blackducksoftware.integration.hub.util.HubIntTestHelper;
import com.blackducksoftware.integration.hub.util.TestLogger;
import com.blackducksoftware.integration.hub.version.api.DistributionEnum;
import com.blackducksoftware.integration.hub.version.api.PhaseEnum;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.google.gson.Gson;

public class HubIntRestServiceTest {

	private static Properties testProperties;

	private static HubIntTestHelper helper;

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void testInit() throws Exception {
		testProperties = new Properties();
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final InputStream is = classLoader.getResourceAsStream("test.properties");
		try {
			testProperties.load(is);
		} catch (final IOException e) {
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
			final ProjectItem project = helper.getProjectByName(testProperties.getProperty("TEST_PROJECT"));

			final List<ReleaseItem> projectVersions = helper.getVersionsForProject(project.getId());
			boolean versionExists = false;
			for (final ReleaseItem release : projectVersions) {
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

		} catch (final ProjectDoesNotExistException e) {
			helper.createHubProjectAndVersion(testProperties.getProperty("TEST_PROJECT"), testProperties.getProperty("TEST_VERSION"),
					testProperties.getProperty("TEST_PHASE"), testProperties.getProperty("TEST_DISTRIBUTION"));
		}
	}

	@AfterClass
	public static void testTeardown() {
		try {
			final ProjectItem project = helper.getProjectByName(testProperties.getProperty("TEST_PROJECT"));
			helper.deleteHubProject(project.getId());
		} catch (final Exception e) {

		}

	}

	@Test
	public void testSetCookies() throws Exception {
		final TestLogger logger = new TestLogger();

		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
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
		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
		restService.setTimeout(0);
	}

	@Test
	public void testSetTimeout() throws Exception {
		final TestLogger logger = new TestLogger();
		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
		restService.setTimeout(120);
		restService.setLogger(logger);
		restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

		assertNotNull(restService.getCookies());
		assertTrue(!restService.getCookies().isEmpty());
		assertTrue(logger.getErrorList().isEmpty());
	}

	@Test
	public void testGetProjectMatches() throws Exception {
		final TestLogger logger = new TestLogger();

		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
		restService.setLogger(logger);
		restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));
		final String testProjectName = "TESTNAME";
		String projectId = null;
		try {

			projectId = restService.createHubProject(testProjectName);

			// Sleep for 3 second, server takes a second before you can start using projects
			Thread.sleep(3000);

			final List<AutoCompleteItem> matches = restService.getProjectMatches(testProjectName);

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
		final TestLogger logger = new TestLogger();

		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
		restService.setLogger(logger);
		restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

		final ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_PROJECT"));

		assertNotNull(project);
		assertEquals(testProperties.getProperty("TEST_PROJECT"), project.getName());
		assertTrue(logger.getErrorList().isEmpty());
	}

	@Test
	public void testGetProjectByNameSpecialCharacters() throws Exception {
		final TestLogger logger = new TestLogger();

		final String projectName = "CItest!@#$^&";

		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
		restService.setLogger(logger);
		restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));
		String projectId = null;
		try {

			projectId = restService.createHubProject(projectName);

			assertTrue(StringUtils.isNotBlank(projectId));

			final ProjectItem project = restService.getProjectByName(projectName);

			assertNotNull(project);
			assertEquals(projectName, project.getName());
			assertTrue(logger.getErrorList().isEmpty());

		} finally {
			if (StringUtils.isBlank(projectId)) {
				try {
					final ProjectItem project = restService.getProjectByName(projectName);
					projectId = project.getId();
				} catch (final Exception e) {
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
		final TestLogger logger = new TestLogger();

		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
		restService.setLogger(logger);
		restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

		ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_PROJECT"));

		assertNotNull(project);
		assertEquals(testProperties.getProperty("TEST_PROJECT"), project.getName());
		assertTrue(logger.getErrorList().isEmpty());

		final String id = project.getId();
		project = restService.getProjectById(id);

		assertNotNull(project);
		assertEquals(testProperties.getProperty("TEST_PROJECT"), project.getName());
		assertEquals(id, project.getId());
		assertTrue(logger.getErrorList().isEmpty());
	}

	@Test
	public void testGetVersionsForProject() throws Exception {
		final TestLogger logger = new TestLogger();

		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
		restService.setLogger(logger);
		restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

		final ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_PROJECT"));

		assertNotNull(project);
		assertEquals(testProperties.getProperty("TEST_PROJECT"), project.getName());
		assertTrue(logger.getErrorList().isEmpty());

		final String id = project.getId();
		final List<ReleaseItem> releases = restService.getVersionsForProject(id);

		assertNotNull(releases);
		assertTrue(!releases.isEmpty());

		boolean foundRelease = false;
		for (final ReleaseItem release : releases) {
			if (release.getVersion().equals(testProperties.getProperty("TEST_VERSION"))) {
				foundRelease = true;
			}
		}
		assertTrue(foundRelease);

		assertTrue(logger.getErrorList().isEmpty());
	}

	@Test
	public void testCreateHubProject() throws Exception {
		final TestLogger logger = new TestLogger();

		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
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
					final ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_CREATE_PROJECT"));
					projectId = project.getId();
				} catch (final Exception e) {
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
		final TestLogger logger = new TestLogger();

		final String projectName = "CItest!@#$^&";

		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
		restService.setLogger(logger);
		restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));
		String projectId = null;
		try {

			projectId = restService.createHubProject(projectName);

			assertTrue(StringUtils.isNotBlank(projectId));
		} finally {
			if (StringUtils.isBlank(projectId)) {
				try {
					final ProjectItem project = restService.getProjectByName(projectName);
					projectId = project.getId();
				} catch (final Exception e) {
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
		final TestLogger logger = new TestLogger();

		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
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
					final ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_CREATE_PROJECT"));
					projectId = project.getId();
				} catch (final Exception e) {
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
		final TestLogger logger = new TestLogger();

		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
		restService.setLogger(logger);
		restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));
		// TEST_CREATE_PROJECT
		String projectId = null;
		try {

			projectId = restService.createHubProject(testProperties.getProperty("TEST_CREATE_PROJECT"));
			assertTrue(StringUtils.isNotBlank(projectId));

			final String versionId = restService.createHubVersion(testProperties.getProperty("TEST_CREATE_VERSION"), projectId, PhaseEnum.DEVELOPMENT.name(),
					DistributionEnum.INTERNAL.name());

			assertTrue(StringUtils.isNotBlank(versionId));
		} finally {
			if (StringUtils.isBlank(projectId)) {
				try {
					final ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_CREATE_PROJECT"));
					projectId = project.getId();
				} catch (final Exception e) {
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
		final TestLogger logger = new TestLogger();

		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
		restService.setLogger(logger);
		restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

		final String version = restService.getHubVersion();

		assertTrue(StringUtils.isNotBlank(version));
		assertTrue(logger.getErrorList().isEmpty());
	}

	@Test
	public void testCompareWithHubVersion() throws Exception {
		final TestLogger logger = new TestLogger();

		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
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
		final TestLogger logger = new TestLogger();

		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
		restService.setLogger(logger);
		restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

		final ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_PROJECT"));
		final List<ReleaseItem> releases = restService.getVersionsForProject(project.getId());
		ReleaseItem release = null;
		for (final ReleaseItem currentRelease : releases) {
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
		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));

		restService.generateHubReport(null, ReportFormatEnum.UNKNOWN);
	}

	@Test
	public void testGenerateHubReportAndReadReport() throws Exception {
		final TestLogger logger = new TestLogger();

		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
		restService.setLogger(logger);
		restService.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));

		final ProjectItem project = restService.getProjectByName(testProperties.getProperty("TEST_PROJECT"));

		final String versionId = restService.createHubVersion("Report Version", project.getId(), PhaseEnum.DEVELOPMENT.name(), DistributionEnum.INTERNAL.name());

		String reportUrl = null;
		System.err.println(versionId);

		// Give the server a second to recognize the new version
		Thread.sleep(1000);

		reportUrl = restService.generateHubReport(versionId, ReportFormatEnum.JSON);

		assertNotNull(reportUrl, reportUrl);

		DateTime timeFinished = null;
		ReportInformationItem reportInfo = null;

		while (timeFinished == null) {
			Thread.sleep(5000);
			reportInfo = restService.getReportLinks(reportUrl);

			timeFinished = reportInfo.getTimeFinishedAt();
		}

		final List<MetaLink> links = reportInfo.get_meta().getLinks();

		MetaLink contentLink = null;
		for (final MetaLink link : links) {
			if (link.getRel().equalsIgnoreCase("content")) {
				contentLink = link;
				break;
			}
		}
		assertNotNull("Could not find the content link for the report at : " + reportUrl, contentLink);
		// The project specified in the test properties file will be deleted at the end of the tests
		// So we dont need to worry about cleaning up the reports

		final VersionReport report = restService.getReportContent(contentLink.getHref());
		assertNotNull(report);
		assertNotNull(report.getDetailedReleaseSummary());
		assertNotNull(report.getDetailedReleaseSummary().getPhase());
		assertNotNull(report.getDetailedReleaseSummary().getDistribution());
		assertNotNull(report.getDetailedReleaseSummary().getProjectId());
		assertNotNull(report.getDetailedReleaseSummary().getProjectName());
		assertNotNull(report.getDetailedReleaseSummary().getVersionId());
		assertNotNull(report.getDetailedReleaseSummary().getVersion());

		final String reportId = restService.getReportIdFromReportUrl(reportUrl);
		assertEquals(204, restService.deleteHubReport(versionId, reportId));

	}

	@Test
	public void testGetReportIdFromReportUrl() throws Exception {
		final TestLogger logger = new TestLogger();

		final HubIntRestService restService = new HubIntRestService(testProperties.getProperty("TEST_HUB_SERVER_URL"));
		restService.setLogger(logger);
		final String expectedId = "IDThatShouldBeFound";

		String reportUrl = "test/test/test/id/id/yoyo.yo/" + expectedId;
		String reportId = restService.getReportIdFromReportUrl(reportUrl);
		assertEquals(expectedId, reportId);

		reportUrl = "test/test/" + expectedId + "/test/id/id/yoyo.yo/";
		reportId = restService.getReportIdFromReportUrl(reportUrl);
		assertEquals("yoyo.yo", reportId);
	}

	@Test
	public void testGetCodeLocations() throws Exception {
		final TestLogger logger = new TestLogger();

		final HubIntRestService restService = new HubIntRestService("FakeUrl");
		restService.setLogger(logger);

		final String fakeHost = "TestHost";
		final String serverPath1 = "/Test/Fake/Path";
		final String serverPath2 = "/Test/Fake/Path/Child/";
		final String serverPath3 = "/Test/Fake/File";

		final HubIntRestService restServiceSpy = Mockito.spy(restService);

		final ClientResource clientResource = new ClientResource("");
		final ClientResource resourceSpy = Mockito.spy(clientResource);

		Mockito.when(resourceSpy.handle()).then(new Answer<Representation>() {
			@Override
			public Representation answer(final InvocationOnMock invocation) throws Throwable {

				final ScanLocationResults scanLocationResults = new ScanLocationResults();
				scanLocationResults.setTotalCount(3);
				final ScanLocationItem sl1 = new ScanLocationItem();
				sl1.setHost(fakeHost);
				sl1.setPath(serverPath1);
				final ScanLocationItem sl2 = new ScanLocationItem();
				sl2.setHost(fakeHost);
				sl2.setPath(serverPath2);
				final ScanLocationItem sl3 = new ScanLocationItem();
				sl3.setHost(fakeHost);
				sl3.setPath(serverPath3);

				final List<ScanLocationItem> items = new ArrayList<ScanLocationItem>();
				items.add(sl1);
				items.add(sl2);
				items.add(sl3);

				scanLocationResults.setItems(items);

				final String scResults = new Gson().toJson(scanLocationResults);
				final StringRepresentation rep = new StringRepresentation(scResults);
				final Response response = new Response(null);
				response.setEntity(rep);

				resourceSpy.setResponse(response);
				return null;
			}
		});

		Mockito.when(restServiceSpy.createClientResource()).thenReturn(resourceSpy);

		final List<String> scanTargets = new ArrayList<String>();
		scanTargets.add("Test/Fake/Path/Child");
		scanTargets.add("Test\\Fake\\File");

		final List<ScanLocationItem> codeLocations = restServiceSpy.getScanLocations(fakeHost, scanTargets);

		assertNotNull(codeLocations);
		assertTrue(codeLocations.size() == 2);
		assertNotNull(codeLocations.get(0));
		assertNotNull(codeLocations.get(1));
	}

	@Test
	public void testGetCodeLocationsUnmatched() throws Exception {
		exception.expect(HubIntegrationException.class);
		exception.expectMessage("Could not determine the code location");

		final TestLogger logger = new TestLogger();

		final HubIntRestService restService = new HubIntRestService("FakeUrl");
		restService.setLogger(logger);

		final String fakeHost = "TestHost";

		final HubIntRestService restServiceSpy = Mockito.spy(restService);

		final ClientResource clientResource = new ClientResource("");
		final ClientResource resourceSpy = Mockito.spy(clientResource);

		Mockito.when(resourceSpy.handle()).then(new Answer<Representation>() {
			@Override
			public Representation answer(final InvocationOnMock invocation) throws Throwable {

				final ScanLocationResults scanLocationResults = new ScanLocationResults();
				scanLocationResults.setTotalCount(0);

				final List<ScanLocationItem> items = new ArrayList<ScanLocationItem>();

				scanLocationResults.setItems(items);

				final String scResults = new Gson().toJson(scanLocationResults);
				final StringRepresentation rep = new StringRepresentation(scResults);
				final Response response = new Response(null);
				response.setEntity(rep);

				resourceSpy.setResponse(response);
				return null;
			}
		});

		Mockito.when(restServiceSpy.createClientResource()).thenReturn(resourceSpy);

		final List<String> scanTargets = new ArrayList<String>();
		scanTargets.add("Test/Fake/Path/Child");

		restServiceSpy.getScanLocations(fakeHost, scanTargets);
	}

	@Test
	public void testGetPolicyStatus() throws Exception {
		final TestLogger logger = new TestLogger();
		final HubIntRestService restService = new HubIntRestService("FakeUrl");
		restService.setLogger(logger);

		final String overallStatus = PolicyStatusEnum.IN_VIOLATION.name();
		final String updatedAt = new DateTime().toString();

		final PolicyStatus policyStatus = new PolicyStatus(overallStatus, updatedAt, null, null);

		final HubIntRestService restServiceSpy = Mockito.spy(restService);

		final ClientResource clientResource = new ClientResource("");
		final ClientResource resourceSpy = Mockito.spy(clientResource);

		Mockito.when(resourceSpy.handle()).then(new Answer<Representation>() {
			@Override
			public Representation answer(final InvocationOnMock invocation) throws Throwable {
				final String scResults = new Gson().toJson(policyStatus);
				final StringRepresentation rep = new StringRepresentation(scResults);
				final Response response = new Response(null);
				response.setEntity(rep);

				resourceSpy.setResponse(response);
				return null;
			}
		});

		Mockito.when(restServiceSpy.createClientResource()).thenReturn(resourceSpy);

		assertEquals(policyStatus, restServiceSpy.getPolicyStatus("projectId", "versionId"));

		try {
			assertEquals(policyStatus, restServiceSpy.getPolicyStatus("", ""));
		} catch (final IllegalArgumentException e) {
			assertEquals("Missing the project Id to get the policy status of.", e.getMessage());
		}

		try {
			assertEquals(policyStatus, restServiceSpy.getPolicyStatus("projectId", ""));
		} catch (final IllegalArgumentException e) {
			assertEquals("Missing the version Id to get the policy status of.", e.getMessage());
		}

		try {
			assertEquals(policyStatus, restServiceSpy.getPolicyStatus(null, null));
		} catch (final IllegalArgumentException e) {
			assertEquals("Missing the project Id to get the policy status of.", e.getMessage());
		}

		try {
			assertEquals(policyStatus, restServiceSpy.getPolicyStatus("projectId", null));
		} catch (final IllegalArgumentException e) {
			assertEquals("Missing the version Id to get the policy status of.", e.getMessage());
		}
	}

}
