package com.blackducksoftware.integration.hub.teamcity.agent.artifacts;

import java.io.File;
import java.io.IOException;

import jetbrains.buildServer.agent.AgentLifeCycleAdapter;
import jetbrains.buildServer.agent.AgentLifeCycleListener;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.util.EventDispatcher;

import org.jetbrains.annotations.NotNull;

import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;

public class RiskReportListener extends AgentLifeCycleAdapter {
    @NotNull
    private final ArtifactsWatcher artifactsWatcher;

    private final BuildRunnerContext context;

    public RiskReportListener(@NotNull final EventDispatcher<AgentLifeCycleListener> agentDispatcher, @NotNull final ArtifactsWatcher artifactsWatcher,
            BuildRunnerContext context) {
        this.artifactsWatcher = artifactsWatcher;
        this.context = context;

        agentDispatcher.addListener(this);
    }

    @Override
    public void buildFinished(@NotNull AgentRunningBuild agentRunningBuild, @NotNull BuildFinishedStatus status) {
        try {
            String workingDirectoryCanonicalPath = context.getWorkingDirectory().getCanonicalPath();
            String reportPath = workingDirectoryCanonicalPath + File.separator + HubConstantValues.HUB_RISK_REPORT_FILENAME;
            File riskReportFile = new File(reportPath);
            if (null != riskReportFile && riskReportFile.exists()) {
                artifactsWatcher.addNewArtifactsPath(reportPath);
            }
        } catch (IOException e) {
            agentRunningBuild.getBuildLogger().error("IOException processing risk report: " + e.getMessage());
        }
    }

}
