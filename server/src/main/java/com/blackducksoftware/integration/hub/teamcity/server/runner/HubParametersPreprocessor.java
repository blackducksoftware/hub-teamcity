package com.blackducksoftware.integration.hub.teamcity.server.runner;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.ParametersPreprocessor;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.buildLog.BuildLog;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubCredentialsBean;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubProxyInfo;
import com.blackducksoftware.integration.hub.teamcity.server.global.HubServerListener;
import com.blackducksoftware.integration.hub.teamcity.server.global.ServerHubConfigPersistenceManager;

public class HubParametersPreprocessor implements ParametersPreprocessor {

    private final ServerHubConfigPersistenceManager serverPeristanceManager;

    private BuildLog log = null;

    public HubParametersPreprocessor(@NotNull final HubServerListener serverListener) {
        serverPeristanceManager = serverListener.getConfigManager();
    }

    @Override
    public void fixRunBuildParameters(@NotNull SRunningBuild build, @NotNull Map<String, String> runParameters, @NotNull Map<String, String> buildParameters) {
        log = build.getBuildLog();

        // This condition checks that the Protex Build step has been added to this Build
        if ((runParameters.containsKey(HubConstantValues.HUB_PROJECT_NAME) && StringUtils.isNotBlank(runParameters.get(HubConstantValues.HUB_PROJECT_NAME)))
                || (runParameters.containsKey(HubConstantValues.HUB_PROJECT_VERSION) && StringUtils.isNotBlank(runParameters
                        .get(HubConstantValues.HUB_PROJECT_VERSION)))) {

            handleLog("Hub Plugin enabled.", null);

            HubCredentialsBean credentials = serverPeristanceManager.getConfiguredServer().getGlobalCredentials();

            HubProxyInfo proxyInfo = serverPeristanceManager.getConfiguredServer().getProxyInfo();

            if (!runParameters.containsKey(HubConstantValues.HUB_URL)) {
                runParameters.put(HubConstantValues.HUB_URL, trimIfStringNotNull(serverPeristanceManager.getConfiguredServer().getHubUrl()));
            }
            if (!runParameters.containsKey(HubConstantValues.HUB_USERNAME)) {
                runParameters.put(HubConstantValues.HUB_USERNAME, trimIfStringNotNull(credentials.getHubUser()));
            }
            if (!runParameters.containsKey(HubConstantValues.HUB_PASSWORD)) {
                runParameters.put(HubConstantValues.HUB_PASSWORD, trimIfStringNotNull(credentials.getEncryptedPassword()));
            }
            if (!runParameters.containsKey(HubConstantValues.HUB_PROXY_HOST)) {
                runParameters.put(HubConstantValues.HUB_PROXY_HOST, trimIfStringNotNull(proxyInfo.getHost()));
            }
            if (!runParameters.containsKey(HubConstantValues.HUB_PROXY_PORT)) {
                runParameters.put(HubConstantValues.HUB_PROXY_PORT, String.valueOf(proxyInfo.getPort()));
            }
            if (!runParameters.containsKey(HubConstantValues.HUB_NO_PROXY_HOSTS)) {
                runParameters.put(HubConstantValues.HUB_NO_PROXY_HOSTS, trimIfStringNotNull(proxyInfo.getIgnoredProxyHosts()));
            }
            if (StringUtils.isNotBlank(proxyInfo.getProxyUsername()) && StringUtils.isNotBlank(proxyInfo.getProxyPassword())) {
                if (!runParameters.containsKey(HubConstantValues.HUB_PROXY_USER)) {
                    runParameters.put(HubConstantValues.HUB_PROXY_USER, trimIfStringNotNull(proxyInfo.getProxyUsername()));
                }
                if (!runParameters.containsKey(HubConstantValues.HUB_PROXY_PASS)) {
                    runParameters.put(HubConstantValues.HUB_PROXY_PASS, trimIfStringNotNull(proxyInfo.getProxyPassword()));
                }
            }
        }
    }

    private String trimIfStringNotNull(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        } else {
            return value.trim();
        }
    }

    private void handleLog(String txt, Throwable e) {
        log.message(HubConstantValues.PLUGIN_LOG + txt, Status.NORMAL, new Date(), "", "", Collections.EMPTY_LIST);
        if (e != null) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            log.message(HubConstantValues.PLUGIN_LOG + sw.toString(), Status.NORMAL, new Date(), "", "", Collections.EMPTY_LIST);
        }
    }

}
