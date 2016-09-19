package com.blackducksoftware.integration.hub.test;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URL;

public class TestUtils {
	public static InputStream getInputStreamFromClasspathFile(final String name) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
	}

	public static OutputStream getOutputStreamFromClasspathFile(final String name) {
		try {
			final URL url = Thread.currentThread().getContextClassLoader().getResource(name);
			final File file = new File(url.toURI().getPath());
			return new FileOutputStream(file);
		} catch (final Exception e) {
			fail("Could not get OutputStream from: " + name + " msg: " + e.getMessage());
			return null;
		}
	}

	public static Object getField(final Object obj, final String fieldName) {
		Field field;
		try {
			field = obj.getClass().getDeclaredField(fieldName);
			final boolean originalValue = field.isAccessible();
			try {
				field.setAccessible(true);
				return field.get(obj);
			} catch (final Exception e) {
				throw e;
			} finally {
				field.setAccessible(originalValue);
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			fail(String.format("Couldn't get field %s on %s", fieldName, obj.toString()));
		}

		return null;
	}

	public static void setField(final Object obj, final String fieldName, final Object value) {
		try {
			final Field field = obj.getClass().getDeclaredField(fieldName);
			final boolean originalValue = field.isAccessible();
			try {
				field.setAccessible(true);
				field.set(obj, value);
			} catch (final Exception e) {
				throw e;
			} finally {
				field.setAccessible(originalValue);
			}
		} catch (final Exception e) {
			fail(String.format("Couldn't set field %s on %s to %s", fieldName, obj.toString(), value.toString()));
		}
	}

}
