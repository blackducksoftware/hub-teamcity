package com.blackducksoftware.integration.hub.teamcity.mocks;

import org.mockito.Mockito;

import jetbrains.buildServer.serverSide.SBuildServer;

public class MockSBuildServer {
	public static SBuildServer getMockedSBuildServer(final String serverVersion) {
		final SBuildServer mockedSBuildServer = Mockito.mock(SBuildServer.class);

		Mockito.when(mockedSBuildServer.getFullServerVersion()).thenReturn(serverVersion);
		return mockedSBuildServer;
	}

}
