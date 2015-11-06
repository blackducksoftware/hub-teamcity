package com.blackducksoftware.integration.hub.teamcity.mocks;

import jetbrains.buildServer.web.openapi.WebControllerManager;

import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.Controller;

public class MockWebControllerManager {

    public static WebControllerManager getMockedWebControllerManager() {
        WebControllerManager mockedWebControllerManager = Mockito.mock(WebControllerManager.class);

        Mockito.doNothing().when(mockedWebControllerManager).registerController(Mockito.anyString(), Mockito.any(Controller.class));

        return mockedWebControllerManager;
    }

}
