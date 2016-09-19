package com.blackducksoftware.integration.hub.policy.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.blackducksoftware.integration.hub.api.policy.PolicyValue;

public class PolicyValueTest {
	@Test
	public void testPolicyValue() {
		final String label1 = "label1";
		final String value1 = "value1";

		final String label2 = "label2";
		final String value2 = "value2";

		final PolicyValue item1 = new PolicyValue(label1, value1);
		final PolicyValue item2 = new PolicyValue(label2, value2);
		final PolicyValue item3 = new PolicyValue(label1, value1);

		assertEquals(label1, item1.getLabel());
		assertEquals(value1, item1.getValue());

		assertEquals(label2, item2.getLabel());
		assertEquals(value2, item2.getValue());

		assertTrue(!item1.equals(item2));
		assertTrue(item1.equals(item3));

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());
	}

}
