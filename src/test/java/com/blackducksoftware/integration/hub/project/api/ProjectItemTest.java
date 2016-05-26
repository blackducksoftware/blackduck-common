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
package com.blackducksoftware.integration.hub.project.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.blackducksoftware.integration.hub.meta.MetaInformation;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class ProjectItemTest {

	@Test
	public void testProjectItem() {
		final String name1 = "Name1";
		final String source1 = "Source1";
		final String href1 = "href1";
		final MetaInformation metaInfo1 = new MetaInformation(null, href1, null);

		final String name2 = "Name2";
		final String source2 = "Source2";
		final String href2 = "href2";
		final MetaInformation metaInfo2 = new MetaInformation(null, href2, null);

		final ProjectItem item1 = new ProjectItem(name1, source1, metaInfo1);
		final ProjectItem item2 = new ProjectItem(name2, source2, metaInfo2);
		final ProjectItem item3 = new ProjectItem(name1, source1, metaInfo1);

		assertEquals(name1, item1.getName());
		assertEquals(source1, item1.getSource());
		assertEquals(href1, item1.get_meta().getHref());
		assertEquals(metaInfo1, item1.get_meta());

		assertEquals(name2, item2.getName());
		assertEquals(source2, item2.getSource());
		assertEquals(href2, item2.get_meta().getHref());
		assertEquals(metaInfo2, item2.get_meta());

		assertTrue(!item1.equals(item2));
		assertTrue(item1.equals(item3));

		EqualsVerifier.forClass(ProjectItem.class).suppress(Warning.NONFINAL_FIELDS)
		.suppress(Warning.STRICT_INHERITANCE).verify();

		assertTrue(item1.hashCode() != item2.hashCode());
		assertEquals(item1.hashCode(), item3.hashCode());

		final StringBuilder builder = new StringBuilder();
		builder.append("ProjectItem [name=");
		builder.append(item1.getName());
		builder.append(", source=");
		builder.append(item1.getSource());
		builder.append(", _meta=");
		builder.append(item1.get_meta());
		builder.append("]");

		assertEquals(builder.toString(), item1.toString());
	}

}
