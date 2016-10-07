package com.blackducksoftware.integration.hub.rest;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.util.HubUrlParser;

public class RestConnectionTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testGetBaseUrlWithPort() throws URISyntaxException {
		final String urlPrefix = "https://hub.bds.com:8080/";
		final String projectVersionRelativeUrl = "api/projects/1234/versions/5678";
		final String projectVersionUrl = urlPrefix + projectVersionRelativeUrl;

		assertEquals(urlPrefix, HubUrlParser.getBaseUrl(projectVersionUrl));
	}

	@Test
	public void testGetBaseUrlWithoutPort() throws URISyntaxException {
		final String urlPrefix = "https://hub.bds.com/";
		final String projectVersionRelativeUrl = "api/projects/1234/versions/5678";
		final String projectVersionUrl = urlPrefix + projectVersionRelativeUrl;

		assertEquals(urlPrefix, HubUrlParser.getBaseUrl(projectVersionUrl));
	}

	@Test
	public void testGetRelativeUrl() throws URISyntaxException {
		final String urlPrefix = "https://hub.bds.com:8080/";
		final String projectVersionRelativeUrl = "api/projects/1234/versions/5678";
		final String projectVersionUrl = urlPrefix + projectVersionRelativeUrl;

		assertEquals(projectVersionRelativeUrl, HubUrlParser.getRelativeUrl(projectVersionUrl));
	}

	@Test
	public void testGetRelativeUrlTrailingSlashNormalization() throws URISyntaxException {
		final String urlPrefix = "https://hub.bds.com:8080/";
		final String projectVersionRelativeUrl = "api/projects/1234/versions/5678" + "/";
		final String projectVersionUrl = urlPrefix + projectVersionRelativeUrl;

		assertEquals(projectVersionRelativeUrl, HubUrlParser.getRelativeUrl(projectVersionUrl));
	}
}
