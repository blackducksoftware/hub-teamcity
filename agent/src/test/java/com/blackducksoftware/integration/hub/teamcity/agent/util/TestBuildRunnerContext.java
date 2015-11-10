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

    private Map<String, String> runnerParameters = new HashMap<String, String>();

    private File workingDirectory;

    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    @Override
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    @Override
    public void addRunnerParameter(String key, String value) {
        runnerParameters.put(key, value);
    }

    public void removeRunnerParameter(String key) {
        runnerParameters.remove(key);
    }

    public String getRunnerParameter(String key) {
        return runnerParameters.get(key);
    }

    @Override
    public Map<String, String> getRunnerParameters() {
        return runnerParameters;
    }

    @Override
    public void addConfigParameter(String arg0, String arg1) {

    }

    @Override
    public void addEnvironmentVariable(String arg0, String arg1) {

    }

    @Override
    public void addSystemProperty(String arg0, String arg1) {

    }

    @Override
    public AgentRunningBuild getBuild() {

        return null;
    }

    @Override
    public BuildParametersMap getBuildParameters() {

        return null;
    }

    @Override
    public Map<String, String> getConfigParameters() {

        return null;
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
    public String getToolPath(String arg0) throws ToolCannotBeFoundException {

        return null;
    }

    @Override
    public boolean parametersHaveReferencesTo(Collection<String> arg0) {

        return false;
    }

}
