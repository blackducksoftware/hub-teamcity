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
package com.blackducksoftware.integration.hub.teamcity.agent.util;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildParametersMap;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.ToolCannotBeFoundException;
import jetbrains.buildServer.parameters.ValueResolver;

public class TestBuildRunnerContext implements BuildRunnerContext {
    private Map<String, String> runnerParameters = null;

    private File workingDirectory;

    private Map<String, String> environmentParameters = null;

    private Map<String, String> configParameters = null;

    private Map<String, String> systemParameters = null;

    private AgentRunningBuild build;

    public void setWorkingDirectory(final File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public TestBuildRunnerContext() {
        runnerParameters = new HashMap<String, String>();
        environmentParameters = new HashMap<String, String>();
        configParameters = new HashMap<String, String>();
        systemParameters = new HashMap<String, String>();
    }

    @Override
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public void addRunnerParameter(final String key, final String value) {
        runnerParameters.put(key, value);
    }

    public void removeRunnerParameter(final String key) {
        runnerParameters.remove(key);
    }

    public String getRunnerParameter(final String key) {
        return runnerParameters.get(key);
    }

    @Override
    public Map<String, String> getRunnerParameters() {
        return runnerParameters;
    }

    @Override
    public void addConfigParameter(final String key, final String value) {
        configParameters.put(key, value);
    }

    @Override
    public void addEnvironmentVariable(final String key, final String value) {
        environmentParameters.put(key, value);
    }

    @Override
    public void addSystemProperty(final String key, final String value) {
        systemParameters.put(key, value);
    }

    @Override
    public AgentRunningBuild getBuild() {
        return build;
    }

    public void setBuild(final AgentRunningBuild build) {
        this.build = build;
    }

    @Override
    public BuildParametersMap getBuildParameters() {
        final BuildParametersMap buildParameterMap = new BuildParametersMap() {
            @Override
            public Map<String, String> getAllParameters() {
                // TODO Auto-generated function stub
                return null;
            }

            @Override
            public Map<String, String> getEnvironmentVariables() {
                return environmentParameters;
            }

            @Override
            public Map<String, String> getSystemProperties() {
                return systemParameters;
            }
        };

        return buildParameterMap;
    }

    @Override
    public Map<String, String> getConfigParameters() {
        return configParameters;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public ValueResolver getParametersResolver() {
        return null;
    }

    @Override
    public String getRunType() {
        return null;
    }

    @Override
    public String getToolPath(final String arg0) throws ToolCannotBeFoundException {
        return null;
    }

    @Override
    public boolean parametersHaveReferencesTo(final Collection<String> arg0) {
        return false;
    }

    @Override
    public String getId() {
        return "testBuildId";
    }

}
