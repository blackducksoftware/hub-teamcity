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
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;

import com.blackducksoftware.integration.hub.CIEnvironmentVariables;
import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.ScanExecutor;
import com.blackducksoftware.integration.hub.ScanExecutor.Result;
import com.blackducksoftware.integration.hub.builder.HubScanJobConfigBuilder;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.builder.ValidationResultEnum;
import com.blackducksoftware.integration.hub.builder.ValidationResults;
import com.blackducksoftware.integration.hub.cli.CLIInstaller;
import com.blackducksoftware.integration.hub.cli.CLILocation;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.MissingPolicyStatusException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.exception.VersionDoesNotExistException;
import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubProxyInfo;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.job.HubScanJobConfig;
import com.blackducksoftware.integration.hub.job.HubScanJobFieldEnum;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.policy.api.PolicyStatus;
import com.blackducksoftware.integration.hub.policy.api.PolicyStatusEnum;
import com.blackducksoftware.integration.hub.polling.HubEventPolling;
import com.blackducksoftware.integration.hub.project.api.ProjectItem;
import com.blackducksoftware.integration.hub.report.api.HubReportGenerationInfo;
import com.blackducksoftware.integration.hub.report.api.HubRiskReportData;
import com.blackducksoftware.integration.hub.report.api.RiskReportGenerator;
import com.blackducksoftware.integration.hub.teamcity.agent.HubAgentBuildLogger;
import com.blackducksoftware.integration.hub.teamcity.agent.exceptions.TeamCityHubPluginException;
import com.blackducksoftware.integration.hub.teamcity.common.HubBundle;
import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;
import com.blackducksoftware.integration.hub.util.HostnameHelper;
import com.blackducksoftware.integration.hub.version.api.DistributionEnum;
import com.blackducksoftware.integration.hub.version.api.PhaseEnum;
import com.blackducksoftware.integration.hub.version.api.ReleaseItem;
import com.google.gson.Gson;

import jetbrains.buildServer.agent.AgentBuildFeature;
import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;

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
	public BuildFinishedStatus call() throws IOException {
		final BuildProgressLogger buildLogger = build.getBuildLogger();
		final HubAgentBuildLogger hubLogger = new HubAgentBuildLogger(buildLogger);
		final String logLevel = getAnyParameterType(HubConstantValues.HUB_LOG_LEVEL);
		hubLogger.setLogLevel(logLevel);
		setHubLogger(hubLogger);

		if (StringUtils.isBlank(System.getProperty("http.maxRedirects"))) {
			// If this property is not set the default is 20
			// When not set the Authenticator redirects in a loop and results in
			// an error for too many redirects
			System.setProperty("http.maxRedirects", "3");
		}

		result = BuildFinishedStatus.FINISHED_SUCCESS;

		logger.targetStarted("Hub Build Step");

		final HubServerConfigBuilder configBuilder = new HubServerConfigBuilder();

		final String serverUrl = getParameter(HubConstantValues.HUB_URL);
		configBuilder.setHubUrl(serverUrl);
		configBuilder.setUsername(getParameter(HubConstantValues.HUB_USERNAME));
		configBuilder.setPassword(getParameter(HubConstantValues.HUB_PASSWORD));
		configBuilder.setProxyHost(getParameter(HubConstantValues.HUB_PROXY_HOST));
		configBuilder.setProxyPort(getParameter(HubConstantValues.HUB_PROXY_PORT));
		configBuilder.setIgnoredProxyHosts(getParameter(HubConstantValues.HUB_NO_PROXY_HOSTS));
		configBuilder.setProxyUsername(getParameter(HubConstantValues.HUB_PROXY_USER));
		configBuilder.setProxyPassword(getParameter(HubConstantValues.HUB_PROXY_PASS));
		final ValidationResults<GlobalFieldKey, HubServerConfig> builderResults = configBuilder.build();

		final HubServerConfig globalConfig = builderResults.getConstructedObject();
		printGlobalConfiguration(globalConfig);

		final String projectName = getParameter(HubConstantValues.HUB_PROJECT_NAME);
		final String projectVersion = getParameter(HubConstantValues.HUB_PROJECT_VERSION);
		final String phase = getParameter(HubConstantValues.HUB_VERSION_PHASE);
		final String distribution = getParameter(HubConstantValues.HUB_VERSION_DISTRIBUTION);
		final String shouldGenerateRiskReport = getParameter(HubConstantValues.HUB_GENERATE_RISK_REPORT);
		final String maxWaitTimeForRiskReport = getParameter(HubConstantValues.HUB_MAX_WAIT_TIME_FOR_RISK_REPORT);
		final String scanMemory = getParameter(HubConstantValues.HUB_SCAN_MEMORY);

		final File workingDirectory = context.getWorkingDirectory();
		final String workingDirectoryPath = workingDirectory.getCanonicalPath();

		final List<String> scanTargetPaths = new ArrayList<String>();
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

		final String localHostName = HostnameHelper.getMyHostname();
		logger.info("Running on machine : " + localHostName);

		try {
			final HubScanJobConfigBuilder hubScanJobConfigBuilder = new HubScanJobConfigBuilder(true);
			hubScanJobConfigBuilder.setProjectName(projectName);
			hubScanJobConfigBuilder.setVersion(projectVersion);
			hubScanJobConfigBuilder.setPhase(PhaseEnum.getPhaseByDisplayValue(phase).name());
			hubScanJobConfigBuilder
					.setDistribution(DistributionEnum.getDistributionByDisplayValue(distribution).name());
			hubScanJobConfigBuilder.setWorkingDirectory(workingDirectoryPath);
			hubScanJobConfigBuilder.setShouldGenerateRiskReport(shouldGenerateRiskReport);
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

			final ValidationResults<HubScanJobFieldEnum, HubScanJobConfig> globalResults = hubScanJobConfigBuilder
					.build();
			if (!builderResults.isSuccess()) {
				result = BuildFinishedStatus.FINISHED_FAILED;
				final Set<HubScanJobFieldEnum> keys = globalResults.getResultMap().keySet();
				for (final HubScanJobFieldEnum fieldKey : keys) {
					if (globalResults.hasErrors(fieldKey)) {
						logger.error(globalResults.getResultString(fieldKey, ValidationResultEnum.ERROR));
					}
					if (globalResults.hasWarnings(fieldKey)) {
						logger.warn(globalResults.getResultString(fieldKey, ValidationResultEnum.WARN));
					}
				}
				return result;
			}
			final HubScanJobConfig jobConfig = globalResults.getConstructedObject();

			printJobConfiguration(jobConfig);

			if (globalResults.isSuccess()) {
				final URL hubUrl = globalConfig.getHubUrl();
				final HubIntRestService restService = new HubIntRestService(serverUrl);
				restService.setLogger(logger);
				final HubProxyInfo proxyInfo = globalConfig.getProxyInfo();
				if (proxyInfo != null) {
					if (!globalConfig.getProxyInfo().shouldUseProxyForUrl(hubUrl)) {
						restService.setProxyProperties(proxyInfo);
					}
				}
				restService.setCookies(globalConfig.getGlobalCredentials().getUsername(),
						globalConfig.getGlobalCredentials().getDecryptedPassword());

				final Map<String, String> teamCityEnvironmentVariables = context.getBuildParameters()
						.getEnvironmentVariables();
				final CIEnvironmentVariables ciEnvironmentVariables = new CIEnvironmentVariables();
				ciEnvironmentVariables.putAll(teamCityEnvironmentVariables);

				final File hubToolDir = new File(build.getAgentConfiguration().getAgentToolsDirectory(), "HubCLI");
				final CLILocation cliLocation = new CLILocation(hubToolDir);
				final CLIInstaller installer = new CLIInstaller(cliLocation, ciEnvironmentVariables);
				if (proxyInfo.shouldUseProxyForUrl(hubUrl)) {
					installer.setProxyHost(proxyInfo.getHost());
					installer.setProxyPort(proxyInfo.getPort());
					installer.setProxyUserName(proxyInfo.getUsername());
					installer.setProxyPassword(proxyInfo.getDecryptedPassword());
				}
				installer.performInstallation(logger, restService, localHostName);

				File hubCLI = null;
				if (cliLocation.getCLIExists(hubLogger)) {
					hubCLI = cliLocation.getCLI(hubLogger);
				} else {
					hubLogger.error("Could not find the Hub scan CLI.");
					result = BuildFinishedStatus.FINISHED_FAILED;
					return result;
				}
				final File oneJarFile = cliLocation.getOneJarFile();
				final File javaExec = cliLocation.getProvidedJavaExec();

				ProjectItem project = null;
				ReleaseItem version = null;
				if (null != jobConfig.getProjectName() && null != jobConfig.getVersion()) {
					project = ensureProjectExists(restService, logger, projectName);
					version = ensureVersionExists(restService, logger, projectVersion, project, jobConfig);
				}

				final HubSupportHelper hubSupport = new HubSupportHelper();
				hubSupport.checkHubSupport(restService, hubLogger);

				final ScanExecutor scanExecutor = doHubScan(restService, hubLogger, oneJarFile, hubCLI, javaExec,
						globalConfig, jobConfig, hubSupport);

				final HubReportGenerationInfo hubReportGenerationInfo = new HubReportGenerationInfo();
				hubReportGenerationInfo.setService(restService);
				hubReportGenerationInfo.setHostname(localHostName);
				hubReportGenerationInfo.setProject(project);
				hubReportGenerationInfo.setVersion(version);
				hubReportGenerationInfo.setScanTargets(jobConfig.getScanTargetPaths());
				hubReportGenerationInfo.setMaximumWaitTime(jobConfig.getMaxWaitTimeForBomUpdateInMilliseconds());
				hubReportGenerationInfo.setScanStatusDirectory(scanExecutor.getScanStatusDirectoryPath());

				boolean waitForBom = true;
				if (BuildFinishedStatus.FINISHED_SUCCESS == result && jobConfig.isShouldGenerateRiskReport()) {
					final RiskReportGenerator riskReportGenerator = new RiskReportGenerator(hubReportGenerationInfo,
							hubSupport);
					// will wait for bom to be updated while generating the
					// report.
					final HubRiskReportData hubRiskReportData = riskReportGenerator.generateHubReport(logger);
					waitForBom = false;
					final String reportPath = workingDirectoryPath + File.separator
							+ HubConstantValues.HUB_RISK_REPORT_FILENAME;

					final Gson gson = new Gson();
					final String contents = gson.toJson(hubRiskReportData);

					final FileWriter writer = new FileWriter(reportPath);
					writer.write(contents);
					writer.close();

					artifactsWatcher.addNewArtifactsPath(reportPath);
					// If we do not wait, the report tab will not be added and
					// it will appear that the report was unsuccessful
					Thread.sleep(2000);
				}
				checkPolicyFailures(build, hubLogger, hubSupport, restService, hubReportGenerationInfo, version,
						waitForBom);
			} else {
				logger.info("Skipping Hub Build Step");
				result = BuildFinishedStatus.FINISHED_FAILED;
			}
		} catch (final Exception e) {
			logger.error(e);
			result = BuildFinishedStatus.FINISHED_FAILED;
		}
		logger.targetFinished("Hub Build Step");
		return result;
	}

	private String getParameter(@NotNull final String parameterName) {
		return StringUtils.trimToNull(context.getRunnerParameters().get(parameterName));
	}

	private String getConfigParameter(@NotNull final String parameterName) {
		return StringUtils.trimToNull(context.getConfigParameters().get(parameterName));
	}

	private String getSystemVariable(@NotNull final String parameterName) {
		return StringUtils.trimToNull(context.getBuildParameters().getSystemProperties().get(parameterName));
	}

	private String getEnvironmentVariable(@NotNull final String parameterName) {
		return StringUtils.trimToNull(context.getBuildParameters().getEnvironmentVariables().get(parameterName));
	}

	private String getAnyParameterType(@NotNull final String parameterName) {
		String value = getParameter(parameterName);
		if (value == null) {
			value = getConfigParameter(parameterName);
		}
		if (value == null) {
			value = getSystemVariable(parameterName);
		}
		if (value == null) {
			value = getEnvironmentVariable(parameterName);
		}
		return value;
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
	}

	private ProjectItem ensureProjectExists(final HubIntRestService service, final IntLogger logger,
			final String projectName) throws IOException, URISyntaxException, TeamCityHubPluginException {
		ProjectItem project = null;
		try {
			project = service.getProjectByName(projectName);
		} catch (final NullPointerException npe) {
			project = createProject(service, logger, projectName);
		} catch (final ProjectDoesNotExistException e) {
			project = createProject(service, logger, projectName);
		} catch (final BDRestException e) {
			if (e.getResource() != null) {
				if (e.getResource() != null) {
					logger.error("Status : " + e.getResource().getStatus().getCode());
					logger.error("Response : " + e.getResource().getResponse().getEntityAsText());
				}
				throw new TeamCityHubPluginException("Problem getting the Project. ", e);
			}
		}

		return project;
	}

	private ProjectItem createProject(final HubIntRestService service, final IntLogger logger, final String projectName)
			throws IOException, URISyntaxException, TeamCityHubPluginException {
		// Project was not found, try to create it
		ProjectItem project = null;
		try {
			final String projectUrl = service.createHubProject(projectName);
			project = service.getProject(projectUrl);
		} catch (final BDRestException e1) {
			if (e1.getResource() != null) {
				logger.error("Status : " + e1.getResource().getStatus().getCode());
				logger.error("Response : " + e1.getResource().getResponse().getEntityAsText());
			}
			throw new TeamCityHubPluginException("Problem creating the Project. ", e1);
		}

		return project;
	}

	/**
	 * Ensures the Version exists. Returns the version URL
	 */
	private ReleaseItem ensureVersionExists(final HubIntRestService service, final IntLogger logger,
			final String projectVersion, final ProjectItem project, final HubScanJobConfig jobConfig)
			throws IOException, URISyntaxException, TeamCityHubPluginException {
		ReleaseItem version = null;
		try {
			version = service.getVersion(project, projectVersion);
			if (!version.getPhase().equals(jobConfig.getPhase())) {
				logger.warn(
						"The selected Phase does not match the Phase of this Version. If you wish to update the Phase please do so in the Hub UI.");
			}
			if (!version.getDistribution().equals(jobConfig.getDistribution())) {
				logger.warn(
						"The selected Distribution does not match the Distribution of this Version. If you wish to update the Distribution please do so in the Hub UI.");
			}
		} catch (final NullPointerException npe) {
			version = createVersion(service, logger, projectVersion, project, jobConfig);
		} catch (final VersionDoesNotExistException e) {
			version = createVersion(service, logger, projectVersion, project, jobConfig);
		} catch (final BDRestException e) {
			throw new TeamCityHubPluginException("Could not retrieve or create the specified version.", e);
		}
		return version;
	}

	private ReleaseItem createVersion(final HubIntRestService service, final IntLogger logger,
			final String projectVersion, final ProjectItem project, final HubScanJobConfig jobConfig)
			throws IOException, URISyntaxException, TeamCityHubPluginException {
		ReleaseItem version = null;

		try {
			final String versionURL = service.createHubVersion(project, projectVersion, jobConfig.getPhase(),
					jobConfig.getDistribution());
			version = service.getProjectVersion(versionURL);
		} catch (final BDRestException e1) {
			if (e1.getResource() != null) {
				logger.error("Status : " + e1.getResource().getStatus().getCode());
				logger.error("Response : " + e1.getResource().getResponse().getEntityAsText());
			}
			throw new TeamCityHubPluginException("Problem creating the Version. ", e1);
		}

		return version;
	}

	public ScanExecutor doHubScan(final HubIntRestService service, final HubAgentBuildLogger logger,
			final File oneJarFile, final File scanExec, File javaExec, final HubServerConfig globalConfig,
			final HubScanJobConfig jobConfig, final HubSupportHelper supportHelper) throws HubIntegrationException,
			IOException, URISyntaxException, NumberFormatException, NoSuchMethodException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, EncryptionException {
		final TeamCityScanExecutor scan = new TeamCityScanExecutor(globalConfig.getHubUrl().toString(),
				globalConfig.getGlobalCredentials().getUsername(),
				globalConfig.getGlobalCredentials().getDecryptedPassword(), jobConfig.getScanTargetPaths(),
				Integer.valueOf(context.getBuild().getBuildNumber()), supportHelper, logger);

		if (globalConfig.getProxyInfo() != null) {
			final URL hubUrl = globalConfig.getHubUrl();
			if (globalConfig.getProxyInfo().shouldUseProxyForUrl(hubUrl)) {
				addProxySettingsToScanner(logger, scan, globalConfig.getProxyInfo());
			}
		}

		scan.setScanMemory(jobConfig.getScanMemory());
		scan.setWorkingDirectory(jobConfig.getWorkingDirectory());
		scan.setVerboseRun(isVerbose());
		if (null != jobConfig.getProjectName() && null != jobConfig.getVersion()) {
			scan.setProject(jobConfig.getProjectName());
			scan.setVersion(jobConfig.getVersion());
		}

		if (javaExec == null) {
			String javaHome = getEnvironmentVariable("JAVA_HOME");
			if (StringUtils.isBlank(javaHome)) {
				// We couldn't get the JAVA_HOME variable so lets try to get the
				// home
				// of the java that is running this process
				javaHome = System.getProperty("java.home");
			}
			javaExec = new File(javaHome);
			if (StringUtils.isBlank(javaHome) || javaExec == null || !javaExec.exists()) {
				throw new HubIntegrationException(
						"The JAVA_HOME could not be determined, the Hub CLI can not be executed.");
			}
			javaExec = new File(javaExec, "bin");
			if (SystemUtils.IS_OS_WINDOWS) {
				javaExec = new File(javaExec, "java.exe");
			} else {
				javaExec = new File(javaExec, "java");
			}
		}

		final Result scanResult = scan.setupAndRunScan(scanExec.getAbsolutePath(), oneJarFile.getAbsolutePath(),
				javaExec.getAbsolutePath());
		if (scanResult != Result.SUCCESS) {
			result = BuildFinishedStatus.FINISHED_FAILED;
		}

		return scan;
	}

	public void addProxySettingsToScanner(final IntLogger logger, final TeamCityScanExecutor scan,
			final HubProxyInfo proxyInfo) throws HubIntegrationException, URISyntaxException, MalformedURLException,
			IllegalArgumentException, EncryptionException {
		if (proxyInfo != null) {
			if (StringUtils.isNotBlank(proxyInfo.getHost()) && proxyInfo.getPort() != 0) {
				if (StringUtils.isNotBlank(proxyInfo.getUsername())
						&& StringUtils.isNotBlank(proxyInfo.getMaskedPassword())) {
					scan.setProxyHost(proxyInfo.getHost());
					scan.setProxyPort(proxyInfo.getPort());
					scan.setProxyUsername(proxyInfo.getUsername());
					scan.setProxyPassword(proxyInfo.getDecryptedPassword());
				} else {
					scan.setProxyHost(proxyInfo.getHost());
					scan.setProxyPort(proxyInfo.getPort());
				}
				if (logger != null) {
					logger.debug("Using proxy: '" + proxyInfo.getHost() + "' at Port: '" + proxyInfo.getPort() + "'");
				}
			}
		}
	}

	private void checkPolicyFailures(final AgentRunningBuild build, final IntLogger logger,
			final HubSupportHelper hubSupport, final HubIntRestService restService,
			final HubReportGenerationInfo bomUpdateInfo, final ReleaseItem version, final boolean waitForBom) {
		// Check if User specified our Failure Condition on policy
		final Collection<AgentBuildFeature> features = build.getBuildFeaturesOfType(HubBundle.POLICY_FAILURE_CONDITION);
		// The feature is only allowed to have a single instance in the
		// configuration therefore we just want to make
		// sure the feature collection has something meaning that it was
		// configured.
		if (features != null && features.iterator() != null && !features.isEmpty()
				&& features.iterator().next() != null) {
			if (hubSupport.isPolicyApiSupport() == false) {
				final String message = "This version of the Hub does not have support for Policies.";
				build.stopBuild(message);
			} else {
				if (version == null) {
					logger.error("Can not check policy violations if you have not specified a Project and Version.");
					return;
				}
				final String policyStatusLink = version.getLink(ReleaseItem.POLICY_STATUS_LINK);
				try {
					if (waitForBom) {
						logger.debug("Waiting for the bom to be updated with the scan results.");
						waitForBomToBeUpdated(logger, restService, hubSupport, bomUpdateInfo);
					}
					// We use this conditional in case there are other failure
					// conditions in the future
					final PolicyStatus policyStatus = restService.getPolicyStatus(policyStatusLink);
					if (policyStatus == null) {
						build.stopBuild("Could not find any information about the Policy status of the bom.");
						return;
					}
					boolean policyViolationFound = false;
					if (policyStatus.getOverallStatusEnum() == PolicyStatusEnum.IN_VIOLATION) {
						policyViolationFound = true;
					}

					if (policyStatus.getCountInViolation() == null) {
						logger.error("Could not find the number of bom entries In Violation of a Policy.");
					} else {
						logger.info("Found " + policyStatus.getCountInViolation().getValue()
								+ " bom entries to be In Violation of a defined Policy.");
					}
					if (policyStatus.getCountInViolationOverridden() == null) {
						logger.error("Could not find the number of bom entries In Violation Overridden of a Policy.");
					} else {
						logger.info("Found " + policyStatus.getCountInViolationOverridden().getValue()
								+ " bom entries to be In Violation of a defined Policy, but they have been overridden.");
					}
					if (policyStatus.getCountNotInViolation() == null) {
						logger.error("Could not find the number of bom entries Not In Violation of a Policy.");
					} else {
						logger.info("Found " + policyStatus.getCountNotInViolation().getValue()
								+ " bom entries to be Not In Violation of a defined Policy.");
					}

					if (policyViolationFound) {
						build.stopBuild("Policy Violations found");
					}
				} catch (final MissingPolicyStatusException e) {
					logger.warn(e.getMessage());
				} catch (final IOException e) {
					logger.error(e.getMessage(), e);
					build.stopBuild(e.getMessage());
				} catch (final BDRestException e) {
					logger.error(e.getMessage(), e);
					build.stopBuild(e.getMessage());
				} catch (final URISyntaxException e) {
					logger.error(e.getMessage(), e);
					build.stopBuild(e.getMessage());
				} catch (final InterruptedException e) {
					logger.error(e.getMessage(), e);
					build.stopBuild(e.getMessage());
				} catch (final HubIntegrationException e) {
					logger.error(e.getMessage(), e);
					build.stopBuild(e.getMessage());
				}
			}
		}
	}

	public void waitForBomToBeUpdated(final IntLogger logger, final HubIntRestService service,
			final HubSupportHelper supportHelper, final HubReportGenerationInfo bomUpdateInfo)
			throws InterruptedException, BDRestException, HubIntegrationException, URISyntaxException, IOException {
		final HubEventPolling hubEventPolling = new HubEventPolling(service);
		if (supportHelper.isCliStatusDirOptionSupport()) {
			hubEventPolling.assertBomUpToDate(bomUpdateInfo, logger);
		} else {
			hubEventPolling.assertBomUpToDate(bomUpdateInfo);
		}
	}

}
