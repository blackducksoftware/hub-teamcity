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
package com.blackducksoftware.integration.hub.teamcity.server.runner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import com.blackducksoftware.integration.hub.teamcity.server.global.ServerHubConfigPersistenceManager;

import jetbrains.buildServer.controllers.BaseFormXmlController;

public class HubRunTypeConfigController extends BaseFormXmlController {
	private final ServerHubConfigPersistenceManager serverPeristanceManager;
	private final String actualUrl;
	private final String actualJsp;

	public HubRunTypeConfigController(@NotNull final String actualUrl, @NotNull final String actualJsp,
			@NotNull final ServerHubConfigPersistenceManager serverPeristanceManager) {
		this.actualUrl = actualUrl;
		this.actualJsp = actualJsp;
		this.serverPeristanceManager = serverPeristanceManager;
	}

	@Override
	protected ModelAndView doHandle(final HttpServletRequest request, final HttpServletResponse response)
			throws Exception {
		if (isPost(request) && (request.getParameter("onServerChange") != null)) {
			return super.doHandle(request, response);
		}
		return doGet(request, response);
	}

	@Override
	protected ModelAndView doGet(final HttpServletRequest request, final HttpServletResponse response) {
		final ModelAndView modelAndView = new ModelAndView(actualJsp);
		modelAndView.getModel().put("runnerType", request.getParameter("runnerType"));
		modelAndView.getModel().put("controllerUrl", actualUrl);
		modelAndView.getModel().put("hubConfigPersistenceManager", serverPeristanceManager);
		return modelAndView;
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response,
			final Element xmlResponse) {
	}

}
