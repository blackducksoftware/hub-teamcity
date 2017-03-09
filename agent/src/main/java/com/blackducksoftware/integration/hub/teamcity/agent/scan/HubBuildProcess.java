/**
 * Black Duck Hub Plug-In for TeamCity Agent
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
package com.blackducksoftware.integration.hub.teamcity.agent.scan;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.codelocation.CodeLocationRequestService;
import com.blackducksoftware.integration.hub.api.item.MetaService;
import com.blackducksoftware.integration.hub.api.project.ProjectRequestService;
import com.blackducksoftware.integration.hub.api.project.version.ProjectVersionRequestService;
import com.blackducksoftware.integration.hub.builder.HubScanConfigBuilder;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.dataservice.cli.CLIDataService;
import com.blackducksoftware.integration.hub.dataservice.policystatus.PolicyStatusDescription;
import com.blackducksoftware.integration.hub.dataservice.report.RiskReportDataService;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.model.enumeration.VersionBomPolicyStatusOverallStatusEnum;
import com.blackducksoftware.integration.hub.model.view.CodeLocationView;
import com.blackducksoftware.integration.hub.model.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.model.view.ProjectView;
import com.blackducksoftware.integration.hub.model.view.ScanSummaryView;
import com.blackducksoftware.integration.hub.model.view.VersionBomPolicyStatusView;
import com.blackducksoftware.integration.hub.phonehome.IntegrationInfo;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.scan.HubScanConfig;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.teamcity.agent.HubAgentBuildLogger;
import com.blackducksoftware.integration.hub.teamcity.common.HubBundle;
import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;
import com.blackducksoftware.integration.hub.util.HostnameHelper;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phone.home.enums.ThirdPartyName;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

import jetbrains.buildServer.agent.AgentBuildFeature;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.version.ServerVersionHolder;

public class HubBuildProcess extends HubCallableBuildProcess {
    private static final int DEFAULT_MAX_WAIT_TIME_MILLISEC = 5 * 60 * 1000;

    @NotNull
    private final AgentRunningBuild build;

    @NotNull
    private final BuildRunnerContext context;

    @NotNull
    private final ArtifactsWatcher artifactsWatcher;

    private HubAgentBuildLogger logger;

    private BuildFinishedStatus result;

    private Boolean verbose;

    public HubBuildProcess(@NotNull final AgentRunningBuild build, @NotNull final BuildRunnerContext context,
            @NotNull final ArtifactsWatcher artifactsWatcher) {
        this.build = build;
        this.context = context;
        this.artifactsWatcher = artifactsWatcher;
    }

    public void setverbose(final boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isVerbose() {
        if (verbose == null) {
            verbose = true;
        }
        return verbose;
    }

    public void setHubLogger(final HubAgentBuildLogger logger) {
        this.logger = logger;
    }

    @Override
    public BuildFinishedStatus call() throws IOException, NoSuchMethodException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, EncryptionException {
        final BuildProgressLogger buildLogger = build.getBuildLogger();
        final HubAgentBuildLogger hubLogger = new HubAgentBuildLogger(buildLogger);

        final Map<String, String> variables = getVariables();
        final CIEnvironmentVariables commonVariables = new CIEnvironmentVariables();
        commonVariables.putAll(variables);
        hubLogger.setLogLevel(commonVariables);
        setHubLogger(hubLogger);

        if (StringUtils.isBlank(System.getProperty("http.maxRedirects"))) {
            // If this property is not set the default is 20
            // When not set the Authenticator redirects in a loop and results in
            // an error for too many redirects
            System.setProperty("http.maxRedirects", "3");
        }

        result = BuildFinishedStatus.FINISHED_SUCCESS;

        logger.targetStarted("Hub Build Step");

        final String localHostName = HostnameHelper.getMyHostname();
        logger.info("Running on machine : " + localHostName);

        final String thirdPartyVersion = ServerVersionHolder.getVersion().getDisplayVersion();
        final String pluginVersion = getPluginVersion(commonVariables);
        logger.info("TeamCity version : " + thirdPartyVersion);
        logger.info("Hub TeamCity Plugin version : " + pluginVersion);

        try {
            final HubServerConfig hubConfig = getHubServerConfig(logger, commonVariables);
            if (hubConfig == null) {
                logger.error("Please verify the correct dependent Hub configuration plugin is installed");
                logger.error("Please verify the configuration is correct if the plugin is installed.");
                result = BuildFinishedStatus.FINISHED_FAILED;
                return result;
            }
            hubConfig.print(logger);

            final boolean isRiskReportGenerated = Boolean.parseBoolean(commonVariables.getValue(HubConstantValues.HUB_GENERATE_RISK_REPORT));

            boolean isFailOnPolicySelected = false;
            final Collection<AgentBuildFeature> features = build.getBuildFeaturesOfType(HubBundle.POLICY_FAILURE_CONDITION);
            // The feature is only allowed to have a single instance in the
            // configuration therefore we just want to make
            // sure the feature collection has something meaning that it was
            // configured.
            if (features != null && features.iterator() != null && !features.isEmpty()
                    && features.iterator().next() != null) {
                isFailOnPolicySelected = true;
            }

            long waitTimeForReport = DEFAULT_MAX_WAIT_TIME_MILLISEC;
            final String maxWaitTimeForRiskReport = commonVariables.getValue(HubConstantValues.HUB_MAX_WAIT_TIME_FOR_RISK_REPORT);
            if (StringUtils.isNotBlank(maxWaitTimeForRiskReport)) {
                waitTimeForReport = NumberUtils.toInt(maxWaitTimeForRiskReport);
                if (waitTimeForReport <= 0) {
                    // 5 minutes is the default
                    waitTimeForReport = 5 * 60 * 1000;
                } else {
                    waitTimeForReport = waitTimeForReport * 60 * 1000;
                }
            } else {
                waitTimeForReport = 5 * 60 * 1000;
            }

            logger.info("--> Generate Risk Report : " + isRiskReportGenerated);
            logger.info("--> Bom wait time : " + maxWaitTimeForRiskReport);
            logger.info("--> Check Policies : " + isFailOnPolicySelected);

            final RestConnection restConnection = getRestConnection(logger, hubConfig);

            restConnection.connect();

            final HubServicesFactory services = new HubServicesFactory(restConnection);
            services.addEnvironmentVariables(variables);
            final MetaService metaService = services.createMetaService(logger);
            final CLIDataService cliDataService = services.createCLIDataService(logger);
            final File workingDirectory = context.getWorkingDirectory();
            final File toolsDir = new File(build.getAgentConfiguration().getAgentToolsDirectory(), "HubCLI");

            final HubScanConfig hubScanConfig = getScanConfig(workingDirectory, toolsDir, hubLogger, commonVariables);
            if (hubScanConfig == null) {
                logger.error("Please verify the Black Duck Hub Runner configuration is correct.");
                result = BuildFinishedStatus.FINISHED_FAILED;
                return result;
            }

            List<ScanSummaryView> scanSummaryList = null;
            try {
                scanSummaryList = cliDataService.installAndRunScan(hubConfig, hubScanConfig,
                        new IntegrationInfo(ThirdPartyName.TEAM_CITY.getName(), thirdPartyVersion, pluginVersion));

            } catch (final HubIntegrationException e) {

                result = BuildFinishedStatus.FINISHED_FAILED;
                return result;
            }
            if (!hubScanConfig.isDryRun()) {
                final ProjectVersionView version = getProjectVersionFromScanStatus(services.createCodeLocationRequestService(logger),
                        services.createProjectVersionRequestService(logger),
                        metaService, scanSummaryList.get(0));
                final ProjectView project = getProjectFromVersion(services.createProjectRequestService(), metaService, version);

                if (isRiskReportGenerated || isFailOnPolicySelected) {
                    logger.info("Waiting for Bom to be updated");
                    services.createScanStatusDataService(logger, waitTimeForReport).assertBomImportScansFinished(scanSummaryList);
                }
                if (isRiskReportGenerated) {
                    logger.info("Generating Risk Report");
                    publishRiskReportFiles(logger, workingDirectory, services.createRiskReportDataService(logger, waitTimeForReport),
                            project, version);
                }
                if (isFailOnPolicySelected) {
                    logger.info("Checking for Policy violations.");
                    checkPolicyFailures(build, logger, services, metaService, version,
                            hubScanConfig.isDryRun());
                }
            } else {
                if (isRiskReportGenerated) {
                    logger.warn("Will not generate the risk report because this was a dry run scan.");
                }
                if (isFailOnPolicySelected) {
                    logger.warn("Will not run the Failure conditions because this was a dry run scan.");
                }
            }
        } catch (final Exception e) {
            logger.error(e);
            result = BuildFinishedStatus.FINISHED_FAILED;
        }
        logger.targetFinished("Hub Build Step");
        return result;
    }

    public RestConnection getRestConnection(final IntLogger logger, final HubServerConfig hubServerConfig) throws EncryptionException {
        final RestConnection restConnection = new CredentialsRestConnection(logger, hubServerConfig.getHubUrl(),
                hubServerConfig.getGlobalCredentials().getUsername(), hubServerConfig.getGlobalCredentials().getDecryptedPassword(),
                hubServerConfig.getTimeout());
        restConnection.proxyHost = hubServerConfig.getProxyInfo().getHost();
        restConnection.proxyPort = hubServerConfig.getProxyInfo().getPort();
        restConnection.proxyNoHosts = hubServerConfig.getProxyInfo().getIgnoredProxyHosts();
        restConnection.proxyUsername = hubServerConfig.getProxyInfo().getUsername();
        restConnection.proxyPassword = hubServerConfig.getProxyInfo().getDecryptedPassword();
        return restConnection;
    }

    private ProjectVersionView getProjectVersionFromScanStatus(final CodeLocationRequestService codeLocationRequestService,
            final ProjectVersionRequestService projectVersionRequestService, final MetaService metaService, final ScanSummaryView scanSummaryItem)
            throws IntegrationException {
        final CodeLocationView codeLocationItem = codeLocationRequestService
                .getItem(metaService.getFirstLink(scanSummaryItem, MetaService.CODE_LOCATION_BOM_STATUS_LINK), CodeLocationView.class);
        final String projectVersionUrl = codeLocationItem.getMappedProjectVersion();
        final ProjectVersionView projectVersion = projectVersionRequestService.getItem(projectVersionUrl, ProjectVersionView.class);
        return projectVersion;
    }

    private ProjectView getProjectFromVersion(final ProjectRequestService projectRequestService, final MetaService metaService,
            final ProjectVersionView version)
            throws IntegrationException {
        final String projectURL = metaService.getFirstLink(version, MetaService.PROJECT_LINK);
        final ProjectView projectVersion = projectRequestService.getItem(projectURL, ProjectView.class);
        return projectVersion;
    }

    private HubServerConfig getHubServerConfig(final IntLogger logger, final CIEnvironmentVariables commonVariables) {
        final HubServerConfigBuilder configBuilder = new HubServerConfigBuilder();

        // read the credentials and proxy info using the existing objects.
        final String serverUrl = commonVariables.getValue(HubConstantValues.HUB_URL);
        final String timeout = commonVariables.getValue(HubConstantValues.HUB_CONNECTION_TIMEOUT);
        final String username = commonVariables.getValue(HubConstantValues.HUB_USERNAME);
        final String password = commonVariables.getValue(HubConstantValues.HUB_PASSWORD);
        final String passwordLength = commonVariables.getValue(HubConstantValues.HUB_PASSWORD_LENGTH);

        final String proxyHost = commonVariables.getValue(HubConstantValues.HUB_PROXY_HOST);
        final String proxyPort = commonVariables.getValue(HubConstantValues.HUB_PROXY_PORT);
        final String ignoredProxyHosts = commonVariables.getValue(HubConstantValues.HUB_NO_PROXY_HOSTS);
        final String proxyUsername = commonVariables.getValue(HubConstantValues.HUB_PROXY_USER);
        final String proxyPassword = commonVariables.getValue(HubConstantValues.HUB_PROXY_PASS);
        final String proxyPasswordLength = commonVariables.getValue(HubConstantValues.HUB_PROXY_PASS_LENGTH);

        configBuilder.setHubUrl(serverUrl);
        configBuilder.setUsername(username);
        configBuilder.setPassword(password);
        configBuilder.setPasswordLength(NumberUtils.toInt(passwordLength));
        configBuilder.setTimeout(timeout);
        configBuilder.setProxyHost(proxyHost);
        configBuilder.setProxyPort(proxyPort);
        configBuilder.setIgnoredProxyHosts(ignoredProxyHosts);
        configBuilder.setProxyUsername(proxyUsername);
        configBuilder.setProxyPassword(proxyPassword);
        configBuilder.setProxyPasswordLength(NumberUtils.toInt(proxyPasswordLength));

        try {
            return configBuilder.build();
        } catch (final IllegalStateException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private HubScanConfig getScanConfig(final File workingDirectory, final File toolsDir,
            final IntLogger logger, final CIEnvironmentVariables commonVariables) throws IOException {

        final String projectName = commonVariables.getValue(HubConstantValues.HUB_PROJECT_NAME);
        final String projectVersion = commonVariables.getValue(HubConstantValues.HUB_PROJECT_VERSION);
        final String dryRun = commonVariables.getValue(HubConstantValues.HUB_DRY_RUN);
        final String cleanupLogs = commonVariables.getValue(HubConstantValues.HUB_CLEANUP_LOGS_ON_SUCCESS);

        final String scanMemory = commonVariables.getValue(HubConstantValues.HUB_SCAN_MEMORY);

        final String codeLocationName = commonVariables.getValue(HubConstantValues.HUB_CODE_LOCATION_NAME);

        final String hubWorkspaceCheck = commonVariables.getValue(HubConstantValues.HUB_WORKSPACE_CHECK);

        String[] excludePatternArray = new String[0];
        final String excludePatternParameter = commonVariables.getValue(HubConstantValues.HUB_EXCLUDE_PATTERNS);
        if (StringUtils.isNotBlank(excludePatternParameter)) {
            excludePatternArray = excludePatternParameter.split("\\r?\\n");
        }

        final List<String> scanTargets = new ArrayList<>();
        final String scanTargetParameter = commonVariables.getValue(HubConstantValues.HUB_SCAN_TARGETS);
        if (StringUtils.isNotBlank(scanTargetParameter)) {
            final String[] scanTargetPathsArray = scanTargetParameter.split("\\r?\\n");
            for (final String target : scanTargetPathsArray) {
                if (StringUtils.isNotBlank(target)) {
                    final File tmpTarget = new File(target);
                    if (tmpTarget.isAbsolute()) {
                        scanTargets.add(tmpTarget.getCanonicalPath());
                    } else {
                        scanTargets.add(new File(workingDirectory, target).getCanonicalPath());
                    }
                }
            }
        } else {
            scanTargets.add(workingDirectory.getAbsolutePath());
        }

        final HubScanConfigBuilder hubScanConfigBuilder = new HubScanConfigBuilder();
        hubScanConfigBuilder.setProjectName(projectName);
        hubScanConfigBuilder.setVersion(projectVersion);
        hubScanConfigBuilder.setWorkingDirectory(workingDirectory);
        hubScanConfigBuilder.setDryRun(Boolean.valueOf(dryRun));
        hubScanConfigBuilder.setScanMemory(scanMemory);
        hubScanConfigBuilder.setCodeLocationAlias(codeLocationName);
        hubScanConfigBuilder.addAllScanTargetPaths(scanTargets);
        hubScanConfigBuilder.setToolsDir(toolsDir);
        hubScanConfigBuilder.setCleanupLogsOnSuccess(Boolean.valueOf(cleanupLogs));
        hubScanConfigBuilder.setExcludePatterns(excludePatternArray);
        if (Boolean.valueOf(hubWorkspaceCheck)) {
            hubScanConfigBuilder.enableScanTargetPathsWithinWorkingDirectoryCheck();
        }
        try {
            return hubScanConfigBuilder.build();
        } catch (final IllegalStateException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private Map<String, String> getVariables() {
        final Map<String, String> variables = new HashMap<>();
        variables.putAll(context.getBuildParameters().getEnvironmentVariables());
        variables.putAll(context.getBuildParameters().getSystemProperties());
        variables.putAll(context.getConfigParameters());
        variables.putAll(context.getRunnerParameters());
        return variables;
    }

    private void publishRiskReportFiles(final IntLogger logger, final File workingDirectory, final RiskReportDataService riskReportDataService,
            final ProjectView project, final ProjectVersionView version) throws IOException, URISyntaxException, InterruptedException,
            IntegrationException {

        final String reportDirectoryPath = workingDirectory.getCanonicalPath() + File.separator + HubConstantValues.HUB_RISK_REPORT_DIRECTORY_NAME;
        final File reportDirectory = new File(reportDirectoryPath);
        riskReportDataService.createReportFiles(reportDirectory, project, version);
        artifactsWatcher.addNewArtifactsPath(reportDirectoryPath + "=>" + HubConstantValues.HUB_RISK_REPORT_DIRECTORY_NAME);

        // If we do not wait, the report tab will not be added and
        // it will appear that the report was unsuccessful
        Thread.sleep(2000);
    }

    private void checkPolicyFailures(final AgentRunningBuild build, final IntLogger logger,
            final HubServicesFactory services, final MetaService metaService, final ProjectVersionView version,
            final boolean isDryRun) {
        try {
            if (isDryRun) {
                logger.warn("Will not run the Failure conditions because this was a dry run scan.");
                return;
            }
            final String policyStatusLink = metaService.getFirstLink(version, MetaService.POLICY_STATUS_LINK);

            final VersionBomPolicyStatusView policyStatusItem = services.createHubResponseService().getItem(policyStatusLink, VersionBomPolicyStatusView.class);
            if (policyStatusItem == null) {
                final String message = "Could not find any information about the Policy status of the bom.";
                logger.error(message);
                build.stopBuild(message);
            }

            final PolicyStatusDescription policyStatusDescription = new PolicyStatusDescription(policyStatusItem);
            final String policyStatusMessage = policyStatusDescription.getPolicyStatusMessage();
            if (policyStatusItem.getOverallStatus() == VersionBomPolicyStatusOverallStatusEnum.IN_VIOLATION) {
                build.stopBuild(policyStatusMessage);
            } else {
                logger.info(policyStatusMessage);
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            build.stopBuild(e.getMessage());
        }
    }

    private String getPluginVersion(final CIEnvironmentVariables commonVariables) {
        String pluginVersion = commonVariables.getValue(HubConstantValues.PLUGIN_VERSION);
        if (StringUtils.isBlank(pluginVersion)) {
            final String pluginName = commonVariables.getValue(HubConstantValues.PLUGIN_NAME);
            int indexStartOfVersion = 0;
            if (pluginName.endsWith("-SNAPSHOT")) {
                indexStartOfVersion = pluginName.replace("-SNAPSHOT", "").lastIndexOf("-") + 1;
            } else {
                indexStartOfVersion = pluginName.lastIndexOf("-") + 1;
            }
            pluginVersion = pluginName.substring(indexStartOfVersion, pluginName.length());
        }
        return pluginVersion;
    }
}
