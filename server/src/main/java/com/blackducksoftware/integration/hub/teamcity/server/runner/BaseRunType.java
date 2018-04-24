/**
 * Black Duck Hub Plug-In for TeamCity Server
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
 */
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
