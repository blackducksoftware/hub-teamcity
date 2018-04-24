/**
 * Black Duck Hub Plug-In for TeamCity Agent
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
import com.blackducksoftware.integration.hub.api.generated.component.ProjectRequest;
import com.blackducksoftware.integration.hub.api.generated.enumeration.PolicyStatusApprovalStatusType;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectVersionView;
import com.blackducksoftware.integration.hub.api.generated.view.ProjectView;
import com.blackducksoftware.integration.hub.api.generated.view.VersionBomPolicyStatusView;
import com.blackducksoftware.integration.hub.api.view.MetaHandler;
import com.blackducksoftware.integration.hub.configuration.HubScanConfig;
import com.blackducksoftware.integration.hub.configuration.HubScanConfigBuilder;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.service.HubService;
import com.blackducksoftware.integration.hub.service.HubServicesFactory;
import com.blackducksoftware.integration.hub.service.PhoneHomeService;
import com.blackducksoftware.integration.hub.service.ReportService;
import com.blackducksoftware.integration.hub.service.SignatureScannerService;
import com.blackducksoftware.integration.hub.service.model.HostnameHelper;
import com.blackducksoftware.integration.hub.service.model.PolicyStatusDescription;
import com.blackducksoftware.integration.hub.service.model.ProjectRequestBuilder;
import com.blackducksoftware.integration.hub.service.model.ProjectVersionWrapper;
import com.blackducksoftware.integration.hub.teamcity.agent.HubAgentBuildLogger;
import com.blackducksoftware.integration.hub.teamcity.common.HubBundle;
import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBody;
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

    public HubBuildProcess(@NotNull final AgentRunningBuild build, @NotNull final BuildRunnerContext context, @NotNull final ArtifactsWatcher artifactsWatcher) {
        this.build = build;
        this.context = context;
        this.artifactsWatcher = artifactsWatcher;
    }

    public boolean isVerbose() {
        if (verbose == null) {
            verbose = true;
        }
        return verbose;
    }

    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    public void setHubLogger(final HubAgentBuildLogger logger) {
        this.logger = logger;
    }

    @Override
    public BuildFinishedStatus call() throws IOException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, EncryptionException {
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
            if (features != null && features.iterator() != null && !features.isEmpty() && features.iterator().next() != null) {
                isFailOnPolicySelected = true;
            }

            long waitTimeForReport;
            final String maxWaitTimeForRiskReport = commonVariables.getValue(HubConstantValues.HUB_MAX_WAIT_TIME_FOR_RISK_REPORT);
            if (StringUtils.isNotBlank(maxWaitTimeForRiskReport)) {
                waitTimeForReport = NumberUtils.toInt(maxWaitTimeForRiskReport);
                if (waitTimeForReport <= 0) {
                    // 5 minutes is the default
                    waitTimeForReport = DEFAULT_MAX_WAIT_TIME_MILLISEC;
                } else {
                    waitTimeForReport = waitTimeForReport * 60 * 1000;
                }
            } else {
                waitTimeForReport = DEFAULT_MAX_WAIT_TIME_MILLISEC;
            }

            logger.info("--> Generate Risk Report : " + isRiskReportGenerated);
            logger.info("--> Bom wait time : " + maxWaitTimeForRiskReport);
            logger.info("--> Check Policies : " + isFailOnPolicySelected);

            final File workingDirectory = context.getWorkingDirectory();
            final File toolsDir = new File(build.getAgentConfiguration().getAgentToolsDirectory(), "HubCLI");
            final HubScanConfig hubScanConfig = getScanConfig(workingDirectory, toolsDir, hubLogger, commonVariables);

            final RestConnection restConnection = getRestConnection(logger, hubConfig);
            restConnection.connect();

            HubServicesFactory services = new HubServicesFactory(restConnection);
            services.addEnvironmentVariables(variables);

            PhoneHomeService phoneHomeService = services.createPhoneHomeService();
            PhoneHomeRequestBody.Builder builder = phoneHomeService.createInitialPhoneHomeRequestBodyBuilder();
            builder.setArtifactId("hub-teamcity");
            builder.setArtifactVersion(pluginVersion);
            builder.addToMetaData("teamcity.version", thirdPartyVersion);
            phoneHomeService.phoneHome(builder);

            final SignatureScannerService signatureScannerService = services.createSignatureScannerService(hubConfig.getTimeout() * 60 * 1000);

            final ProjectRequest projectRequest = getProjectRequest(hubLogger, commonVariables);
            if (hubScanConfig == null) {
                logger.error("Please verify the Black Duck Hub Runner configuration is correct.");
                result = BuildFinishedStatus.FINISHED_FAILED;
                return result;
            } else if (projectRequest == null) {
                logger.debug("No project and version specified.");
            }

            final boolean shouldWaitForScansFinished = isRiskReportGenerated || isFailOnPolicySelected;
            ProjectVersionWrapper projectVersionWrapper = null;
            try {
                projectVersionWrapper = signatureScannerService.installAndRunControlledScan(hubConfig, hubScanConfig, projectRequest, shouldWaitForScansFinished);

            } catch (final HubIntegrationException e) {
                logger.error(e.getMessage(), e);
                result = BuildFinishedStatus.FINISHED_FAILED;
                return result;
            } catch (final InterruptedException e) {
                logger.error("BD scan was interrupted.");
                result = BuildFinishedStatus.INTERRUPTED;
                return result;
            }
            if (!hubScanConfig.isDryRun()) {
                final MetaHandler metaHandler = new MetaHandler(logger);

                ProjectView project = null;
                if (isRiskReportGenerated) {
                    logger.info("Generating Risk Report");
                    publishRiskReportFiles(logger, workingDirectory, services.createReportService(waitTimeForReport), projectVersionWrapper.getProjectView(), projectVersionWrapper.getProjectVersionView());
                }
                if (isFailOnPolicySelected) {
                    logger.info("Checking for Policy violations.");
                    checkPolicyFailures(build, logger, services.createHubService(), metaHandler, projectVersionWrapper.getProjectVersionView(), hubScanConfig.isDryRun());
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
        return hubServerConfig.createCredentialsRestConnection(logger);
    }

    private HubServerConfig getHubServerConfig(final IntLogger logger, final CIEnvironmentVariables commonVariables) {
        final HubServerConfigBuilder configBuilder = new HubServerConfigBuilder();

        // read the credentials and proxy info using the existing objects.
        final String serverUrl = commonVariables.getValue(HubConstantValues.HUB_URL);
        final String timeout = commonVariables.getValue(HubConstantValues.HUB_CONNECTION_TIMEOUT);
        final String username = commonVariables.getValue(HubConstantValues.HUB_USERNAME);
        final String password = commonVariables.getValue(HubConstantValues.HUB_PASSWORD);
        final String passwordLength = commonVariables.getValue(HubConstantValues.HUB_PASSWORD_LENGTH);

        final String alwaysTrustServerCertificates = commonVariables.getValue(HubConstantValues.HUB_TRUST_SERVER_CERT);

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

        configBuilder.setAlwaysTrustServerCertificate(Boolean.valueOf(alwaysTrustServerCertificates));

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

    private HubScanConfig getScanConfig(final File workingDirectory, final File toolsDir, final IntLogger logger, final CIEnvironmentVariables commonVariables) throws IOException {

        final String dryRun = commonVariables.getValue(HubConstantValues.HUB_DRY_RUN);
        final String cleanupLogs = commonVariables.getValue(HubConstantValues.HUB_CLEANUP_LOGS_ON_SUCCESS);

        final String scanMemory = commonVariables.getValue(HubConstantValues.HUB_SCAN_MEMORY);

        final String codeLocationName = commonVariables.getValue(HubConstantValues.HUB_CODE_LOCATION_NAME);
        final String unmapPreviousCodeLocations = commonVariables.getValue(HubConstantValues.HUB_UNMAP_PREVIOUS_CODE_LOCATIONS);
        final String deletePreviousCodeLocations = commonVariables.getValue(HubConstantValues.HUB_DELETE_PREVIOUS_CODE_LOCATIONS);

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
        hubScanConfigBuilder.setWorkingDirectory(workingDirectory);
        hubScanConfigBuilder.setDryRun(Boolean.valueOf(dryRun));
        hubScanConfigBuilder.setScanMemory(scanMemory);
        hubScanConfigBuilder.setCodeLocationAlias(codeLocationName);
        hubScanConfigBuilder.addAllScanTargetPaths(scanTargets);
        hubScanConfigBuilder.setToolsDir(toolsDir);
        hubScanConfigBuilder.setCleanupLogsOnSuccess(Boolean.valueOf(cleanupLogs));
        hubScanConfigBuilder.setUnmapPreviousCodeLocations(Boolean.valueOf(unmapPreviousCodeLocations));
        hubScanConfigBuilder.setDeletePreviousCodeLocations(Boolean.valueOf(deletePreviousCodeLocations));
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

    private ProjectRequest getProjectRequest(final IntLogger logger, final CIEnvironmentVariables commonVariables) {
        final ProjectRequestBuilder projectRequestBuilder = new ProjectRequestBuilder();
        projectRequestBuilder.setProjectName(commonVariables.getValue(HubConstantValues.HUB_PROJECT_NAME));
        projectRequestBuilder.setVersionName(commonVariables.getValue(HubConstantValues.HUB_PROJECT_VERSION));
        projectRequestBuilder.setPhase(commonVariables.getValue(HubConstantValues.HUB_PHASE));
        projectRequestBuilder.setDistribution(commonVariables.getValue(HubConstantValues.HUB_DISTRIBUTION));
        projectRequestBuilder.setProjectLevelAdjustments(Boolean.valueOf(commonVariables.getValue(HubConstantValues.HUB_MATCH_ADJUSTMENTS)));
        try {
            return projectRequestBuilder.build();
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

    private void publishRiskReportFiles(final IntLogger logger, final File workingDirectory, final ReportService reportSerivce, final ProjectView project, final ProjectVersionView version)
            throws IOException, InterruptedException, IntegrationException {

        final String reportDirectoryPath = workingDirectory.getCanonicalPath() + File.separator + HubConstantValues.HUB_RISK_REPORT_DIRECTORY_NAME;
        final File reportDirectory = new File(reportDirectoryPath);
        reportSerivce.createReportFiles(reportDirectory, project, version);
        artifactsWatcher.addNewArtifactsPath(reportDirectoryPath + "=>" + HubConstantValues.HUB_RISK_REPORT_DIRECTORY_NAME);

        // If we do not wait, the report tab will not be added and
        // it will appear that the report was unsuccessful
        Thread.sleep(2000);
    }

    private void checkPolicyFailures(final AgentRunningBuild build, final IntLogger logger, final HubService hubService, final MetaHandler metaHandler, final ProjectVersionView version, final boolean isDryRun) {
        try {
            if (isDryRun) {
                logger.warn("Will not run the Failure conditions because this was a dry run scan.");
                return;
            }
            String policyStatusLink = null;
            try {
                policyStatusLink = metaHandler.getFirstLink(version, ProjectVersionView.POLICY_STATUS_LINK);
            } catch (final Exception e) {
                logger.warn("Could not get the policy status link, the Hub policy module is not enabled");
            }
            if (null != policyStatusLink) {
                VersionBomPolicyStatusView policyStatusItem = hubService.getResponse(version, ProjectVersionView.POLICY_STATUS_LINK_RESPONSE);
                if (policyStatusItem == null) {
                    final String message = "Could not find any information about the Policy status of the bom.";
                    logger.error(message);
                    build.stopBuild(message);
                }

                final PolicyStatusDescription policyStatusDescription = new PolicyStatusDescription(policyStatusItem);
                final String policyStatusMessage = policyStatusDescription.getPolicyStatusMessage();
                if (policyStatusItem.overallStatus == PolicyStatusApprovalStatusType.IN_VIOLATION) {
                    build.stopBuild(policyStatusMessage);
                } else {
                    logger.info(policyStatusMessage);
                }
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
