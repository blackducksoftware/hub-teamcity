/**
 * Black Duck Hub Plug-In for TeamCity Agent
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
package com.blackducksoftware.integration.hub.teamcity.agent.util;

import java.io.File;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildAgentSystemInfo;
import jetbrains.buildServer.agent.BuildParametersMap;
import jetbrains.buildServer.parameters.ValueResolver;

public class TestBuildAgentConfiguration implements BuildAgentConfiguration {
    private File toolDir;

    @Override
    public File getAgentToolsDirectory() {
        return toolDir;
    }

    public void setAgentToolsDirectory(final File toolDir) {
        this.toolDir = toolDir;
    }

    @Override
    public void addAlternativeAgentAddress(final String arg0) {
    }

    @Override
    public void addConfigurationParameter(final String arg0, final String arg1) {
    }

    @Override
    public void addCustomProperty(final String arg0, final String arg1) {
    }

    @Override
    public void addEnvironmentVariable(final String arg0, final String arg1) {
    }

    @Override
    public void addSystemProperty(final String arg0, final String arg1) {
    }

    @Override
    public File getAgentHomeDirectory() {
        return null;
    }

    @Override
    public File getAgentLibDirectory() {
        return null;
    }

    @Override
    public File getAgentLogsDirectory() {
        return null;
    }

    @Override
    public Map<String, String> getAgentParameters() {
        return null;
    }

    @Override
    public File getAgentPluginsDirectory() {
        return null;
    }

    @Override
    public File getAgentTempDirectory() {
        return null;
    }

    @Override
    public File getAgentUpdateDirectory() {
        return null;
    }

    @Override
    public String getAuthorizationToken() {
        return null;
    }

    @NotNull
    @Override
    public String getPingCode() {
        return null;
    }

    @Override
    public BuildParametersMap getBuildParameters() {
        return null;
    }

    @Override
    public File getBuildTempDirectory() {
        return null;
    }

    @Override
    public File getCacheDirectory(final String arg0) {
        return null;
    }

    @NotNull
    @Override
    public File getSystemDirectory() {
        return null;
    }

    @Override
    public Map<String, String> getConfigurationParameters() {
        return null;
    }

    @Override
    public Map<String, String> getCustomProperties() {
        return null;
    }

    @Override
    public String getEnv(final String arg0) {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getOwnAddress() {
        return null;
    }

    @Override
    public int getOwnPort() {
        return 0;
    }

    @Override
    public ValueResolver getParametersResolver() {
        return null;
    }

    @Override
    public int getServerConnectionTimeout() {
        return 0;
    }

    @Override
    public String getServerUrl() {
        return null;
    }

    @Override
    public BuildAgentSystemInfo getSystemInfo() {
        return null;
    }

    @Override
    public File getTempDirectory() {
        return null;
    }

    @Override
    public File getWorkDirectory() {
        return null;
    }

}
