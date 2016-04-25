package com.blackducksoftware.integration.hub.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blackducksoftware.integration.hub.util.TestLogger;

public class HubCredentialsBuilderTest {
	private List<String> expectedMessages;

	private TestLogger logger;

	@Before
	public void setUp() {
		expectedMessages = new ArrayList<String>();
		logger = new TestLogger();
	}

	@After
	public void tearDown() {
		final List<String> outputList = logger.getOutputList();
		final String outputString = logger.getOutputString();
		assertEquals("Too many/not enough messages expected: \n" + outputString, expectedMessages.size(),
				outputList.size());

		for (final String expectedMessage : expectedMessages) {
			assertTrue("Did not find the expected message : " + expectedMessage, outputList.contains(expectedMessage));
		}
	}

	@Test
	public void testvalidateCredentialsNull() throws Exception {
		expectedMessages.add("No Hub Username was found.");
		expectedMessages.add("No Hub Password was found.");

		final HubCredentialsBuilder builder = new HubCredentialsBuilder();

		assertFalse(builder.validateCredentials(logger));
	}

	@Test
	public void testvalidateCredentialsEmpty() throws Exception {
		expectedMessages.add("No Hub Username was found.");
		expectedMessages.add("No Hub Password was found.");

		final HubCredentialsBuilder builder = new HubCredentialsBuilder();
		builder.setUsername("");
		builder.setPassword("   ");

		assertFalse(builder.validateCredentials(logger));
	}

	@Test
	public void testvalidateCredentials() throws Exception {
		final HubCredentialsBuilder builder = new HubCredentialsBuilder();
		builder.setUsername("User");
		builder.setPassword("Password");

		assertTrue(builder.validateCredentials(logger));
	}

	@Test
	public void testValidateHubUserNull() throws Exception {
		expectedMessages.add("No Hub Username was found.");

		final HubCredentialsBuilder builder = new HubCredentialsBuilder();

		assertFalse(builder.validateUsername(logger));
	}

	@Test
	public void testValidateHubUser() throws Exception {
		final HubCredentialsBuilder builder = new HubCredentialsBuilder();
		builder.setUsername("User");
		assertTrue(builder.validateUsername(logger));
	}

	@Test
	public void testValidateHubPasswordNull() throws Exception {
		expectedMessages.add("No Hub Password was found.");

		final HubCredentialsBuilder builder = new HubCredentialsBuilder();

		assertFalse(builder.validatePassword(logger));
	}

	@Test
	public void testValidateHubPassword() throws Exception {
		final HubCredentialsBuilder builder = new HubCredentialsBuilder();
		builder.setPassword("Password");
		assertTrue(builder.validatePassword(logger));
	}

}
