package com.blackducksoftware.integration.hub.teamcity.agent.scan;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.BuildRunnerContext;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.response.ReleaseItem;
import com.blackducksoftware.integration.hub.teamcity.agent.HubAgentBuildLogger;
import com.blackducksoftware.integration.hub.teamcity.agent.HubParameterValidator;
import com.blackducksoftware.integration.hub.teamcity.agent.exceptions.TeamCityHubPluginException;
import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubCredentialsBean;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubProxyInfo;
import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;
import com.blackducksoftware.integration.suite.sdk.logging.LogLevel;

public class HubBuildProcess extends HubCallableBuildProcess {

    @NotNull
    private final AgentRunningBuild build;

    @NotNull
    private final BuildRunnerContext context;

    private HubAgentBuildLogger logger;

    public HubBuildProcess(@NotNull final AgentRunningBuild build, @NotNull final BuildRunnerContext context) {
        this.build = build;
        this.context = context;
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

        BuildFinishedStatus result = BuildFinishedStatus.FINISHED_SUCCESS;

        logger.targetStarted("Hub Build Step");

        String serverUrl = getParameter(HubConstantValues.HUB_URL);

        HubCredentialsBean credential = new HubCredentialsBean(getParameter(HubConstantValues.HUB_USERNAME), getParameter(HubConstantValues.HUB_PASSWORD));

        HubProxyInfo proxyInfo = new HubProxyInfo();
        proxyInfo.setHost(getParameter(HubConstantValues.HUB_PROXY_HOST));
        if (getParameter(HubConstantValues.HUB_PROXY_PORT) != null) {
            proxyInfo.setPort(Integer.valueOf(getParameter(HubConstantValues.HUB_PROXY_PORT)));
        }
        proxyInfo.setIgnoredProxyHosts(getParameter(HubConstantValues.HUB_NO_PROXY_HOSTS));
        proxyInfo.setProxyUsername(getParameter(HubConstantValues.HUB_PROXY_USER));
        proxyInfo.setProxyPassword(getParameter(HubConstantValues.HUB_PROXY_PASS));

        String projectName = getParameter(HubConstantValues.HUB_PROJECT_NAME);
        String version = getParameter(HubConstantValues.HUB_PROJECT_VERSION);

        String phase = getParameter(HubConstantValues.HUB_VERSION_PHASE);
        String distribution = getParameter(HubConstantValues.HUB_VERSION_DISTRIBUTION);

        String hubCLIPath = getParameter(HubConstantValues.HUB_CLI_PATH);
        String hubScanMemory = getParameter(HubConstantValues.HUB_SCAN_MEMORY);
        String hubScanTargets = getParameter(HubConstantValues.HUB_SCAN_TARGETS);

        File workingDirectory = context.getWorkingDirectory();
        String workingDirectoryPath = workingDirectory.getCanonicalPath();

        File cliHome = null;
        if (StringUtils.isBlank(hubCLIPath)) {
            String cliHomePath = getEnvironmentVariable(HubConstantValues.HUB_CLI_ENV_VAR);
            if (StringUtils.isNotBlank(cliHomePath)) {
                cliHome = new File(cliHomePath);
            }
        } else {
            cliHome = new File(hubCLIPath);
        }

        List<File> scanTargets = new ArrayList<File>();

        if (StringUtils.isNotBlank(hubScanTargets)) {
            if (hubScanTargets.contains(System.getProperty("line.separator"))) {
                String[] scanTargetPaths = hubScanTargets.split(System.getProperty("line.separator"));
                for (String target : scanTargetPaths) {
                    scanTargets.add(new File(workingDirectory, target));
                }
            } else {
                scanTargets.add(new File(workingDirectory, hubScanTargets));
            }
        } else {
            scanTargets.add(workingDirectory);
        }

        printGlobalConfguration(serverUrl, credential, proxyInfo);
        printJobConfguration(projectName, version, phase, distribution,
                scanTargets, workingDirectoryPath, hubScanMemory, cliHome);
        try {
            if (isGlobalConfigValid(serverUrl, credential) && isJobConfigValid(scanTargets, workingDirectoryPath, hubScanMemory, cliHome)) {
                HubIntRestService restService = new HubIntRestService(serverUrl);
                restService.setLogger(logger);
                if (proxyInfo != null) {
                    Integer port = (proxyInfo.getPort() == null) ? 0 : proxyInfo.getPort();

                    restService.setProxyProperties(proxyInfo.getHost(), port,
                            proxyInfo.getNoProxyHostPatterns(), proxyInfo.getProxyUsername(), proxyInfo.getProxyPassword());
                }
                restService.setCookies(credential.getHubUser(), credential.getDecryptedPassword());

                String projectId = ensureProjectExists(restService, logger, projectName);
                String versionId = ensureVersionExists(restService, logger, version, projectId, phase, distribution);
                doHubScan();
                doHubScanMapping();

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

    public boolean isGlobalConfigValid(final String serverUrl, final HubCredentialsBean credential) throws IOException {

        HubParameterValidator validator = new HubParameterValidator(logger);

        boolean isUrlEmpty = validator.isServerUrlEmpty(serverUrl);
        boolean credentialsConfigured = validator.isHubCredentialConfigured(credential);

        return !isUrlEmpty && credentialsConfigured;
    }

    public boolean isJobConfigValid(final List<File> scanTargets, final String workingDirectory, final String memory, final File cliHomeDirectory)
            throws IOException {

        HubParameterValidator validator = new HubParameterValidator(logger);

        boolean scanTargetsValid = true;

        if (scanTargets == null) {
            logger.error("No scan targets configured.");
            scanTargetsValid = false;
        } else {
            for (File target : scanTargets) {
                if (!validator.validateTargetPath(target, workingDirectory)) {
                    scanTargetsValid = false;
                }
            }
        }
        boolean validScanMemory = validator.validateScanMemory(memory);

        boolean validCliHome = false;
        validCliHome = validator.validateCLIPath(cliHomeDirectory);

        return scanTargetsValid && validScanMemory && validCliHome;
    }

    public void printGlobalConfguration(final String serverUrl, final HubCredentialsBean credential, HubProxyInfo proxyInfo) {

        logger.info("--> Hub Server Url : " + serverUrl);
        if (credential != null) {
            logger.info("--> Hub User : " + credential.getHubUser());
        }

        if (proxyInfo != null) {
            if (StringUtils.isNotBlank(proxyInfo.getHost())) {
                logger.info("--> Proxy Host : " + proxyInfo.getHost());
            }
            if (proxyInfo.getPort() != null) {
                logger.info("--> Proxy Port : " + proxyInfo.getPort());
            }
            if (StringUtils.isNotBlank(proxyInfo.getIgnoredProxyHosts())) {
                logger.info("--> No Proxy Hosts : " + proxyInfo.getIgnoredProxyHosts());
            }
            if (StringUtils.isNotBlank(proxyInfo.getProxyUsername())) {
                logger.info("--> Proxy Username : " + proxyInfo.getProxyUsername());
            }
        }
    }

    public void printJobConfguration(final String projectName, final String version, final String phase, final String distribution,
            final List<File> scanTargets,
            final String workingDirectoryPath, final String hubScanMemory, final File cliHomeDirectory) {
        logger.info("Working directory : " + workingDirectoryPath);

        logger.info("--> Project : " + projectName);
        logger.info("--> Version : " + version);

        logger.info("--> Version Phase : " + phase);
        logger.info("--> Version Distribution : " + distribution);

        if (cliHomeDirectory != null) {
            logger.info("--> CLI Path : " + cliHomeDirectory.getAbsolutePath());
        }

        logger.info("--> Hub scan memory : " + hubScanMemory + " MB");

        if (scanTargets != null && scanTargets.size() > 0) {
            logger.info("--> Hub scan targets : ");
            for (File target : scanTargets) {
                logger.info("    --> " + target.getAbsolutePath());
            }
        }

    }

    private String ensureProjectExists(HubIntRestService service, IntLogger logger, String projectName) throws IOException, URISyntaxException,
            TeamCityHubPluginException {
        String projectId = null;
        try {
            projectId = service.getProjectByName(projectName).getId();
            logger.info("Found project : " + projectName);
        } catch (BDRestException e) {
            if (e.getResource() != null) {
                if (e.getResource().getResponse().getStatus().getCode() == 404) {
                    // Project was not found, try to create it
                    try {
                        logger.info("Creating project : " + projectName);
                        projectId = service.createHubProject(projectName);
                        logger.debug("Project created!");

                    } catch (BDRestException e1) {
                        if (e1.getResource() != null) {
                            logger.error("Status : " + e1.getResource().getStatus().getCode());
                            logger.error("Response : " + e1.getResource().getResponse().getEntityAsText());
                        }
                        throw new TeamCityHubPluginException("Problem creating the Project. ", e1);
                    }
                } else {
                    if (e.getResource() != null) {
                        logger.error("Status : " + e.getResource().getStatus().getCode());
                        logger.error("Response : " + e.getResource().getResponse().getEntityAsText());
                    }
                    throw new TeamCityHubPluginException("Problem getting the Project. ", e);
                }
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

    public void doHubScan() {

    }

    public void doHubScanMapping() {

    }

}
