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
package com.blackducksoftware.integration.hub.teamcity.server.global;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import com.blackducksoftware.integration.hub.teamcity.common.beans.HubCredentialsBean;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubProxyInfo;
import com.blackducksoftware.integration.hub.teamcity.common.beans.ServerHubConfigBean;
import com.blackducksoftware.integration.hub.version.api.DistributionEnum;
import com.blackducksoftware.integration.hub.version.api.PhaseEnum;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.StreamException;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.crypt.RSACipher;

public class ServerHubConfigPersistenceManager {
	private static final String CONFIG_FILE_NAME = "hub-config.xml";

	private final File configFile;
	private ServerHubConfigBean configuredServer = new ServerHubConfigBean();
	private final XStream xStream;

	public ServerHubConfigPersistenceManager(@NotNull final ServerPaths serverPaths) {
		xStream = new XStream();
		xStream.setClassLoader(ServerHubConfigBean.class.getClassLoader());
		xStream.processAnnotations(new Class[] { HubCredentialsBean.class });
		xStream.processAnnotations(new Class[] { HubProxyInfo.class });
		final HubCredentialsBean credentials = new HubCredentialsBean("");
		final HubProxyInfo proxyInfo = new HubProxyInfo();

		configuredServer.setGlobalCredentials(credentials);
		configuredServer.setProxyInfo(proxyInfo);

		configFile = new File(serverPaths.getConfigDir(), CONFIG_FILE_NAME);
		loadSettings();
	}

	public File getConfigFile() {
		return configFile;
	}

	public ServerHubConfigBean getConfiguredServer() {
		return configuredServer;
	}

	private void setConfiguredServer(final ServerHubConfigBean bean) {
		configuredServer = bean;
	}

	public void loadSettings() {
		if (configFile.exists()) {
			synchronized (this) {
				FileInputStream inputStream = null;
				boolean errorLoadingConfig = false;
				try {
					inputStream = new FileInputStream(configFile);
					final ServerHubConfigBean serverConfig = (ServerHubConfigBean) xStream.fromXML(inputStream);
					if (serverConfig != null) {
						setConfiguredServer(serverConfig);
					} else {
						errorLoadingConfig = true;
					}
				} catch (final FileNotFoundException e) {
					errorLoadingConfig = true;
					Loggers.SERVER.error("Failed to load Hub config file: " + configFile, e);
				} catch (final StreamException e) {
					errorLoadingConfig = true;
					Loggers.SERVER.error("Failed to load Hub config file: " + configFile, e);
				} finally {
					IOUtils.closeQuietly(inputStream);
				}
				if (errorLoadingConfig) {
					setConfiguredServer(defaultEmptyConfiguration());
				}
			}
		} else {
			setConfiguredServer(defaultEmptyConfiguration());
		}
	}

	private ServerHubConfigBean defaultEmptyConfiguration() {
		final ServerHubConfigBean config = new ServerHubConfigBean();
		final HubCredentialsBean credentials = new HubCredentialsBean("", "");
		final HubProxyInfo proxyInfo = new HubProxyInfo();

		configuredServer.setGlobalCredentials(credentials);
		configuredServer.setProxyInfo(proxyInfo);

		return config;
	}

	public void persist() throws IOException {
		FileOutputStream outputStream = null;
		try {
			if (!configFile.getParentFile().exists() && configFile.getParentFile().mkdirs()) {
				Loggers.SERVER.info("Directory created for the Hub configuration file at : "
						+ configFile.getParentFile().getCanonicalPath());
			} else if (configFile.exists() && configFile.delete()) {
				Loggers.SERVER.info("Old Hub configuration file removed, to be replaced by a new configuration.");
			}

			synchronized (this) {
				outputStream = new FileOutputStream(configFile);
				xStream.toXML(configuredServer, outputStream);
			}
		} catch (final FileNotFoundException e) {
			Loggers.SERVER.error("Failed to save Hub config file: " + configFile, e);
		} finally {
			IOUtils.closeQuietly(outputStream);
		}
	}

	public String getHexEncodedPublicKey() {
		return RSACipher.getHexEncodedPublicKey();
	}

	public String getRandom() {
		return String.valueOf(Math.random());
	}

	public List<String> getPhaseOptions() {
		final List<String> phaseList = new ArrayList<String>();
		for (final PhaseEnum phase : PhaseEnum.values()) {
			if (phase != PhaseEnum.UNKNOWNPHASE) {
				phaseList.add(phase.getDisplayValue());
			}
		}

		return phaseList;
	}

	public List<String> getDistributionOptions() {
		final List<String> distributionList = new ArrayList<String>();
		for (final DistributionEnum distribution : DistributionEnum.values()) {
			if (distribution != DistributionEnum.UNKNOWNDISTRIBUTION) {
				distributionList.add(distribution.getDisplayValue());
			}
		}

		return distributionList;
	}

}
