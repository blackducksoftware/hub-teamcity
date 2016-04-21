/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *******************************************************************************/
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

	public void setAgentToolsDirectory(final File toolDir) {
		this.toolDir = toolDir;
	}

	@Override
	public void addAlternativeAgentAddress(final String arg0) {
	}

	@Override
	public void addConfigurationParameter(final String arg0, final String arg1) {
	}

	@Override
	public void addCustomProperty(final String arg0, final String arg1) {
	}

	@Override
	public void addEnvironmentVariable(final String arg0, final String arg1) {
	}

	@Override
	public void addSystemProperty(final String arg0, final String arg1) {
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
	public File getCacheDirectory(final String arg0) {
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
	public String getEnv(final String arg0) {
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
