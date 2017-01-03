/**
 * Hub Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
