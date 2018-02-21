/**
 * hub-common
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.blackducksoftware.integration.hub.service.model;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Remember to close the Streams when they are done being used.
 */
public class StreamRedirectThread extends Thread {
    private final InputStream in;

    private final OutputStream out;

    public StreamRedirectThread(final InputStream in, final OutputStream out) {
        super("Stream Redirect Thread");
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            int i;
            while ((i = in.read()) >= 0) {
                if (i == -1) {
                    break;
                }
                out.write(i);
            }
        } catch (final IOException e) {
            // Ignore
        }
    }

}
