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
package com.blackducksoftware.integration.hub.teamcity.agent.scan;

import org.jetbrains.annotations.NotNull;

import com.blackducksoftware.integration.hub.teamcity.common.HubBundle;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentBuildRunner;
import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.agent.plugins.beans.AgentPluginInfoImpl;

public class HubBuildRunner implements AgentBuildRunner {
	@NotNull
	private final ArtifactsWatcher artifactsWatcher;

	@NotNull
	private final AgentPluginInfoImpl pluginInfo;

	public HubBuildRunner(@NotNull final ArtifactsWatcher artifactsWatcher,
			@NotNull final AgentPluginInfoImpl pluginInfo) {
		this.artifactsWatcher = artifactsWatcher;
		this.pluginInfo = pluginInfo;
	}

	@Override
	public BuildProcess createBuildProcess(@NotNull final AgentRunningBuild runningBuild,
			@NotNull final BuildRunnerContext context) throws RunBuildException {
		return new HubBuildProcess(runningBuild, context, artifactsWatcher, pluginInfo);
	}

	@Override
	public AgentBuildRunnerInfo getRunnerInfo() {
		return new AgentBuildRunnerInfo() {
			@Override
			public boolean canRun(final BuildAgentConfiguration arg0) {
				return true;
			}

			@Override
			public String getType() {
				return HubBundle.RUNNER_TYPE;
			}
		};
	}

}
