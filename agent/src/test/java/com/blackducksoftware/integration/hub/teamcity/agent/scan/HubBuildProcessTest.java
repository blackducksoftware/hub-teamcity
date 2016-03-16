package com.blackducksoftware.integration.hub.teamcity.agent.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jetbrains.buildServer.agent.BuildFinishedStatus;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.response.DistributionEnum;
import com.blackducksoftware.integration.hub.response.PhaseEnum;
import com.blackducksoftware.integration.hub.response.ProjectItem;
import com.blackducksoftware.integration.hub.teamcity.agent.HubAgentBuildLogger;
import com.blackducksoftware.integration.hub.teamcity.agent.util.TeamCityHubIntTestHelper;
import com.blackducksoftware.integration.hub.teamcity.agent.util.TestAgentRunningBuild;
import com.blackducksoftware.integration.hub.teamcity.agent.util.TestBuildAgentConfiguration;
import com.blackducksoftware.integration.hub.teamcity.agent.util.TestBuildProgressLogger;
import com.blackducksoftware.integration.hub.teamcity.agent.util.TestBuildRunnerContext;
import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubCredentialsBean;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubProxyInfo;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubScanJobConfig;
import com.blackducksoftware.integration.hub.teamcity.common.beans.ServerHubConfigBean;
import com.blackducksoftware.integration.suite.encryption.PasswordEncrypter;

public class HubBuildProcessTest {
    private static Properties testProperties;

    private static HubAgentBuildLogger logger;

    private static TestBuildProgressLogger testLogger;

    private static File testEmptyDirectory;

    private static File testSourceFile;

    private static File workingDirectory;

    private static TeamCityHubIntTestHelper restHelper;

    @BeforeClass
    public static void testStartup() throws Exception {
        testProperties = new Properties();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = classLoader.getResourceAsStream("test.properties");

        try {
            testProperties.load(is);

        } catch (IOException e) {
            System.err.println("reading test.properties failed!");
        }

        testLogger = new TestBuildProgressLogger();
        logger = new HubAgentBuildLogger(testLogger);

        String workingDirPath = HubBuildProcessTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        workingDirPath = workingDirPath.substring(0, workingDirPath.indexOf("/target"));
        workingDirPath = workingDirPath + "/test-workspace";

        workingDirectory = new File(workingDirPath);

        String testEmptyPath = workingDirPath + "/emptyDirectory";
        testEmptyDirectory = new File(testEmptyPath);
        if (!testEmptyDirectory.exists()) {
            testEmptyDirectory.mkdirs();
        }

        String sourcePath = workingDirPath + "/directory";
        testSourceFile = new File(sourcePath);
        if (!testSourceFile.exists()) {
            testSourceFile.mkdirs();
        }

        restHelper = new TeamCityHubIntTestHelper(testProperties.getProperty("TEST_HUB_SERVER_URL"));
        restHelper.setCookies(testProperties.getProperty("TEST_USERNAME"), testProperties.getProperty("TEST_PASSWORD"));
    }

    @After
    public void testCleanup() {
        testLogger.clearAllOutput();
    }

    @Test
    public void testConstructor() {
        assertNotNull(new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext()));

    }

    @Test
    public void testPrintGlobalConfigurationNull() {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        process.printGlobalConfguration(null);

        String output = testLogger.getProgressMessagesString();
        assertTrue(output, !output.contains("--> Hub Server Url : "));
        assertTrue(output, !output.contains("--> Hub User :"));
        assertTrue(output, !output.contains("--> Proxy Host :"));
        assertTrue(output, !output.contains("--> Proxy Port :"));
        assertTrue(output, !output.contains("--> No Proxy Hosts :"));
        assertTrue(output, !output.contains("--> Proxy Username :"));

    }

    @Test
    public void testPrintGlobalConfigurationEmpty() {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        process.printGlobalConfguration(new ServerHubConfigBean());

        String output = testLogger.getProgressMessagesString();
        assertTrue(output, output.contains("--> Hub Server Url : "));
        assertTrue(output, !output.contains("--> Hub User :"));
        assertTrue(output, !output.contains("--> Proxy Host :"));
        assertTrue(output, !output.contains("--> Proxy Port :"));
        assertTrue(output, !output.contains("--> No Proxy Hosts :"));
        assertTrue(output, !output.contains("--> Proxy Username :"));

    }

    @Test
    public void testPrintGlobalConfigurationPassThroughProxy() {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);
        HubProxyInfo proxyInfo = new HubProxyInfo();
        proxyInfo.setHost("Test host");
        proxyInfo.setPort(3126);
        proxyInfo.setIgnoredProxyHosts("ignore test");

        ServerHubConfigBean globalConfig = new ServerHubConfigBean();
        globalConfig.setHubUrl("testUrl");
        globalConfig.setGlobalCredentials(new HubCredentialsBean("testUser", "testPassword"));
        globalConfig.setProxyInfo(proxyInfo);

        process.printGlobalConfguration(globalConfig);

        String output = testLogger.getProgressMessagesString();
        assertTrue(output, output.contains("--> Hub Server Url : testUrl"));
        assertTrue(output, output.contains("--> Hub User : testUser"));
        assertTrue(output, output.contains("--> Proxy Host : Test host"));
        assertTrue(output, output.contains("--> Proxy Port : 3126"));
        assertTrue(output, output.contains("--> No Proxy Hosts : ignore test"));
        assertTrue(output, !output.contains("--> Proxy Username :"));
    }

    @Test
    public void testPrintGlobalConfigurationAuthenticatedProxy() {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);
        HubProxyInfo proxyInfo = new HubProxyInfo();
        proxyInfo.setHost("Test host");
        proxyInfo.setPort(3126);
        proxyInfo.setIgnoredProxyHosts("ignore test");
        proxyInfo.setProxyUsername("testProxyUser");
        proxyInfo.setProxyPassword("testProxyPassword");

        ServerHubConfigBean globalConfig = new ServerHubConfigBean();
        globalConfig.setHubUrl("testUrl");
        globalConfig.setGlobalCredentials(new HubCredentialsBean("testUser", "testPassword"));
        globalConfig.setProxyInfo(proxyInfo);

        process.printGlobalConfguration(globalConfig);

        String output = testLogger.getProgressMessagesString();
        assertTrue(output, output.contains("--> Hub Server Url : testUrl"));
        assertTrue(output, output.contains("--> Hub User : testUser"));
        assertTrue(output, output.contains("--> Proxy Host : Test host"));
        assertTrue(output, output.contains("--> Proxy Port : 3126"));
        assertTrue(output, output.contains("--> No Proxy Hosts : ignore test"));
        assertTrue(output, output.contains("--> Proxy Username : testProxyUser"));
    }

    @Test
    public void testPrintJobConfigurationNull() {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        HubScanJobConfig jobConfig = new HubScanJobConfig();

        process.printJobConfguration(jobConfig);

        String output = testLogger.getProgressMessagesString();
        assertTrue(output, output.contains("Working directory : "));
        assertTrue(output, output.contains("--> Project : "));
        assertTrue(output, output.contains("--> Version : "));
        assertTrue(output, output.contains("--> Version Phase : "));
        assertTrue(output, output.contains("--> Version Distribution : "));
        assertTrue(output, output.contains("--> Hub scan memory : "));
        assertTrue(output, !output.contains("--> Hub scan targets : "));
        assertTrue(output, !output.contains("--> CLI Path : "));
    }

    @Test
    public void testPrintJobConfiguration() {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        List<File> scanTargets = new ArrayList<File>();
        File testTarget = new File("");
        scanTargets.add(testTarget);

        HubScanJobConfig jobConfig = new HubScanJobConfig();
        jobConfig.setProjectName("testProject");
        jobConfig.setVersion("testVersion");
        jobConfig.setPhase("testPhase");
        jobConfig.setDistribution("testDistribution");
        jobConfig.setHubScanTargets(scanTargets);
        jobConfig.setWorkingDirectory("workingDirPath");
        jobConfig.setHubScanMemory("scan Memory");

        process.printJobConfguration(jobConfig);

        String output = testLogger.getProgressMessagesString();
        assertTrue(output, output.contains("Working directory : "));
        assertTrue(output, output.contains("--> Project : "));
        assertTrue(output, output.contains("--> Version : "));
        assertTrue(output, output.contains("--> Version Phase : "));
        assertTrue(output, output.contains("--> Version Distribution : "));
        assertTrue(output, output.contains("--> Hub scan memory : "));

        assertTrue(output, output.contains("--> Hub scan targets : "));
        assertTrue(output, output.contains("--> " + testTarget.getAbsolutePath()));
    }

    @Test
    public void testPrintJobConfigurationEmptyScanTargets() {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        List<File> scanTargets = new ArrayList<File>();

        HubScanJobConfig jobConfig = new HubScanJobConfig();
        jobConfig.setHubScanTargets(scanTargets);

        process.printJobConfguration(jobConfig);

        String output = testLogger.getProgressMessagesString();
        assertTrue(output, !output.contains("--> Hub scan targets : "));
    }

    @Test
    public void testIsGlobalConfigValidNull() throws Exception {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        ServerHubConfigBean globalConfig = new ServerHubConfigBean();
        globalConfig.setHubUrl(null);
        globalConfig.setGlobalCredentials(null);

        assertTrue(!process.isGlobalConfigValid(globalConfig));

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("There is no Server URL specified"));
        assertTrue(output, output.contains("There are no credentials configured."));
    }

    @Test
    public void testIsGlobalConfigValidEmptyCredentials() throws Exception {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        ServerHubConfigBean globalConfig = new ServerHubConfigBean();
        globalConfig.setHubUrl("testUrl");
        globalConfig.setGlobalCredentials(new HubCredentialsBean(""));

        assertTrue(!process.isGlobalConfigValid(globalConfig));

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("There is no Hub username specified"));
        assertTrue(output, output.contains("There is no Hub password specified."));
    }

    @Test
    public void testIsGlobalConfigValid() throws Exception {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        ServerHubConfigBean globalConfig = new ServerHubConfigBean();
        globalConfig.setHubUrl("http://testUrl");
        globalConfig.setGlobalCredentials(new HubCredentialsBean("TestUser", "TestPass"));

        boolean validConfig = process.isGlobalConfigValid(globalConfig);

        if (!validConfig) {
            if (testLogger.getErrorMessages().size() != 0) {
                for (String error : testLogger.getErrorMessages()) {
                    System.out.print(error);
                }
            }
            fail();
        } else {
            assertTrue(testLogger.getErrorMessages().size() == 0);
        }
    }

    @Test
    public void testIsJobConfigValidNull() throws Exception {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        assertTrue(!process.isJobConfigValid(null));

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, !output.contains("There is no memory specified for the Hub scan."));
        assertTrue(output, !output.contains("No scan targets configured."));
    }

    @Test
    public void testIsJobConfigValidEmpty() throws Exception {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        HubScanJobConfig jobConfig = new HubScanJobConfig();

        assertTrue(!process.isJobConfigValid(jobConfig));

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("There is no memory specified for the Hub scan."));
        assertTrue(output, output.contains("No scan targets configured."));
    }

    @Test
    public void testIsJobConfigValidInvalid() throws Exception {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        List<File> scanTargets = new ArrayList<File>();

        scanTargets.add(new File(testSourceFile, "emptyFile.txt"));
        scanTargets.add(new File("fakeOutsideWorkspace"));

        HubScanJobConfig jobConfig = new HubScanJobConfig();
        jobConfig.setHubScanTargets(scanTargets);
        jobConfig.setWorkingDirectory(workingDirectory.getAbsolutePath());
        jobConfig.setHubScanMemory("23");

        assertTrue(!process.isJobConfigValid(jobConfig));

        String output = testLogger.getErrorMessagesString();
        assertTrue(output, output.contains("Can not scan targets outside the working directory."));
        assertTrue(output, output.contains("The Hub scan requires at least 4096 MB of memory."));
    }

    @Test
    public void testIsJobConfigValidTargetNotExisting() throws Exception {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        List<File> scanTargets = new ArrayList<File>();

        scanTargets.add(new File(testSourceFile, "fakeFile"));

        HubScanJobConfig jobConfig = new HubScanJobConfig();
        jobConfig.setHubScanTargets(scanTargets);
        jobConfig.setWorkingDirectory(workingDirectory.getAbsolutePath());
        jobConfig.setHubScanMemory("4096");

        assertTrue(!process.isJobConfigValid(jobConfig));

        String output = testLogger.getErrorMessagesString();
        assertTrue(output, output.contains("The scan target '"));
        assertTrue(output, output.contains("' does not exist."));
    }

    @Test
    public void testIsJobConfigValidTargetValid() throws Exception {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        List<File> scanTargets = new ArrayList<File>();

        scanTargets.add(new File(testSourceFile, "emptyFile.txt"));

        HubScanJobConfig jobConfig = new HubScanJobConfig();
        jobConfig.setHubScanTargets(scanTargets);
        jobConfig.setWorkingDirectory(workingDirectory.getAbsolutePath());
        jobConfig.setHubScanMemory("4096");

        boolean validJobConfig = process
                .isJobConfigValid(jobConfig);

        if (!validJobConfig) {
            if (testLogger.getErrorMessages().size() != 0) {
                for (String error : testLogger.getErrorMessages()) {
                    System.out.print(error);
                }
            }
            fail();
        } else {
            assertTrue(testLogger.getErrorMessages().size() == 0);
        }
    }

    @Test
    public void testCallNothingConfigured() throws Exception {
        TestBuildRunnerContext context = new TestBuildRunnerContext();
        context.setWorkingDirectory(workingDirectory);

        TestAgentRunningBuild build = new TestAgentRunningBuild();
        build.setLogger(testLogger);

        HubBuildProcess process = new HubBuildProcess(build, context);

        assertEquals(BuildFinishedStatus.FINISHED_FAILED, process.call());

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("There is no Server URL specified"));
        assertTrue(output, output.contains("There is no Hub username specified"));
        assertTrue(output, output.contains("There is no Hub password specified."));

        assertTrue(output, !output.contains("There is no memory specified for the Hub scan. The scan requires a minimum of 4096 MB."));
        assertTrue(output, !output.contains("The Hub CLI path has not been set."));

        String progressOutput = testLogger.getProgressMessagesString();

        assertTrue(progressOutput, progressOutput.contains("Skipping Hub Build Step"));
    }

    @Test
    public void testCallGlobalPartiallyConfigured() throws Exception {
        TestBuildRunnerContext context = new TestBuildRunnerContext();
        context.setWorkingDirectory(workingDirectory);

        context.addRunnerParameter(HubConstantValues.HUB_URL, "testUrl");
        context.addRunnerParameter(HubConstantValues.HUB_USERNAME, "testUser");

        TestAgentRunningBuild build = new TestAgentRunningBuild();
        build.setLogger(testLogger);

        HubBuildProcess process = new HubBuildProcess(build, context);

        assertEquals(BuildFinishedStatus.FINISHED_FAILED, process.call());

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, !output.contains("There is no Server URL specified"));
        assertTrue(output, !output.contains("There is no Hub username specified"));
        assertTrue(output, output.contains("There is no Hub password specified."));

        assertTrue(output, !output.contains("There is no memory specified for the Hub scan. The scan requires a minimum of 4096 MB."));

        String progressOutput = testLogger.getProgressMessagesString();

        assertTrue(progressOutput, progressOutput.contains("Skipping Hub Build Step"));
    }

    @Test
    public void testCallGlobalConfigured() throws Exception {
        TestBuildRunnerContext context = new TestBuildRunnerContext();
        context.setWorkingDirectory(workingDirectory);

        context.addRunnerParameter(HubConstantValues.HUB_URL, "http://testUrl");
        context.addRunnerParameter(HubConstantValues.HUB_USERNAME, "testUser");
        context.addRunnerParameter(HubConstantValues.HUB_PASSWORD, "testPassword");

        TestAgentRunningBuild build = new TestAgentRunningBuild();
        build.setLogger(testLogger);

        HubBuildProcess process = new HubBuildProcess(build, context);

        assertEquals(BuildFinishedStatus.FINISHED_FAILED, process.call());

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, !output.contains("There is no Server URL specified"));
        assertTrue(output, !output.contains("There is no Hub username specified"));
        assertTrue(output, !output.contains("There is no Hub password specified."));

        assertTrue(output, output.contains("There is no memory specified for the Hub scan. The scan requires a minimum of 4096 MB."));

        String progressOutput = testLogger.getProgressMessagesString();

        assertTrue(progressOutput, progressOutput.contains("Skipping Hub Build Step"));
    }

    @Test
    public void testCallJobPartiallyConfiguredCLIEnvVarSet() throws Exception {
        TestBuildRunnerContext context = new TestBuildRunnerContext();
        context.setWorkingDirectory(workingDirectory);

        context.addEnvironmentVariable(HubConstantValues.HUB_CLI_ENV_VAR, (new File(workingDirectory, "scan.cli-2.1.2")).getAbsolutePath());

        context.addRunnerParameter(HubConstantValues.HUB_URL, "http://testUrl");
        context.addRunnerParameter(HubConstantValues.HUB_USERNAME, "testUser");
        context.addRunnerParameter(HubConstantValues.HUB_PASSWORD, "testPassword");

        TestAgentRunningBuild build = new TestAgentRunningBuild();
        build.setLogger(testLogger);

        HubBuildProcess process = new HubBuildProcess(build, context);

        assertEquals(BuildFinishedStatus.FINISHED_FAILED, process.call());

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, !output.contains("There is no Server URL specified"));
        assertTrue(output, !output.contains("There is no Hub username specified"));
        assertTrue(output, !output.contains("There is no Hub password specified."));

        assertTrue(output, output.contains("There is no memory specified for the Hub scan. The scan requires a minimum of 4096 MB."));
        assertTrue(output, !output.contains("The Hub CLI path has not been set."));

        String progressOutput = testLogger.getProgressMessagesString();

        assertTrue(progressOutput, progressOutput.contains("Skipping Hub Build Step"));
    }

    @Test
    public void testCallFullyConfigured() throws Exception {
        try {
            TestBuildRunnerContext context = new TestBuildRunnerContext();
            context.setWorkingDirectory(workingDirectory);

            context.addRunnerParameter(HubConstantValues.HUB_URL, testProperties.getProperty("TEST_HUB_SERVER_URL"));
            context.addRunnerParameter(HubConstantValues.HUB_USERNAME, testProperties.getProperty("TEST_USERNAME"));
            context.addRunnerParameter(HubConstantValues.HUB_PASSWORD, PasswordEncrypter.publicEncrypt(testProperties.getProperty("TEST_PASSWORD")));

            context.addRunnerParameter(HubConstantValues.HUB_PROJECT_NAME, testProperties.getProperty("TEST_PROJECT"));
            context.addRunnerParameter(HubConstantValues.HUB_PROJECT_VERSION, testProperties.getProperty("TEST_VERSION"));
            context.addRunnerParameter(HubConstantValues.HUB_VERSION_PHASE, PhaseEnum.DEVELOPMENT.getDisplayValue());
            context.addRunnerParameter(HubConstantValues.HUB_VERSION_DISTRIBUTION, DistributionEnum.INTERNAL.getDisplayValue());
            context.addRunnerParameter(HubConstantValues.HUB_SCAN_MEMORY, "4096");

            context.addRunnerParameter(HubConstantValues.HUB_SCAN_TARGETS, "directory/emptyFile.txt" + System.getProperty("line.separator")
                    + "directory/secondEmptyFile.txt");

            context.addEnvironmentVariable("JAVA_HOME", System.getProperty("java.home"));

            TestBuildAgentConfiguration agentConfig = new TestBuildAgentConfiguration();
            agentConfig.setAgentToolsDirectory(new File(workingDirectory, "tools"));

            TestAgentRunningBuild build = new TestAgentRunningBuild();
            context.setBuild(build);
            build.setAgentConfiguration(agentConfig);
            build.setLogger(testLogger);

            HubBuildProcess process = new HubBuildProcess(build, context);

            BuildFinishedStatus result = process.call();

            String output = testLogger.getErrorMessagesString();

            assertTrue(output, !output.contains("There is no Server URL specified"));
            assertTrue(output, !output.contains("There is no Hub username specified"));
            assertTrue(output, !output.contains("There is no Hub password specified."));

            assertTrue(output, !output.contains("There is no memory specified for the Hub scan. The scan requires a minimum of 4096 MB."));
            assertTrue(output, StringUtils.isBlank(output));

            String progressOutput = testLogger.getProgressMessagesString();

            assertTrue(progressOutput, progressOutput.contains("--> Hub Server Url : " + testProperties.getProperty("TEST_HUB_SERVER_URL")));
            assertTrue(progressOutput, progressOutput.contains("--> Hub User : " + testProperties.getProperty("TEST_USERNAME")));
            assertTrue(progressOutput, !progressOutput.contains("--> Proxy Host :"));
            assertTrue(progressOutput, !progressOutput.contains("--> Proxy Port :"));
            assertTrue(progressOutput, !progressOutput.contains("--> No Proxy Hosts :"));
            assertTrue(progressOutput, !progressOutput.contains("--> Proxy Username :"));

            assertTrue(progressOutput, progressOutput.contains("Working directory : "));
            assertTrue(progressOutput, progressOutput.contains("--> Project : " + testProperties.getProperty("TEST_PROJECT")));
            assertTrue(progressOutput, progressOutput.contains("--> Version : " + testProperties.getProperty("TEST_VERSION")));
            assertTrue(progressOutput, progressOutput.contains("--> Version Phase : " + PhaseEnum.DEVELOPMENT.getDisplayValue()));
            assertTrue(progressOutput, progressOutput.contains("--> Version Distribution : " + DistributionEnum.INTERNAL.getDisplayValue()));
            assertTrue(progressOutput, progressOutput.contains("--> Hub scan memory : 4096"));
            assertTrue(progressOutput, progressOutput.contains("--> Hub scan targets : "));

            assertTrue(progressOutput, progressOutput.contains("Hub CLI command"));
            assertTrue(progressOutput, progressOutput.contains("-Done-jar.silent"));
            assertTrue(progressOutput, progressOutput.contains("-Done-jar.jar.path"));
            assertTrue(progressOutput, progressOutput.contains("-Xmx"));
            assertTrue(progressOutput, progressOutput.contains("-jar"));
            assertTrue(progressOutput, progressOutput.contains("--scheme"));
            assertTrue(progressOutput, progressOutput.contains("--host"));
            assertTrue(progressOutput, progressOutput.contains("--username"));
            assertTrue(progressOutput, progressOutput.contains("--password"));
            assertTrue(progressOutput, progressOutput.contains("--port"));
            assertTrue(progressOutput, progressOutput.contains("--logDir"));
            assertTrue(progressOutput, progressOutput.contains("Hub CLI return code"));
            assertTrue(progressOutput, progressOutput.contains("Finished in") && progressOutput.contains("with status SUCCESS"));
            assertTrue(progressOutput, progressOutput.contains("You can view the BlackDuck Scan CLI logs at"));

            assertEquals(BuildFinishedStatus.FINISHED_SUCCESS, result);
            // assertTrue(progressOutput,
            // progressOutput.contains("Waiting a few seconds for the scans to be recognized by the Hub server."));
            // assertTrue(progressOutput, progressOutput.contains("Checking for the scan location with Host name"));
            // assertTrue(progressOutput, progressOutput.contains("MATCHED"));
            // assertTrue(progressOutput, progressOutput.contains("These scan Id's were found for the scan targets."));
            // assertTrue(progressOutput, progressOutput.contains("Mapping the scan location with id:"));
            // assertTrue(progressOutput, progressOutput.contains("Asset reference mapping object :"));
            // assertTrue(progressOutput, progressOutput.contains("Successfully mapped the scan with id"));
        } finally {
            try {
                ProjectItem project = restHelper.getProjectByName(testProperties.getProperty("TEST_PROJECT"));
                if (project != null && project.getId() != null) {
                    restHelper.deleteHubProject(project.getId());
                }
            } catch (ProjectDoesNotExistException e) {
                // ignore this one
            }
        }
    }

    @Test
    public void testCallFullyConfiguredPassThroughProxyProxyIgnored() throws Exception {
        try {
            TestBuildRunnerContext context = new TestBuildRunnerContext();
            context.setWorkingDirectory(workingDirectory);

            context.addRunnerParameter(HubConstantValues.HUB_URL, testProperties.getProperty("TEST_HUB_SERVER_URL"));
            context.addRunnerParameter(HubConstantValues.HUB_USERNAME, testProperties.getProperty("TEST_USERNAME"));
            context.addRunnerParameter(HubConstantValues.HUB_PASSWORD, PasswordEncrypter.publicEncrypt(testProperties.getProperty("TEST_PASSWORD")));

            context.addRunnerParameter(HubConstantValues.HUB_PROXY_HOST, testProperties.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));
            context.addRunnerParameter(HubConstantValues.HUB_PROXY_PORT, testProperties.getProperty("TEST_PROXY_PORT_PASSTHROUGH"));
            String serverUrl = testProperties.getProperty("TEST_HUB_SERVER_URL");
            URL hubUrl = new URL(serverUrl);
            context.addRunnerParameter(HubConstantValues.HUB_NO_PROXY_HOSTS, "fake host ," + hubUrl.getHost() + ",testHost");

            context.addRunnerParameter(HubConstantValues.HUB_PROJECT_NAME, testProperties.getProperty("TEST_PROJECT"));
            context.addRunnerParameter(HubConstantValues.HUB_PROJECT_VERSION, testProperties.getProperty("TEST_VERSION"));
            context.addRunnerParameter(HubConstantValues.HUB_VERSION_PHASE, PhaseEnum.DEVELOPMENT.getDisplayValue());
            context.addRunnerParameter(HubConstantValues.HUB_VERSION_DISTRIBUTION, DistributionEnum.INTERNAL.getDisplayValue());
            context.addRunnerParameter(HubConstantValues.HUB_SCAN_MEMORY, "4096");

            context.addRunnerParameter(HubConstantValues.HUB_SCAN_TARGETS, "directory/emptyFile.txt");

            context.addEnvironmentVariable("JAVA_HOME", System.getProperty("java.home"));
            TestBuildAgentConfiguration agentConfig = new TestBuildAgentConfiguration();
            agentConfig.setAgentToolsDirectory(new File(workingDirectory, "tools"));
            TestAgentRunningBuild build = new TestAgentRunningBuild();
            build.setAgentConfiguration(agentConfig);
            context.setBuild(build);
            build.setLogger(testLogger);

            HubBuildProcess process = new HubBuildProcess(build, context);
            BuildFinishedStatus result = process.call();

            String output = testLogger.getErrorMessagesString();

            assertTrue(output, !output.contains("There is no Server URL specified"));
            assertTrue(output, !output.contains("There is no Hub username specified"));
            assertTrue(output, !output.contains("There is no Hub password specified."));

            assertTrue(output, !output.contains("There is no memory specified for the Hub scan. The scan requires a minimum of 4096 MB."));
            assertTrue(output, StringUtils.isBlank(output));

            String progressOutput = testLogger.getProgressMessagesString();

            assertTrue(progressOutput, progressOutput.contains("--> Hub Server Url : " + testProperties.getProperty("TEST_HUB_SERVER_URL")));
            assertTrue(progressOutput, progressOutput.contains("--> Hub User : " + testProperties.getProperty("TEST_USERNAME")));
            assertTrue(progressOutput, progressOutput.contains("--> Proxy Host :"));
            assertTrue(progressOutput, progressOutput.contains("--> Proxy Port :"));
            assertTrue(progressOutput, progressOutput.contains("--> No Proxy Hosts :"));
            assertTrue(progressOutput, !progressOutput.contains("--> Proxy Username :"));

            assertTrue(progressOutput, progressOutput.contains("Working directory : "));
            assertTrue(progressOutput, progressOutput.contains("--> Project : " + testProperties.getProperty("TEST_PROJECT")));
            assertTrue(progressOutput, progressOutput.contains("--> Version : " + testProperties.getProperty("TEST_VERSION")));
            assertTrue(progressOutput, progressOutput.contains("--> Version Phase : " + PhaseEnum.DEVELOPMENT.getDisplayValue()));
            assertTrue(progressOutput, progressOutput.contains("--> Version Distribution : " + DistributionEnum.INTERNAL.getDisplayValue()));
            assertTrue(progressOutput, progressOutput.contains("--> Hub scan memory : 4096"));
            assertTrue(progressOutput, progressOutput.contains("--> Hub scan targets : "));

            assertTrue(progressOutput, progressOutput.contains("Hub CLI command"));
            assertTrue(progressOutput, progressOutput.contains("-Done-jar.silent"));
            assertTrue(progressOutput, progressOutput.contains("-Done-jar.jar.path"));
            assertTrue(progressOutput, !progressOutput.contains("-Dhttp.proxyHost"));
            assertTrue(progressOutput, !progressOutput.contains("-Dhttp.proxyPort"));
            assertTrue(progressOutput, progressOutput.contains("-Xmx"));
            assertTrue(progressOutput, progressOutput.contains("-jar"));
            assertTrue(progressOutput, progressOutput.contains("--scheme"));
            assertTrue(progressOutput, progressOutput.contains("--host"));
            assertTrue(progressOutput, progressOutput.contains("--username"));
            assertTrue(progressOutput, progressOutput.contains("--password"));
            assertTrue(progressOutput, progressOutput.contains("--port"));
            assertTrue(progressOutput, progressOutput.contains("--logDir"));
            assertTrue(progressOutput, progressOutput.contains("Hub CLI return code"));
            assertTrue(progressOutput, progressOutput.contains("Finished in") && progressOutput.contains("with status SUCCESS"));
            assertTrue(progressOutput, progressOutput.contains("You can view the BlackDuck Scan CLI logs at"));

            assertEquals(BuildFinishedStatus.FINISHED_SUCCESS, result);
            // assertTrue(progressOutput,
            // progressOutput.contains("Waiting a few seconds for the scans to be recognized by the Hub server."));
            // assertTrue(progressOutput, progressOutput.contains("Checking for the scan location with Host name"));
            // assertTrue(progressOutput, progressOutput.contains("MATCHED"));
            // assertTrue(progressOutput, progressOutput.contains("These scan Id's were found for the scan targets."));
            // assertTrue(progressOutput, progressOutput.contains("Mapping the scan location with id:"));
            // assertTrue(progressOutput, progressOutput.contains("Asset reference mapping object :"));
            // assertTrue(progressOutput, progressOutput.contains("Successfully mapped the scan with id"));
        } finally {
            try {
                ProjectItem project = restHelper.getProjectByName(testProperties.getProperty("TEST_PROJECT"));
                if (project != null && project.getId() != null) {
                    restHelper.deleteHubProject(project.getId());
                }
            } catch (ProjectDoesNotExistException e) {
                // ignore this one
            }

        }
    }

    @Test
    public void testCallFullyConfiguredPassThroughProxy() throws Exception {
        try {
            TestBuildRunnerContext context = new TestBuildRunnerContext();
            context.setWorkingDirectory(workingDirectory);

            context.addEnvironmentVariable(HubConstantValues.HUB_CLI_ENV_VAR, (new File(workingDirectory, "scan.cli-2.1.2")).getAbsolutePath());

            context.addRunnerParameter(HubConstantValues.HUB_URL, testProperties.getProperty("TEST_HUB_SERVER_URL"));
            context.addRunnerParameter(HubConstantValues.HUB_USERNAME, testProperties.getProperty("TEST_USERNAME"));
            context.addRunnerParameter(HubConstantValues.HUB_PASSWORD, PasswordEncrypter.publicEncrypt(testProperties.getProperty("TEST_PASSWORD")));

            context.addRunnerParameter(HubConstantValues.HUB_PROXY_HOST, testProperties.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));
            context.addRunnerParameter(HubConstantValues.HUB_PROXY_PORT, testProperties.getProperty("TEST_PROXY_PORT_PASSTHROUGH"));
            context.addRunnerParameter(HubConstantValues.HUB_NO_PROXY_HOSTS, "ignoreHost, otherhost");

            context.addRunnerParameter(HubConstantValues.HUB_PROJECT_NAME, testProperties.getProperty("TEST_PROJECT"));
            context.addRunnerParameter(HubConstantValues.HUB_PROJECT_VERSION, testProperties.getProperty("TEST_VERSION"));
            context.addRunnerParameter(HubConstantValues.HUB_VERSION_PHASE, PhaseEnum.DEVELOPMENT.getDisplayValue());
            context.addRunnerParameter(HubConstantValues.HUB_VERSION_DISTRIBUTION, DistributionEnum.INTERNAL.getDisplayValue());
            context.addRunnerParameter(HubConstantValues.HUB_SCAN_MEMORY, "4096");

            context.addEnvironmentVariable("JAVA_HOME", System.getProperty("java.home"));
            TestBuildAgentConfiguration agentConfig = new TestBuildAgentConfiguration();
            agentConfig.setAgentToolsDirectory(new File(workingDirectory, "tools"));
            TestAgentRunningBuild build = new TestAgentRunningBuild();
            build.setAgentConfiguration(agentConfig);
            context.setBuild(build);
            build.setLogger(testLogger);

            HubBuildProcess process = new HubBuildProcess(build, context);
            BuildFinishedStatus result = process.call();

            String output = testLogger.getErrorMessagesString();

            assertTrue(output, !output.contains("There is no Server URL specified"));
            assertTrue(output, !output.contains("There is no Hub username specified"));
            assertTrue(output, !output.contains("There is no Hub password specified."));

            assertTrue(output, !output.contains("There is no memory specified for the Hub scan. The scan requires a minimum of 4096 MB."));
            assertTrue(output, StringUtils.isBlank(output));

            String progressOutput = testLogger.getProgressMessagesString();

            assertTrue(progressOutput, progressOutput.contains("--> Hub Server Url : " + testProperties.getProperty("TEST_HUB_SERVER_URL")));
            assertTrue(progressOutput, progressOutput.contains("--> Hub User : " + testProperties.getProperty("TEST_USERNAME")));
            assertTrue(progressOutput, progressOutput.contains("--> Proxy Host :"));
            assertTrue(progressOutput, progressOutput.contains("--> Proxy Port :"));
            assertTrue(progressOutput, progressOutput.contains("--> No Proxy Hosts :"));
            assertTrue(progressOutput, !progressOutput.contains("--> Proxy Username :"));

            assertTrue(progressOutput, progressOutput.contains("Working directory : "));
            assertTrue(progressOutput, progressOutput.contains("--> Project : " + testProperties.getProperty("TEST_PROJECT")));
            assertTrue(progressOutput, progressOutput.contains("--> Version : " + testProperties.getProperty("TEST_VERSION")));
            assertTrue(progressOutput, progressOutput.contains("--> Version Phase : " + PhaseEnum.DEVELOPMENT.getDisplayValue()));
            assertTrue(progressOutput, progressOutput.contains("--> Version Distribution : " + DistributionEnum.INTERNAL.getDisplayValue()));
            assertTrue(progressOutput, progressOutput.contains("--> Hub scan memory : 4096"));
            assertTrue(progressOutput, progressOutput.contains("--> Hub scan targets : "));

            assertTrue(progressOutput, progressOutput.contains("Hub CLI command"));
            assertTrue(progressOutput, progressOutput.contains("-Done-jar.silent"));
            assertTrue(progressOutput, progressOutput.contains("-Done-jar.jar.path"));
            assertTrue(progressOutput, progressOutput.contains("-Dhttp.proxyHost"));
            assertTrue(progressOutput, progressOutput.contains("-Dhttp.proxyPort"));
            assertTrue(progressOutput, progressOutput.contains("-Xmx"));
            assertTrue(progressOutput, progressOutput.contains("-jar"));
            assertTrue(progressOutput, progressOutput.contains("--scheme"));
            assertTrue(progressOutput, progressOutput.contains("--host"));
            assertTrue(progressOutput, progressOutput.contains("--username"));
            assertTrue(progressOutput, progressOutput.contains("--password"));
            assertTrue(progressOutput, progressOutput.contains("--port"));
            assertTrue(progressOutput, progressOutput.contains("--logDir"));
            assertTrue(progressOutput, progressOutput.contains("Hub CLI return code"));
            assertTrue(progressOutput, progressOutput.contains("Finished in") && progressOutput.contains("with status SUCCESS"));
            assertTrue(progressOutput, progressOutput.contains("You can view the BlackDuck Scan CLI logs at"));

            assertEquals(BuildFinishedStatus.FINISHED_SUCCESS, result);
            // assertTrue(progressOutput,
            // progressOutput.contains("Waiting a few seconds for the scans to be recognized by the Hub server."));
            // assertTrue(progressOutput, progressOutput.contains("Checking for the scan location with Host name"));
            // assertTrue(progressOutput, progressOutput.contains("MATCHED"));
            // assertTrue(progressOutput, progressOutput.contains("These scan Id's were found for the scan targets."));
            // assertTrue(progressOutput, progressOutput.contains("Mapping the scan location with id:"));
            // assertTrue(progressOutput, progressOutput.contains("Asset reference mapping object :"));
            // assertTrue(progressOutput, progressOutput.contains("Successfully mapped the scan with id"));
        } finally {
            try {
                ProjectItem project = restHelper.getProjectByName(testProperties.getProperty("TEST_PROJECT"));
                if (project != null && project.getId() != null) {
                    restHelper.deleteHubProject(project.getId());
                }
            } catch (ProjectDoesNotExistException e) {
                // ignore this one
            }

        }
    }

    // @Test
    // public void testCallFullyConfiguredBasicAuthProxy() throws Exception {
    // try {
    // TestBuildRunnerContext context = new TestBuildRunnerContext();
    // context.setWorkingDirectory(workingDirectory);
    //
    // context.addEnvironmentVariable(HubConstantValues.HUB_CLI_ENV_VAR, (new File(workingDirectory,
    // "scan.cli-2.1.2")).getAbsolutePath());
    //
    // context.addRunnerParameter(HubConstantValues.HUB_URL, testProperties.getProperty("TEST_HUB_SERVER_URL"));
    // context.addRunnerParameter(HubConstantValues.HUB_USERNAME, testProperties.getProperty("TEST_USERNAME"));
    // context.addRunnerParameter(HubConstantValues.HUB_PASSWORD,
    // PasswordEncrypter.publicEncrypt(testProperties.getProperty("TEST_PASSWORD")));
    //
    // context.addRunnerParameter(HubConstantValues.HUB_PROXY_HOST,
    // testProperties.getProperty("TEST_PROXY_HOST_BASIC"));
    // context.addRunnerParameter(HubConstantValues.HUB_PROXY_PORT,
    // testProperties.getProperty("TEST_PROXY_PORT_BASIC"));
    // context.addRunnerParameter(HubConstantValues.HUB_NO_PROXY_HOSTS, "ignoreHost, otherhost");
    // context.addRunnerParameter(HubConstantValues.HUB_PROXY_USER,
    // testProperties.getProperty("TEST_PROXY_USER_BASIC"));
    // context.addRunnerParameter(HubConstantValues.HUB_PROXY_PASS,
    // testProperties.getProperty("TEST_PROXY_PASSWORD_BASIC"));
    //
    // context.addRunnerParameter(HubConstantValues.HUB_CLI_PATH, (new File(workingDirectory,
    // "scan.cli-2.1.2")).getAbsolutePath());
    // context.addRunnerParameter(HubConstantValues.HUB_PROJECT_NAME, testProperties.getProperty("TEST_PROJECT"));
    // context.addRunnerParameter(HubConstantValues.HUB_PROJECT_VERSION, testProperties.getProperty("TEST_VERSION"));
    // context.addRunnerParameter(HubConstantValues.HUB_VERSION_PHASE, PhaseEnum.DEVELOPMENT.name());
    // context.addRunnerParameter(HubConstantValues.HUB_VERSION_DISTRIBUTION, DistributionEnum.INTERNAL.name());
    // context.addRunnerParameter(HubConstantValues.HUB_SCAN_MEMORY, "4096");
    //
    // context.addEnvironmentVariable("JAVA_HOME", System.getProperty("java.home"));
    //
    // TestAgentRunningBuild build = new TestAgentRunningBuild();
    // context.setBuild(build);
    // build.setLogger(testLogger);
    //
    // HubBuildProcess process = new HubBuildProcess(build, context);
    //
    // assertEquals(BuildFinishedStatus.FINISHED_SUCCESS, process.call());
    //
    // String output = testLogger.getErrorMessagesString();
    //
    // assertTrue(output, !output.contains("There is no Server URL specified"));
    // assertTrue(output, !output.contains("There is no Hub username specified"));
    // assertTrue(output, !output.contains("There is no Hub password specified."));
    //
    // assertTrue(output,
    // !output.contains("There is no memory specified for the Hub scan. The scan requires a minimum of 4096 MB."));
    // assertTrue(output, !output.contains("The Hub CLI path has not been set."));
    //
    // String progressOutput = testLogger.getProgressMessagesString();
    //
    // assertTrue(progressOutput, progressOutput.contains("--> Hub Server Url : " +
    // testProperties.getProperty("TEST_HUB_SERVER_URL")));
    // assertTrue(progressOutput, progressOutput.contains("--> Hub User : " +
    // testProperties.getProperty("TEST_USERNAME")));
    // assertTrue(progressOutput, progressOutput.contains("--> Proxy Host :"));
    // assertTrue(progressOutput, progressOutput.contains("--> Proxy Port :"));
    // assertTrue(progressOutput, progressOutput.contains("--> No Proxy Hosts :"));
    // assertTrue(progressOutput, progressOutput.contains("--> Proxy Username :"));
    //
    // assertTrue(progressOutput, progressOutput.contains("Working directory : "));
    // assertTrue(progressOutput, progressOutput.contains("--> Project : " +
    // testProperties.getProperty("TEST_PROJECT")));
    // assertTrue(progressOutput, progressOutput.contains("--> Version : " +
    // testProperties.getProperty("TEST_VERSION")));
    // assertTrue(progressOutput, progressOutput.contains("--> Version Phase : " + PhaseEnum.DEVELOPMENT.name()));
    // assertTrue(progressOutput, progressOutput.contains("--> Version Distribution : " +
    // DistributionEnum.INTERNAL.name()));
    // assertTrue(progressOutput, progressOutput.contains("--> Hub scan memory : 4096"));
    // assertTrue(progressOutput, progressOutput.contains("--> Hub scan targets : "));
    // assertTrue(progressOutput, progressOutput.contains("--> CLI Path : "));
    // } finally {
    // ProjectItem project = restHelper.getProjectByName(testProperties.getProperty("TEST_PROJECT"));
    // if (project != null && project.getId() != null) {
    // restHelper.deleteHubProject(project.getId());
    // }
    // }
    // }
}
