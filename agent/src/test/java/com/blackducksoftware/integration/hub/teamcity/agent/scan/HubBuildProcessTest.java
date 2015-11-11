package com.blackducksoftware.integration.hub.teamcity.agent.scan;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.teamcity.agent.HubAgentBuildLogger;
import com.blackducksoftware.integration.hub.teamcity.agent.util.TestAgentRunningBuild;
import com.blackducksoftware.integration.hub.teamcity.agent.util.TestBuildProgressLogger;
import com.blackducksoftware.integration.hub.teamcity.agent.util.TestBuildRunnerContext;
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
}
