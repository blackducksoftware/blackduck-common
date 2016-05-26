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

import java.io.IOException;
import java.io.OutputStream;

public class TestOutputStream extends OutputStream {
    public StringBuilder buffer = new StringBuilder();

    public void resetBuffer() {
        buffer = new StringBuilder();
    }

    @Override
    public void write(int b) throws IOException {

        String stringAcii = new String(Character.toChars(b));
        buffer.append(stringAcii);
    }

    @Override
    public void write(byte[] byteArray) throws IOException {
        String currentLine = new String(byteArray, "UTF-8");
        buffer.append(currentLine);
    }

    @Override
    public void write(byte[] byteArray, int offset, int length) throws IOException {
        String currentLine = new String(byteArray, offset, length, "UTF-8");
        buffer.append(currentLine);
    }

}
