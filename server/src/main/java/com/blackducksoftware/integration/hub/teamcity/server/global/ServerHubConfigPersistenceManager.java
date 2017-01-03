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
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.blackducksoftware.integration.hub.api.version.DistributionEnum;
import com.blackducksoftware.integration.hub.api.version.PhaseEnum;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.google.gson.Gson;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.crypt.RSACipher;

public class ServerHubConfigPersistenceManager {
    private static final String CONFIG_FILE_NAME = "hub-config.json";

    private Gson gson;

    private final File configFile;

    private HubServerConfig hubServerConfig;

    public ServerHubConfigPersistenceManager(@NotNull final ServerPaths serverPaths) {
        gson = new Gson();
        configFile = new File(serverPaths.getConfigDir(), CONFIG_FILE_NAME);
        loadSettings();
    }

    public File getConfigFile() {
        return configFile;
    }

    public HubServerConfig getHubServerConfig() {
        return hubServerConfig;
    }

    public void setHubServerConfig(HubServerConfig hubServerConfig) {
        this.hubServerConfig = hubServerConfig;
    }

    public void loadSettings() {
        if (configFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
                hubServerConfig = gson.fromJson(reader, HubServerConfig.class);
            } catch (final IOException e) {
                Loggers.SERVER.error("Failed to load Hub config file: " + configFile, e);
            }
        }
    }

    public void persist() throws IOException {
        if (!configFile.getParentFile().exists() && configFile.getParentFile().mkdirs()) {
            Loggers.SERVER.info("Directory created for the Hub configuration file at : "
                    + configFile.getParentFile().getCanonicalPath());
        } else if (configFile.exists() && configFile.delete()) {
            Loggers.SERVER.info("Old Hub configuration file removed, to be replaced by a new configuration.");
        }

        configFile.createNewFile();

        String hubServerConfigJson = gson.toJson(hubServerConfig);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
            writer.write(hubServerConfigJson);
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

    public List<String> getPhaseOptions() {
        final List<String> phaseList = new ArrayList<>();
        for (final PhaseEnum phase : PhaseEnum.values()) {
            if (phase != PhaseEnum.UNKNOWNPHASE) {
                phaseList.add(phase.getDisplayValue());
            }
        }

        return phaseList;
    }

    public List<String> getDistributionOptions() {
        final List<String> distributionList = new ArrayList<>();
        for (final DistributionEnum distribution : DistributionEnum.values()) {
            if (distribution != DistributionEnum.UNKNOWNDISTRIBUTION) {
                distributionList.add(distribution.getDisplayValue());
            }
        }

        return distributionList;
    }

}
