package com.blackducksoftware.integration.hub.teamcity.agent.scan;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.AgentBuildRunner;
import jetbrains.buildServer.agent.AgentBuildRunnerInfo;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildProcess;
import jetbrains.buildServer.agent.BuildRunnerContext;

import org.jetbrains.annotations.NotNull;

import com.blackducksoftware.integration.hub.teamcity.common.HubBundle;

public class HubBuildRunner implements AgentBuildRunner {

    @Override
    public BuildProcess createBuildProcess(@NotNull final AgentRunningBuild runningBuild, @NotNull final BuildRunnerContext context) throws RunBuildException {
        return new HubBuildProcess(runningBuild, context);
    }

    @Override
    public AgentBuildRunnerInfo getRunnerInfo() {
        return new AgentBuildRunnerInfo() {

            @Override
            public boolean canRun(BuildAgentConfiguration arg0) {
                return true;
            }

            @Override
            public String getType() {
                return HubBundle.RUNNER_TYPE;
            }

        };
    }

}
