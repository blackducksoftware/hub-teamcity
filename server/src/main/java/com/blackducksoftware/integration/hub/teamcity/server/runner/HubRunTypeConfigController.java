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
