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
package com.blackducksoftware.integration.hub.teamcity.server.runner.scan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.blackducksoftware.integration.hub.teamcity.common.HubBundle;
import com.blackducksoftware.integration.hub.teamcity.server.global.HubServerListener;
import com.blackducksoftware.integration.hub.teamcity.server.runner.BaseRunType;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

public class HubRunner extends BaseRunType {
    public HubRunner(@NotNull final RunTypeRegistry runTypeRegistry,
            @NotNull final WebControllerManager webControllerManager, @NotNull final PluginDescriptor pluginDescriptor,
            @NotNull final HubServerListener serverListener) {
        super(webControllerManager, pluginDescriptor, serverListener.getConfigManager());
        runTypeRegistry.registerRunType(this);
        registerView("hubRunnerView.html", "bdHubRunner/hubRunnerView.jsp");
        registerEdit("hubRunnerEdit.html", "bdHubRunner/hubRunnerEdit.jsp");
    }

    @Override
    public String getDescription() {
        return HubBundle.RUNNER_DESCRIPTION;
    }

    @Override
    public String getDisplayName() {
        return HubBundle.RUNNER_DISPLAY_NAME;
    }

    @Override
    public String getType() {
        return HubBundle.RUNNER_TYPE;
    }

    @Override
    public Map<String, String> getDefaultRunnerProperties() {
        return null;
    }

    @Override
    @Nullable
    public PropertiesProcessor getRunnerPropertiesProcessor() {
        return new PropertiesProcessor() {
            @Override
            public Collection<InvalidProperty> process(final Map<String, String> properties) {
                final Collection<InvalidProperty> result = new ArrayList<InvalidProperty>();

                return result;
            }
        };
    }

}
