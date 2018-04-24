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
package com.blackducksoftware.integration.hub.teamcity.server.global;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.EventDispatcher;

public class HubServerListener extends BuildServerAdapter {
    private final SBuildServer server;

    private final ServerHubConfigPersistenceManager configPersistenceManager;

    public HubServerListener(@NotNull final EventDispatcher<BuildServerListener> dispatcher,
            @NotNull final SBuildServer server, @NotNull final ServerPaths serverPaths) {
        this.server = server;

        dispatcher.addListener(this);

        configPersistenceManager = new ServerHubConfigPersistenceManager(serverPaths);
    }

    @Override
    public void serverStartup() {
        Loggers.SERVER.info("The Black Duck Software Hub Plugin is running on server version '"
                + server.getFullServerVersion() + "'.");
    }

    public ServerHubConfigPersistenceManager getConfigManager() {
        return configPersistenceManager;
    }

}
