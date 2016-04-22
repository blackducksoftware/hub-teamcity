/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.teamcity.mocks;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.lang3.StringUtils;
import org.mockito.Mockito;

import jetbrains.buildServer.serverSide.ServerPaths;

public class MockServerPaths {
	public static ServerPaths getMockedServerPaths(final String parentDir, final String configDir)
			throws UnsupportedEncodingException {
		final ServerPaths mockedServerPaths = Mockito.mock(ServerPaths.class);

		final String confDir = getConfigDirectory(parentDir, configDir);

		Mockito.when(mockedServerPaths.getConfigDir()).thenReturn(confDir);
		return mockedServerPaths;
	}

	public static String getConfigDirectory(final String parentDir, final String configDir)
			throws UnsupportedEncodingException {
		String confDir = URLDecoder
				.decode(MockServerPaths.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
		confDir = confDir.substring(0, confDir.indexOf("/target"));
		confDir = confDir + "/test-workspace";

		if (StringUtils.isNotBlank(parentDir)) {
			confDir = confDir + "/" + parentDir;
		}

		if (StringUtils.isNotBlank(configDir)) {
			confDir = confDir + "/" + configDir;
		}
		return confDir;
	}

}
