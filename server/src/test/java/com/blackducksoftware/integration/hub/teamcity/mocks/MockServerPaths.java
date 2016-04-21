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
