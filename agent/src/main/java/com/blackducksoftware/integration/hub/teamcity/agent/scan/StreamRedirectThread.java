package com.blackducksoftware.integration.hub.teamcity.agent.scan;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Remember to close the Streams when they are done being used.
 *
 * @author jrichard
 */
public class StreamRedirectThread extends Thread {
    private final InputStream in;

    private final OutputStream out;

    public StreamRedirectThread(InputStream in, OutputStream out) {
        super("Stream Redirect Thread");
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {
        try {
            int i;
            while ((i = in.read()) >= 0) {
                out.write(i);
            }
        } catch (IOException e) {
            // Ignore
        }
    }
}
