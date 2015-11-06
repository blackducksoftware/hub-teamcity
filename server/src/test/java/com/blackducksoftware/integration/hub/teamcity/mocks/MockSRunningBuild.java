package com.blackducksoftware.integration.hub.teamcity.mocks;

import jetbrains.buildServer.serverSide.SRunningBuild;

import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.teamcity.helper.TestBuildLog;

public class MockSRunningBuild {

    public static SRunningBuild getMockedSRunningBuild(TestBuildLog buildLog) {
        SRunningBuild mockedSRunningBuild = Mockito.mock(SRunningBuild.class);

        if (buildLog == null) {
            buildLog = new TestBuildLog();
        }

        Mockito.when(mockedSRunningBuild.getBuildLog()).thenReturn(buildLog);
        return mockedSRunningBuild;
    }
}
