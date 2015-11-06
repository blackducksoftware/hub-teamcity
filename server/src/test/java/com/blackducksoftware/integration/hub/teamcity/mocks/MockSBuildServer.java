package com.blackducksoftware.integration.hub.teamcity.mocks;

import jetbrains.buildServer.serverSide.SBuildServer;

import org.mockito.Mockito;

public class MockSBuildServer {

    public static SBuildServer getMockedSBuildServer(final String serverVersion) {
        SBuildServer mockedSBuildServer = Mockito.mock(SBuildServer.class);

        Mockito.when(mockedSBuildServer.getFullServerVersion()).thenReturn(serverVersion);
        return mockedSBuildServer;
    }

}
