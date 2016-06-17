package com.blackducksoftware.integration.hub.item;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.meta.MetaInformation;
import com.blackducksoftware.integration.hub.meta.MetaLink;

public class HubItemTest {

	private static final String OTHER_LINK_NAME = "some-other-link";
	private static final String POLICY_RULE_LINK_NAME = "policy-rule";
	private static final String OTHER_URL = "some other url";
	private static final String TEST_POLICY_RULE_URL_2 = "test policy rule url 2";
	private static final String TEST_POLICY_RULE_URL_1 = "test policy rule url 1";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() {

		final List<MetaLink> links = new ArrayList<MetaLink>();

		links.add(new MetaLink(POLICY_RULE_LINK_NAME, TEST_POLICY_RULE_URL_1));
		links.add(new MetaLink(POLICY_RULE_LINK_NAME, TEST_POLICY_RULE_URL_2));
		links.add(new MetaLink(OTHER_LINK_NAME, OTHER_URL));

		final MetaInformation meta = new MetaInformation(null, null, links);
		final HubItem hubItem = new HubItem(meta);

		assertEquals(2, hubItem.getLinks(POLICY_RULE_LINK_NAME).size());
		assertEquals(TEST_POLICY_RULE_URL_1, hubItem.getLinks(POLICY_RULE_LINK_NAME).get(0));
		assertEquals(TEST_POLICY_RULE_URL_2, hubItem.getLinks(POLICY_RULE_LINK_NAME).get(1));

		assertEquals(1, hubItem.getLinks(OTHER_LINK_NAME).size());
		assertEquals(OTHER_URL, hubItem.getLinks(OTHER_LINK_NAME).get(0));
	}

}
