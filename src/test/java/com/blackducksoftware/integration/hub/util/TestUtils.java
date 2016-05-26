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
