/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
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
