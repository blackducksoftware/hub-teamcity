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
package com.blackducksoftware.integration.hub.teamcity.mocks;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.mockito.Mockito;

import jetbrains.buildServer.serverSide.ServerPaths;

public class MockServerPaths {
	public static ServerPaths getMockedServerPaths(final String parentDir, final String configDir) {
		final ServerPaths mockedServerPaths = Mockito.mock(ServerPaths.class);

		final String confDir = getConfigDirectory(parentDir, configDir);

		Mockito.when(mockedServerPaths.getConfigDir()).thenReturn(confDir);
		return mockedServerPaths;
	}

	public static String getConfigDirectory(final String parentDir, final String configDir) {
		String confDir = MockServerPaths.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		confDir = confDir.substring(0, confDir.indexOf("/target"));
		confDir = confDir + "/test-workspace";

		if (StringUtils.isNotBlank(parentDir)) {
			confDir = confDir + File.separator + parentDir;
		}

		if (StringUtils.isNotBlank(configDir)) {
			confDir = confDir + File.separator + configDir;
		}
		return confDir;
	}

}
