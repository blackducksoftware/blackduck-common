package com.blackducksoftware.integration.hub.api.component;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ComponentQueryTest {

	private final String ID = "maven";
	private final String GROUP = "group";
	private final String ARTIFACT = "artifact";
	private final String VERSION = "version";
	private final String EXPECTED_QUERY = "id:maven|group|artifact|version";

	@Test
	public void testGetQuery() {
		final ComponentQuery query = new ComponentQuery(ID, GROUP, ARTIFACT, VERSION);
		assertEquals(EXPECTED_QUERY, query.getQuery());
	}

}
