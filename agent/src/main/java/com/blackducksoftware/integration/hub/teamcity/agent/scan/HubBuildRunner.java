/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
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

public class HubBuildRunner implements AgentBuildRunner {
	@NotNull
	private final ArtifactsWatcher artifactsWatcher;

	public HubBuildRunner(@NotNull final ArtifactsWatcher artifactsWatcher) {
		this.artifactsWatcher = artifactsWatcher;
	}

	@Override
	public BuildProcess createBuildProcess(@NotNull final AgentRunningBuild runningBuild,
			@NotNull final BuildRunnerContext context) throws RunBuildException {
		return new HubBuildProcess(runningBuild, context, artifactsWatcher);
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
