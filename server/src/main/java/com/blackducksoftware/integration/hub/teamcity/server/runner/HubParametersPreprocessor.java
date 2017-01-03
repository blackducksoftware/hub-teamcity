/**
 * Black Duck Hub Plug-In for TeamCity Server
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.hub.teamcity.server.runner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;
import com.blackducksoftware.integration.hub.teamcity.server.global.HubServerListener;
import com.blackducksoftware.integration.hub.teamcity.server.global.ServerHubConfigPersistenceManager;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.ParametersPreprocessor;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.buildLog.BuildLog;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

public class HubParametersPreprocessor implements ParametersPreprocessor {
    private final ServerHubConfigPersistenceManager serverPeristanceManager;

    private BuildLog log = null;

    private final PluginDescriptor pluginDescriptor;

    public HubParametersPreprocessor(@NotNull final HubServerListener serverListener, @NotNull PluginDescriptor pluginDescriptor) {
        serverPeristanceManager = serverListener.getConfigManager();
        this.pluginDescriptor = pluginDescriptor;
    }

    @Override
    public void fixRunBuildParameters(@NotNull final SRunningBuild build,
            @NotNull final Map<String, String> runParameters, @NotNull final Map<String, String> buildParameters) {
        log = build.getBuildLog();

        if (isHubBuildStepConfigured(runParameters)) {
            handleLog("Hub Plugin enabled.", null);
            addGlobalParameterMap(runParameters);
            addPluginData(runParameters);
        }
    }

    private void addGlobalParameterMap(final Map<String, String> runParameters) {
        HubServerConfig hubServerConfig = serverPeristanceManager.getHubServerConfig();
        if (!runParameters.containsKey(HubConstantValues.HUB_URL)) {
            runParameters.put(HubConstantValues.HUB_URL,
                    StringUtils.trimToEmpty(hubServerConfig.getHubUrl().toString()));
        }
        if (!runParameters.containsKey(HubConstantValues.HUB_USERNAME)) {
            runParameters.put(HubConstantValues.HUB_USERNAME, StringUtils.trimToEmpty(hubServerConfig.getGlobalCredentials().getUsername()));
        }
        if (!runParameters.containsKey(HubConstantValues.HUB_PASSWORD)) {
            runParameters.put(HubConstantValues.HUB_PASSWORD,
                    StringUtils.trimToEmpty(hubServerConfig.getGlobalCredentials().getEncryptedPassword()));
        }
        if (!runParameters.containsKey(HubConstantValues.HUB_PASSWORD_LENGTH)) {
            runParameters.put(HubConstantValues.HUB_PASSWORD_LENGTH, Integer.toString(hubServerConfig.getGlobalCredentials().getActualPasswordLength()));
        }
        if (!runParameters.containsKey(HubConstantValues.HUB_CONNECTION_TIMEOUT)) {
            runParameters.put(HubConstantValues.HUB_CONNECTION_TIMEOUT,
                    String.valueOf(hubServerConfig.getTimeout()));
        }
        String ignoredProxyHosts = hubServerConfig.getProxyInfo().getIgnoredProxyHosts();
        if (!runParameters.containsKey(HubConstantValues.HUB_NO_PROXY_HOSTS) && StringUtils.isNotBlank(ignoredProxyHosts)) {
            runParameters.put(HubConstantValues.HUB_NO_PROXY_HOSTS, StringUtils.trimToEmpty(ignoredProxyHosts));
        }

        String proxyHost = hubServerConfig.getProxyInfo().getHost();
        int proxyPort = hubServerConfig.getProxyInfo().getPort();
        String proxyUsername = hubServerConfig.getProxyInfo().getUsername();
        String proxyPassword = hubServerConfig.getProxyInfo().getEncryptedPassword();
        String proxyPasswordLength = Integer.toString(hubServerConfig.getProxyInfo().getActualPasswordLength());
        if (StringUtils.isNotBlank(proxyHost) && proxyPort > 0) {
            if (!runParameters.containsKey(HubConstantValues.HUB_PROXY_HOST)) {
                runParameters.put(HubConstantValues.HUB_PROXY_HOST, StringUtils.trimToEmpty(proxyHost));
            }

            if (!runParameters.containsKey(HubConstantValues.HUB_PROXY_PORT)) {
                runParameters.put(HubConstantValues.HUB_PROXY_PORT, String.valueOf(proxyPort));
            }

            if (StringUtils.isNotBlank(proxyUsername) && StringUtils.isNotBlank(proxyPassword)) {
                if (!runParameters.containsKey(HubConstantValues.HUB_PROXY_USER)) {
                    runParameters.put(HubConstantValues.HUB_PROXY_USER, StringUtils.trimToEmpty(proxyUsername));
                }
                if (!runParameters.containsKey(HubConstantValues.HUB_PROXY_PASS)) {
                    runParameters.put(HubConstantValues.HUB_PROXY_PASS, StringUtils.trimToEmpty(proxyPassword));
                }
                if (!runParameters.containsKey(HubConstantValues.HUB_PROXY_PASS_LENGTH)) {
                    runParameters.put(HubConstantValues.HUB_PROXY_PASS_LENGTH, proxyPasswordLength);
                }
            }
        }
    }

    private void addPluginData(final Map<String, String> runParameters) {
        if (!runParameters.containsKey(HubConstantValues.PLUGIN_VERSION)) {
            runParameters.put(HubConstantValues.PLUGIN_VERSION, pluginDescriptor.getPluginVersion());
        }

        if (!runParameters.containsKey(HubConstantValues.PLUGIN_NAME)) {
            runParameters.put(HubConstantValues.PLUGIN_NAME, pluginDescriptor.getPluginName());
        }
    }

    private void handleLog(final String txt, final Throwable e) {
        log.message(HubConstantValues.PLUGIN_LOG + txt, Status.NORMAL, new Date(), "", "", Collections.EMPTY_LIST);
        if (e != null) {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            log.message(HubConstantValues.PLUGIN_LOG + sw.toString(), Status.NORMAL, new Date(), "", "",
                    Collections.EMPTY_LIST);
        }
    }

    private boolean isHubBuildStepConfigured(@NotNull final Map<String, String> runParameters) {
        final boolean hubVersionPhase = runParameters.containsKey(HubConstantValues.HUB_VERSION_PHASE)
                && StringUtils.isNotBlank(runParameters.get(HubConstantValues.HUB_VERSION_PHASE));
        final boolean hubVersionDistribution = runParameters.containsKey(HubConstantValues.HUB_VERSION_DISTRIBUTION)
                && StringUtils.isNotBlank(runParameters.get(HubConstantValues.HUB_VERSION_DISTRIBUTION));

        return hubVersionPhase && hubVersionDistribution;
    }

}
