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
