/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *******************************************************************************/
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
