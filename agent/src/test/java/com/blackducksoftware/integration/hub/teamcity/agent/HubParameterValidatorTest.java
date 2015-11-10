package com.blackducksoftware.integration.hub.teamcity.agent;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.teamcity.agent.util.TestBuildProgressLogger;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubCredentialsBean;

public class HubParameterValidatorTest {

    private static String testWorkspace;

    @BeforeClass
    public static void testStartup() {
        testWorkspace = HubParameterValidatorTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        testWorkspace = testWorkspace.substring(0, testWorkspace.indexOf("/target"));
        testWorkspace = testWorkspace + "/test-workspace";
    }

    @Test
    public void testConstructorNoLogger() {
        assertNotNull(new HubParameterValidator(new HubAgentBuildLogger(null)));
    }

    @Test
    public void testConstructorWithLogger() {
        assertNotNull(new HubParameterValidator(new HubAgentBuildLogger(new TestBuildProgressLogger())));
    }

    @Test
    public void testIsServerUrlEmptyNoUrl() {
        TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        HubAgentBuildLogger buildLogger = new HubAgentBuildLogger(testLogger);
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        assertTrue(validator.isServerUrlEmpty(null));

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("There is no Server URL specified"));
    }

    @Test
    public void testIsServerUrlEmptyBlankUrl() {
        TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        HubAgentBuildLogger buildLogger = new HubAgentBuildLogger(testLogger);
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        assertTrue(validator.isServerUrlEmpty(""));

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("There is no Server URL specified"));
    }

    @Test
    public void testIsServerUrlEmptyUrlNotEmpty() {
        TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        HubAgentBuildLogger buildLogger = new HubAgentBuildLogger(testLogger);
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        assertTrue(!validator.isServerUrlEmpty("testUrl"));
        assertTrue(testLogger.getErrorMessages().size() == 0);
    }

    @Test
    public void testIsHubCredentialConfiguredEmptyCredentials() {
        TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        HubAgentBuildLogger buildLogger = new HubAgentBuildLogger(testLogger);
        HubParameterValidator validator = new HubParameterValidator(buildLogger);
        assertTrue(!validator.isHubCredentialConfigured(new HubCredentialsBean("")));

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("There is no Hub username specified"));
        assertTrue(output, output.contains("There is no Hub password specified."));
    }

    @Test
    public void testIsHubCredentialConfiguredValidCredentials() {
        TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        HubAgentBuildLogger buildLogger = new HubAgentBuildLogger(testLogger);
        HubParameterValidator validator = new HubParameterValidator(buildLogger);
        HubCredentialsBean credential = new HubCredentialsBean("user", "password");
        assertTrue(validator.isHubCredentialConfigured(credential));
        assertTrue(testLogger.getErrorMessages().size() == 0);
    }

    @Test
    public void testValidateSourcePathOutsideWorkingDirectory() throws Exception {
        TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        HubAgentBuildLogger buildLogger = new HubAgentBuildLogger(testLogger);
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        assertTrue(!validator.validateTargetPath(new File(""), testWorkspace));

        String output = testLogger.getErrorMessagesString();
        assertTrue(output, output.contains("Can not scan targets outside the working directory."));
    }

    @Test
    public void testValidateSourcePathTargetNonExistent() throws Exception {
        TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        HubAgentBuildLogger buildLogger = new HubAgentBuildLogger(testLogger);
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        String sourcePath = testWorkspace + "/fakeDirectory";
        assertTrue(!validator.validateTargetPath(new File(sourcePath), testWorkspace));

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("The scan target '" + sourcePath + "' does not exist."));
    }

    @Test
    public void testValidateSourcePathValid() throws Exception {
        TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        HubAgentBuildLogger buildLogger = new HubAgentBuildLogger(testLogger);
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        String sourcePath = testWorkspace + "/directory";

        File sourceTarget = new File(sourcePath);
        if (!sourceTarget.exists()) {
            sourceTarget.mkdirs();
        }

        boolean validSourcePath = validator.validateTargetPath(new File(sourcePath), testWorkspace);
        if (!validSourcePath) {
            if (testLogger.getErrorMessages().size() != 0) {
                for (String error : testLogger.getErrorMessages()) {
                    System.out.print(error);
                }
            }
            fail();
        } else {
            assertTrue(validSourcePath);
            assertTrue(testLogger.getErrorMessages().size() == 0);
        }
    }

    @Test
    public void testValidateScanMemory() throws Exception {
        TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        HubAgentBuildLogger buildLogger = new HubAgentBuildLogger(testLogger);
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        assertTrue(!validator.validateScanMemory(null));
        String output = testLogger.getErrorMessagesString();
        assertTrue(output, output.contains("There is no memory specified for the Hub scan. The scan requires a minimum of 4096 MB."));
        testLogger.clearErrorMessages();

        assertTrue(!validator.validateScanMemory(""));
        output = testLogger.getErrorMessagesString();
        assertTrue(output, output.contains("There is no memory specified for the Hub scan. The scan requires a minimum of 4096 MB."));
        testLogger.clearErrorMessages();

        assertTrue(!validator.validateScanMemory("   "));
        output = testLogger.getErrorMessagesString();
        assertTrue(output, output.contains("There is no memory specified for the Hub scan. The scan requires a minimum of 4096 MB."));
        testLogger.clearErrorMessages();

        assertTrue(!validator.validateScanMemory("506"));
        output = testLogger.getErrorMessagesString();
        assertTrue(output, output.contains("The Hub scan requires at least 4096 MB of memory."));
        testLogger.clearErrorMessages();

        // assertTrue(!validator.validateScanMemory("20.1"));
        // output = testLogger.getErrorMessagesString();
        // assertTrue(output, output.contains("Should not specify this much memory for the Hub Scan : 20.1 GB"));
        // testLogger.clearErrorMessages();

        assertTrue(!validator.validateScanMemory("two"));
        output = testLogger.getErrorMessagesString();
        assertTrue(output, output.contains("The amount of memory provided must be in the form of an Integer. Ex: 4096, 4608, etc."));
        testLogger.clearErrorMessages();

        assertTrue(validator.validateScanMemory("5069"));

        assertTrue(validator.validateScanMemory("4096"));
    }
}
