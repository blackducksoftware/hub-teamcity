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

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import com.blackducksoftware.integration.hub.teamcity.server.global.ServerHubConfigPersistenceManager;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunType;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

public abstract class BaseRunType extends RunType {
	private final PluginDescriptor pluginDescriptor;
	private final WebControllerManager webControllerManager;
	private final ServerHubConfigPersistenceManager serverPeristanceManager;
	private String viewUrl;
	private String editUrl;

	public BaseRunType(@NotNull final WebControllerManager webControllerManager,
			@NotNull final PluginDescriptor pluginDescriptor,
			@NotNull final ServerHubConfigPersistenceManager serverPeristanceManager) {
		this.webControllerManager = webControllerManager;
		this.pluginDescriptor = pluginDescriptor;
		this.serverPeristanceManager = serverPeristanceManager;
	}

	@Override
	public String getEditRunnerParamsJspFilePath() {
		return editUrl;
	}

	@Override
	public String getViewRunnerParamsJspFilePath() {
		return viewUrl;
	}

	@Override
	public Map<String, String> getDefaultRunnerProperties() {
		return null;
	}

	protected void registerEdit(@NotNull final String url, @NotNull final String jsp) {
		editUrl = pluginDescriptor.getPluginResourcesPath(url);
		final String actualJsp = pluginDescriptor.getPluginResourcesPath(jsp);
		webControllerManager.registerController(editUrl,
				new HubRunTypeConfigController(editUrl, actualJsp, serverPeristanceManager));
	}

	protected void registerView(@NotNull final String url, @NotNull final String jsp) {
		viewUrl = pluginDescriptor.getPluginResourcesPath(url);
		final String actualJsp = pluginDescriptor.getPluginResourcesPath(jsp);

		webControllerManager.registerController(viewUrl, new BaseController() {
			@Override
			protected ModelAndView doHandle(final HttpServletRequest request, final HttpServletResponse response) {
				final ModelAndView modelAndView = new ModelAndView(actualJsp);
				modelAndView.getModel().put("controllerUrl", viewUrl);
				return modelAndView;
			}
		});
	}

	@Override
	public PropertiesProcessor getRunnerPropertiesProcessor() {
		return null;
	}

}
