package com.blackducksoftware.integration.hub.teamcity.agent.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import jetbrains.buildServer.agent.BuildFinishedStatus;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.teamcity.agent.HubAgentBuildLogger;
import com.blackducksoftware.integration.hub.teamcity.agent.util.TestAgentRunningBuild;
import com.blackducksoftware.integration.hub.teamcity.agent.util.TestBuildProgressLogger;
import com.blackducksoftware.integration.hub.teamcity.agent.util.TestBuildRunnerContext;
import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubCredentialsBean;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubProxyInfo;

public class HubBuildProcessTest {
    private static Properties testProperties;

    private static HubAgentBuildLogger logger;

    private static TestBuildProgressLogger testLogger;

    private static File testEmptyDirectory;

    private static File testSourceFile;

    private static File workingDirectory;

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

        process.printGlobalConfguration(null, null, null);

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

        process.printGlobalConfguration("testUrl", new HubCredentialsBean("testUser", "testPassword"), proxyInfo);

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

        process.printGlobalConfguration("testUrl", new HubCredentialsBean("testUser", "testPassword"), proxyInfo);

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

        process.printJobConfguration(null, null, null, null, null, null, null, null);

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

        process.printJobConfguration("testProject", "testVersion",
                "testPhase", "testDistribution", scanTargets,
                "workingDirPath", "scan Memory", testTarget);

        String output = testLogger.getProgressMessagesString();
        assertTrue(output, output.contains("Working directory : "));
        assertTrue(output, output.contains("--> Project : "));
        assertTrue(output, output.contains("--> Version : "));
        assertTrue(output, output.contains("--> Version Phase : "));
        assertTrue(output, output.contains("--> Version Distribution : "));
        assertTrue(output, output.contains("--> Hub scan memory : "));

        assertTrue(output, output.contains("--> Hub scan targets : "));
        assertTrue(output, output.contains("--> " + testTarget.getAbsolutePath()));
        assertTrue(output, output.contains("--> CLI Path : " + testTarget.getAbsolutePath()));
    }

    @Test
    public void testPrintJobConfigurationEmptyScanTargets() {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        List<File> scanTargets = new ArrayList<File>();

        process.printJobConfguration(null, null, null, null, scanTargets, null, null, null);

        String output = testLogger.getProgressMessagesString();
        assertTrue(output, !output.contains("--> Hub scan targets : "));
    }

    @Test
    public void testIsGlobalConfigValidNull() throws Exception {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        assertTrue(!process.isGlobalConfigValid(null, null));

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("There is no Server URL specified"));
        assertTrue(output, output.contains("There are no credentials configured."));
    }

    @Test
    public void testIsGlobalConfigValidEmptyCredentials() throws Exception {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        assertTrue(!process.isGlobalConfigValid("TestUrl", new HubCredentialsBean("")));

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("There is no Hub username specified"));
        assertTrue(output, output.contains("There is no Hub password specified."));
    }

    @Test
    public void testIsGlobalConfigValid() throws Exception {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        boolean validConfig = process.isGlobalConfigValid("TestUrl", new HubCredentialsBean("TestUser", "TestPass"));

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

        assertTrue(!process.isJobConfigValid(null, null, null, null));

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("There is no memory specified for the Hub scan."));
        assertTrue(output, output.contains("The Hub CLI path has not been set."));
        assertTrue(output, output.contains("No scan targets configured."));
    }

    @Test
    public void testIsJobConfigValidInvalid() throws Exception {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        List<File> scanTargets = new ArrayList<File>();

        scanTargets.add(new File(testSourceFile, "emptyFile.txt"));
        scanTargets.add(new File("fakeOutsideWorkspace"));

        assertTrue(!process.isJobConfigValid(scanTargets, workingDirectory.getAbsolutePath(), "23", new File("")));

        String output = testLogger.getErrorMessagesString();
        assertTrue(output, output.contains("Can not scan targets outside the working directory."));
        assertTrue(output, output.contains("The Hub scan requires at least 4096 MB of memory."));
        assertTrue(output, output.contains("The Hub CLI home directory does not exist at"));
    }

    @Test
    public void testIsJobConfigValidTargetNotExisting() throws Exception {
        HubBuildProcess process = new HubBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext());
        process.setHubLogger(logger);

        List<File> scanTargets = new ArrayList<File>();

        scanTargets.add(new File(testSourceFile, "fakeFile"));

        assertTrue(!process.isJobConfigValid(scanTargets, workingDirectory.getAbsolutePath(), "4096", new File(workingDirectory, "scan.cli-2.1.2")));

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

        boolean validJobConfig = process
                .isJobConfigValid(scanTargets, workingDirectory.getAbsolutePath(), "4096", new File(workingDirectory, "scan.cli-2.1.2"));

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
        assertTrue(output, !output.contains("The Hub CLI path has not been set."));

        String progressOutput = testLogger.getProgressMessagesString();

        assertTrue(progressOutput, progressOutput.contains("Skipping Hub Build Step"));
    }

    @Test
    public void testCallGlobalConfigured() throws Exception {
        TestBuildRunnerContext context = new TestBuildRunnerContext();
        context.setWorkingDirectory(workingDirectory);

        context.addRunnerParameter(HubConstantValues.HUB_URL, "testUrl");
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
        assertTrue(output, output.contains("The Hub CLI path has not been set."));

        String progressOutput = testLogger.getProgressMessagesString();

        assertTrue(progressOutput, progressOutput.contains("Skipping Hub Build Step"));
    }

    @Test
    public void testCallJobPartiallyConfiguredCLIEnvVarSet() throws Exception {
        TestBuildRunnerContext context = new TestBuildRunnerContext();
        context.setWorkingDirectory(workingDirectory);

        context.addEnvironmentVariable(HubConstantValues.HUB_CLI_ENV_VAR, (new File(workingDirectory, "scan.cli-2.1.2")).getAbsolutePath());

        context.addRunnerParameter(HubConstantValues.HUB_URL, "testUrl");
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
        TestBuildRunnerContext context = new TestBuildRunnerContext();
        context.setWorkingDirectory(workingDirectory);

        context.addRunnerParameter(HubConstantValues.HUB_URL, "testUrl");
        context.addRunnerParameter(HubConstantValues.HUB_USERNAME, "testUser");
        context.addRunnerParameter(HubConstantValues.HUB_PASSWORD, "testPassword");

        context.addRunnerParameter(HubConstantValues.HUB_CLI_PATH, (new File(workingDirectory, "scan.cli-2.1.2")).getAbsolutePath());
        context.addRunnerParameter(HubConstantValues.HUB_PROJECT_NAME, "testProject");
        context.addRunnerParameter(HubConstantValues.HUB_PROJECT_VERSION, "testVersion");
        context.addRunnerParameter(HubConstantValues.HUB_VERSION_PHASE, "phase");
        context.addRunnerParameter(HubConstantValues.HUB_VERSION_DISTRIBUTION, "dist");
        context.addRunnerParameter(HubConstantValues.HUB_SCAN_MEMORY, "4096");

        context.addRunnerParameter(HubConstantValues.HUB_SCAN_TARGETS, "directory/emptyFile.txt" + System.getProperty("line.separator")
                + "directory/secondEmptyFile.txt");

        TestAgentRunningBuild build = new TestAgentRunningBuild();
        build.setLogger(testLogger);

        HubBuildProcess process = new HubBuildProcess(build, context);

        assertEquals(BuildFinishedStatus.FINISHED_SUCCESS, process.call());

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, !output.contains("There is no Server URL specified"));
        assertTrue(output, !output.contains("There is no Hub username specified"));
        assertTrue(output, !output.contains("There is no Hub password specified."));

        assertTrue(output, !output.contains("There is no memory specified for the Hub scan. The scan requires a minimum of 4096 MB."));
        assertTrue(output, !output.contains("The Hub CLI path has not been set."));

        String progressOutput = testLogger.getProgressMessagesString();

        assertTrue(progressOutput, progressOutput.contains("--> Hub Server Url : testUrl"));
        assertTrue(progressOutput, progressOutput.contains("--> Hub User : testUser"));
        assertTrue(progressOutput, !progressOutput.contains("--> Proxy Host :"));
        assertTrue(progressOutput, !progressOutput.contains("--> Proxy Port :"));
        assertTrue(progressOutput, !progressOutput.contains("--> No Proxy Hosts :"));
        assertTrue(progressOutput, !progressOutput.contains("--> Proxy Username :"));

        assertTrue(progressOutput, progressOutput.contains("Working directory : "));
        assertTrue(progressOutput, progressOutput.contains("--> Project : testProject"));
        assertTrue(progressOutput, progressOutput.contains("--> Version : testVersion"));
        assertTrue(progressOutput, progressOutput.contains("--> Version Phase : phase"));
        assertTrue(progressOutput, progressOutput.contains("--> Version Distribution : dist"));
        assertTrue(progressOutput, progressOutput.contains("--> Hub scan memory : 4096"));
        assertTrue(progressOutput, progressOutput.contains("--> Hub scan targets : "));
        assertTrue(progressOutput, progressOutput.contains("--> CLI Path : "));
    }

    @Test
    public void testCallFullyConfiguredPassThroughProxyProxyIgnored() throws Exception {
        TestBuildRunnerContext context = new TestBuildRunnerContext();
        context.setWorkingDirectory(workingDirectory);

        context.addRunnerParameter(HubConstantValues.HUB_URL, "testUrl");
        context.addRunnerParameter(HubConstantValues.HUB_USERNAME, "testUser");
        context.addRunnerParameter(HubConstantValues.HUB_PASSWORD, "testPassword");

        context.addRunnerParameter(HubConstantValues.HUB_PROXY_HOST, "testProxyHost");
        context.addRunnerParameter(HubConstantValues.HUB_PROXY_PORT, "3128");
        context.addRunnerParameter(HubConstantValues.HUB_NO_PROXY_HOSTS, "ignoreHost, testProxyHost");

        context.addRunnerParameter(HubConstantValues.HUB_CLI_PATH, (new File(workingDirectory, "scan.cli-2.1.2")).getAbsolutePath());
        context.addRunnerParameter(HubConstantValues.HUB_PROJECT_NAME, "testProject");
        context.addRunnerParameter(HubConstantValues.HUB_PROJECT_VERSION, "testVersion");
        context.addRunnerParameter(HubConstantValues.HUB_VERSION_PHASE, "phase");
        context.addRunnerParameter(HubConstantValues.HUB_VERSION_DISTRIBUTION, "dist");
        context.addRunnerParameter(HubConstantValues.HUB_SCAN_MEMORY, "4096");

        context.addRunnerParameter(HubConstantValues.HUB_SCAN_TARGETS, "directory/emptyFile.txt");

        TestAgentRunningBuild build = new TestAgentRunningBuild();
        build.setLogger(testLogger);

        HubBuildProcess process = new HubBuildProcess(build, context);

        assertEquals(BuildFinishedStatus.FINISHED_SUCCESS, process.call());

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, !output.contains("There is no Server URL specified"));
        assertTrue(output, !output.contains("There is no Hub username specified"));
        assertTrue(output, !output.contains("There is no Hub password specified."));

        assertTrue(output, !output.contains("There is no memory specified for the Hub scan. The scan requires a minimum of 4096 MB."));
        assertTrue(output, !output.contains("The Hub CLI path has not been set."));

        String progressOutput = testLogger.getProgressMessagesString();

        assertTrue(progressOutput, progressOutput.contains("--> Hub Server Url : testUrl"));
        assertTrue(progressOutput, progressOutput.contains("--> Hub User : testUser"));
        assertTrue(progressOutput, progressOutput.contains("--> Proxy Host :"));
        assertTrue(progressOutput, progressOutput.contains("--> Proxy Port :"));
        assertTrue(progressOutput, progressOutput.contains("--> No Proxy Hosts :"));
        assertTrue(progressOutput, !progressOutput.contains("--> Proxy Username :"));

        assertTrue(progressOutput, progressOutput.contains("Working directory : "));
        assertTrue(progressOutput, progressOutput.contains("--> Project : testProject"));
        assertTrue(progressOutput, progressOutput.contains("--> Version : testVersion"));
        assertTrue(progressOutput, progressOutput.contains("--> Version Phase : phase"));
        assertTrue(progressOutput, progressOutput.contains("--> Version Distribution : dist"));
        assertTrue(progressOutput, progressOutput.contains("--> Hub scan memory : 4096"));
        assertTrue(progressOutput, progressOutput.contains("--> Hub scan targets : "));
        assertTrue(progressOutput, progressOutput.contains("--> CLI Path : "));
    }

    @Test
    public void testCallFullyConfiguredPassThroughProxy() throws Exception {
        TestBuildRunnerContext context = new TestBuildRunnerContext();
        context.setWorkingDirectory(workingDirectory);

        context.addEnvironmentVariable(HubConstantValues.HUB_CLI_ENV_VAR, (new File(workingDirectory, "scan.cli-2.1.2")).getAbsolutePath());

        context.addRunnerParameter(HubConstantValues.HUB_URL, "testUrl");
        context.addRunnerParameter(HubConstantValues.HUB_USERNAME, "testUser");
        context.addRunnerParameter(HubConstantValues.HUB_PASSWORD, "testPassword");

        context.addRunnerParameter(HubConstantValues.HUB_PROXY_HOST, "testProxyHost");
        context.addRunnerParameter(HubConstantValues.HUB_PROXY_PORT, "3130");
        context.addRunnerParameter(HubConstantValues.HUB_NO_PROXY_HOSTS, "ignoreHost, otherhost");

        context.addRunnerParameter(HubConstantValues.HUB_CLI_PATH, (new File(workingDirectory, "scan.cli-2.1.2")).getAbsolutePath());
        context.addRunnerParameter(HubConstantValues.HUB_PROJECT_NAME, "testProject");
        context.addRunnerParameter(HubConstantValues.HUB_PROJECT_VERSION, "testVersion");
        context.addRunnerParameter(HubConstantValues.HUB_VERSION_PHASE, "phase");
        context.addRunnerParameter(HubConstantValues.HUB_VERSION_DISTRIBUTION, "dist");
        context.addRunnerParameter(HubConstantValues.HUB_SCAN_MEMORY, "4096");

        TestAgentRunningBuild build = new TestAgentRunningBuild();
        build.setLogger(testLogger);

        HubBuildProcess process = new HubBuildProcess(build, context);

        assertEquals(BuildFinishedStatus.FINISHED_SUCCESS, process.call());

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, !output.contains("There is no Server URL specified"));
        assertTrue(output, !output.contains("There is no Hub username specified"));
        assertTrue(output, !output.contains("There is no Hub password specified."));

        assertTrue(output, !output.contains("There is no memory specified for the Hub scan. The scan requires a minimum of 4096 MB."));
        assertTrue(output, !output.contains("The Hub CLI path has not been set."));

        String progressOutput = testLogger.getProgressMessagesString();

        assertTrue(progressOutput, progressOutput.contains("--> Hub Server Url : testUrl"));
        assertTrue(progressOutput, progressOutput.contains("--> Hub User : testUser"));
        assertTrue(progressOutput, progressOutput.contains("--> Proxy Host :"));
        assertTrue(progressOutput, progressOutput.contains("--> Proxy Port :"));
        assertTrue(progressOutput, progressOutput.contains("--> No Proxy Hosts :"));
        assertTrue(progressOutput, !progressOutput.contains("--> Proxy Username :"));

        assertTrue(progressOutput, progressOutput.contains("Working directory : "));
        assertTrue(progressOutput, progressOutput.contains("--> Project : testProject"));
        assertTrue(progressOutput, progressOutput.contains("--> Version : testVersion"));
        assertTrue(progressOutput, progressOutput.contains("--> Version Phase : phase"));
        assertTrue(progressOutput, progressOutput.contains("--> Version Distribution : dist"));
        assertTrue(progressOutput, progressOutput.contains("--> Hub scan memory : 4096"));
        assertTrue(progressOutput, progressOutput.contains("--> Hub scan targets : "));
        assertTrue(progressOutput, progressOutput.contains("--> CLI Path : "));
    }

    @Test
    public void testCallFullyConfiguredAuthenticatedProxy() throws Exception {
        TestBuildRunnerContext context = new TestBuildRunnerContext();
        context.setWorkingDirectory(workingDirectory);

        context.addEnvironmentVariable(HubConstantValues.HUB_CLI_ENV_VAR, (new File(workingDirectory, "scan.cli-2.1.2")).getAbsolutePath());

        context.addRunnerParameter(HubConstantValues.HUB_URL, "testUrl");
        context.addRunnerParameter(HubConstantValues.HUB_USERNAME, "testUser");
        context.addRunnerParameter(HubConstantValues.HUB_PASSWORD, "testPassword");

        context.addRunnerParameter(HubConstantValues.HUB_PROXY_HOST, "testProxyHost");
        context.addRunnerParameter(HubConstantValues.HUB_PROXY_PORT, "3130");
        context.addRunnerParameter(HubConstantValues.HUB_NO_PROXY_HOSTS, "ignoreHost, otherhost");
        context.addRunnerParameter(HubConstantValues.HUB_PROXY_USER, "testProxyUser");
        context.addRunnerParameter(HubConstantValues.HUB_PROXY_PASS, "testProxyPass");

        context.addRunnerParameter(HubConstantValues.HUB_CLI_PATH, (new File(workingDirectory, "scan.cli-2.1.2")).getAbsolutePath());
        context.addRunnerParameter(HubConstantValues.HUB_PROJECT_NAME, "testProject");
        context.addRunnerParameter(HubConstantValues.HUB_PROJECT_VERSION, "testVersion");
        context.addRunnerParameter(HubConstantValues.HUB_VERSION_PHASE, "phase");
        context.addRunnerParameter(HubConstantValues.HUB_VERSION_DISTRIBUTION, "dist");
        context.addRunnerParameter(HubConstantValues.HUB_SCAN_MEMORY, "4096");

        TestAgentRunningBuild build = new TestAgentRunningBuild();
        build.setLogger(testLogger);

        HubBuildProcess process = new HubBuildProcess(build, context);

        assertEquals(BuildFinishedStatus.FINISHED_SUCCESS, process.call());

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, !output.contains("There is no Server URL specified"));
        assertTrue(output, !output.contains("There is no Hub username specified"));
        assertTrue(output, !output.contains("There is no Hub password specified."));

        assertTrue(output, !output.contains("There is no memory specified for the Hub scan. The scan requires a minimum of 4096 MB."));
        assertTrue(output, !output.contains("The Hub CLI path has not been set."));

        String progressOutput = testLogger.getProgressMessagesString();

        assertTrue(progressOutput, progressOutput.contains("--> Hub Server Url : testUrl"));
        assertTrue(progressOutput, progressOutput.contains("--> Hub User : testUser"));
        assertTrue(progressOutput, progressOutput.contains("--> Proxy Host :"));
        assertTrue(progressOutput, progressOutput.contains("--> Proxy Port :"));
        assertTrue(progressOutput, progressOutput.contains("--> No Proxy Hosts :"));
        assertTrue(progressOutput, progressOutput.contains("--> Proxy Username :"));

        assertTrue(progressOutput, progressOutput.contains("Working directory : "));
        assertTrue(progressOutput, progressOutput.contains("--> Project : testProject"));
        assertTrue(progressOutput, progressOutput.contains("--> Version : testVersion"));
        assertTrue(progressOutput, progressOutput.contains("--> Version Phase : phase"));
        assertTrue(progressOutput, progressOutput.contains("--> Version Distribution : dist"));
        assertTrue(progressOutput, progressOutput.contains("--> Hub scan memory : 4096"));
        assertTrue(progressOutput, progressOutput.contains("--> Hub scan targets : "));
        assertTrue(progressOutput, progressOutput.contains("--> CLI Path : "));
    }
}
