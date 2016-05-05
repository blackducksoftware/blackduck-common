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
package com.blackducksoftware.integration.hub.builder;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractBuilder<Key, Type> {

	public abstract ValidationResults<Key, Type> build();

	public abstract ValidationResults<Key, Type> assertValid();

	protected int stringToInteger(final String integer, final Integer defaultInt) {
		final String integerString = StringUtils.trimToNull(integer);
		if (integerString != null) {
			try {
				return Integer.valueOf(integerString);
			} catch (final NumberFormatException e) {
				if (defaultInt != null) {
					throw new IllegalArgumentException("The String : " + integer + " , is not an Integer.", e);
				}
			}
		}
		return defaultInt;
	}
}
