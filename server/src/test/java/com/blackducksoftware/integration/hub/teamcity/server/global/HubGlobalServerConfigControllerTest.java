package com.blackducksoftware.integration.hub.teamcity.server.global;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

import jetbrains.buildServer.log.LogInitializer;
import jetbrains.buildServer.serverSide.ServerPaths;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import com.blackducksoftware.integration.hub.teamcity.mocks.MockServerPaths;

public class HubGlobalServerConfigControllerTest {

    private static Properties testProperties;

    private final static String parentDir = "configController";

    private static PrintStream orgStream = null;

    private static PrintStream orgErrStream = null;

    private static ByteArrayOutputStream byteOutput = null;

    private static PrintStream currStream = null;

    @BeforeClass
    public static void startup() {
        orgStream = System.out;
        orgErrStream = System.err;
        byteOutput = new ByteArrayOutputStream();
        currStream = new PrintStream(byteOutput);
        System.setOut(currStream);
        System.setErr(currStream);

        LogInitializer.setUnitTest(true);
        LogInitializer.addConsoleAppender();
        LogInitializer.initServerLogging();

        testProperties = new Properties();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream is = classLoader.getResourceAsStream("test.properties");

        try {
            testProperties.load(is);

        } catch (IOException e) {
            System.err.println("reading test.properties failed!");
        }
    }

    @Before
    public void testSetup() throws Exception {
        currStream.flush();
        byteOutput.flush();
        byteOutput.reset();
    }

    @AfterClass
    public static void tearDown() {
        System.setOut(orgStream);
        System.setErr(orgErrStream);
    }

    private ServerPaths getMockedServerPaths(final String configDir) {

        return MockServerPaths.getMockedServerPaths(parentDir, configDir);
    }

    private String getConfigDirectory(final String configDir) {

        return MockServerPaths.getConfigDirectory(parentDir, configDir);
    }

    // @Test
    // public void testConstructor() throws IOException {
    // ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
    // ServerProtexConfigPersistenceManager persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // assertNotNull(new ProtexGlobalServerConfigController(persistenceManager));
    // }
    //
    // @Test
    // public void testIsTestConnectionRequest() throws IOException {
    // ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
    // ServerProtexConfigPersistenceManager persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    //
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    //
    // assertTrue(!controller.isTestConnectionRequest(req));
    //
    // MockHttpServletRequest.addGetParameter(req, "testConnection", "false");
    //
    // assertTrue(!controller.isTestConnectionRequest(req));
    //
    // MockHttpServletRequest.addGetParameter(req, "testConnection", "true");
    //
    // assertTrue(controller.isTestConnectionRequest(req));
    //
    // }
    //
    // @Test
    // public void testIsSavingRequest() throws IOException {
    // ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
    // ServerProtexConfigPersistenceManager persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    //
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    //
    // assertTrue(!controller.isSavingRequest(req));
    //
    // MockHttpServletRequest.addGetParameter(req, "saving", "false");
    //
    // assertTrue(!controller.isSavingRequest(req));
    //
    // MockHttpServletRequest.addGetParameter(req, "saving", "true");
    //
    // assertTrue(controller.isSavingRequest(req));
    //
    // }
    //
    // @Test
    // public void testIsSavingServerRequest() throws IOException {
    // ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
    // ServerProtexConfigPersistenceManager persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    //
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    //
    // assertTrue(!controller.isSavingServerRequest(req));
    //
    // MockHttpServletRequest.addGetParameter(req, "savingServer", "false");
    //
    // assertTrue(!controller.isSavingServerRequest(req));
    //
    // MockHttpServletRequest.addGetParameter(req, "savingServer", "true");
    //
    // assertTrue(controller.isSavingServerRequest(req));
    //
    // }
    //
    // @Test
    // public void testIsDeleteServerRequest() throws IOException {
    // ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
    // ServerProtexConfigPersistenceManager persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    //
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    //
    // assertTrue(!controller.isDeleteServerRequest(req));
    //
    // MockHttpServletRequest.addGetParameter(req, "deleteServer", "serverId");
    //
    // assertTrue(controller.isDeleteServerRequest(req));
    //
    // }
    //
    // @Test
    // public void testIsSavingCredentialRequest() throws IOException {
    // ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
    // ServerProtexConfigPersistenceManager persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    //
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    //
    // assertTrue(!controller.isSavingCredentialRequest(req));
    //
    // MockHttpServletRequest.addGetParameter(req, "savingCredential", "false");
    //
    // assertTrue(!controller.isSavingCredentialRequest(req));
    //
    // MockHttpServletRequest.addGetParameter(req, "savingCredential", "true");
    //
    // assertTrue(controller.isSavingCredentialRequest(req));
    //
    // }
    //
    // @Test
    // public void testIsDeleteCredentialRequest() throws IOException {
    // ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
    // ServerProtexConfigPersistenceManager persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    //
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    //
    // assertTrue(!controller.isDeleteCredentialRequest(req));
    //
    // MockHttpServletRequest.addGetParameter(req, "deleteCredential", "credentialId");
    //
    // assertTrue(controller.isDeleteCredentialRequest(req));
    //
    // }
    //
    // @Test
    // public void testDoGet() throws IOException {
    // ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
    // ServerProtexConfigPersistenceManager persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    //
    // assertNull(controller.doGet(null, null));
    // }
    //
    // @Test
    // public void testDoPostNoParameters() throws IOException {
    // ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
    // ServerProtexConfigPersistenceManager persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, false);
    //
    // controller.doPost(req, null, new Element("testElement"));
    // // Nothing should happen
    // }
    //
    // @Test
    // public void testDoPostNoRequestType() throws IOException {
    // ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
    // ServerProtexConfigPersistenceManager persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // controller.doPost(req, null, new Element("testElement"));
    // // Nothing should happen
    // }
    //
    // @Test
    // public void testDoPostUnknownRequestType() throws IOException {
    // ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
    // ServerProtexConfigPersistenceManager persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "fakeRequest", "true");
    //
    // controller.doPost(req, null, new Element("testElement"));
    // // Nothing should happen
    // }
    //
    // @Test
    // public void testDoPostSaveConfigWithErrors() throws IOException {
    // String configDir = "SaveConfig";
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // ServerProtexConfigPersistenceManager persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "saving", "true");
    //
    // MockHttpServletRequest.addGetParameter(req, "protexProxyServer", "fakeProxyHost");
    //
    // MockHttpServletRequest.addGetParameter(req, "protexProxyPort", "2345aSgsgdrfh");
    //
    // MockHttpServletRequest.addGetParameter(req, "protexNoProxyHost", "*.*.8srgasrg/asad?sd/sdg.../?,/m.");
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // Iterator iterator = element.getDescendants();
    // Element errorProtexProxyPort = null;
    // Element errorProtexNoProxyHost = null;
    //
    // while (iterator.hasNext()) {
    // Object descendent = iterator.next();
    // if (descendent instanceof Element) {
    // Element descendentElement = (Element) descendent;
    //
    // String id = descendentElement.getAttributeValue("id");
    // if (StringUtils.isNotBlank(id)) {
    // if (id.equalsIgnoreCase("errorProtexProxyPort")) {
    // errorProtexProxyPort = descendentElement;
    // } else if (id.equalsIgnoreCase("errorProtexNoProxyHost")) {
    // errorProtexNoProxyHost = descendentElement;
    // }
    // }
    // }
    // }
    // assertNotNull(errorProtexProxyPort);
    // assertEquals("Please enter a valid Proxy port.", errorProtexProxyPort.getText());
    // assertNotNull(errorProtexNoProxyHost);
    // String errorString = errorProtexNoProxyHost.getText();
    // assertTrue(errorString, errorString.contains("The host : "));
    // assertTrue(errorString, errorString.contains(" : is not a valid regular expression."));
    //
    // String output = byteOutput.toString();
    // assertTrue(output, output.contains("Saving has errors"));
    // assertTrue(output, output.contains("errorProtexProxyPort : Please enter a valid Proxy port."));
    // assertTrue(output, output.contains("errorProtexNoProxyHost : The host : "));
    //
    // } finally {
    // String configPath = getConfigDirectory(configDir);
    // File config = new File(configPath + File.separator + "protex-config.xml");
    // if (config.exists()) {
    // config.delete();
    // }
    // }
    // }
    //
    // @Test
    // public void testDoPostSaveConfig() throws IOException {
    // String configDir = "SaveConfig";
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // ServerProtexConfigPersistenceManager persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "saving", "true");
    //
    // MockHttpServletRequest.addGetParameter(req, "protexProxyServer", "newProxyHost");
    //
    // MockHttpServletRequest.addGetParameter(req, "protexProxyPort", "56789");
    //
    // MockHttpServletRequest.addGetParameter(req, "protexNoProxyHost", "ignoreThisHost, andthishost");
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // assertTrue(element.getChildren("errors").isEmpty());
    //
    // ProtexProxyInfo proxyInfo = persistenceManager.getGlobalConfig().getProxyInfo();
    // assertEquals("newProxyHost", proxyInfo.getHost());
    // assertEquals(56789, proxyInfo.getPort().intValue());
    // assertEquals("ignoreThisHost, andthishost", proxyInfo.getIgnoredProxyHosts());
    // } finally {
    // String configPath = getConfigDirectory(configDir);
    // File config = new File(configPath + File.separator + "protex-config.xml");
    // if (config.exists()) {
    // config.delete();
    // }
    // }
    // }
    //
    // @Test
    // public void testDoPostSaveCredentialWithErrors() throws IOException {
    // String configDir = "SaveCredential";
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // ServerProtexConfigPersistenceManager persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "savingCredential", "true");
    //
    // MockHttpServletRequest.addGetParameter(req, "protexUsername", "");
    //
    // MockHttpServletRequest.addGetParameter(req, "encryptedProtexPassword", "");
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // Iterator iterator = element.getDescendants();
    // Element errorProtexUsername = null;
    // Element errorProtexPassword = null;
    //
    // while (iterator.hasNext()) {
    // Object descendent = iterator.next();
    // if (descendent instanceof Element) {
    // Element descendentElement = (Element) descendent;
    //
    // String id = descendentElement.getAttributeValue("id");
    // if (StringUtils.isNotBlank(id)) {
    // if (id.equalsIgnoreCase("errorProtexUsername")) {
    // errorProtexUsername = descendentElement;
    // } else if (id.equalsIgnoreCase("errorProtexPassword")) {
    // errorProtexPassword = descendentElement;
    // }
    // }
    // }
    // }
    // assertNotNull(errorProtexUsername);
    // assertEquals("Must provide a Username", errorProtexUsername.getText());
    // assertNotNull(errorProtexPassword);
    // assertEquals("Must provide a Password", errorProtexPassword.getText());
    // } finally {
    // String configPath = getConfigDirectory(configDir);
    // File config = new File(configPath + File.separator + "protex-config.xml");
    // if (config.exists()) {
    // config.delete();
    // }
    // }
    // }
    //
    // @Test
    // public void testDoPostSaveCredential() throws IOException {
    // String configDir = "SaveCredential";
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // ServerProtexConfigPersistenceManager persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // assertEquals(0, persistenceManager.getGlobalConfig().getProtexCredentials().size());
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "savingCredential", "true");
    //
    // MockHttpServletRequest.addGetParameter(req, "protexUsername", "TestUser");
    // String webEncryptedPassword = RSACipher.encryptDataForWeb("Fake Password");
    // MockHttpServletRequest.addGetParameter(req,
    // "encryptedProtexPassword", webEncryptedPassword);
    //
    // MockHttpServletRequest.addGetParameter(req, "protexUserDescription", "Fake description");
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // assertTrue(element.getChildren("errors").isEmpty());
    //
    // assertEquals(1, persistenceManager.getGlobalConfig().getProtexCredentials().size());
    // List<ProtexCredential> credentials = persistenceManager.getGlobalConfig().getProtexCredentials();
    // ProtexCredential credential = credentials.get(0);
    // assertTrue(credential.getDisplayId(), StringUtils.isNotBlank(credential.getDisplayId()));
    // assertEquals("TestUser", credential.getProtexUser());
    // assertTrue(credential.getEncryptedPassword(), StringUtils.isNotBlank(credential.getEncryptedPassword()));
    // assertEquals("Fake description", credential.getDescription());
    //
    // } finally {
    // String configPath = getConfigDirectory(configDir);
    // File config = new File(configPath + File.separator + "protex-config.xml");
    // if (config.exists()) {
    // config.delete();
    // }
    // }
    // }
    //
    // @Test
    // public void testDoPostSaveCredentialUpdateExisting() throws IOException {
    // String configDir = "SaveCredential";
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // ServerProtexConfigPersistenceManager persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // assertEquals(2, persistenceManager.getGlobalConfig().getProtexCredentials().size());
    // ProtexCredential originalCredential = defaultGlobalConfig.getProtexCredentials().get(0);
    // UUID credentialId = originalCredential.getId();
    // String protexUserName = originalCredential.getProtexUser();
    // String protexPassword = originalCredential.getEncryptedPassword();
    // String protexDescription = originalCredential.getDescription();
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "savingCredential", "true");
    //
    // MockHttpServletRequest.addGetParameter(req, "credentialId", credentialId.toString());
    // MockHttpServletRequest.addGetParameter(req, "protexUsername", "This is the updated Username");
    // String webEncryptedPassword = RSACipher.encryptDataForWeb("Updated password");
    // MockHttpServletRequest.addGetParameter(req,
    // "encryptedProtexPassword", webEncryptedPassword);
    //
    // MockHttpServletRequest.addGetParameter(req, "protexUserDescription", "This is the updated description");
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // assertTrue(element.getChildren("errors").isEmpty());
    //
    // assertEquals(2, persistenceManager.getGlobalConfig().getProtexCredentials().size());
    //
    // List<ProtexCredential> credentials = persistenceManager.getGlobalConfig().getProtexCredentials();
    // ProtexCredential editedCredential = null;
    // for (ProtexCredential credential : credentials) {
    // if (credential.getId().equals(credentialId)) {
    // editedCredential = credential;
    // break;
    // }
    // }
    // assertNotNull(editedCredential);
    // assertEquals(credentialId, editedCredential.getId());
    // assertEquals("This is the updated Username", editedCredential.getProtexUser());
    // assertTrue(editedCredential.getEncryptedPassword(),
    // StringUtils.isNotBlank(editedCredential.getEncryptedPassword()));
    // assertEquals("This is the updated description", editedCredential.getDescription());
    //
    // assertTrue(!editedCredential.getProtexUser().equals(protexUserName));
    // assertTrue(!editedCredential.getEncryptedPassword().equals(protexPassword));
    // assertTrue(!editedCredential.getDescription().equals(protexDescription));
    //
    // } finally {
    // String configPath = getConfigDirectory(configDir);
    // File config = new File(configPath + File.separator + "protex-config.xml");
    // if (config.exists()) {
    // config.delete();
    // }
    // }
    // }
    //
    // @Test
    // public void testDoPostDeleteCredentialWithErrors() throws IOException {
    // String configDir = "DeleteCredential";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "deleteCredential", UUID.randomUUID().toString());
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // Iterator iterator = element.getDescendants();
    // Element errorSaving = null;
    //
    // while (iterator.hasNext()) {
    // Object descendent = iterator.next();
    // if (descendent instanceof Element) {
    // Element descendentElement = (Element) descendent;
    //
    // String id = descendentElement.getAttributeValue("id");
    // if (StringUtils.isNotBlank(id)) {
    // if (id.equalsIgnoreCase("errorSaving")) {
    // errorSaving = descendentElement;
    // }
    // }
    // }
    // }
    // assertNotNull(errorSaving);
    //
    // assertEquals("Could not find the specified credential to delete.", errorSaving.getText());
    //
    // } finally {
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    // persistenceManager.persist();
    // }
    // }
    //
    // @Test
    // public void testDoPostDeleteCredential() throws IOException {
    // String configDir = "DeleteCredential";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // assertEquals(2, persistenceManager.getGlobalConfig().getProtexCredentials().size());
    // ProtexCredential originalCredential = defaultGlobalConfig.getProtexCredentials().get(0);
    // UUID credentialId = originalCredential.getId();
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "deleteCredential", credentialId.toString());
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // assertTrue(element.getChildren("errors").isEmpty());
    //
    // assertEquals(1, persistenceManager.getGlobalConfig().getProtexCredentials().size());
    // for (ProtexCredential credential : persistenceManager.getGlobalConfig().getProtexCredentials()) {
    // if (credential.getId().equals(credentialId)) {
    // fail("Did not delete the credential");
    // }
    // }
    //
    // } finally {
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    // persistenceManager.persist();
    // }
    // }
    //
    // @Test
    // public void testDoPostSaveServerEmptyValues() throws IOException {
    // String configDir = "SaveServer";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "savingServer", "true");
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // Iterator iterator = element.getDescendants();
    // Element errorProtexName = null;
    // Element errorProtexUrl = null;
    // Element errorProtexTimeout = null;
    //
    // while (iterator.hasNext()) {
    // Object descendent = iterator.next();
    // if (descendent instanceof Element) {
    // Element descendentElement = (Element) descendent;
    //
    // String id = descendentElement.getAttributeValue("id");
    // if (StringUtils.isNotBlank(id)) {
    // if (id.equalsIgnoreCase("errorProtexName")) {
    // errorProtexName = descendentElement;
    // } else if (id.equalsIgnoreCase("errorProtexUrl")) {
    // errorProtexUrl = descendentElement;
    // } else if (id.equalsIgnoreCase("errorProtexTimeout")) {
    // errorProtexTimeout = descendentElement;
    // }
    //
    // }
    // }
    // }
    // assertNotNull(errorProtexName);
    // assertNotNull(errorProtexUrl);
    // assertNotNull(errorProtexTimeout);
    //
    // assertEquals("Please specify a name for this Protex server.", errorProtexName.getText());
    // assertEquals("Please specify a URL for this Protex server.", errorProtexUrl.getText());
    // assertEquals("Please specify the timeout as an Integer value.", errorProtexTimeout.getText());
    //
    // } finally {
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    // persistenceManager.persist();
    // }
    // }
    //
    // @Test
    // public void testDoPostSaveServerBadUrl() throws IOException {
    // String configDir = "SaveServer";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    //
    // ProtexServer existingServer = defaultGlobalConfig.getProtexServers().get(0);
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "savingServer", "true");
    //
    // MockHttpServletRequest.addGetParameter(req, "protexName", "Random name that should not exist");
    // MockHttpServletRequest.addGetParameter(req, "protexUrl", "zdsfhsdh");
    // MockHttpServletRequest.addGetParameter(req, "protexTimeout", "300");
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // Iterator iterator = element.getDescendants();
    // Element errorProtexUrl = null;
    //
    // while (iterator.hasNext()) {
    // Object descendent = iterator.next();
    // if (descendent instanceof Element) {
    // Element descendentElement = (Element) descendent;
    //
    // String id = descendentElement.getAttributeValue("id");
    // if (StringUtils.isNotBlank(id)) {
    // if (id.equalsIgnoreCase("errorProtexUrl")) {
    // errorProtexUrl = descendentElement;
    // }
    // }
    // }
    // }
    // assertNotNull(errorProtexUrl);
    //
    // assertEquals("Please specify a valid URL of a Protex server.", errorProtexUrl.getText());
    //
    // } finally {
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    // persistenceManager.persist();
    // }
    // }
    //
    // @Test
    // public void testDoPostSaveServerSameServerNameAsExisting() throws IOException {
    // String configDir = "SaveServer";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    //
    // ProtexServer existingServer = defaultGlobalConfig.getProtexServers().get(0);
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "savingServer", "true");
    //
    // MockHttpServletRequest.addGetParameter(req, "protexName", existingServer.getProtexName());
    // MockHttpServletRequest.addGetParameter(req, "protexUrl", "https://www.blackducksoftware.com/");
    // MockHttpServletRequest.addGetParameter(req, "protexTimeout", "300");
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // Iterator iterator = element.getDescendants();
    // Element errorProtexName = null;
    //
    // while (iterator.hasNext()) {
    // Object descendent = iterator.next();
    // if (descendent instanceof Element) {
    // Element descendentElement = (Element) descendent;
    //
    // String id = descendentElement.getAttributeValue("id");
    // if (StringUtils.isNotBlank(id)) {
    // if (id.equalsIgnoreCase("errorProtexName")) {
    // errorProtexName = descendentElement;
    // }
    // }
    // }
    // }
    // assertNotNull(errorProtexName);
    //
    // assertEquals("There is already a server defined with this name. Please specify a unique name for this Protex server.",
    // errorProtexName.getText());
    //
    // } finally {
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    // persistenceManager.persist();
    // }
    // }
    //
    // @Test
    // public void testDoPostSaveServerSameServerURLAsExisting() throws IOException {
    // String configDir = "SaveServer";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    //
    // ProtexServer existingServer = defaultGlobalConfig.getProtexServers().get(0);
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "savingServer", "true");
    //
    // MockHttpServletRequest.addGetParameter(req, "protexName", "Unique Name");
    // MockHttpServletRequest.addGetParameter(req, "protexUrl", existingServer.getProtexUrl());
    // MockHttpServletRequest.addGetParameter(req, "protexTimeout", "300");
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // Iterator iterator = element.getDescendants();
    // Element errorProtexUrl = null;
    //
    // while (iterator.hasNext()) {
    // Object descendent = iterator.next();
    // if (descendent instanceof Element) {
    // Element descendentElement = (Element) descendent;
    //
    // String id = descendentElement.getAttributeValue("id");
    // if (StringUtils.isNotBlank(id)) {
    // if (id.equalsIgnoreCase("errorProtexUrl")) {
    // errorProtexUrl = descendentElement;
    // }
    // }
    // }
    // }
    // assertNotNull(errorProtexUrl);
    //
    // assertEquals("There is already a server defined with this URL. Please specify a unique URL for this Protex server.",
    // errorProtexUrl.getText());
    //
    // } finally {
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    // persistenceManager.persist();
    // }
    // }
    //
    // @Test
    // public void testDoPostSaveServerBadTimeout() throws IOException {
    // String configDir = "SaveServer";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    //
    // ProtexServer existingServer = defaultGlobalConfig.getProtexServers().get(0);
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "savingServer", "true");
    //
    // MockHttpServletRequest.addGetParameter(req, "protexName", "Unique Name");
    // MockHttpServletRequest.addGetParameter(req, "protexUrl", "https://www.blackducksoftware.com/");
    // MockHttpServletRequest.addGetParameter(req, "protexTimeout", "zxfgdhzsdgh");
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // Iterator iterator = element.getDescendants();
    // Element errorProtexTimeout = null;
    //
    // while (iterator.hasNext()) {
    // Object descendent = iterator.next();
    // if (descendent instanceof Element) {
    // Element descendentElement = (Element) descendent;
    //
    // String id = descendentElement.getAttributeValue("id");
    // if (StringUtils.isNotBlank(id)) {
    // if (id.equalsIgnoreCase("errorProtexTimeout")) {
    // errorProtexTimeout = descendentElement;
    // }
    // }
    // }
    // }
    // assertNotNull(errorProtexTimeout);
    //
    // assertEquals("Please specify the timeout as an Integer value.", errorProtexTimeout.getText());
    //
    // MockHttpServletRequest.addGetParameter(req, "protexTimeout", "-2");
    //
    // element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // iterator = element.getDescendants();
    // errorProtexTimeout = null;
    //
    // while (iterator.hasNext()) {
    // Object descendent = iterator.next();
    // if (descendent instanceof Element) {
    // Element descendentElement = (Element) descendent;
    //
    // String id = descendentElement.getAttributeValue("id");
    // if (StringUtils.isNotBlank(id)) {
    // if (id.equalsIgnoreCase("errorProtexTimeout")) {
    // errorProtexTimeout = descendentElement;
    // }
    // }
    // }
    // }
    // assertNotNull(errorProtexTimeout);
    //
    // assertEquals("Please specify a timeout greater than 0.", errorProtexTimeout.getText());
    //
    // } finally {
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    // persistenceManager.persist();
    // }
    // }
    //
    // @Test
    // public void testDoPostSaveServer() throws IOException {
    // String configDir = "SaveServer";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // assertEquals(5, persistenceManager.getGlobalConfig().getProtexServers().size());
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "savingServer", "true");
    //
    // String serverName = "Unique Name";
    // String serverURL = "http://integration-hub.blackducksoftware.com";
    // int timeout = 300;
    //
    // MockHttpServletRequest.addGetParameter(req, "protexName", serverName);
    // MockHttpServletRequest.addGetParameter(req, "protexUrl", serverURL);
    // MockHttpServletRequest.addGetParameter(req, "protexTimeout", String.valueOf(timeout));
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // assertTrue(element.getChildren("errors").isEmpty());
    //
    // assertEquals(6, persistenceManager.getGlobalConfig().getProtexServers().size());
    // List<ProtexServer> servers = persistenceManager.getGlobalConfig().getProtexServers();
    // ProtexServer newServer = null;
    //
    // for (ProtexServer server : servers) {
    // if (server.getProtexName().equals(serverName)) {
    // newServer = server;
    // break;
    // }
    // }
    // assertNotNull(newServer);
    //
    // assertTrue(newServer.getDisplayProtexId(), StringUtils.isNotBlank(newServer.getDisplayProtexId()));
    // assertEquals(serverName, newServer.getProtexName());
    // assertEquals(serverURL, newServer.getProtexUrl());
    // assertEquals(timeout, newServer.getProtexTimeout());
    //
    // } finally {
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    // persistenceManager.persist();
    // }
    // }
    //
    // @Test
    // public void testDoPostSaveServerUpdateExisting() throws IOException {
    // String configDir = "SaveServer";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    //
    // List<ProtexServer> protexServers = new ArrayList<ProtexServer>();
    // ProtexServer serverToUpdate = null;
    //
    // for (ProtexServer server : defaultGlobalConfig.getProtexServers()) {
    // // Find the non protex server so we can change it to a real protex url in this test
    // if (server.getDisplayProtexId().equals(ServerTestHelper.nonProtexServerId)) {
    // serverToUpdate = server;
    // break;
    // }
    // }
    // assertNotNull(serverToUpdate);
    // protexServers.add(serverToUpdate);
    //
    // persistenceManager.getGlobalConfig().setProtexServers(protexServers);
    // assertEquals(1, persistenceManager.getGlobalConfig().getProtexServers().size());
    //
    // UUID serverId = serverToUpdate.getProtexId();
    // String originalServerName = serverToUpdate.getProtexName();
    // String originalServerURL = serverToUpdate.getProtexUrl();
    // int originalTimeout = serverToUpdate.getProtexTimeout();
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "savingServer", "true");
    //
    // String serverName = "New Unique Name";
    // String serverURL = "http://qa-px-integration.blackducksoftware.com";
    // int timeout = 1234;
    //
    // assertTrue(!originalServerName.equalsIgnoreCase(serverName));
    // assertTrue(!originalServerURL.equalsIgnoreCase(serverURL));
    // assertTrue(originalTimeout != timeout);
    //
    // MockHttpServletRequest.addGetParameter(req, "protexId", serverId.toString());
    // MockHttpServletRequest.addGetParameter(req, "protexName", serverName);
    // MockHttpServletRequest.addGetParameter(req, "protexUrl", serverURL);
    // MockHttpServletRequest.addGetParameter(req, "protexTimeout", String.valueOf(timeout));
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // assertTrue(element.getChildren("errors").isEmpty());
    //
    // assertEquals(1, persistenceManager.getGlobalConfig().getProtexServers().size());
    // List<ProtexServer> servers = persistenceManager.getGlobalConfig().getProtexServers();
    // ProtexServer editedServer = null;
    //
    // for (ProtexServer server : servers) {
    // if (server.getProtexId().equals(UUID.fromString(ServerTestHelper.nonProtexServerId))) {
    // editedServer = server;
    // break;
    // }
    // }
    // assertNotNull(editedServer);
    //
    // assertEquals(editedServer.getDisplayProtexId(), ServerTestHelper.nonProtexServerId);
    // assertEquals(serverName, editedServer.getProtexName());
    // assertEquals(serverURL, editedServer.getProtexUrl());
    // assertEquals(timeout, editedServer.getProtexTimeout());
    //
    // } finally {
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    // persistenceManager.persist();
    // }
    // }
    //
    // @Test
    // public void testDoPostDeleteServerWithErrors() throws IOException {
    // String configDir = "DeleteServer";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "deleteServer", UUID.randomUUID().toString());
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // Iterator iterator = element.getDescendants();
    // Element errorSaving = null;
    //
    // while (iterator.hasNext()) {
    // Object descendent = iterator.next();
    // if (descendent instanceof Element) {
    // Element descendentElement = (Element) descendent;
    //
    // String id = descendentElement.getAttributeValue("id");
    // if (StringUtils.isNotBlank(id)) {
    // if (id.equalsIgnoreCase("errorSaving")) {
    // errorSaving = descendentElement;
    // }
    // }
    // }
    // }
    // assertNotNull(errorSaving);
    //
    // assertEquals("Could not find the specified server to delete.", errorSaving.getText());
    //
    // } finally {
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    // persistenceManager.persist();
    // }
    // }
    //
    // @Test
    // public void testDoPostDeleteServer() throws IOException {
    // String configDir = "DeleteServer";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // ProtexServer serverToDelete = persistenceManager.getGlobalConfig().getProtexServers().get(0);
    //
    // MockHttpServletRequest.addGetParameter(req, "deleteServer", serverToDelete.getDisplayProtexId());
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // assertTrue(element.getChildren("errors").isEmpty());
    //
    // for (ProtexServer server : persistenceManager.getGlobalConfig().getProtexServers()) {
    // if (server.getProtexId().equals(serverToDelete.getProtexId())) {
    // fail("Did not delete the server");
    // }
    // }
    //
    // } finally {
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    // persistenceManager.persist();
    // }
    // }
    //
    // @Test
    // public void testDoPostTestConnectionEmptyValues() throws IOException {
    // String configDir = "EmptyConfig";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    //
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "testConnection", "true");
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // Iterator iterator = element.getDescendants();
    // Element errorProtexTestUrls = null;
    // Element errorProtexCredentials = null;
    //
    // while (iterator.hasNext()) {
    // Object descendent = iterator.next();
    // if (descendent instanceof Element) {
    // Element descendentElement = (Element) descendent;
    //
    // String id = descendentElement.getAttributeValue("id");
    // if (StringUtils.isNotBlank(id)) {
    // if (id.equalsIgnoreCase("errorProtexTestUrls")) {
    // errorProtexTestUrls = descendentElement;
    // } else if (id.equalsIgnoreCase("errorProtexCredentials")) {
    // errorProtexCredentials = descendentElement;
    // }
    // }
    // }
    // }
    // assertNotNull(errorProtexTestUrls);
    // assertNotNull(errorProtexCredentials);
    // assertEquals("Need to choose a server to test.", errorProtexTestUrls.getText());
    // assertEquals("Need to choose credentials to test.", errorProtexCredentials.getText());
    // }
    //
    // @Test
    // public void testDoPostTestConnectionUnknownServer() throws IOException {
    // String configDir = "EmptyConfig";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    //
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "testConnection", "true");
    // MockHttpServletRequest.addGetParameter(req, "protexServerId", UUID.randomUUID().toString());
    // MockHttpServletRequest.addGetParameter(req, "protexCredentialId", ServerTestHelper.realCredentialId);
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // Iterator iterator = element.getDescendants();
    // Element errorProtexTestUrls = null;
    //
    // while (iterator.hasNext()) {
    // Object descendent = iterator.next();
    // if (descendent instanceof Element) {
    // Element descendentElement = (Element) descendent;
    //
    // String id = descendentElement.getAttributeValue("id");
    // if (StringUtils.isNotBlank(id)) {
    // if (id.equalsIgnoreCase("errorProtexTestUrls")) {
    // errorProtexTestUrls = descendentElement;
    // }
    // }
    // }
    // }
    // assertNotNull(errorProtexTestUrls);
    // assertEquals("Could not find the specified server.", errorProtexTestUrls.getText());
    //
    // }
    //
    // @Test
    // public void testDoPostTestConnectionBadURL() throws IOException {
    // String configDir = "EmptyConfig";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "testConnection", "true");
    // MockHttpServletRequest.addGetParameter(req, "protexServerId", ServerTestHelper.badUrlServerId);
    // MockHttpServletRequest.addGetParameter(req, "protexCredentialId", ServerTestHelper.realCredentialId);
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // Iterator iterator = element.getDescendants();
    // Element errorProtexTestUrls = null;
    //
    // while (iterator.hasNext()) {
    // Object descendent = iterator.next();
    // if (descendent instanceof Element) {
    // Element descendentElement = (Element) descendent;
    //
    // String id = descendentElement.getAttributeValue("id");
    // if (StringUtils.isNotBlank(id)) {
    // if (id.equalsIgnoreCase("errorProtexTestUrls")) {
    // errorProtexTestUrls = descendentElement;
    // }
    // }
    // }
    // }
    // assertNotNull(errorProtexTestUrls);
    //
    // assertEquals("Please specify a valid URL of a Protex server.", errorProtexTestUrls.getText());
    //
    // } finally {
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    // persistenceManager.persist();
    // }
    // }
    //
    // @Test
    // public void testDoPostTestConnectionUnknownHost() throws IOException {
    // String configDir = "EmptyConfig";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    // try {
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "testConnection", "true");
    // MockHttpServletRequest.addGetParameter(req, "protexServerId", ServerTestHelper.unknownHostServerId);
    // MockHttpServletRequest.addGetParameter(req, "protexCredentialId", ServerTestHelper.realCredentialId);
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // Iterator iterator = element.getDescendants();
    // Element errorProtexTestUrls = null;
    //
    // while (iterator.hasNext()) {
    // Object descendent = iterator.next();
    // if (descendent instanceof Element) {
    // Element descendentElement = (Element) descendent;
    //
    // String id = descendentElement.getAttributeValue("id");
    // if (StringUtils.isNotBlank(id)) {
    // if (id.equalsIgnoreCase("errorProtexTestUrls")) {
    // errorProtexTestUrls = descendentElement;
    // }
    // }
    // }
    // }
    // assertNotNull(errorProtexTestUrls);
    //
    // assertTrue(errorProtexTestUrls.getText(),
    // errorProtexTestUrls.getText().contains("Trouble reaching the Protex server."));
    //
    // } finally {
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    // persistenceManager.persist();
    // }
    // }
    //
    // @Test
    // public void testDoPostTestConnectionUnknownCredential() throws IOException {
    // String configDir = "EmptyConfig";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    //
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "testConnection", "true");
    // MockHttpServletRequest.addGetParameter(req, "protexServerId", ServerTestHelper.realProtexServerId);
    // MockHttpServletRequest.addGetParameter(req, "protexCredentialId", UUID.randomUUID().toString());
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // Iterator iterator = element.getDescendants();
    // Element errorProtexCredentials = null;
    //
    // while (iterator.hasNext()) {
    // Object descendent = iterator.next();
    // if (descendent instanceof Element) {
    // Element descendentElement = (Element) descendent;
    //
    // String id = descendentElement.getAttributeValue("id");
    // if (StringUtils.isNotBlank(id)) {
    // if (id.equalsIgnoreCase("errorProtexCredentials")) {
    // errorProtexCredentials = descendentElement;
    // }
    // }
    // }
    // }
    // assertNotNull(errorProtexCredentials);
    // assertEquals("Could not find the specified credential.", errorProtexCredentials.getText());
    // }
    //
    // @Test
    // public void testDoPostTestConnectionNonProtexServer() throws IOException {
    // String configDir = "EmptyConfig";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    //
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "testConnection", "true");
    // MockHttpServletRequest.addGetParameter(req, "protexServerId", ServerTestHelper.nonProtexServerId);
    // MockHttpServletRequest.addGetParameter(req, "protexCredentialId", ServerTestHelper.realCredentialId);
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // Iterator iterator = element.getDescendants();
    // Element errorProtexTestUrls = null;
    //
    // while (iterator.hasNext()) {
    // Object descendent = iterator.next();
    // if (descendent instanceof Element) {
    // Element descendentElement = (Element) descendent;
    //
    // String id = descendentElement.getAttributeValue("id");
    // if (StringUtils.isNotBlank(id)) {
    // if (id.equalsIgnoreCase("errorProtexTestUrls")) {
    // errorProtexTestUrls = descendentElement;
    // }
    // }
    // }
    // }
    // assertNotNull(errorProtexTestUrls);
    // assertEquals("HTTP response '404: Not Found' when communicating with https://www.blackducksoftware.com/protex-sdk/v7_0",
    // errorProtexTestUrls.getText());
    // }
    //
    // @Test
    // public void testDoPostTestConnectionBadCredentials() throws IOException {
    // String configDir = "EmptyConfig";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    //
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "testConnection", "true");
    // MockHttpServletRequest.addGetParameter(req, "protexServerId", ServerTestHelper.realProtexServerId);
    // MockHttpServletRequest.addGetParameter(req, "protexCredentialId", ServerTestHelper.fakeCredentialId);
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // Iterator iterator = element.getDescendants();
    // Element errorProtexCredentials = null;
    //
    // while (iterator.hasNext()) {
    // Object descendent = iterator.next();
    // if (descendent instanceof Element) {
    // Element descendentElement = (Element) descendent;
    //
    // String id = descendentElement.getAttributeValue("id");
    // if (StringUtils.isNotBlank(id)) {
    // if (id.equalsIgnoreCase("errorProtexCredentials")) {
    // errorProtexCredentials = descendentElement;
    // }
    // }
    // }
    // }
    // assertNotNull(errorProtexCredentials);
    // assertEquals("The user name or password provided was not valid.", errorProtexCredentials.getText());
    // }
    //
    // @Test
    // public void testDoPostTestConnection() throws IOException {
    // String configDir = "EmptyConfig";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    //
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    // persistenceManager.getGlobalConfig().setProxyInfo(defaultGlobalConfig.getProxyInfo());
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "testConnection", "true");
    // MockHttpServletRequest.addGetParameter(req, "protexServerId", ServerTestHelper.realProtexServerId);
    // MockHttpServletRequest.addGetParameter(req, "protexCredentialId", ServerTestHelper.realCredentialId);
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // assertTrue(element.getChildren("errors").isEmpty());
    // }
    //
    // @Test
    // public void testDoPostTestConnectionPassThroughProxy() throws IOException {
    // String configDir = "EmptyConfig";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    //
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    //
    // ProtexProxyInfo proxyInfo = new ProtexProxyInfo();
    // proxyInfo.setHost(testProperties.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));
    // proxyInfo.setPort(Integer.valueOf(testProperties.getProperty("TEST_PROXY_PORT_PASSTHROUGH")));
    //
    // persistenceManager.getGlobalConfig().setProxyInfo(proxyInfo);
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "testConnection", "true");
    // MockHttpServletRequest.addGetParameter(req, "protexServerId", ServerTestHelper.realProtexServerId);
    // MockHttpServletRequest.addGetParameter(req, "protexCredentialId", ServerTestHelper.realCredentialId);
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // assertTrue(element.getChildren("errors").isEmpty());
    // }
    //
    // @Test
    // public void testDoPostTestConnectionBasicAuthProxy() throws IOException {
    // String configDir = "EmptyConfig";
    // ServerProtexConfigPersistenceManager persistenceManager = null;
    // ProtexGlobalConfig defaultGlobalConfig = ServerTestHelper.getDefaultValidConfiguration();
    //
    // ServerPaths serverPaths = getMockedServerPaths(configDir);
    // persistenceManager = new ServerProtexConfigPersistenceManager(serverPaths);
    // persistenceManager.getGlobalConfig().setProtexCredentials(defaultGlobalConfig.getProtexCredentials());
    // persistenceManager.getGlobalConfig().setProtexServers(defaultGlobalConfig.getProtexServers());
    //
    // ProtexProxyInfo proxyInfo = new ProtexProxyInfo();
    // proxyInfo.setHost(testProperties.getProperty("TEST_PROXY_HOST_BASIC"));
    // proxyInfo.setPort(Integer.valueOf(testProperties.getProperty("TEST_PROXY_PORT_BASIC")));
    // proxyInfo.setProxyUsername(testProperties.getProperty("TEST_PROXY_USER_BASIC"));
    // proxyInfo.setProxyPassword(testProperties.getProperty("TEST_PROXY_PASSWORD_BASIC"));
    //
    // persistenceManager.getGlobalConfig().setProxyInfo(proxyInfo);
    //
    // ProtexGlobalServerConfigController controller = new ProtexGlobalServerConfigController(persistenceManager);
    // HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
    // MockHttpServletRequest.requestHasParamters(req, true);
    //
    // MockHttpServletRequest.addGetParameter(req, "testConnection", "true");
    // MockHttpServletRequest.addGetParameter(req, "protexServerId", ServerTestHelper.realProtexServerId);
    // MockHttpServletRequest.addGetParameter(req, "protexCredentialId", ServerTestHelper.realCredentialId);
    //
    // Element element = new Element("testElement");
    //
    // controller.doPost(req, null, element);
    //
    // assertTrue(element.getChildren("errors").isEmpty());
    // }
}
