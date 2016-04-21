package com.blackducksoftware.integration.hub.teamcity.mocks;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import jetbrains.buildServer.web.openapi.PluginDescriptor;

public class MockPluginDescriptor {
	public static PluginDescriptor getMockedPluginDescriptor() {
		final PluginDescriptor mockedPluginDescriptor = Mockito.mock(PluginDescriptor.class);

		Mockito.doAnswer(new Answer<String>() {
			@Override
			public String answer(final InvocationOnMock invocation) {
				final Object[] args = invocation.getArguments();

				return (String) args[0];
			}
		}).when(mockedPluginDescriptor).getPluginResourcesPath(Mockito.anyString());

		return mockedPluginDescriptor;
	}

}
