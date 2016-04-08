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

import java.util.Locale;
import java.util.ResourceBundle;

public class HubResourceBundleHelper {
	private static final String RESOURCE_BASE_NAME = "com.blackducksoftware.integration.hub.resources.HubResources";

	private Locale locale = Locale.US;

	private String keyPrefix;

	public String getString(String key) {
		if (null != keyPrefix) {
			key = keyPrefix + "." + key;
		}

		return ResourceBundle.getBundle(RESOURCE_BASE_NAME, locale).getString(key);
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(final Locale locale) {
		this.locale = locale;
	}

	public String getKeyPrefix() {
		return keyPrefix;
	}

	public void setKeyPrefix(final String keyPrefix) {
		this.keyPrefix = keyPrefix;
	}

}
