/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
		configPersistenceManager.loadSettings();
		model.put("hubConfigPersistenceManager", configPersistenceManager);
	}

}
