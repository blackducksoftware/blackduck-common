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
package com.blackducksoftware.integration.hub.meta;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

import org.junit.Test;

public class MetaInformationTest {


	@Test
	public void testMetaInformation(){
		final String rel1 = "rel1";
		final String href1 = "href1";

		final String rel2 = "rel2";
		final String href2 = "href2";

		final MetaLink link1 = new MetaLink(rel1, href1);
		final List<MetaLink> links1 = new ArrayList<MetaLink>();
		links1.add(link1);

		final MetaLink link2 = new MetaLink(rel2, href2);
		final List<MetaLink> links2 = new ArrayList<MetaLink>();
		links2.add(link2);

		final List<String> allow1 = new ArrayList<String>();
		allow1.add("GET");

		final List<String> allow2 = new ArrayList<String>();
		allow2.add("PUT");


		final MetaInformation item1 = new MetaInformation(allow1, href1, links1);
		final MetaInformation item2 = new MetaInformation(allow2, href2, links2);
		final MetaInformation item3 = new MetaInformation(allow1, href1, links1);

		assertEquals(allow1, item1.getAllow());
		assertEquals(href1, item1.getHref());
		assertEquals(links1, item1.getLinks());
		assertEquals(allow2, item2.getAllow());
		assertEquals(href2, item2.getHref());
		assertEquals(links2, item2.getLinks());

		assertTrue(item1.equals(item3));
		assertTrue(!item1.equals(item2));

		EqualsVerifier.forClass(MetaInformation.class).suppress(Warning.STRICT_INHERITANCE).verify();

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());


		final StringBuilder builder = new StringBuilder();
		builder.append("MetaInformation [allow=");
		builder.append(item1.getAllow());
		builder.append(", href=");
		builder.append(item1.getHref());
		builder.append(", links=");
		builder.append(item1.getLinks());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());

	}
}
