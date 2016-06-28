package com.blackducksoftware.integration.hub.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blackducksoftware.integration.hub.exception.MissingUUIDException;

public class HubUrlParserTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testGetUUIDFromURLNullIdentifier() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("No identifier was provided.");
		HubUrlParser.getUUIDFromURL(null, null);
	}

	@Test
	public void testGetUUIDFromURLBlankIdentifier() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("No identifier was provided.");
		HubUrlParser.getUUIDFromURL("", null);
	}

	@Test
	public void testGetUUIDFromURLNullURL() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("No URL was provided to parse.");
		final String identifier = "none";
		HubUrlParser.getUUIDFromURL(identifier, null);
	}

	@Test
	public void testGetUUIDFromURLNoUUID() throws Exception {
		final String identifier = "none";
		final URL url = new URL("http://google");
		exception.expect(MissingUUIDException.class);
		exception.expectMessage("The String provided : " + url
				+ ", does not contain any UUID's for the specified identifer : " + identifier);
		HubUrlParser.getUUIDFromURL(identifier, url);
	}

	@Test
	public void testGetUUIDFromURLNoUUIDFromIdentifier() throws Exception {
		final UUID uuid = UUID.randomUUID();
		final String identifier = "none";
		final URL url = new URL("http://google/" + uuid + "/");
		exception.expect(MissingUUIDException.class);
		exception.expectMessage("The String provided : " + url
				+ ", does not contain any UUID's for the specified identifer : " + identifier);
		HubUrlParser.getUUIDFromURL(identifier, url);
	}

	@Test
	public void testGetUUIDFromURL() throws Exception {
		final UUID uuid = UUID.randomUUID();
		final String identifier = "google";
		final URL url = new URL("http://google/" + uuid + "/");
		final UUID uuidFound = HubUrlParser.getUUIDFromURL(identifier, url);
		assertNotNull(uuidFound);
		assertEquals(uuid, uuidFound);
	}

	@Test
	public void testGetUUIDFromURLMultipleUUIDs() throws Exception {
		final UUID uuid = UUID.randomUUID();
		final UUID uuid2 = UUID.randomUUID();
		final UUID uuid3 = UUID.randomUUID();
		final UUID uuid4 = UUID.randomUUID();
		final String identifier = "google";
		final URL url = new URL(
				"http://" + uuid + "/google/" + uuid2 + "/version/" + uuid3 + "/component/" + uuid4 + "/");
		final UUID uuidFound = HubUrlParser.getUUIDFromURL(identifier, url);
		assertNotNull(uuidFound);
		assertEquals(uuid2, uuidFound);
	}

	@Test
	public void testGetUUIDFromURLStringNullIdentifier() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("No identifier was provided.");
		HubUrlParser.getUUIDFromURLString(null, null);
	}

	@Test
	public void testGetUUIDFromURLStringBlankIdentifier() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("No identifier was provided.");
		HubUrlParser.getUUIDFromURLString("", null);
	}

	@Test
	public void testGetUUIDFromURLStringNullURLString() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("No url String was provided to parse.");
		final String identifier = "none";
		HubUrlParser.getUUIDFromURLString(identifier, null);
	}

	@Test
	public void testGetUUIDFromURLStringNoUUID() throws Exception {
		final String identifier = "none";
		final String url = "http://google";
		exception.expect(MissingUUIDException.class);
		exception.expectMessage("The String provided : " + url
				+ ", does not contain any UUID's for the specified identifer : " + identifier);
		HubUrlParser.getUUIDFromURLString(identifier, url);
	}

	@Test
	public void testGetUUIDFromURLStringNoUUIDFromIdentifier() throws Exception {
		final UUID uuid = UUID.randomUUID();
		final String identifier = "none";
		final String url = "http://google/" + uuid + "/";
		exception.expect(MissingUUIDException.class);
		exception.expectMessage("The String provided : " + url
				+ ", does not contain any UUID's for the specified identifer : " + identifier);
		HubUrlParser.getUUIDFromURLString(identifier, url);
	}

	@Test
	public void testGetUUIDFromURLString() throws Exception {
		final UUID uuid = UUID.randomUUID();
		final String identifier = "google";
		final String url = "http://google/" + uuid + "/";
		final UUID uuidFound = HubUrlParser.getUUIDFromURLString(identifier, url);
		assertNotNull(uuidFound);
		assertEquals(uuid, uuidFound);
	}

	@Test
	public void testGetUUIDFromURLStringMultipleUUIDs() throws Exception {
		final UUID uuid = UUID.randomUUID();
		final UUID uuid2 = UUID.randomUUID();
		final UUID uuid3 = UUID.randomUUID();
		final UUID uuid4 = UUID.randomUUID();
		final String identifier = "google";
		final String url = "http://" + uuid + "/google/" + uuid2 + "/version/" + uuid3 + "/component/" + uuid4
				+ "/";
		final UUID uuidFound = HubUrlParser.getUUIDFromURLString(identifier, url);
		assertNotNull(uuidFound);
		assertEquals(uuid2, uuidFound);
	}
}
