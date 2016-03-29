package com.blackducksoftware.integration.hub.util;

import java.io.InputStream;
import java.io.OutputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class XStreamHelper<T extends Object> {
    private XStream xStream = new XStream(new DomDriver());

    public XStream getXStream() {
        return xStream;
    }

    public String toXML(T obj) {
        return xStream.toXML(obj);
    }

    public void toXML(T obj, OutputStream outputStream) {
        xStream.toXML(obj, outputStream);
    }

    public T fromXML(String s) {
        return (T) xStream.fromXML(s);
    }

    public T fromXML(InputStream inputStream) {
        return (T) xStream.fromXML(inputStream);
    }

}
