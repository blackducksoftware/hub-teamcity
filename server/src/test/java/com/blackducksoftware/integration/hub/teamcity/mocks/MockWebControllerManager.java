package com.blackducksoftware.integration.hub.teamcity.mocks;

import org.mockito.Mockito;
import org.springframework.web.servlet.mvc.Controller;

import jetbrains.buildServer.web.openapi.WebControllerManager;

public class MockWebControllerManager {
	public static WebControllerManager getMockedWebControllerManager() {
		final WebControllerManager mockedWebControllerManager = Mockito.mock(WebControllerManager.class);

		Mockito.doNothing().when(mockedWebControllerManager).registerController(Mockito.anyString(),
				Mockito.any(Controller.class));

		return mockedWebControllerManager;
	}

}
