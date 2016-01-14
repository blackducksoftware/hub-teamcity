package com.blackducksoftware.integration.hub.teamcity.agent.scan;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
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
import org.jetbrains.annotations.NotNull;
import org.restlet.data.Status;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.response.ReleaseItem;
import com.blackducksoftware.integration.hub.response.VersionComparison;
import com.blackducksoftware.integration.hub.teamcity.agent.HubAgentBuildLogger;
import com.blackducksoftware.integration.hub.teamcity.agent.HubParameterValidator;
import com.blackducksoftware.integration.hub.teamcity.agent.exceptions.TeamCityHubPluginException;
import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubCredentialsBean;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubProxyInfo;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubScanJobConfig;
import com.blackducksoftware.integration.hub.teamcity.common.beans.ServerHubConfigBean;
import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;
import com.blackducksoftware.integration.suite.sdk.logging.LogLevel;

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

        HubScanJobConfig jobConfig = new HubScanJobConfig();

        jobConfig.setProjectName(getParameter(HubConstantValues.HUB_PROJECT_NAME));
        jobConfig.setVersion(getParameter(HubConstantValues.HUB_PROJECT_VERSION));

        jobConfig.setPhase(getParameter(HubConstantValues.HUB_VERSION_PHASE));
        jobConfig.setDistribution(getParameter(HubConstantValues.HUB_VERSION_DISTRIBUTION));
        String hubCliParameter = getParameter(HubConstantValues.HUB_CLI_PATH);

        jobConfig.setHubScanMemory(getParameter(HubConstantValues.HUB_SCAN_MEMORY));
        String scanTargetParameter = getParameter(HubConstantValues.HUB_SCAN_TARGETS);

        File workingDirectory = context.getWorkingDirectory();
        String workingDirectoryPath = workingDirectory.getCanonicalPath();
        jobConfig.setWorkingDirectory(workingDirectoryPath);

        File cliHome = null;
        if (StringUtils.isBlank(hubCliParameter)) {
            String cliHomePath = getEnvironmentVariable(HubConstantValues.HUB_CLI_ENV_VAR);
            if (StringUtils.isNotBlank(cliHomePath)) {
                cliHome = new File(cliHomePath);
            }
        } else {
            cliHome = new File(hubCliParameter);
        }

        jobConfig.setHubCLIPath(cliHome);

        List<File> scanTargets = new ArrayList<File>();

        if (StringUtils.isNotBlank(scanTargetParameter)) {
            if (scanTargetParameter.contains(System.getProperty("line.separator"))) {
                String[] scanTargetPaths = scanTargetParameter.split(System.getProperty("line.separator"));
                for (String target : scanTargetPaths) {
                    scanTargets.add(new File(workingDirectory, target));
                }
            } else {
                scanTargets.add(new File(workingDirectory, scanTargetParameter));
            }
        } else {
            scanTargets.add(workingDirectory);
        }

        jobConfig.setHubScanTargets(scanTargets);

        String localHostName = InetAddress.getLocalHost().getHostName();
        logger.info("Running on machine : " + localHostName);

        printGlobalConfguration(globalConfig);
        printJobConfguration(jobConfig);
        try {
            if (isGlobalConfigValid(globalConfig) && isJobConfigValid(jobConfig)) {
                HubIntRestService restService = new HubIntRestService(serverUrl);
                restService.setLogger(logger);
                if (proxyInfo != null) {
                    URL hubUrl = new URL(globalConfig.getHubUrl());
                    if (!HubProxyInfo.checkMatchingNoProxyHostPatterns(hubUrl.getHost(), proxyInfo.getNoProxyHostPatterns())) {
                        Integer port = (proxyInfo.getPort() == null) ? 0 : proxyInfo.getPort();

                        restService.setProxyProperties(proxyInfo.getHost(), port,
                                proxyInfo.getNoProxyHostPatterns(), proxyInfo.getProxyUsername(), proxyInfo.getProxyPassword());
                    }
                }
                restService.setCookies(credential.getHubUser(), credential.getDecryptedPassword());

                String projectId = ensureProjectExists(restService, logger, jobConfig.getProjectName(), jobConfig.getVersion(), jobConfig.getPhase(),
                        jobConfig.getDistribution());
                String versionId = ensureVersionExists(restService, logger, jobConfig.getVersion(), projectId, jobConfig.getPhase(),
                        jobConfig.getDistribution());
                boolean mappingDone = doHubScan(restService, hubLogger, cliHome, globalConfig, jobConfig);

                // Only map the scans to a Project Version if the Project name and Project Version have been
                // configured
                if (!mappingDone && result.equals(BuildFinishedStatus.FINISHED_SUCCESS) && StringUtils.isNotBlank(jobConfig.getProjectName())
                        && StringUtils.isNotBlank(jobConfig.getVersion())) {
                    // Wait 5 seconds for the scans to be recognized in the Hub server
                    logger.info("Waiting a few seconds for the scans to be recognized by the Hub server.");
                    Thread.sleep(5000);

                    doHubScanMapping(restService, logger, jobConfig, localHostName, versionId);
                }

            } else {
                logger.info("Skipping Hub Build Step");
                result = BuildFinishedStatus.FINISHED_FAILED;
            }
        } catch (Exception e) {
            logger.error(e);
            result = BuildFinishedStatus.FINISHED_FAILED;

            // } catch (NoSuchMethodException e) {
            // // TODO Auto-generated catch block
            // throw new RuntimeException(e);
            // } catch (IllegalAccessException e) {
            // // TODO Auto-generated catch block
            // throw new RuntimeException(e);
            // } catch (IllegalArgumentException e) {
            // // TODO Auto-generated catch block
            // throw new RuntimeException(e);
            // } catch (InvocationTargetException e) {
            // // TODO Auto-generated catch block
            // throw new RuntimeException(e);
            // } catch (HubIntegrationException e) {
            // // TODO Auto-generated catch block
            // throw new RuntimeException(e);
            // } catch (URISyntaxException e) {
            // // TODO Auto-generated catch block
            // throw new RuntimeException(e);
            // } catch (IOException e) {
            // // TODO Auto-generated catch block
            // throw new RuntimeException(e);
            // } catch (TeamCityHubPluginException e) {
            // // TODO Auto-generated catch block
            // throw new RuntimeException(e);
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

        boolean scanTargetsValid = true;

        if (jobConfig.getHubScanTargets() == null || jobConfig.getHubScanTargets().isEmpty()) {
            logger.error("No scan targets configured.");
            scanTargetsValid = false;
        } else {
            for (File target : jobConfig.getHubScanTargets()) {
                if (!validator.validateTargetPath(target, jobConfig.getWorkingDirectory())) {
                    scanTargetsValid = false;
                }
            }
        }
        boolean validScanMemory = validator.validateScanMemory(jobConfig.getHubScanMemory());

        boolean validCliHome = false;
        validCliHome = validator.validateCLIPath(jobConfig.getHubCLIPath());

        return scanTargetsValid && validScanMemory && validCliHome;
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

        if (jobConfig.getHubCLIPath() != null) {
            logger.info("--> CLI Path : " + jobConfig.getHubCLIPath().getAbsolutePath());
        }

        logger.info("--> Hub scan memory : " + jobConfig.getHubScanMemory() + " MB");

        if (jobConfig.getHubScanTargets() != null && jobConfig.getHubScanTargets().size() > 0) {
            logger.info("--> Hub scan targets : ");
            for (File target : jobConfig.getHubScanTargets()) {
                logger.info("    --> " + target.getAbsolutePath());
            }
        }

    }

    private String ensureProjectExists(HubIntRestService service, IntLogger logger, String projectName,
            String projectVersion, String phase, String distribution) throws IOException, URISyntaxException,
            TeamCityHubPluginException {

        String projectId = null;
        try {
            projectId = service.getProjectByName(projectName).getId();

        } catch (ProjectDoesNotExistException e) {
            // Project was not found, try to create it
            try {
                logger.info("Creating project : " + projectName + " and version : " + projectVersion);
                projectId = service.createHubProjectAndVersion(projectName, projectVersion, phase, distribution);
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
            String projectId, String phase, String distribution) throws IOException, URISyntaxException, TeamCityHubPluginException {
        String versionId = null;
        try {

            List<ReleaseItem> projectVersions = service.getVersionsForProject(projectId);
            for (ReleaseItem release : projectVersions) {
                if (projectVersion.equals(release.getVersion())) {
                    versionId = release.getId();
                    logger.info("Found version : " + projectVersion);
                    if (!release.getPhase().equals(phase)) {
                        logger.warn("The selected Phase does not match the Phase of this Version. If you wish to update the Phase please do so in the Hub UI.");
                    }
                    if (!release.getDistribution().equals(distribution)) {
                        logger.warn("The selected Distribution does not match the Distribution of this Version. If you wish to update the Distribution please do so in the Hub UI.");
                    }
                }
            }
            if (versionId == null) {
                logger.info("Creating version : " + projectVersion);
                versionId = service.createHubVersion(projectVersion, projectId, phase, distribution);
                logger.debug("Version created!");
            }
        } catch (BDRestException e) {
            throw new TeamCityHubPluginException("Could not retrieve or create the specified version.", e);
        }
        return versionId;
    }

    public Boolean doHubScan(HubIntRestService service, HubAgentBuildLogger logger,
            File cliHome, ServerHubConfigBean globalConfig, HubScanJobConfig jobConfig) throws HubIntegrationException, IOException, URISyntaxException,
            NumberFormatException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        VersionComparison logOptionComparison = null;
        VersionComparison mappingComparison = null;
        Boolean mappingDone = false;
        try {
            // The logDir option wasnt added until Hub version 2.0.1
            logOptionComparison = service.compareWithHubVersion("2.0.1");

            mappingComparison = service.compareWithHubVersion("2.2.0");
        } catch (BDRestException e) {
            if (e.getResourceException().getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND)) {
                // The Hub server is version 2.0.0 and the version endpoint does not exist
            } else {
                logger.error(e.getResourceException().getMessage());
            }
        }
        File oneJarFile = getOneJarFile(cliHome);

        File scanExec = getScanExecFile(cliHome);

        TeamCityScanExecutor scan = new TeamCityScanExecutor(globalConfig.getHubUrl(), globalConfig.getGlobalCredentials().getHubUser(),
                globalConfig.getGlobalCredentials().getDecryptedPassword(), jobConfig.getHubScanTargetPaths(), Integer.valueOf(context.getBuild()
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
        scan.setScanMemory(Integer.valueOf(jobConfig.getHubScanMemory()));
        scan.setWorkingDirectory(jobConfig.getWorkingDirectory());
        // scan.setVerboseRun(isVerbose());
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

        String operatingSystem = System.getProperty("os.name");

        // FIXME get the correct java
        String javaHome = getEnvironmentVariable("JAVA_HOME");

        File javaExec = new File(javaHome);
        javaExec = new File(javaExec, "bin");
        if (!operatingSystem.toLowerCase().contains("windows")) {
            javaExec = new File(javaExec, "java");
        } else {
            javaExec = new File(javaExec, "java.exe");
        }

        com.blackducksoftware.integration.hub.ScanExecutor.Result sanResult = scan.setupAndRunScan(scanExec.getAbsolutePath(),
                oneJarFile.getAbsolutePath(), javaExec.getAbsolutePath());
        if (sanResult != com.blackducksoftware.integration.hub.ScanExecutor.Result.SUCCESS) {
            result = BuildFinishedStatus.FINISHED_FAILED;
        }

        return mappingDone;

    }

    private File getOneJarFile(File cliHome) {
        File oneJarFile = new File(cliHome, "lib");

        oneJarFile = new File(oneJarFile, "cache");

        oneJarFile = new File(oneJarFile, "scan.cli.impl-standalone.jar");
        return oneJarFile;
    }

    private File getScanExecFile(File cliHome) {
        File scanExecFile = new File(cliHome, "lib");

        File[] cliFiles = scanExecFile.listFiles();

        for (File file : cliFiles) {
            if (file.getName().contains("scan.cli")) {
                scanExecFile = file;
                break;
            }
        }

        return scanExecFile;
    }

    public void doHubScanMapping(HubIntRestService service, IntLogger logger, HubScanJobConfig jobConfig, String localHostName, String versionId)
            throws UnknownHostException,
            InterruptedException, BDRestException, HubIntegrationException, URISyntaxException {
        Map<String, Boolean> scanLocationIds = service.getScanLocationIds(localHostName, jobConfig.getHubScanTargetPaths(), versionId);
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
