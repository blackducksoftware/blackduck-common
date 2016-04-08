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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

public class HostnameHelper {

	public static String getMyHostname() {
		String hostName = null;

		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException e) {
			try {
				// Get the network interfaces for this machine
				final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

				while (interfaces.hasMoreElements()) {
					final NetworkInterface nic = interfaces.nextElement();
					// Get the addresses for this network interface
					final Enumeration<InetAddress> addresses = nic.getInetAddresses();
					// will loop through the addresses until it finds a non loop back address that has a host name
					while (hostName == null && addresses.hasMoreElements()) {
						final InetAddress address = addresses.nextElement();
						// if the address is not a loopback address then get the host name
						if (!address.isLoopbackAddress()) {
							hostName = address.getHostName();
							break;
						}
					}
					if (hostName != null) {
						break;
					}
				}
			} catch (final SocketException se) {
				// ignore this
				return hostName;
			}
		}
		return hostName;
	}
}
