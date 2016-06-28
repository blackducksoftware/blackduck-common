package com.blackducksoftware.integration.hub.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.blackducksoftware.integration.hub.exception.MissingUUIDException;

public class HubUrlParserTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void testGetUUIDsFromURLNullURL() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("No URL was provided to parse.");
		HubUrlParser.getUUIDsFromURL(null);
	}

	@Test
	public void testGetUUIDsFromURLNoUUID() throws Exception {
		exception.expect(MissingUUIDException.class);
		exception.expectMessage("The String provided does not contain any UUID's.");
		final URL url = new URL("http://google.com");
		HubUrlParser.getUUIDsFromURL(url);
	}

	@Test
	public void testGetUUIDsFromURL() throws Exception {
		final UUID uuid = UUID.randomUUID();
		final URL url = new URL("http://google.com/" + uuid + "/");
		final List<UUID> uuids = HubUrlParser.getUUIDsFromURL(url);
		assertNotNull(uuids);
		assertTrue(!uuids.isEmpty());
		assertEquals(uuid, uuids.get(0));
	}

	@Test
	public void testGetUUIDsFromURLMultipleUUIDs() throws Exception {
		final UUID uuid = UUID.randomUUID();
		final UUID uuid2 = UUID.randomUUID();
		final UUID uuid3 = UUID.randomUUID();
		final UUID uuid4 = UUID.randomUUID();
		final URL url = new URL(
				"http://" + uuid + "/google.com/" + uuid2 + "/version/" + uuid3 + "/component/" + uuid4 + "/");
		final List<UUID> uuids = HubUrlParser.getUUIDsFromURL(url);
		assertNotNull(uuids);
		assertTrue(!uuids.isEmpty());
		assertTrue(uuids.size() == 4);
	}

	@Test
	public void testGetUUIDsFromURLStringNullURL() throws Exception {
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("No url String was provided to parse.");
		HubUrlParser.getUUIDsFromURLString(null);
	}

	@Test
	public void testGetUUIDsFromURLStringNoUUID() throws Exception {
		exception.expect(MissingUUIDException.class);
		exception.expectMessage("The String provided does not contain any UUID's.");
		final String url = "http://google.com";
		HubUrlParser.getUUIDsFromURLString(url);
	}

	@Test
	public void testGetUUIDsFromURLString() throws Exception {
		final UUID uuid = UUID.randomUUID();
		final String url = "http://google.com/" + uuid + "/";
		final List<UUID> uuids = HubUrlParser.getUUIDsFromURLString(url);
		assertNotNull(uuids);
		assertTrue(!uuids.isEmpty());
		assertEquals(uuid, uuids.get(0));
	}

	@Test
	public void testGetUUIDsFromURLStringMultipleUUIDs() throws Exception {
		final UUID uuid = UUID.randomUUID();
		final UUID uuid2 = UUID.randomUUID();
		final UUID uuid3 = UUID.randomUUID();
		final UUID uuid4 = UUID.randomUUID();
		final String url = "http://" + uuid + "/google.com/" + uuid2 + "/version/" + uuid3 + "/component/" + uuid4
				+ "/";
		final List<UUID> uuids = HubUrlParser.getUUIDsFromURLString(url);
		assertNotNull(uuids);
		assertTrue(!uuids.isEmpty());
		assertTrue(uuids.size() == 4);
	}
}
