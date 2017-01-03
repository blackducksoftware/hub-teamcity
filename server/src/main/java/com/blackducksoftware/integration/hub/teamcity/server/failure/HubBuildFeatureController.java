/**
 * Black Duck Hub Plug-In for TeamCity Server
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
