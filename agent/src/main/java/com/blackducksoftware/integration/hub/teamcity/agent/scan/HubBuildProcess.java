package com.blackducksoftware.integration.hub.teamcity.agent.scan;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.NotNull;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.HubScanJobConfig;
import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.ScanExecutor.Result;
import com.blackducksoftware.integration.hub.cli.CLIInstaller;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.report.api.BomReportGenerator;
import com.blackducksoftware.integration.hub.report.api.HubBomReportData;
import com.blackducksoftware.integration.hub.report.api.HubReportGenerationInfo;
import com.blackducksoftware.integration.hub.response.DistributionEnum;
import com.blackducksoftware.integration.hub.response.PhaseEnum;
import com.blackducksoftware.integration.hub.response.ReleaseItem;
import com.blackducksoftware.integration.hub.response.VersionComparison;
import com.blackducksoftware.integration.hub.teamcity.agent.HubAgentBuildLogger;
import com.blackducksoftware.integration.hub.teamcity.agent.HubParameterValidator;
import com.blackducksoftware.integration.hub.teamcity.agent.exceptions.TeamCityHubPluginException;
import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubCredentialsBean;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubProxyInfo;
import com.blackducksoftware.integration.hub.teamcity.common.beans.ServerHubConfigBean;
import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;
import com.blackducksoftware.integration.suite.sdk.logging.LogLevel;
import com.blackducksoftware.integration.util.HostnameHelper;

public class HubBuildProcess extends HubCallableBuildProcess {
    @NotNull
    private final AgentRunningBuild build;

    @NotNull
    private final BuildRunnerContext context;

    private HubAgentBuildLogger logger;

    private BuildFinishedStatus result;

    private Boolean verbose;

    public HubBuildProcess(@NotNull final AgentRunningBuild build, @NotNull final BuildRunnerContext context) {
        this.build = build;
        this.context = context;
    }

    public void setverbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isVerbose() {
        if (verbose == null) {
            verbose = true;
        }
        return verbose;
    }

    public void setHubLogger(HubAgentBuildLogger logger) {
        this.logger = logger;
    }

    @Override
    public BuildFinishedStatus call() throws IOException {
        final BuildProgressLogger buildLogger = build.getBuildLogger();
        HubAgentBuildLogger hubLogger = new HubAgentBuildLogger(buildLogger);
        hubLogger.setLogLevel(LogLevel.DEBUG);
        setHubLogger(hubLogger);

        if (StringUtils.isBlank(System.getProperty("http.maxRedirects"))) {
            // If this property is not set the default is 20
            // When not set the Authenticator redirects in a loop and results in an error for too many redirects
            System.setProperty("http.maxRedirects", "3");
        }

        result = BuildFinishedStatus.FINISHED_SUCCESS;

        logger.targetStarted("Hub Build Step");

        ServerHubConfigBean globalConfig = new ServerHubConfigBean();

        String serverUrl = getParameter(HubConstantValues.HUB_URL);
        globalConfig.setHubUrl(serverUrl);

        HubCredentialsBean credential = new HubCredentialsBean(getParameter(HubConstantValues.HUB_USERNAME), getParameter(HubConstantValues.HUB_PASSWORD));
        globalConfig.setGlobalCredentials(credential);

        HubProxyInfo proxyInfo = new HubProxyInfo();
        proxyInfo.setHost(getParameter(HubConstantValues.HUB_PROXY_HOST));
        if (getParameter(HubConstantValues.HUB_PROXY_PORT) != null) {
            proxyInfo.setPort(Integer.valueOf(getParameter(HubConstantValues.HUB_PROXY_PORT)));
        }
        proxyInfo.setIgnoredProxyHosts(getParameter(HubConstantValues.HUB_NO_PROXY_HOSTS));
        proxyInfo.setProxyUsername(getParameter(HubConstantValues.HUB_PROXY_USER));
        proxyInfo.setProxyPassword(getParameter(HubConstantValues.HUB_PROXY_PASS));

        globalConfig.setProxyInfo(proxyInfo);

        String projectName = getParameter(HubConstantValues.HUB_PROJECT_NAME);
        String version = getParameter(HubConstantValues.HUB_PROJECT_VERSION);

        String phase = getParameter(HubConstantValues.HUB_VERSION_PHASE);
        String distribution = getParameter(HubConstantValues.HUB_VERSION_DISTRIBUTION);

        String scanMemory = getParameter(HubConstantValues.HUB_SCAN_MEMORY);

        File workingDirectory = context.getWorkingDirectory();
        String workingDirectoryPath = workingDirectory.getCanonicalPath();

        List<String> scanTargetPaths = new ArrayList<String>();
        String scanTargetParameter = getParameter(HubConstantValues.HUB_SCAN_TARGETS);
        if (StringUtils.isNotBlank(scanTargetParameter)) {
            String[] scanTargetPathsArray = scanTargetParameter.split("\\r?\\n");
            for (String target : scanTargetPathsArray) {
                if (!StringUtils.isBlank(target)) {
                    scanTargetPaths.add(new File(workingDirectory, target).getAbsolutePath());
                }
            }
        } else {
            scanTargetPaths.add(workingDirectory.getAbsolutePath());
        }

        String shouldGenerateRiskReport = getParameter(HubConstantValues.HUB_GENERATE_RISK_REPORT);
        String maxWaitTimeForRiskReport = getParameter(HubConstantValues.HUB_MAX_WAIT_TIME_FOR_RISK_REPORT);

        HubScanJobConfig jobConfig = new HubScanJobConfig(projectName, version, phase, distribution, workingDirectoryPath, scanMemory,
                shouldGenerateRiskReport, maxWaitTimeForRiskReport);
        jobConfig.addAllScanTargetPaths(scanTargetPaths);

        String localHostName = HostnameHelper.getMyHostname();
        logger.info("Running on machine : " + localHostName);

        printGlobalConfguration(globalConfig);
        printJobConfguration(jobConfig);
        URL hubUrl = new URL(globalConfig.getHubUrl());
        try {
            if (isGlobalConfigValid(globalConfig) && isJobConfigValid(jobConfig)) {
                HubIntRestService restService = new HubIntRestService(serverUrl);
                restService.setLogger(logger);
                if (proxyInfo != null) {
                    if (!HubProxyInfo.checkMatchingNoProxyHostPatterns(hubUrl.getHost(), proxyInfo.getNoProxyHostPatterns())) {
                        Integer port = (proxyInfo.getPort() == null) ? 0 : proxyInfo.getPort();

                        restService.setProxyProperties(proxyInfo.getHost(), port,
                                proxyInfo.getNoProxyHostPatterns(), proxyInfo.getProxyUsername(), proxyInfo.getProxyPassword());
                    }
                }
                restService.setCookies(credential.getHubUser(), credential.getDecryptedPassword());

                File hubToolDir = new File(build.getAgentConfiguration().getAgentToolsDirectory(), "HubCLI");
                CLIInstaller installer = new CLIInstaller(hubToolDir);
                if (!HubProxyInfo.checkMatchingNoProxyHostPatterns(hubUrl.getHost(), proxyInfo.getNoProxyHostPatterns())) {
                    Integer port = (proxyInfo.getPort() == null) ? 0 : proxyInfo.getPort();
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
                File oneJarFile = installer.getOneJarFile();

                File javaExec = installer.getProvidedJavaExec();

                String projectId = null;
                String versionId = null;

                // TODO this code is a remnant of an old Hub version, the CLI will create the project and version for us
                if (StringUtils.isNotBlank(jobConfig.getProjectName()) && StringUtils.isNotBlank(jobConfig.getVersion())) {
                    projectId = ensureProjectExists(restService, logger, jobConfig.getProjectName(), jobConfig.getVersion(), jobConfig.getPhase(),
                            jobConfig.getDistribution());
                    versionId = ensureVersionExists(restService, logger, jobConfig.getVersion(), projectId, jobConfig.getPhase(),
                            jobConfig.getDistribution());
                }

                boolean mappingDone = doHubScan(restService, hubLogger, oneJarFile, hubCLI, javaExec, globalConfig, jobConfig);

                // Only map the scans to a Project Version if the Project name and Project Version have been
                // configured
                if (!mappingDone && result.equals(BuildFinishedStatus.FINISHED_SUCCESS) && StringUtils.isNotBlank(jobConfig.getProjectName())
                        && StringUtils.isNotBlank(jobConfig.getVersion())) {
                    // Wait 5 seconds for the scans to be recognized in the Hub server
                    logger.info("Waiting a few seconds for the scans to be recognized by the Hub server.");
                    Thread.sleep(5000);

                    doHubScanMapping(restService, logger, jobConfig, localHostName, versionId);
                }

                if (BuildFinishedStatus.FINISHED_SUCCESS == result && jobConfig.isShouldGenerateRiskReport()) {
                    // TODO
                    // if Hub older than 3.0.0, we should get the project and version Id ourselves. If either doesnt
                    // exist, throw exception

                    // if Hub 3.0.0 or newer, can we use the status files to get the code locations and get the version
                    // the CL is mapped to and the project this version belongs to

                    HubReportGenerationInfo hubReportGenerationInfo = new HubReportGenerationInfo();
                    hubReportGenerationInfo.setService(restService);
                    hubReportGenerationInfo.setHostname(localHostName);
                    hubReportGenerationInfo.setProjectId(projectId);
                    hubReportGenerationInfo.setVersionId(versionId);
                    hubReportGenerationInfo.setScanTargets(jobConfig.getScanTargetPaths());
                    hubReportGenerationInfo.setMaximumWaitTime(jobConfig.getMaxWaitTimeForRiskReportInMilliseconds());

                    HubSupportHelper hubSupport = new HubSupportHelper();
                    hubSupport.checkHubSupport(restService, hubLogger);

                    BomReportGenerator bomReportGenerator = new BomReportGenerator(hubReportGenerationInfo, hubSupport);
                    HubBomReportData hubBomReportData = bomReportGenerator.generateHubReport(logger);
                }
            } else {
                logger.info("Skipping Hub Build Step");
                result = BuildFinishedStatus.FINISHED_FAILED;
            }
        } catch (Exception e) {
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
        String result = value.trim();
        return result;
    }

    private String getEnvironmentVariable(@NotNull final String parameterName) {
        final String value = context.getBuildParameters().getEnvironmentVariables().get(parameterName);
        if (value == null || value.trim().length() == 0) {
            return null;
        }
        String result = value.trim();
        return result;

    }

    public boolean isGlobalConfigValid(final ServerHubConfigBean globalConfig) throws IOException {

        HubParameterValidator validator = new HubParameterValidator(logger);

        boolean isUrlValid = validator.isServerUrlValid(globalConfig.getHubUrl());
        boolean credentialsConfigured = validator.isHubCredentialConfigured(globalConfig.getGlobalCredentials());

        return isUrlValid && credentialsConfigured;
    }

    public boolean isJobConfigValid(final HubScanJobConfig jobConfig)
            throws IOException {
        if (jobConfig == null) {
            return false;
        }

        HubParameterValidator validator = new HubParameterValidator(logger);

        boolean projectConfig = true;

        projectConfig = validator.validateProjectNameAndVersion(jobConfig.getProjectName(), jobConfig.getVersion());

        boolean scanTargetsValid = true;

        if (jobConfig.getScanTargetPaths().isEmpty()) {
            logger.error("No scan targets configured.");
            scanTargetsValid = false;
        } else {
            for (String absolutePath : jobConfig.getScanTargetPaths()) {
                if (!validator.validateTargetPath(absolutePath, jobConfig.getWorkingDirectory())) {
                    scanTargetsValid = false;
                }
            }
        }

        return projectConfig && scanTargetsValid;
    }

    public void printGlobalConfguration(final ServerHubConfigBean globalConfig) {
        if (globalConfig == null) {
            return;
        }

        logger.info("--> Hub Server Url : " + globalConfig.getHubUrl());
        if (globalConfig.getGlobalCredentials() != null && StringUtils.isNotBlank(globalConfig.getGlobalCredentials().getHubUser())) {
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

    public void printJobConfguration(final HubScanJobConfig jobConfig) {
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
            for (String absolutePath : jobConfig.getScanTargetPaths()) {
                logger.info("    --> " + absolutePath);
            }
        }
    }

    private String ensureProjectExists(HubIntRestService service, IntLogger logger, String projectName,
            String projectVersion, String phaseDisplayValue, String distributionDisplayValue) throws IOException, URISyntaxException,
            TeamCityHubPluginException {

        String projectId = null;
        try {
            projectId = service.getProjectByName(projectName).getId();

        } catch (ProjectDoesNotExistException e) {
            // Project was not found, try to create it

            PhaseEnum phase = PhaseEnum.getPhaseByDisplayValue(phaseDisplayValue);
            DistributionEnum distribution = DistributionEnum.getDistributionByDisplayValue(distributionDisplayValue);

            try {
                logger.info("Creating project : " + projectName + " and version : " + projectVersion);
                projectId = service.createHubProjectAndVersion(projectName, projectVersion, phase.name(), distribution.name());
                logger.debug("Project and Version created!");

            } catch (BDRestException e1) {
                if (e1.getResource() != null) {
                    logger.error("Status : " + e1.getResource().getStatus().getCode());
                    logger.error("Response : " + e1.getResource().getResponse().getEntityAsText());
                }
                throw new TeamCityHubPluginException("Problem creating the Project. ", e1);
            }
        } catch (BDRestException e) {
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

    private String ensureVersionExists(HubIntRestService service, IntLogger logger, String projectVersion,
            String projectId, String phaseDisplayValue, String distributionDisplayValue) throws IOException, URISyntaxException, TeamCityHubPluginException {
        String versionId = null;
        try {

            PhaseEnum phase = PhaseEnum.getPhaseByDisplayValue(phaseDisplayValue);
            DistributionEnum distribution = DistributionEnum.getDistributionByDisplayValue(distributionDisplayValue);

            List<ReleaseItem> projectVersions = service.getVersionsForProject(projectId);
            for (ReleaseItem release : projectVersions) {
                if (projectVersion.equals(release.getVersion())) {
                    versionId = release.getId();
                    logger.info("Found version : " + projectVersion);
                    if (!release.getPhase().equals(phase.name())) {
                        logger.warn("The selected Phase does not match the Phase of this Version. If you wish to update the Phase please do so in the Hub UI.");
                    }
                    if (!release.getDistribution().equals(distribution.name())) {
                        logger.warn("The selected Distribution does not match the Distribution of this Version. If you wish to update the Distribution please do so in the Hub UI.");
                    }
                }
            }
            if (versionId == null) {
                logger.info("Creating version : " + projectVersion);
                versionId = service.createHubVersion(projectVersion, projectId, phase.name(), distribution.name());
                logger.debug("Version created!");
            }
        } catch (BDRestException e) {
            throw new TeamCityHubPluginException("Could not retrieve or create the specified version.", e);
        }
        return versionId;
    }

    public Boolean doHubScan(HubIntRestService service, HubAgentBuildLogger logger,
            File oneJarFile, File scanExec, File javaExec, ServerHubConfigBean globalConfig, HubScanJobConfig jobConfig) throws HubIntegrationException,
            IOException,
            URISyntaxException,
            NumberFormatException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        VersionComparison logOptionComparison = null;
        VersionComparison mappingComparison = null;
        Boolean mappingDone = false;
        try {
            // The logDir option wasnt added until Hub version 2.0.1
            logOptionComparison = service.compareWithHubVersion("2.0.1");

            mappingComparison = service.compareWithHubVersion("2.2.0");
        } catch (BDRestException e) {
            ResourceException resEx = null;
            if (e.getCause() != null && e.getCause() instanceof ResourceException) {
                resEx = (ResourceException) e.getCause();
            }
            if (resEx != null && resEx.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND)) {
                // The Hub server is version 2.0.0 and the version endpoint does not exist
            } else if (resEx != null) {
                logger.error(resEx.getMessage());
            } else {
                logger.error(e.getMessage());
            }
        }

        TeamCityScanExecutor scan = new TeamCityScanExecutor(globalConfig.getHubUrl(), globalConfig.getGlobalCredentials().getHubUser(),
                globalConfig.getGlobalCredentials().getDecryptedPassword(), jobConfig.getScanTargetPaths(), Integer.valueOf(context.getBuild()
                        .getBuildNumber()));
        scan.setLogger(logger);

        if (globalConfig.getProxyInfo() != null) {
            URL hubUrl = new URL(globalConfig.getHubUrl());
            if (!HubProxyInfo.checkMatchingNoProxyHostPatterns(hubUrl.getHost(), globalConfig.getProxyInfo().getNoProxyHostPatterns())) {
                addProxySettingsToScanner(logger, scan, globalConfig.getProxyInfo());
            }
        }

        if (logOptionComparison != null && logOptionComparison.getNumericResult() < 0) {
            // The logDir option wasnt added until Hub version 2.0.1
            // So if the result is that 2.0.1 is less than the actual version, we know that it supports the log option
            scan.setHubSupportLogOption(true);
        } else {
            scan.setHubSupportLogOption(false);
        }
        scan.setScanMemory(jobConfig.getScanMemory());
        scan.setWorkingDirectory(jobConfig.getWorkingDirectory());
        scan.setVerboseRun(isVerbose());
        if (mappingComparison != null && mappingComparison.getNumericResult() <= 0 &&
                StringUtils.isNotBlank(jobConfig.getProjectName())
                && StringUtils.isNotBlank(jobConfig.getVersion())) {
            // FIXME Which version was this fixed in?

            // The project and release options werent working until Hub version 2.2.?
            // So if the result is that 2.2.0 is less than or equal to the actual version, we know that it supports
            // these options
            scan.setCliSupportsMapping(true);
            scan.setProject(jobConfig.getProjectName());
            scan.setVersion(jobConfig.getVersion());
            mappingDone = true; // Mapping will be done by the CLI during the scan
        } else {
            scan.setCliSupportsMapping(false);
        }

        if (javaExec == null) {
            String javaHome = getEnvironmentVariable("JAVA_HOME");
            if (StringUtils.isBlank(javaHome)) {
                // We couldn't get the JAVA_HOME variable so lets try to get the home
                // of the java that is running this process
                javaHome = System.getProperty("java.home");
            }
            javaExec = new File(javaHome);
            if (StringUtils.isBlank(javaHome) || javaExec == null || !javaExec.exists()) {
                throw new HubIntegrationException("The JAVA_HOME could not be determined, the Hub CLI can not be executed.");
            }
            javaExec = new File(javaExec, "bin");
            if (SystemUtils.IS_OS_WINDOWS) {
                javaExec = new File(javaExec, "java.exe");
            } else {
                javaExec = new File(javaExec, "java");
            }
        }

        Result scanResult = scan.setupAndRunScan(scanExec.getAbsolutePath(), oneJarFile.getAbsolutePath(), javaExec.getAbsolutePath());
        if (scanResult != Result.SUCCESS) {
            result = BuildFinishedStatus.FINISHED_FAILED;
        }

        return mappingDone;

    }

    public void doHubScanMapping(HubIntRestService service, IntLogger logger, HubScanJobConfig jobConfig, String localHostName, String versionId)
            throws UnknownHostException,
            InterruptedException, BDRestException, HubIntegrationException, URISyntaxException {
        Map<String, Boolean> scanLocationIds = service.getScanLocationIds(localHostName, jobConfig.getScanTargetPaths(), versionId);
        if (scanLocationIds != null && !scanLocationIds.isEmpty()) {
            logger.debug("These scan Id's were found for the scan targets.");
            for (Entry<String, Boolean> scanId : scanLocationIds.entrySet()) {
                logger.debug(scanId.getKey());
            }

            service.mapScansToProjectVersion(scanLocationIds, versionId);
        } else {
            logger.debug("There was an issue getting the Scan Location Id's for the defined scan targets.");
        }

    }

    public void addProxySettingsToScanner(IntLogger logger, TeamCityScanExecutor scan, HubProxyInfo proxyInfo) throws HubIntegrationException,
            URISyntaxException,
            MalformedURLException {
        if (proxyInfo != null) {

            if (StringUtils.isNotBlank(proxyInfo.getHost()) && proxyInfo.getPort() != 0) {
                if (StringUtils.isNotBlank(proxyInfo.getProxyUsername()) && StringUtils.isNotBlank(proxyInfo.getProxyPassword())) {
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
}
