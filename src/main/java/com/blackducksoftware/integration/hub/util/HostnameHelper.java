/*
 * Copyright (C) 2016 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
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
                    // will loop through the addresses until it finds a non loop
                    // back address that has a host name
                    while (hostName == null && addresses.hasMoreElements()) {
                        final InetAddress address = addresses.nextElement();
                        // if the address is not a loopback address then get the
                        // host name
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
