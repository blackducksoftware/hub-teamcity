package com.blackducksoftware.integration.hub.teamcity.agent.artifacts;

import java.io.File;
import java.io.IOException;

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.util.EventDispatcher;

import org.jetbrains.annotations.NotNull;

import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;

public class RiskReportListener extends AgentLifeCycleAdapter {
    @NotNull
    private final ArtifactsWatcher artifactsWatcher;

    public RiskReportListener(@NotNull final EventDispatcher<AgentLifeCycleListener> agentDispatcher, @NotNull final ArtifactsWatcher artifactsWatcher) {
        this.artifactsWatcher = artifactsWatcher;
        agentDispatcher.addListener(this);
    }

    @Override
    public void runnerFinished(@NotNull BuildRunnerContext runner, @NotNull BuildFinishedStatus status) {
        runner.getBuild().getBuildLogger().error("risk report listener called for build finished");
        try {
            String workingDirectoryCanonicalPath = runner.getWorkingDirectory().getCanonicalPath();
            String reportPath = workingDirectoryCanonicalPath + File.separator + HubConstantValues.HUB_RISK_REPORT_FILENAME;

            artifactsWatcher.addNewArtifactsPath(reportPath);
        } catch (IOException e) {
            runner.getBuild().getBuildLogger().error("IOException processing risk report: " + e.getMessage());
        }
    }

}
