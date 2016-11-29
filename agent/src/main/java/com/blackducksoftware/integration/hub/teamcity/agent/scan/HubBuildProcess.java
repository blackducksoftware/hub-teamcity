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
package com.blackducksoftware.integration.hub.teamcity.agent.scan;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.restlet.resource.ResourceException;

import com.blackducksoftware.integration.builder.ValidationResultEnum;
import com.blackducksoftware.integration.builder.ValidationResults;
import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.ScanExecutor.Result;
import com.blackducksoftware.integration.hub.api.HubServicesFactory;
import com.blackducksoftware.integration.hub.api.HubVersionRestService;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusEnum;
import com.blackducksoftware.integration.hub.api.policy.PolicyStatusItem;
import com.blackducksoftware.integration.hub.api.scan.ScanSummaryItem;
import com.blackducksoftware.integration.hub.api.version.DistributionEnum;
import com.blackducksoftware.integration.hub.api.version.PhaseEnum;
import com.blackducksoftware.integration.hub.builder.HubScanJobConfigBuilder;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.cli.CLIDownloadService;
import com.blackducksoftware.integration.hub.cli.SimpleScanService;
import com.blackducksoftware.integration.hub.dataservices.policystatus.PolicyStatusDataService;
import com.blackducksoftware.integration.hub.dataservices.policystatus.PolicyStatusDescription;
import com.blackducksoftware.integration.hub.dataservices.report.RiskReportDataService;
import com.blackducksoftware.integration.hub.dataservices.scan.ScanStatusDataService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.UnexpectedHubResponseException;
import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.job.HubScanJobConfig;
import com.blackducksoftware.integration.hub.job.HubScanJobFieldEnum;
import com.blackducksoftware.integration.hub.rest.CredentialsRestConnection;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.teamcity.agent.HubAgentBuildLogger;
import com.blackducksoftware.integration.hub.teamcity.common.HubBundle;
import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;
import com.blackducksoftware.integration.hub.util.HostnameHelper;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.phone.home.PhoneHomeClient;
import com.blackducksoftware.integration.phone.home.enums.BlackDuckName;
import com.blackducksoftware.integration.phone.home.enums.ThirdPartyName;
import com.blackducksoftware.integration.phone.home.exception.PhoneHomeException;
import com.blackducksoftware.integration.phone.home.exception.PropertiesLoaderException;
import com.blackducksoftware.integration.util.CIEnvironmentVariables;

import jetbrains.buildServer.agent.AgentBuildFeature;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.version.ServerVersionHolder;

public class HubBuildProcess extends HubCallableBuildProcess {
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
        final CIEnvironmentVariables commonVariables = getCommonVariables();
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

        final HubServerConfig globalConfig = getHubServerConfig(hubLogger);
        printGlobalConfiguration(globalConfig);

        final String localHostName = HostnameHelper.getMyHostname();
        logger.info("Running on machine : " + localHostName);

        try {
            final File workingDirectory = context.getWorkingDirectory();
            final HubScanJobConfig jobConfig = getJobConfig(workingDirectory);
            if (jobConfig == null) {
                result = BuildFinishedStatus.FINISHED_FAILED;
                return result;
            }
            printJobConfiguration(jobConfig);
            final RestConnection restConnection = new CredentialsRestConnection(globalConfig);
            HubServicesFactory services = new HubServicesFactory(restConnection);
            final HubIntRestService restService = new HubIntRestService(restConnection);
            HubVersionRestService versionRestService = services.createHubVersionRestService();
            final String hubVersion = versionRestService.getHubVersion();

            final File hubToolDir = new File(build.getAgentConfiguration().getAgentToolsDirectory(), "HubCLI");

            final HubSupportHelper hubSupport = new HubSupportHelper();
            hubSupport.checkHubSupport(versionRestService, hubLogger);

            CLIDownloadService cliDownloadService = services.createCliDownloadService(logger);
            cliDownloadService.performInstallation(globalConfig.getProxyInfo(), hubToolDir, commonVariables, globalConfig.getHubUrl().toString(), hubVersion,
                    localHostName);
            try {
                String regId = null;
                String hubHostName = null;
                try {
                    regId = restService.getRegistrationId();
                } catch (final Exception e) {
                    logger.debug("Could not get the Hub registration Id.");
                }
                try {
                    final URL url = globalConfig.getHubUrl();
                    hubHostName = url.getHost();
                } catch (final Exception e) {
                    logger.debug("Could not get the Hub Host name.");
                }
                bdPhoneHome(hubVersion, regId, hubHostName);
            } catch (final Exception e) {
                logger.debug("Unable to phone-home", e);
            }
            int scanMemory = jobConfig.getScanMemory();
            String workDirectory = jobConfig.getWorkingDirectory();
            String projectName = jobConfig.getProjectName();
            String versionName = jobConfig.getVersion();
            boolean isDryRun = jobConfig.isDryRun();
            final SimpleScanService simpleScanService = services.createSimpleScanService(logger, restConnection, globalConfig, hubSupport, commonVariables,
                    hubToolDir, scanMemory, true, isDryRun, projectName, versionName, jobConfig.getScanTargetPaths(), workDirectory);

            Result scanResult = simpleScanService.setupAndExecuteScan();

            if (scanResult != Result.SUCCESS) {
                logger.error("Hub Scan Failed");
                result = BuildFinishedStatus.FINISHED_FAILED;
            } else {
                final List<ScanSummaryItem> scanSummaryList = simpleScanService.getScanSummaryItems();
                final long maximumWaitTime = jobConfig.getMaxWaitTimeForBomUpdateInMilliseconds();
                ScanStatusDataService scanStatusDataService = services.createScanStatusDataService();
                boolean waitForBom = true;
                if (BuildFinishedStatus.FINISHED_SUCCESS == result && jobConfig.isShouldGenerateRiskReport()
                        && !jobConfig.isDryRun()) {
                    waitForBom = false;
                    generateRiskReport(hubLogger, workingDirectory, scanSummaryList, scanStatusDataService, services.createRiskReportDataService(hubLogger),
                            projectName, versionName,
                            maximumWaitTime);
                }

                final Collection<AgentBuildFeature> features = build.getBuildFeaturesOfType(HubBundle.POLICY_FAILURE_CONDITION);
                // The feature is only allowed to have a single instance in the
                // configuration therefore we just want to make
                // sure the feature collection has something meaning that it was
                // configured.
                if (features != null && features.iterator() != null && !features.isEmpty()
                        && features.iterator().next() != null) {
                    if (waitForBom) {
                        waitForHub(scanStatusDataService, scanSummaryList, hubLogger, maximumWaitTime);
                    }
                    checkPolicyFailures(build, hubLogger, services, projectName, versionName, jobConfig.isDryRun());
                }
            }
        } catch (final Exception e) {
            logger.error(e);
            result = BuildFinishedStatus.FINISHED_FAILED;
        }
        logger.targetFinished("Hub Build Step");
        return result;
    }

    private HubServerConfig getHubServerConfig(final IntLogger logger)
            throws IllegalArgumentException, EncryptionException {
        final HubServerConfigBuilder configBuilder = new HubServerConfigBuilder();

        // read the credentials and proxy info using the existing objects.
        final String serverUrl = getParameter(HubConstantValues.HUB_URL);
        final String timeout = getParameter(HubConstantValues.HUB_CONNECTION_TIMEOUT);
        String username = getParameter(HubConstantValues.HUB_USERNAME);
        String password = getParameter(HubConstantValues.HUB_PASSWORD);
        String passwordLength = getParameter(HubConstantValues.HUB_PASSWORD_LENGTH);

        String proxyHost = getParameter(HubConstantValues.HUB_PROXY_HOST);
        String proxyPort = getParameter(HubConstantValues.HUB_PROXY_PORT);
        String ignoredProxyHosts = getParameter(HubConstantValues.HUB_NO_PROXY_HOSTS);
        String proxyUsername = getParameter(HubConstantValues.HUB_PROXY_USER);
        String proxyPassword = getParameter(HubConstantValues.HUB_PROXY_PASS);
        String proxyPasswordLength = getParameter(HubConstantValues.HUB_PROXY_PASS_LENGTH);

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

        final ValidationResults<GlobalFieldKey, HubServerConfig> builderResults = configBuilder.buildResults();
        if (builderResults.hasErrors()) {
            logger.error(builderResults.getAllResultString(ValidationResultEnum.ERROR));
        }

        return builderResults.getConstructedObject();
    }

    private HubScanJobConfig getJobConfig(File workingDirectory) throws IOException {

        final String projectName = getParameter(HubConstantValues.HUB_PROJECT_NAME);
        final String projectVersion = getParameter(HubConstantValues.HUB_PROJECT_VERSION);
        final String phase = getParameter(HubConstantValues.HUB_VERSION_PHASE);
        final String distribution = getParameter(HubConstantValues.HUB_VERSION_DISTRIBUTION);
        final String shouldGenerateRiskReport = getParameter(HubConstantValues.HUB_GENERATE_RISK_REPORT);
        final String maxWaitTimeForRiskReport = getParameter(HubConstantValues.HUB_MAX_WAIT_TIME_FOR_RISK_REPORT);
        final String dryRun = getParameter(HubConstantValues.HUB_DRY_RUN);
        final String scanMemory = getParameter(HubConstantValues.HUB_SCAN_MEMORY);

        final String workingDirectoryPath = workingDirectory.getCanonicalPath();

        final List<String> scanTargetPaths = new ArrayList<>();
        final String scanTargetParameter = getParameter(HubConstantValues.HUB_SCAN_TARGETS);
        if (StringUtils.isNotBlank(scanTargetParameter)) {
            final String[] scanTargetPathsArray = scanTargetParameter.split("\\r?\\n");
            for (final String target : scanTargetPathsArray) {
                if (!StringUtils.isBlank(target)) {
                    scanTargetPaths.add(new File(workingDirectory, target).getAbsolutePath());
                }
            }
        } else {
            scanTargetPaths.add(workingDirectory.getAbsolutePath());
        }

        final HubScanJobConfigBuilder hubScanJobConfigBuilder = new HubScanJobConfigBuilder(true);
        hubScanJobConfigBuilder.setProjectName(projectName);
        hubScanJobConfigBuilder.setVersion(projectVersion);
        hubScanJobConfigBuilder.setPhase(PhaseEnum.getPhaseByDisplayValue(phase).name());
        hubScanJobConfigBuilder
                .setDistribution(DistributionEnum.getDistributionByDisplayValue(distribution).name());
        hubScanJobConfigBuilder.setWorkingDirectory(workingDirectoryPath);
        hubScanJobConfigBuilder.setShouldGenerateRiskReport(shouldGenerateRiskReport);
        hubScanJobConfigBuilder.setDryRun(Boolean.valueOf(dryRun));
        if (StringUtils.isBlank(maxWaitTimeForRiskReport)) {
            hubScanJobConfigBuilder
                    .setMaxWaitTimeForBomUpdate(HubScanJobConfigBuilder.DEFAULT_BOM_UPDATE_WAIT_TIME_IN_MINUTES);
        } else {
            hubScanJobConfigBuilder.setMaxWaitTimeForBomUpdate(maxWaitTimeForRiskReport);
        }
        if (StringUtils.isBlank(maxWaitTimeForRiskReport)) {
            hubScanJobConfigBuilder.setScanMemory(HubScanJobConfigBuilder.DEFAULT_MEMORY_IN_MEGABYTES);
        } else {
            hubScanJobConfigBuilder.setScanMemory(scanMemory);
        }
        hubScanJobConfigBuilder.addAllScanTargetPaths(scanTargetPaths);

        final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> jobConfigResults = hubScanJobConfigBuilder
                .buildResults();
        if (!jobConfigResults.isSuccess()) {
            final Set<HubScanJobFieldEnum> keys = jobConfigResults.getResultMap().keySet();
            for (final HubScanJobFieldEnum fieldKey : keys) {
                if (jobConfigResults.hasErrors(fieldKey)) {
                    logger.error(jobConfigResults.getResultString(fieldKey, ValidationResultEnum.ERROR));
                }
                if (jobConfigResults.hasWarnings(fieldKey)) {
                    logger.warn(jobConfigResults.getResultString(fieldKey, ValidationResultEnum.WARN));
                }
            }
        }
        return jobConfigResults.getConstructedObject();
    }

    private String getParameter(@NotNull final String parameterName) {
        return StringUtils.trimToNull(context.getRunnerParameters().get(parameterName));
    }

    private CIEnvironmentVariables getCommonVariables() {
        final CIEnvironmentVariables variables = new CIEnvironmentVariables();
        variables.putAll(context.getBuildParameters().getEnvironmentVariables());
        variables.putAll(context.getBuildParameters().getSystemProperties());
        variables.putAll(context.getConfigParameters());
        variables.putAll(context.getRunnerParameters());
        return variables;
    }

    public void printGlobalConfiguration(final HubServerConfig globalConfig) {
        if (globalConfig == null) {
            return;
        }
        logger.alwaysLog("--> Log Level " + logger.getLogLevel().name());

        logger.alwaysLog("--> Hub Server Url : " + globalConfig.getHubUrl());
        if (globalConfig.getGlobalCredentials() != null
                && StringUtils.isNotBlank(globalConfig.getGlobalCredentials().getUsername())) {
            logger.alwaysLog("--> Hub User : " + globalConfig.getGlobalCredentials().getUsername());
        }
        logger.alwaysLog("--> Hub Connection Timeout : " + globalConfig.getTimeout());

        if (globalConfig.getProxyInfo() != null) {
            if (StringUtils.isNotBlank(globalConfig.getProxyInfo().getHost())) {
                logger.alwaysLog("--> Proxy Host : " + globalConfig.getProxyInfo().getHost());
            }
            if (globalConfig.getProxyInfo().getPort() > 0) {
                logger.alwaysLog("--> Proxy Port : " + globalConfig.getProxyInfo().getPort());
            }
            if (StringUtils.isNotBlank(globalConfig.getProxyInfo().getIgnoredProxyHosts())) {
                logger.alwaysLog("--> No Proxy Hosts : " + globalConfig.getProxyInfo().getIgnoredProxyHosts());
            }
            if (StringUtils.isNotBlank(globalConfig.getProxyInfo().getUsername())) {
                logger.alwaysLog("--> Proxy Username : " + globalConfig.getProxyInfo().getUsername());
            }
        }
    }

    public void printJobConfiguration(final HubScanJobConfig jobConfig) {
        if (jobConfig == null) {
            return;
        }
        logger.alwaysLog("Working directory : " + jobConfig.getWorkingDirectory());
        logger.alwaysLog("TeamCity Hub Plugin version : " + getPluginVersion());
        logger.alwaysLog("--> Project : " + jobConfig.getProjectName());
        logger.alwaysLog("--> Version : " + jobConfig.getVersion());
        logger.alwaysLog("--> Version Phase : " + PhaseEnum.valueOf(jobConfig.getPhase()).getDisplayValue());
        logger.alwaysLog("--> Version Distribution : "
                + DistributionEnum.valueOf(jobConfig.getDistribution()).getDisplayValue());
        logger.alwaysLog("--> Hub scan memory : " + jobConfig.getScanMemory() + " MB");

        if (jobConfig.getScanTargetPaths().size() > 0) {
            logger.alwaysLog("--> Hub scan targets : ");
            for (final String absolutePath : jobConfig.getScanTargetPaths()) {
                logger.alwaysLog("    --> " + absolutePath);
            }
        }

        logger.alwaysLog("-> Generate Hub report : " + jobConfig.isShouldGenerateRiskReport());
        final String formattedTime = String.format("%d minutes", jobConfig.getMaxWaitTimeForBomUpdate());
        logger.alwaysLog("-> Maximum wait time for the BOM Update : " + formattedTime);
        logger.alwaysLog("-> Dry Run : " + jobConfig.isDryRun());
    }

    private void generateRiskReport(final IntLogger logger, final File workingDirectory, final List<ScanSummaryItem> scanSummaryList,
            final ScanStatusDataService scanStatusDataService, final RiskReportDataService riskReportDataService,
            final String projectName, final String versionName, long maximumWaitTime) throws IOException, BDRestException, URISyntaxException,
            ProjectDoesNotExistException, HubIntegrationException, InterruptedException, UnexpectedHubResponseException {
        logger.info("Generating Risk Report");
        waitForHub(scanStatusDataService, scanSummaryList, logger, maximumWaitTime);
        final String reportDirectoryPath = workingDirectory.getCanonicalPath() + File.separator + HubConstantValues.HUB_RISK_REPORT_DIRECTORY_NAME;
        File reportDirectory = new File(reportDirectoryPath);
        riskReportDataService.createRiskReport(reportDirectory, projectName, versionName);
        artifactsWatcher.addNewArtifactsPath(reportDirectoryPath + "=>" + HubConstantValues.HUB_RISK_REPORT_DIRECTORY_NAME);

        // If we do not wait, the report tab will not be added and
        // it will appear that the report was unsuccessful
        Thread.sleep(2000);
    }

    private void checkPolicyFailures(final AgentRunningBuild build, final IntLogger logger,
            final HubServicesFactory services, final String projectName, String versionName,
            final boolean isDryRun) throws UnexpectedHubResponseException {
        try {
            if (isDryRun) {
                logger.warn("Will not run the Failure conditions because this was a dry run scan.");
                return;
            }

            final PolicyStatusDataService policyStatusDataService = services.createPolicyStatusDataService();

            final PolicyStatusItem policyStatusItem = policyStatusDataService
                    .getPolicyStatusForProjectAndVersion(projectName, versionName);
            if (policyStatusItem == null) {
                String message = "Could not find any information about the Policy status of the bom.";
                logger.error(message);
                build.stopBuild(message);
            }

            final PolicyStatusDescription policyStatusDescription = new PolicyStatusDescription(policyStatusItem);
            final String policyStatusMessage = policyStatusDescription.getPolicyStatusMessage();
            if (policyStatusItem.getOverallStatus() == PolicyStatusEnum.IN_VIOLATION) {
                logger.error(policyStatusMessage);
                build.stopBuild(policyStatusMessage);
            }
            logger.info(policyStatusMessage);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            build.stopBuild(e.getMessage());
        }
    }

    private String getPluginVersion() {
        String pluginVersion = getParameter(HubConstantValues.PLUGIN_VERSION);
        if (StringUtils.isBlank(pluginVersion)) {
            final String pluginName = getParameter(HubConstantValues.PLUGIN_NAME);
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

    /**
     * @param blackDuckVersion
     *            Version of the blackduck product, in this instance, the hub
     * @param regId
     *            Registration ID of the hub instance that this plugin uses
     * @param hubHostName
     *            Host name of the hub instance that this plugin uses
     *
     *            This method "phones-home" to the internal BlackDuck
     *            Integrations server. Every time a build is kicked off,
     */
    public void bdPhoneHome(final String blackDuckVersion, final String regId, final String hubHostName)
            throws IOException, PhoneHomeException, PropertiesLoaderException, ResourceException, JSONException {
        final String thirdPartyVersion = ServerVersionHolder.getVersion().getDisplayVersion();
        final String pluginVersion = getPluginVersion();

        final PhoneHomeClient phClient = new PhoneHomeClient();
        phClient.callHomeIntegrations(regId, hubHostName, BlackDuckName.HUB, blackDuckVersion, ThirdPartyName.TEAM_CITY,
                thirdPartyVersion, pluginVersion);
    }

    private void waitForHub(ScanStatusDataService scanStatusDataService, List<ScanSummaryItem> pendingScans, final IntLogger logger, final long timeout) {
        try {
            scanStatusDataService.assertBomImportScansFinished(pendingScans, timeout);
        } catch (IOException | BDRestException | URISyntaxException | ProjectDoesNotExistException | UnexpectedHubResponseException
                | HubIntegrationException | InterruptedException e) {
            logger.error(String.format("There was an error waiting for the scans: %s", e.getMessage()), e);
        }
    }
}
