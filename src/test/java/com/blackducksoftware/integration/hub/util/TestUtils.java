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

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URL;

public class TestUtils {
    public static InputStream getInputStreamFromClasspathFile(String name) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    }

    public static OutputStream getOutputStreamFromClasspathFile(String name) {
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(name);
            File file = new File(url.toURI().getPath());
            return new FileOutputStream(file);
        } catch (Exception e) {
            fail("Could not get OutputStream from: " + name + " msg: " + e.getMessage());
            return null;
        }
    }

    public static <T> void setValue(Class<T> clazz, T instance, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            boolean originalValue = field.isAccessible();
            try {
                field.setAccessible(true);
                field.set(instance, value);
            } catch (Exception e) {
                throw e;
            } finally {
                field.setAccessible(originalValue);
            }
        } catch (Exception e) {
            fail("Could not set field: " + fieldName + " with value: " + value + " msg: " + e.getMessage());
        }
    }

}
