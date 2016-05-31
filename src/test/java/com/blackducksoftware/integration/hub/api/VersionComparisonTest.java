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
package com.blackducksoftware.integration.hub.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class VersionComparisonTest {

	@Test
	public void testVersionComparison() {
		final String consumerVersion1 = "comsumerVersion1";
		final String producerVersion1 = "producerVersion1";
		final Integer numericResult1 = 1;
		final String operatorResult1 = "operator1";

		final String consumerVersion2 = "comsumerVersion2";
		final String producerVersion2 = "producerVersion2";
		final Integer numericResult2 = 2;
		final String operatorResult2 = "operator2";

		final VersionComparison item1 = new VersionComparison(consumerVersion1, producerVersion1, numericResult1, operatorResult1);
		final VersionComparison item2 = new VersionComparison(consumerVersion2, producerVersion2, numericResult2, operatorResult2);
		final VersionComparison item3 = new VersionComparison(consumerVersion1, producerVersion1, numericResult1, operatorResult1);

		assertEquals(consumerVersion1, item1.getConsumerVersion());
		assertEquals(producerVersion1, item1.getProducerVersion());
		assertEquals(numericResult1, item1.getNumericResult());
		assertEquals(operatorResult1, item1.getOperatorResult());

		assertEquals(consumerVersion2, item2.getConsumerVersion());
		assertEquals(producerVersion2, item2.getProducerVersion());
		assertEquals(numericResult2, item2.getNumericResult());
		assertEquals(operatorResult2, item2.getOperatorResult());

		assertTrue(!item1.equals(item2));
		assertTrue(item1.equals(item3));

		EqualsVerifier.forClass(VersionComparison.class).suppress(Warning.STRICT_INHERITANCE).verify();

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());

		final StringBuilder builder = new StringBuilder();
		builder.append("VersionComparison [consumerVersion=");
		builder.append(item1.getConsumerVersion());
		builder.append(", producerVersion=");
		builder.append(item1.getProducerVersion());
		builder.append(", numericResult=");
		builder.append(item1.getNumericResult());
		builder.append(", operatorResult=");
		builder.append(item1.getOperatorResult());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());
	}

}
