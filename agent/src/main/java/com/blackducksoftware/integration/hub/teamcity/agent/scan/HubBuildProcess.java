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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.ScanExecutor;
import com.blackducksoftware.integration.hub.ScanExecutor.Result;
import com.blackducksoftware.integration.hub.cli.CLIInstaller;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.MissingPolicyStatusException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.job.HubScanJobConfig;
import com.blackducksoftware.integration.hub.job.HubScanJobConfigBuilder;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;
import com.blackducksoftware.integration.hub.policy.api.PolicyStatus;
import com.blackducksoftware.integration.hub.policy.api.PolicyStatusEnum;
import com.blackducksoftware.integration.hub.report.api.HubReportGenerationInfo;
import com.blackducksoftware.integration.hub.report.api.HubRiskReportData;
import com.blackducksoftware.integration.hub.report.api.RiskReportGenerator;
import com.blackducksoftware.integration.hub.teamcity.agent.HubAgentBuildLogger;
import com.blackducksoftware.integration.hub.teamcity.agent.HubParameterValidator;
import com.blackducksoftware.integration.hub.teamcity.agent.exceptions.TeamCityHubPluginException;
import com.blackducksoftware.integration.hub.teamcity.common.HubBundle;
import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubCredentialsBean;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubProxyInfo;
import com.blackducksoftware.integration.hub.teamcity.common.beans.ServerHubConfigBean;
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
		hubLogger.setLogLevel(LogLevel.DEBUG);
		setHubLogger(hubLogger);

		if (StringUtils.isBlank(System.getProperty("http.maxRedirects"))) {
			// If this property is not set the default is 20
			// When not set the Authenticator redirects in a loop and results in
			// an error for too many redirects
			System.setProperty("http.maxRedirects", "3");
		}

		result = BuildFinishedStatus.FINISHED_SUCCESS;

		logger.targetStarted("Hub Build Step");

		final ServerHubConfigBean globalConfig = new ServerHubConfigBean();

		final String serverUrl = getParameter(HubConstantValues.HUB_URL);
		globalConfig.setHubUrl(serverUrl);

		final HubCredentialsBean credential = new HubCredentialsBean(getParameter(HubConstantValues.HUB_USERNAME),
				getParameter(HubConstantValues.HUB_PASSWORD));
		globalConfig.setGlobalCredentials(credential);

		final HubProxyInfo proxyInfo = new HubProxyInfo();
		proxyInfo.setHost(getParameter(HubConstantValues.HUB_PROXY_HOST));
		if (getParameter(HubConstantValues.HUB_PROXY_PORT) != null) {
			proxyInfo.setPort(Integer.valueOf(getParameter(HubConstantValues.HUB_PROXY_PORT)));
		}
		proxyInfo.setIgnoredProxyHosts(getParameter(HubConstantValues.HUB_NO_PROXY_HOSTS));
		proxyInfo.setProxyUsername(getParameter(HubConstantValues.HUB_PROXY_USER));
		proxyInfo.setProxyPassword(getParameter(HubConstantValues.HUB_PROXY_PASS));

		globalConfig.setProxyInfo(proxyInfo);
		printGlobalConfiguration(globalConfig);

		final String projectName = getParameter(HubConstantValues.HUB_PROJECT_NAME);
		final String version = getParameter(HubConstantValues.HUB_PROJECT_VERSION);
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
			final HubScanJobConfigBuilder hubScanJobConfigBuilder = new HubScanJobConfigBuilder();
			hubScanJobConfigBuilder.setProjectName(projectName);
			hubScanJobConfigBuilder.setVersion(version);
			hubScanJobConfigBuilder.setPhase(phase);
			hubScanJobConfigBuilder.setDistribution(distribution);
			hubScanJobConfigBuilder.setWorkingDirectory(workingDirectoryPath);
			hubScanJobConfigBuilder.setShouldGenerateRiskReport(shouldGenerateRiskReport);
			hubScanJobConfigBuilder.setMaxWaitTimeForBomUpdate(maxWaitTimeForRiskReport);
			hubScanJobConfigBuilder.setScanMemory(scanMemory);
			hubScanJobConfigBuilder.addAllScanTargetPaths(scanTargetPaths);

			final HubScanJobConfig jobConfig = hubScanJobConfigBuilder.build(logger);

			printJobConfiguration(jobConfig);
			final URL hubUrl = new URL(globalConfig.getHubUrl());

			if (isGlobalConfigValid(globalConfig)) {
				final HubIntRestService restService = new HubIntRestService(serverUrl);
				restService.setLogger(logger);
				if (proxyInfo != null) {
					if (!HubProxyInfo.checkMatchingNoProxyHostPatterns(hubUrl.getHost(),
							proxyInfo.getNoProxyHostPatterns())) {
						final Integer port = (proxyInfo.getPort() == null) ? 0 : proxyInfo.getPort();

						restService.setProxyProperties(proxyInfo.getHost(), port, proxyInfo.getNoProxyHostPatterns(),
								proxyInfo.getProxyUsername(), proxyInfo.getProxyPassword());
					}
				}
				restService.setCookies(credential.getHubUser(), credential.getDecryptedPassword());

				final File hubToolDir = new File(build.getAgentConfiguration().getAgentToolsDirectory(), "HubCLI");
				final CLIInstaller installer = new CLIInstaller(hubToolDir);
				if (!HubProxyInfo.checkMatchingNoProxyHostPatterns(hubUrl.getHost(),
						proxyInfo.getNoProxyHostPatterns())) {
					final Integer port = (proxyInfo.getPort() == null) ? 0 : proxyInfo.getPort();
					installer.setProxyHost(proxyInfo.getHost());
					installer.setProxyPort(port);
					installer.setProxyUserName(proxyInfo.getProxyUsername());
					installer.setProxyPassword(proxyInfo.getProxyPassword());
				}
				installer.performInstallation(logger, restService, localHostName);

				File hubCLI = null;
				if (installer.getCLIExists(hubLogger)) {
					hubCLI = installer.getCLI();
				} else {
					hubLogger.error("Could not find the Hub scan CLI.");
					result = BuildFinishedStatus.FINISHED_FAILED;
					return result;
				}
				final File oneJarFile = installer.getOneJarFile();

				final File javaExec = installer.getProvidedJavaExec();

				String projectId = null;
				String versionId = null;

				// TODO this code is a remnant of an old Hub version, the CLI
				// will create the project and version for us
				if (null != jobConfig.getProjectName() && null != jobConfig.getVersion()) {
					projectId = ensureProjectExists(restService, logger, jobConfig);
					versionId = ensureVersionExists(restService, logger, jobConfig, projectId);
				}

				final HubSupportHelper hubSupport = new HubSupportHelper();
				hubSupport.checkHubSupport(restService, hubLogger);

				final ScanExecutor scanExecutor = doHubScan(restService, hubLogger, oneJarFile, hubCLI, javaExec,
						globalConfig, jobConfig, hubSupport);

				if (BuildFinishedStatus.FINISHED_SUCCESS == result && jobConfig.isShouldGenerateRiskReport()) {
					// TODO
					// if Hub older than 3.0.0, we should get the project and
					// version Id ourselves. If either doesnt
					// exist, throw exception (could be related to the CLI issue
					// HUB-6348

					// if Hub 3.0.0 or newer, can we use the status files to get
					// the code locations and get the version
					// the CL is mapped to and the project this version belongs
					// to

					final HubReportGenerationInfo hubReportGenerationInfo = new HubReportGenerationInfo();
					hubReportGenerationInfo.setService(restService);
					hubReportGenerationInfo.setHostname(localHostName);
					hubReportGenerationInfo.setProjectId(projectId);
					hubReportGenerationInfo.setVersionId(versionId);
					hubReportGenerationInfo.setScanTargets(jobConfig.getScanTargetPaths());
					hubReportGenerationInfo.setMaximumWaitTime(jobConfig.getMaxWaitTimeForRiskReportInMilliseconds());
					hubReportGenerationInfo.setScanStatusDirectory(scanExecutor.getScanStatusDirectoryPath());

					final RiskReportGenerator riskReportGenerator = new RiskReportGenerator(hubReportGenerationInfo,
							hubSupport);
					final HubRiskReportData hubRiskReportData = riskReportGenerator.generateHubReport(logger);

					final String reportPath = workingDirectoryPath + File.separator
							+ HubConstantValues.HUB_RISK_REPORT_FILENAME;

					final Gson gson = new Gson();
					final String contents = gson.toJson(hubRiskReportData);

					final FileWriter writer = new FileWriter(reportPath);
					writer.write(contents);
					writer.close();

					artifactsWatcher.addNewArtifactsPath(reportPath);
				}

				checkPolicyFailures(build, hubLogger, hubSupport, restService, projectId, versionId);

			} else {
				logger.info("Skipping Hub Build Step");
				result = BuildFinishedStatus.FINISHED_FAILED;
			}
		} catch (final Exception e) {
			logger.error(e);
			result = BuildFinishedStatus.FINISHED_FAILED;

		} finally {

		}

		logger.targetFinished("Hub Build Step");
		return result;
	}

	private String getParameter(@NotNull final String parameterName) {
		final String value = context.getRunnerParameters().get(parameterName);
		if (value == null || value.trim().length() == 0) {
			return null;
		}
		final String result = value.trim();
		return result;
	}

	private String getEnvironmentVariable(@NotNull final String parameterName) {
		final String value = context.getBuildParameters().getEnvironmentVariables().get(parameterName);
		if (value == null || value.trim().length() == 0) {
			return null;
		}
		final String result = value.trim();
		return result;

	}

	public boolean isGlobalConfigValid(final ServerHubConfigBean globalConfig) throws IOException {
		final HubParameterValidator validator = new HubParameterValidator(logger);

		final boolean isUrlValid = validator.isServerUrlValid(globalConfig.getHubUrl());
		final boolean credentialsConfigured = validator.isHubCredentialConfigured(globalConfig.getGlobalCredentials());

		return isUrlValid && credentialsConfigured;
	}

	public void printGlobalConfiguration(final ServerHubConfigBean globalConfig) {
		if (globalConfig == null) {
			return;
		}

		logger.info("--> Hub Server Url : " + globalConfig.getHubUrl());
		if (globalConfig.getGlobalCredentials() != null
				&& StringUtils.isNotBlank(globalConfig.getGlobalCredentials().getHubUser())) {
			logger.info("--> Hub User : " + globalConfig.getGlobalCredentials().getHubUser());
		}

		if (globalConfig.getProxyInfo() != null) {
			if (StringUtils.isNotBlank(globalConfig.getProxyInfo().getHost())) {
				logger.info("--> Proxy Host : " + globalConfig.getProxyInfo().getHost());
			}
			if (globalConfig.getProxyInfo().getPort() != null) {
				logger.info("--> Proxy Port : " + globalConfig.getProxyInfo().getPort());
			}
			if (StringUtils.isNotBlank(globalConfig.getProxyInfo().getIgnoredProxyHosts())) {
				logger.info("--> No Proxy Hosts : " + globalConfig.getProxyInfo().getIgnoredProxyHosts());
			}
			if (StringUtils.isNotBlank(globalConfig.getProxyInfo().getProxyUsername())) {
				logger.info("--> Proxy Username : " + globalConfig.getProxyInfo().getProxyUsername());
			}
		}
	}

	public void printJobConfiguration(final HubScanJobConfig jobConfig) {
		if (jobConfig == null) {
			return;
		}
		logger.info("Working directory : " + jobConfig.getWorkingDirectory());

		logger.info("--> Project : " + jobConfig.getProjectName());
		logger.info("--> Version : " + jobConfig.getVersion());

		logger.info("--> Version Phase : " + jobConfig.getPhase());
		logger.info("--> Version Distribution : " + jobConfig.getDistribution());

		logger.info("--> Hub scan memory : " + jobConfig.getScanMemory() + " MB");

		if (jobConfig.getScanTargetPaths().size() > 0) {
			logger.info("--> Hub scan targets : ");
			for (final String absolutePath : jobConfig.getScanTargetPaths()) {
				logger.info("    --> " + absolutePath);
			}
		}
	}

	private String ensureProjectExists(final HubIntRestService service, final IntLogger logger,
			final HubScanJobConfig jobConfig) throws IOException, URISyntaxException, TeamCityHubPluginException {
		final String projectName = jobConfig.getProjectName();
		final String version = jobConfig.getVersion();
		final String phaseString = jobConfig.getPhase();
		final String distributionString = jobConfig.getDistribution();

		String projectId = null;
		try {
			projectId = service.getProjectByName(projectName).getId();
		} catch (final ProjectDoesNotExistException e) {
			// Project was not found, try to create it
			final PhaseEnum phase = PhaseEnum.getPhaseByDisplayValue(phaseString);
			final DistributionEnum distribution = DistributionEnum.getDistributionByDisplayValue(distributionString);

			try {
				logger.info("Creating project : " + projectName + " and version : " + version);
				projectId = service.createHubProjectAndVersion(projectName, version, phase.name(), distribution.name());
				logger.debug("Project and Version created!");
			} catch (final BDRestException e1) {
				if (e1.getResource() != null) {
					logger.error("Status : " + e1.getResource().getStatus().getCode());
					logger.error("Response : " + e1.getResource().getResponse().getEntityAsText());
				}
				throw new TeamCityHubPluginException("Problem creating the Project. ", e1);
			}
		} catch (final BDRestException e) {
			if (e.getResource() != null) {
				if (e.getResource() != null) {
					logger.error("Status : " + e.getResource().getStatus().getCode());
					logger.error("Response : " + e.getResource().getResponse().getEntityAsText());
				}
				throw new TeamCityHubPluginException("Problem getting the Project. ", e);
			}
		}

		return projectId;
	}

	private String ensureVersionExists(final HubIntRestService service, final IntLogger logger,
			final HubScanJobConfig jobConfig, final String projectId)
					throws IOException, URISyntaxException, TeamCityHubPluginException {
		String versionId = null;
		try {
			final String version = jobConfig.getVersion();
			final String phaseString = jobConfig.getPhase();
			final String distributionString = jobConfig.getDistribution();

			final PhaseEnum phase = PhaseEnum.getPhaseByDisplayValue(phaseString);
			final DistributionEnum distribution = DistributionEnum.getDistributionByDisplayValue(distributionString);

			final List<ReleaseItem> projectVersions = service.getVersionsForProject(projectId);
			for (final ReleaseItem release : projectVersions) {
				if (version.equals(release.getVersion())) {
					versionId = release.getId();
					logger.info("Found version : " + version);
					if (!release.getPhase().equals(phase.name())) {
						logger.warn(
								"The selected Phase does not match the Phase of this Version. If you wish to update the Phase please do so in the Hub UI.");
					}
					if (!release.getDistribution().equals(distribution.name())) {
						logger.warn(
								"The selected Distribution does not match the Distribution of this Version. If you wish to update the Distribution please do so in the Hub UI.");
					}
				}
			}
			if (versionId == null) {
				logger.info("Creating version : " + version);
				versionId = service.createHubVersion(version, projectId, phase.name(), distribution.name());
				logger.debug("Version created!");
			}
		} catch (final BDRestException e) {
			throw new TeamCityHubPluginException("Could not retrieve or create the specified version.", e);
		}

		return versionId;
	}

	public ScanExecutor doHubScan(final HubIntRestService service, final HubAgentBuildLogger logger,
			final File oneJarFile, final File scanExec, File javaExec, final ServerHubConfigBean globalConfig,
			final HubScanJobConfig jobConfig, final HubSupportHelper supportHelper)
					throws HubIntegrationException, IOException, URISyntaxException, NumberFormatException,
					NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		final TeamCityScanExecutor scan = new TeamCityScanExecutor(globalConfig.getHubUrl(),
				globalConfig.getGlobalCredentials().getHubUser(),
				globalConfig.getGlobalCredentials().getDecryptedPassword(), jobConfig.getScanTargetPaths(),
				Integer.valueOf(context.getBuild().getBuildNumber()), supportHelper);
		scan.setLogger(logger);

		if (globalConfig.getProxyInfo() != null) {
			final URL hubUrl = new URL(globalConfig.getHubUrl());
			if (!HubProxyInfo.checkMatchingNoProxyHostPatterns(hubUrl.getHost(),
					globalConfig.getProxyInfo().getNoProxyHostPatterns())) {
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
			final HubProxyInfo proxyInfo) throws HubIntegrationException, URISyntaxException, MalformedURLException {
		if (proxyInfo != null) {
			if (StringUtils.isNotBlank(proxyInfo.getHost()) && proxyInfo.getPort() != 0) {
				if (StringUtils.isNotBlank(proxyInfo.getProxyUsername())
						&& StringUtils.isNotBlank(proxyInfo.getProxyPassword())) {
					scan.setProxyHost(proxyInfo.getHost());
					scan.setProxyPort(proxyInfo.getPort());
					scan.setProxyUsername(proxyInfo.getProxyUsername());
					scan.setProxyPassword(proxyInfo.getProxyPassword());
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
			final HubSupportHelper hubSupport, final HubIntRestService restService, final String projectId,
			final String versionId) {
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
				try {
					// We use this conditional in case there are other failure
					// conditions in the future
					final PolicyStatus policyStatus = restService.getPolicyStatus(projectId, versionId);
					if (policyStatus == null) {
						final String message = "Could not find any information about the Policy status of the bom.";
						build.stopBuild(message);
					}
					if (policyStatus.getOverallStatusEnum() == PolicyStatusEnum.IN_VIOLATION) {
						build.stopBuild("There are Policy Violations");
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
				}
			}
		}
	}

}
