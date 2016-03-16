package com.blackducksoftware.integration.hub.teamcity.agent.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jetbrains.buildServer.agent.AgentBuildFeature;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildAgentConfiguration;
import jetbrains.buildServer.agent.BuildInterruptReason;
import jetbrains.buildServer.agent.BuildParametersMap;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.ResolvedParameters;
import jetbrains.buildServer.agent.UnresolvedParameters;
import jetbrains.buildServer.artifacts.ArtifactDependencyInfo;
import jetbrains.buildServer.parameters.ValueResolver;
import jetbrains.buildServer.util.Option;
import jetbrains.buildServer.vcs.VcsChangeInfo;
import jetbrains.buildServer.vcs.VcsRoot;
import jetbrains.buildServer.vcs.VcsRootEntry;

public class TestAgentRunningBuild implements AgentRunningBuild {

    private TestBuildProgressLogger testLogger;

    private BuildAgentConfiguration config;

    public void setLogger(TestBuildProgressLogger testLogger) {
        this.testLogger = testLogger;

    }

    @Override
    public BuildProgressLogger getBuildLogger() {
        return testLogger;
    }

    @Override
    public Collection<AgentBuildFeature> getBuildFeatures() {
        ArrayList<AgentBuildFeature> buildFeatures = new ArrayList<AgentBuildFeature>();
        return buildFeatures;
    }

    @Override
    public Collection<AgentBuildFeature> getBuildFeaturesOfType(String arg0) {
        ArrayList<AgentBuildFeature> buildFeatures = new ArrayList<AgentBuildFeature>();

        return buildFeatures;
    }

    @Override
    public String getAccessCode() {

        return null;
    }

    @Override
    public String getAccessUser() {

        return null;
    }

    @Override
    public BuildAgentConfiguration getAgentConfiguration() {
        return config;
    }

    public void setAgentConfiguration(BuildAgentConfiguration config) {
        this.config = config;
    }

    @Override
    public File getAgentTempDirectory() {

        return null;
    }

    @Override
    public List<ArtifactDependencyInfo> getArtifactDependencies() {

        return null;
    }

    @Override
    public String getBuildCurrentVersion(VcsRoot arg0) {

        return null;
    }

    @Override
    public long getBuildId() {

        return 0;
    }

    @Override
    public String getBuildPreviousVersion(VcsRoot arg0) {

        return null;
    }

    @Override
    public File getBuildTempDirectory() {

        return null;
    }

    @Override
    public String getBuildTypeId() {

        return null;
    }

    @Override
    public String getBuildTypeName() {

        return null;
    }

    @Override
    public <T> T getBuildTypeOptionValue(Option<T> arg0) {

        return null;
    }

    @Override
    public File getDefaultCheckoutDirectory() {

        return null;
    }

    @Override
    public long getExecutionTimeoutMinutes() {

        return 0;
    }

    @Override
    public List<VcsChangeInfo> getPersonalVcsChanges() {

        return null;
    }

    @Override
    public String getProjectName() {

        return null;
    }

    @Override
    public List<VcsChangeInfo> getVcsChanges() {

        return null;
    }

    @Override
    public List<VcsRootEntry> getVcsRootEntries() {

        return null;
    }

    @Override
    public boolean isCheckoutOnAgent() {

        return false;
    }

    @Override
    public boolean isCheckoutOnServer() {

        return false;
    }

    @Override
    public boolean isCleanBuild() {

        return false;
    }

    @Override
    public boolean isCustomCheckoutDirectory() {

        return false;
    }

    @Override
    public boolean isPersonal() {

        return false;
    }

    @Override
    public boolean isPersonalPatchAvailable() {

        return false;
    }

    @Override
    public void addSharedConfigParameter(String arg0, String arg1) {

    }

    @Override
    public void addSharedEnvironmentVariable(String arg0, String arg1) {

    }

    @Override
    public void addSharedSystemProperty(String arg0, String arg1) {

    }

    @Override
    public String getArtifactsPaths() {

        return null;
    }

    @Override
    public String getBuildNumber() {

        return "5678";
    }

    @Override
    public BuildParametersMap getBuildParameters() {

        return null;
    }

    @Override
    public File getCheckoutDirectory() {

        return null;
    }

    @Override
    public boolean getFailBuildOnExitCode() {

        return false;
    }

    @Override
    public BuildInterruptReason getInterruptReason() {

        return null;
    }

    @Override
    public BuildParametersMap getMandatoryBuildParameters() {

        return null;
    }

    @Override
    public ResolvedParameters getResolvedParameters() {

        return null;
    }

    @Override
    public String getRunType() {

        return null;
    }

    @Override
    public Map<String, String> getRunnerParameters() {

        return null;
    }

    @Override
    public BuildParametersMap getSharedBuildParameters() {

        return null;
    }

    @Override
    public Map<String, String> getSharedConfigParameters() {

        return null;
    }

    @Override
    public ValueResolver getSharedParametersResolver() {

        return null;
    }

    @Override
    public UnresolvedParameters getUnresolvedParameters() {

        return null;
    }

    @Override
    public File getWorkingDirectory() {

        return null;
    }

    @Override
    public void stopBuild(String arg0) {

    }

}
