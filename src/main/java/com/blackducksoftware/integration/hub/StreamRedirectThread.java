package com.blackducksoftware.integration.hub;

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
