package com.blackducksoftware.integration.hub.teamcity.server.global;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PositionConstraint;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;
import jetbrains.buildServer.web.openapi.WebControllerManager;

public class HubGlobalServerConfigTab extends SimpleCustomTab {
	private final ServerHubConfigPersistenceManager configPersistenceManager;

	public HubGlobalServerConfigTab(@NotNull final WebControllerManager controllerManager,
			@NotNull final HubServerListener codecenterListener) {
		super(controllerManager, PlaceId.ADMIN_SERVER_CONFIGURATION_TAB, "hub", "hubGlobalServerConfigTab.jsp", "Hub");
		setPosition(PositionConstraint.after("serverConfigGeneral"));
		register();

		configPersistenceManager = codecenterListener.getConfigManager();
		controllerManager.registerController("/admin/hub/serverHubConfigTab.html",
				new HubGlobalServerConfigController(configPersistenceManager));
	}

	@Override
	public void fillModel(@NotNull final Map<String, Object> model, @NotNull final HttpServletRequest request) {
		super.fillModel(model, request);
		model.put("hubConfigPersistenceManager", configPersistenceManager);
	}

}
