package com.blackducksoftware.integration.hub.teamcity.mocks;

import jetbrains.buildServer.web.openapi.PluginDescriptor;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class MockPluginDescriptor {

    public static PluginDescriptor getMockedPluginDescriptor() {
        PluginDescriptor mockedPluginDescriptor = Mockito.mock(PluginDescriptor.class);

        Mockito.doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();

                return (String) args[0];
            }
        }).when(mockedPluginDescriptor).getPluginResourcesPath(Mockito.anyString());

        return mockedPluginDescriptor;
    }

}
