package com.blackducksoftware.integration.hub.teamcity.server.failure;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

public class HubBuildFeatureController extends BaseController {
	@NotNull
	private final PluginDescriptor myDescriptor;

	public HubBuildFeatureController(@NotNull final PluginDescriptor descriptor,
			@NotNull final WebControllerManager web) {
		myDescriptor = descriptor;
		web.registerController(myDescriptor.getPluginResourcesPath("hubBuildFeatureEdit.html"), this);
	}

	@Nullable
	@Override
	protected ModelAndView doHandle(@NotNull final HttpServletRequest request,
			@NotNull final HttpServletResponse response) {
		final ModelAndView mw = new ModelAndView(
				myDescriptor.getPluginResourcesPath("failure/hubBuildFeatureEdit.jsp"));
		return mw;
	}

}
