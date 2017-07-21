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
package com.blackducksoftware.integration.hub.teamcity.server.global;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionDistributionEnum;
import com.blackducksoftware.integration.hub.model.enumeration.ProjectVersionRequestPhaseEnum;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.crypt.RSACipher;

public class ServerHubConfigPersistenceManager {
    private static final String CONFIG_FILE_NAME = "hub-config.json";

    private final Gson gson;

    private final JsonParser jsonParser;

    private final File configFile;

    private HubServerConfig hubServerConfig;

    private boolean hubWorkspaceCheck;

    public ServerHubConfigPersistenceManager(@NotNull final ServerPaths serverPaths) {
        gson = new Gson();
        jsonParser = new JsonParser();
        configFile = new File(serverPaths.getConfigDir(), CONFIG_FILE_NAME);
        loadSettings();
    }

    public File getConfigFile() {
        return configFile;
    }

    public HubServerConfig getHubServerConfig() {
        return hubServerConfig;
    }

    public void setHubServerConfig(final HubServerConfig hubServerConfig) {
        this.hubServerConfig = hubServerConfig;
    }

    public boolean isHubWorkspaceCheck() {
        return hubWorkspaceCheck;
    }

    public void setHubWorkspaceCheck(final boolean hubWorkspaceCheck) {
        this.hubWorkspaceCheck = hubWorkspaceCheck;
    }

    public List<String> getPhaseList() {
        final List<String> phaseList = new LinkedList<>();
        for (final ProjectVersionRequestPhaseEnum phase : ProjectVersionRequestPhaseEnum.values()) {
            phaseList.add(phase.toString());
        }
        return phaseList;
    }

    public List<String> getDistributionList() {
        final List<String> distributionList = new LinkedList<>();
        for (final ProjectVersionDistributionEnum dist : ProjectVersionDistributionEnum.values()) {
            distributionList.add(dist.toString());
        }
        return distributionList;
    }

    public void loadSettings() {
        if (configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                final JsonObject globalConfigJson = jsonParser.parse(reader).getAsJsonObject();
                try {
                    if (globalConfigJson.has("hubServerConfig")) {
                        setHubServerConfig(gson.fromJson(globalConfigJson.get("hubServerConfig"), HubServerConfig.class));
                        setHubWorkspaceCheck(globalConfigJson.get("hubWorkspaceCheck").getAsBoolean());
                    } else {
                        throw new JsonParseException("The Hub Teamcity configuration must be from a previous version.");
                    }
                } catch (final JsonParseException e) {
                    // try to load the old config
                    setHubServerConfig(gson.fromJson(globalConfigJson, HubServerConfig.class));
                    setHubWorkspaceCheck(true);
                }
            } catch (final IOException e) {
                Loggers.SERVER.error("Failed to load Hub config file: " + configFile, e);
            }
        }
    }

    public void persist() throws IOException {
        if (!configFile.getParentFile().exists() && configFile.getParentFile().mkdirs()) {
            Loggers.SERVER.info("Directory created for the Hub configuration file at : " + configFile.getParentFile().getCanonicalPath());
        } else if (configFile.exists() && configFile.delete()) {
            Loggers.SERVER.info("Old Hub configuration file removed, to be replaced by a new configuration.");
        }

        configFile.createNewFile();

        final JsonObject globalConfigJson = new JsonObject();
        final JsonElement hubServerConfigJson = gson.toJsonTree(getHubServerConfig(), HubServerConfig.class);
        globalConfigJson.add("hubServerConfig", hubServerConfigJson);
        globalConfigJson.addProperty("hubWorkspaceCheck", hubWorkspaceCheck);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            writer.write(gson.toJson(globalConfigJson));
        } catch (final IOException e) {
            Loggers.SERVER.error("Failed to save Hub config file: " + configFile, e);
        }
    }

    public String getHexEncodedPublicKey() {
        return RSACipher.getHexEncodedPublicKey();
    }

    public String getRandom() {
        return String.valueOf(Math.random());
    }

}
