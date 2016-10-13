/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
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
