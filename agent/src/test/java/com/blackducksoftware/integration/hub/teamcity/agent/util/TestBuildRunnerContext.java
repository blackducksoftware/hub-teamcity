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

	public void setWorkingDirectory(File workingDirectory) {
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
	public void addConfigParameter(String key, String value) {
		configParameters.put(key, value);
	}

	@Override
	public void addEnvironmentVariable(String key, String value) {
		environmentParameters.put(key, value);
	}

	@Override
	public void addSystemProperty(String key, String value) {
		systemParameters.put(key, value);
	}

	@Override
	public AgentRunningBuild getBuild() {

		return build;
	}

	public void setBuild(AgentRunningBuild build) {
		this.build = build;
	}

	@Override
	public BuildParametersMap getBuildParameters() {
		BuildParametersMap buildParameterMap = new BuildParametersMap() {

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
