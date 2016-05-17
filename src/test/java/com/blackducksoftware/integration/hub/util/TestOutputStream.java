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
