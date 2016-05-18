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
package com.blackducksoftware.integration.hub.util;

import java.io.InputStream;
import java.io.OutputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XStreamHelper<T extends Object> {
	private final XStream xStream = new XStream(new DomDriver());

	public XStream getXStream() {
		return xStream;
	}

	public String toXML(final T obj) {
		return xStream.toXML(obj);
	}

	public void toXML(final T obj, final OutputStream outputStream) {
		xStream.toXML(obj, outputStream);
	}

	@SuppressWarnings("unchecked")
	public T fromXML(final String s) {
		return (T) xStream.fromXML(s);
	}

	@SuppressWarnings("unchecked")
	public T fromXML(final InputStream inputStream) {
		return (T) xStream.fromXML(inputStream);
	}

}
