package com.blackducksoftware.integration.hub.teamcity.agent.util;

import java.io.File;
import java.util.Map;

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

    public void setAgentToolsDirectory(File toolDir) {
        this.toolDir = toolDir;
    }

    @Override
    public void addAlternativeAgentAddress(String arg0) {

    }

    @Override
    public void addConfigurationParameter(String arg0, String arg1) {

    }

    @Override
    public void addCustomProperty(String arg0, String arg1) {

    }

    @Override
    public void addEnvironmentVariable(String arg0, String arg1) {

    }

    @Override
    public void addSystemProperty(String arg0, String arg1) {

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

    @Override
    public BuildParametersMap getBuildParameters() {
        return null;
    }

    @Override
    public File getBuildTempDirectory() {
        return null;
    }

    @Override
    public File getCacheDirectory(String arg0) {
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
    public String getEnv(String arg0) {
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
