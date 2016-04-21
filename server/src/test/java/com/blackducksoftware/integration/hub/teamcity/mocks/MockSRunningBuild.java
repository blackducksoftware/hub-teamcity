package com.blackducksoftware.integration.hub.teamcity.mocks;

import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.teamcity.helper.TestBuildLog;

import jetbrains.buildServer.serverSide.SRunningBuild;

public class MockSRunningBuild {
	public static SRunningBuild getMockedSRunningBuild(TestBuildLog buildLog) {
		final SRunningBuild mockedSRunningBuild = Mockito.mock(SRunningBuild.class);

		if (buildLog == null) {
			buildLog = new TestBuildLog();
		}

		Mockito.when(mockedSRunningBuild.getBuildLog()).thenReturn(buildLog);
		return mockedSRunningBuild;
	}

}
