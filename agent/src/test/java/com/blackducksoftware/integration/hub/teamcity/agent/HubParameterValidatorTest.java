package com.blackducksoftware.integration.hub.teamcity.agent;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.blackducksoftware.integration.hub.teamcity.agent.util.TestBuildProgressLogger;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubCredentialsBean;

public class HubParameterValidatorTest {

    private static String testWorkspace;

    private static TestBuildProgressLogger testLogger;

    private static HubAgentBuildLogger buildLogger;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void testStartup() {
        testWorkspace = HubParameterValidatorTest.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        testWorkspace = testWorkspace.substring(0, testWorkspace.indexOf("/target"));
        testWorkspace = testWorkspace + "/test-workspace";

        testLogger = new TestBuildProgressLogger();
        buildLogger = new HubAgentBuildLogger(testLogger);
    }

    @After
    public void testCleanup() {
        testLogger.clearAllOutput();
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
    public void testIsServerUrlValidNoUrl() {
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        assertTrue(!validator.isServerUrlValid(null));

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("There is no Server URL specified"));
    }

    @Test
    public void testIsServerUrlValidBlankUrl() {
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        assertTrue(!validator.isServerUrlValid(""));

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("There is no Server URL specified"));
    }

    @Test
    public void testIsServerUrlValidUrlInvalid() {
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        assertTrue(!validator.isServerUrlValid("testUrl"));
        String output = testLogger.getErrorMessagesString();
        assertTrue(output, output.contains("The server URL specified is not a valid URL."));
    }

    @Test
    public void testIsServerUrlValidUrlValid() {
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        assertTrue(validator.isServerUrlValid("http://testUrl"));
        assertTrue(testLogger.getErrorMessages().size() == 0);
    }

    @Test
    public void testIsHubCredentialConfiguredEmptyCredentials() {
        HubParameterValidator validator = new HubParameterValidator(buildLogger);
        assertTrue(!validator.isHubCredentialConfigured(new HubCredentialsBean("")));

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("There is no Hub username specified"));
        assertTrue(output, output.contains("There is no Hub password specified."));
    }

    @Test
    public void testIsHubCredentialConfiguredValidCredentials() {
        HubParameterValidator validator = new HubParameterValidator(buildLogger);
        HubCredentialsBean credential = new HubCredentialsBean("user", "password");
        assertTrue(validator.isHubCredentialConfigured(credential));
        assertTrue(testLogger.getErrorMessages().size() == 0);
    }

    @Test
    public void testValidateTargetPathOutsideWorkingDirectory() throws Exception {
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        assertTrue(!validator.validateTargetPath(new File("").getAbsolutePath(), testWorkspace));

        String output = testLogger.getErrorMessagesString();
        assertTrue(output, output.contains("Can not scan targets outside the working directory."));
    }

    @Test
    public void testValidateTargetPathTargetNonExistent() throws Exception {
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        String sourcePath = testWorkspace + "/fakeDirectory";
        assertTrue(!validator.validateTargetPath(new File(sourcePath).getAbsolutePath(), testWorkspace));

        String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("The scan target '" + sourcePath + "' does not exist."));
    }

    @Test
    public void testValidateTargetPathValid() throws Exception {
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        String sourcePath = testWorkspace + "/directory";

        File sourceTarget = new File(sourcePath);
        if (!sourceTarget.exists()) {
            sourceTarget.mkdirs();
        }

        boolean validTargetPath = validator.validateTargetPath(new File(sourcePath).getAbsolutePath(), testWorkspace);
        if (!validTargetPath) {
            if (testLogger.getErrorMessages().size() != 0) {
                for (String error : testLogger.getErrorMessages()) {
                    System.out.print(error);
                }
            }
            fail();
        } else {
            assertTrue(validTargetPath);
            assertTrue(testLogger.getErrorMessages().size() == 0);
        }
    }

    @Test
    public void testValidateProjectNameAndVersionEmpty() {
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        assertTrue(validator.validateProjectNameAndVersion("", ""));
        assertTrue(testLogger.getErrorMessages().size() == 0);

    }

    @Test
    public void testValidateProjectNameAndVersionEmptyProjectName() {
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        assertTrue(!validator.validateProjectNameAndVersion("", "TestVersion"));
        assertTrue(testLogger.getErrorMessages().size() == 1);

    }

    @Test
    public void testValidateProjectNameAndVersionEmptyVersion() {
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        assertTrue(!validator.validateProjectNameAndVersion("TestProject", ""));
        assertTrue(testLogger.getErrorMessages().size() == 1);

    }

    @Test
    public void testValidateProjectNameAndVersion() {
        HubParameterValidator validator = new HubParameterValidator(buildLogger);

        assertTrue(validator.validateProjectNameAndVersion("TestProject", "TestVersion"));
        assertTrue(testLogger.getErrorMessages().size() == 0);

    }

}
