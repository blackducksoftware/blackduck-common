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
