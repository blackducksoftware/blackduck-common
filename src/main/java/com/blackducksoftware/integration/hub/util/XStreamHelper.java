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
