package com.blackducksoftware.integration.hub.teamcity.server.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import jetbrains.buildServer.log.LogInitializer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.crypt.RSACipher;

import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.teamcity.common.beans.HubCredentialsBean;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubProxyInfo;
import com.blackducksoftware.integration.hub.teamcity.mocks.MockHttpServletRequest;
import com.blackducksoftware.integration.hub.teamcity.mocks.MockServerPaths;

public class HubGlobalServerConfigControllerTest {

    private static Properties testProperties;

    private final static String parentDir = "configController";

    @BeforeClass
    public static void startup() {

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

    private ServerPaths getMockedServerPaths(final String configDir) {

        return MockServerPaths.getMockedServerPaths(parentDir, configDir);
    }

    private String getConfigDirectory(final String configDir) {

        return MockServerPaths.getConfigDirectory(parentDir, configDir);
    }

    @Test
    public void testConstructor() throws IOException {
        ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
        ServerHubConfigPersistenceManager persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);
        assertNotNull(new HubGlobalServerConfigController(persistenceManager));
    }

    @Test
    public void testIsTestConnectionRequest() throws IOException {
        ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
        ServerHubConfigPersistenceManager persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);
        HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);

        HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();

        assertTrue(!controller.isTestConnectionRequest(req));

        MockHttpServletRequest.addGetParameter(req, "testConnection", "false");

        assertTrue(!controller.isTestConnectionRequest(req));

        MockHttpServletRequest.addGetParameter(req, "testConnection", "true");

        assertTrue(controller.isTestConnectionRequest(req));

    }

    @Test
    public void testIsSavingRequest() throws IOException {
        ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
        ServerHubConfigPersistenceManager persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);
        HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);

        HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();

        assertTrue(!controller.isSavingRequest(req));

        MockHttpServletRequest.addGetParameter(req, "saving", "false");

        assertTrue(!controller.isSavingRequest(req));

        MockHttpServletRequest.addGetParameter(req, "saving", "true");

        assertTrue(controller.isSavingRequest(req));

    }

    @Test
    public void testDoGet() throws IOException {
        ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
        ServerHubConfigPersistenceManager persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);
        HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);

        assertNull(controller.doGet(null, null));
    }

    @Test
    public void testDoPostNoParameters() throws IOException {
        ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
        ServerHubConfigPersistenceManager persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);
        HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);
        HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
        MockHttpServletRequest.requestHasParamters(req, false);

        controller.doPost(req, null, new Element("testElement"));
        // Nothing should happen
    }

    @Test
    public void testDoPostNoRequestType() throws IOException {
        ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
        ServerHubConfigPersistenceManager persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);
        HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);
        HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
        MockHttpServletRequest.requestHasParamters(req, true);

        controller.doPost(req, null, new Element("testElement"));
        // Nothing should happen
    }

    @Test
    public void testDoPostUnknownRequestType() throws IOException {
        ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
        ServerHubConfigPersistenceManager persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);
        HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);
        HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
        MockHttpServletRequest.requestHasParamters(req, true);

        MockHttpServletRequest.addGetParameter(req, "fakeRequest", "true");

        controller.doPost(req, null, new Element("testElement"));
        // Nothing should happen
    }

    @Test
    public void testDoPostSaveConfigWithErrors() throws IOException {
        String configDir = "SaveConfig";
        try {
            ServerPaths serverPaths = getMockedServerPaths(configDir);
            ServerHubConfigPersistenceManager persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);
            HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);
            HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
            MockHttpServletRequest.requestHasParamters(req, true);

            MockHttpServletRequest.addGetParameter(req, "saving", "true");

            MockHttpServletRequest.addGetParameter(req, "hubProxyServer", "fakeProxyHost");

            MockHttpServletRequest.addGetParameter(req, "hubProxyPort", "2345aSgsgdrfh");

            MockHttpServletRequest.addGetParameter(req, "hubNoProxyHost", "*.*.8srgasrg/asad?sd/sdg.../?");

            Element element = new Element("testElement");

            controller.doPost(req, null, element);

            Iterator iterator = element.getDescendants();
            Element errorUrl = null;
            Element errorUserName = null;
            Element errorPassword = null;
            Element errorHubProxyPort = null;
            Element errorHubNoProxyHost = null;

            while (iterator.hasNext()) {
                Object descendent = iterator.next();
                if (descendent instanceof Element) {
                    Element descendentElement = (Element) descendent;

                    String id = descendentElement.getAttributeValue("id");
                    if (StringUtils.isNotBlank(id)) {
                        if (id.equalsIgnoreCase("errorUrl")) {
                            errorUrl = descendentElement;
                        } else if (id.equalsIgnoreCase("errorUserName")) {
                            errorUserName = descendentElement;
                        } else if (id.equalsIgnoreCase("errorPassword")) {
                            errorPassword = descendentElement;
                        } else if (id.equalsIgnoreCase("errorHubProxyPort")) {
                            errorHubProxyPort = descendentElement;
                        } else if (id.equalsIgnoreCase("errorHubNoProxyHost")) {
                            errorHubNoProxyHost = descendentElement;
                        }
                    }
                }
            }
            assertNotNull(errorUrl);
            assertNotNull(errorUserName);
            assertNotNull(errorPassword);
            assertNotNull(errorHubProxyPort);
            assertNotNull(errorHubNoProxyHost);
            assertEquals("Please specify a URL.", errorUrl.getText());
            assertEquals("Please specify a UserName.", errorUserName.getText());
            assertEquals("There is no saved Password. Please specify a Password.", errorPassword.getText());
            assertTrue(errorHubNoProxyHost.getText(),
                    errorHubNoProxyHost.getText().contains("The host : ") && errorHubNoProxyHost.getText().contains(" : is not a valid regular expression."));
            assertTrue(errorHubProxyPort.getText(), errorHubProxyPort.getText().contains("Please enter a valid Proxy port. "));

        } finally {
            String configPath = getConfigDirectory(configDir);
            File config = new File(configPath + File.separator + "hub-config.xml");
            if (config.exists()) {
                config.delete();
            }
        }
    }

    @Test
    public void testDoPostSaveConfigWithMultipleInvalidNoProxyHostPatterns() throws IOException {
        String configDir = "SaveConfig";
        try {
            ServerPaths serverPaths = getMockedServerPaths(configDir);
            ServerHubConfigPersistenceManager persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);
            HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);
            HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
            MockHttpServletRequest.requestHasParamters(req, true);

            MockHttpServletRequest.addGetParameter(req, "saving", "true");

            MockHttpServletRequest.addGetParameter(req, "hubNoProxyHost", "*.*.8srgasrg/asad?sd/sdg.../?,*.*.8srhjdvdhfvsdg.../?,*.*.8&&&g.../?");

            Element element = new Element("testElement");

            controller.doPost(req, null, element);

            Iterator iterator = element.getDescendants();
            Element errorHubNoProxyHost = null;

            while (iterator.hasNext()) {
                Object descendent = iterator.next();
                if (descendent instanceof Element) {
                    Element descendentElement = (Element) descendent;

                    String id = descendentElement.getAttributeValue("id");
                    if (StringUtils.isNotBlank(id)) {
                        if (id.equalsIgnoreCase("errorHubNoProxyHost")) {
                            errorHubNoProxyHost = descendentElement;
                        }
                    }
                }
            }
            assertNotNull(errorHubNoProxyHost);
            assertTrue(errorHubNoProxyHost.getText(),
                    errorHubNoProxyHost.getText().contains("The host : ") && errorHubNoProxyHost.getText().contains(" : is not a valid regular expression."));

        } finally {
            String configPath = getConfigDirectory(configDir);
            File config = new File(configPath + File.separator + "hub-config.xml");
            if (config.exists()) {
                config.delete();
            }
        }
    }

    @Test
    public void testDoPostSaveConfig() throws Exception {
        String configDir = "SaveConfig";
        try {
            ServerPaths serverPaths = getMockedServerPaths(configDir);
            ServerHubConfigPersistenceManager persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);

            HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);
            HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
            MockHttpServletRequest.requestHasParamters(req, true);

            MockHttpServletRequest.addGetParameter(req, "saving", "true");

            MockHttpServletRequest.addGetParameter(req, "hubUrl", testProperties.getProperty("TEST_HUB_SERVER_URL"));

            MockHttpServletRequest.addGetParameter(req, "hubUser", "newHubUser");

            String webEncryptedPassword = RSACipher.encryptDataForWeb("New HUb password");
            MockHttpServletRequest.addGetParameter(req,
                    "encryptedHubPass", webEncryptedPassword);

            MockHttpServletRequest.addGetParameter(req, "hubProxyServer", testProperties.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));

            MockHttpServletRequest.addGetParameter(req, "hubProxyPort", testProperties.getProperty("TEST_PROXY_PORT_PASSTHROUGH"));

            MockHttpServletRequest.addGetParameter(req, "hubNoProxyHost", "ignoreThisHost, andthishost");

            Element element = new Element("testElement");

            controller.doPost(req, null, element);

            assertTrue(element.getChildren("errors").isEmpty());

            HubCredentialsBean credentials = persistenceManager.getConfiguredServer().getGlobalCredentials();
            assertEquals("newHubUser", credentials.getHubUser());
            assertEquals("New HUb password", credentials.getDecryptedPassword());

            assertEquals(testProperties.getProperty("TEST_HUB_SERVER_URL"), persistenceManager.getConfiguredServer().getHubUrl());

            HubProxyInfo proxyInfo = persistenceManager.getConfiguredServer().getProxyInfo();
            assertEquals(testProperties.getProperty("TEST_PROXY_HOST_PASSTHROUGH"), proxyInfo.getHost());
            assertEquals(Integer.valueOf(testProperties.getProperty("TEST_PROXY_PORT_PASSTHROUGH")), proxyInfo.getPort());
            assertEquals("ignoreThisHost, andthishost", proxyInfo.getIgnoredProxyHosts());
        } finally {
            String configPath = getConfigDirectory(configDir);
            File config = new File(configPath + File.separator + "hub-config.xml");
            if (config.exists()) {
                config.delete();
            }
        }
    }

    @Test
    public void testDoPostSaveConfigUnknownProxyHost() throws Exception {
        String configDir = "SaveConfig";
        try {
            ServerPaths serverPaths = getMockedServerPaths(configDir);
            ServerHubConfigPersistenceManager persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);

            HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);
            HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
            MockHttpServletRequest.requestHasParamters(req, true);

            MockHttpServletRequest.addGetParameter(req, "saving", "true");

            MockHttpServletRequest.addGetParameter(req, "hubUrl", testProperties.getProperty("TEST_HUB_SERVER_URL"));

            MockHttpServletRequest.addGetParameter(req, "hubUser", "newHubUser");

            String webEncryptedPassword = RSACipher.encryptDataForWeb("New HUb password");
            MockHttpServletRequest.addGetParameter(req,
                    "encryptedHubPass", webEncryptedPassword);

            MockHttpServletRequest.addGetParameter(req, "hubProxyServer", "newProxyHost");

            MockHttpServletRequest.addGetParameter(req, "hubProxyPort", "56789");

            MockHttpServletRequest.addGetParameter(req, "hubNoProxyHost", "ignoreThisHost, andthishost");

            Element element = new Element("testElement");

            Element errorUrl = null;

            controller.doPost(req, null, element);

            Iterator iterator = element.getDescendants();

            while (iterator.hasNext()) {
                Object descendent = iterator.next();
                if (descendent instanceof Element) {
                    Element descendentElement = (Element) descendent;

                    String id = descendentElement.getAttributeValue("id");
                    if (StringUtils.isNotBlank(id)) {
                        if (id.equalsIgnoreCase("errorUrl")) {
                            errorUrl = descendentElement;
                        }
                    }
                }
            }
            assertNotNull(errorUrl);

            assertTrue(errorUrl.getText(), errorUrl.getText().contains("Trouble reaching the Hub server."));

        } finally {
            String configPath = getConfigDirectory(configDir);
            File config = new File(configPath + File.separator + "hub-config.xml");
            if (config.exists()) {
                config.delete();
            }
        }
    }

    @Test
    public void testDoPostTestConnectionEmptyValues() throws IOException {
        String configDir = "EmptyConfig";
        ServerHubConfigPersistenceManager persistenceManager = null;

        ServerPaths serverPaths = getMockedServerPaths(configDir);
        persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);

        HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);
        HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
        MockHttpServletRequest.requestHasParamters(req, true);

        MockHttpServletRequest.addGetParameter(req, "testConnection", "true");

        Element element = new Element("testElement");

        controller.doPost(req, null, element);

        Iterator iterator = element.getDescendants();
        Element errorUrl = null;
        Element errorUserName = null;
        Element errorPassword = null;

        while (iterator.hasNext()) {
            Object descendent = iterator.next();
            if (descendent instanceof Element) {
                Element descendentElement = (Element) descendent;

                String id = descendentElement.getAttributeValue("id");
                if (StringUtils.isNotBlank(id)) {
                    if (id.equalsIgnoreCase("errorUrl")) {
                        errorUrl = descendentElement;
                    } else if (id.equalsIgnoreCase("errorUserName")) {
                        errorUserName = descendentElement;
                    } else if (id.equalsIgnoreCase("errorPassword")) {
                        errorPassword = descendentElement;
                    }
                }
            }
        }
        assertNotNull(errorUrl);
        assertNotNull(errorUserName);
        assertNotNull(errorPassword);
        assertEquals("Please specify a URL.", errorUrl.getText());
        assertEquals("Please specify a UserName.", errorUserName.getText());
        assertEquals("There is no saved Password. Please specify a Password.", errorPassword.getText());
    }

    @Test
    public void testDoPostTestConnectionBadURL() throws IOException {
        String configDir = "EmptyConfig";
        ServerHubConfigPersistenceManager persistenceManager = null;
        try {
            ServerPaths serverPaths = getMockedServerPaths(configDir);
            persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);

            HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);
            HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
            MockHttpServletRequest.requestHasParamters(req, true);

            MockHttpServletRequest.addGetParameter(req, "testConnection", "true");

            MockHttpServletRequest.addGetParameter(req, "hubUrl", "fakeHub");

            MockHttpServletRequest.addGetParameter(req, "hubUser", "newHubUser");

            String webEncryptedPassword = RSACipher.encryptDataForWeb("New HUb password");
            MockHttpServletRequest.addGetParameter(req,
                    "encryptedHubPass", webEncryptedPassword);

            Element element = new Element("testElement");

            controller.doPost(req, null, element);

            Iterator iterator = element.getDescendants();
            Element errorUrl = null;

            while (iterator.hasNext()) {
                Object descendent = iterator.next();
                if (descendent instanceof Element) {
                    Element descendentElement = (Element) descendent;

                    String id = descendentElement.getAttributeValue("id");
                    if (StringUtils.isNotBlank(id)) {
                        if (id.equalsIgnoreCase("errorUrl")) {
                            errorUrl = descendentElement;
                        }
                    }
                }
            }
            assertNotNull(errorUrl);

            assertTrue(errorUrl.getText(), errorUrl.getText().contains("Please specify a valid URL of a Hub server. "));

        } finally {
            persistenceManager.getConfiguredServer().setGlobalCredentials(new HubCredentialsBean(""));
            persistenceManager.getConfiguredServer().setHubUrl("");
            persistenceManager.getConfiguredServer().setProxyInfo(new HubProxyInfo());
            persistenceManager.persist();
        }
    }

    @Test
    public void testDoPostTestConnectionUnknownHost() throws IOException {
        String configDir = "EmptyConfig";
        ServerHubConfigPersistenceManager persistenceManager = null;

        ServerPaths serverPaths = getMockedServerPaths(configDir);
        persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);

        HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);
        HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
        MockHttpServletRequest.requestHasParamters(req, true);

        MockHttpServletRequest.addGetParameter(req, "testConnection", "true");

        MockHttpServletRequest.addGetParameter(req, "hubUrl", "http://fakeHub");

        MockHttpServletRequest.addGetParameter(req, "hubUser", "newHubUser");

        String webEncryptedPassword = RSACipher.encryptDataForWeb("New HUb password");
        MockHttpServletRequest.addGetParameter(req,
                "encryptedHubPass", webEncryptedPassword);

        Element element = new Element("testElement");

        controller.doPost(req, null, element);

        Iterator iterator = element.getDescendants();
        Element errorUrl = null;

        while (iterator.hasNext()) {
            Object descendent = iterator.next();
            if (descendent instanceof Element) {
                Element descendentElement = (Element) descendent;

                String id = descendentElement.getAttributeValue("id");
                if (StringUtils.isNotBlank(id)) {
                    if (id.equalsIgnoreCase("errorUrl")) {
                        errorUrl = descendentElement;
                    }
                }
            }
        }
        assertNotNull(errorUrl);
        assertTrue(errorUrl.getText(), errorUrl.getText().contains("Trouble reaching the Hub server. "));

    }

    @Test
    public void testDoPostTestConnectionBadCredential() throws IOException {
        String configDir = "EmptyConfig";
        ServerHubConfigPersistenceManager persistenceManager = null;

        ServerPaths serverPaths = getMockedServerPaths(configDir);
        persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);

        HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);
        HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
        MockHttpServletRequest.requestHasParamters(req, true);

        MockHttpServletRequest.addGetParameter(req, "testConnection", "true");

        MockHttpServletRequest.addGetParameter(req, "hubUrl", testProperties.getProperty("TEST_HUB_SERVER_URL"));

        MockHttpServletRequest.addGetParameter(req, "hubUser", "newHubUser");

        String webEncryptedPassword = RSACipher.encryptDataForWeb("New Hub password");
        MockHttpServletRequest.addGetParameter(req,
                "encryptedHubPass", webEncryptedPassword);

        Element element = new Element("testElement");

        controller.doPost(req, null, element);

        Iterator iterator = element.getDescendants();
        Element errorConnection = null;

        while (iterator.hasNext()) {
            Object descendent = iterator.next();
            if (descendent instanceof Element) {
                Element descendentElement = (Element) descendent;

                String id = descendentElement.getAttributeValue("id");
                if (StringUtils.isNotBlank(id)) {
                    if (id.equalsIgnoreCase("errorConnection")) {
                        errorConnection = descendentElement;
                    }
                }
            }
        }
        assertNotNull(errorConnection);
        assertTrue(errorConnection.getText(), errorConnection.getText().contains("Unauthorized (401)"));

    }

    @Test
    public void testDoPostTestConnection() throws IOException {
        String configDir = "EmptyConfig";
        ServerHubConfigPersistenceManager persistenceManager = null;

        ServerPaths serverPaths = getMockedServerPaths(configDir);
        persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);

        HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);
        HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
        MockHttpServletRequest.requestHasParamters(req, true);

        MockHttpServletRequest.addGetParameter(req, "testConnection", "true");

        MockHttpServletRequest.addGetParameter(req, "hubUrl", testProperties.getProperty("TEST_HUB_SERVER_URL"));

        MockHttpServletRequest.addGetParameter(req, "hubUser", testProperties.getProperty("TEST_USERNAME"));

        String webEncryptedPassword = RSACipher.encryptDataForWeb(testProperties.getProperty("TEST_PASSWORD"));
        MockHttpServletRequest.addGetParameter(req,
                "encryptedHubPass", webEncryptedPassword);

        Element element = new Element("testElement");

        controller.doPost(req, null, element);

        assertTrue(element.getChildren("errors").isEmpty());
    }

    @Test
    public void testDoPostTestConnectionUnknownProxy() throws IOException {
        String configDir = "EmptyConfig";
        ServerHubConfigPersistenceManager persistenceManager = null;

        ServerPaths serverPaths = getMockedServerPaths(configDir);
        persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);

        HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);
        HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
        MockHttpServletRequest.requestHasParamters(req, true);

        MockHttpServletRequest.addGetParameter(req, "testConnection", "true");
        MockHttpServletRequest.addGetParameter(req, "hubUrl", testProperties.getProperty("TEST_HUB_SERVER_URL"));

        MockHttpServletRequest.addGetParameter(req, "hubUser", testProperties.getProperty("TEST_USERNAME"));

        String webEncryptedPassword = RSACipher.encryptDataForWeb(testProperties.getProperty("TEST_PASSWORD"));
        MockHttpServletRequest.addGetParameter(req,
                "encryptedHubPass", webEncryptedPassword);

        MockHttpServletRequest.addGetParameter(req, "hubProxyServer", "fakeProxyServer");

        MockHttpServletRequest.addGetParameter(req, "hubProxyPort", "5678");

        Element element = new Element("testElement");

        controller.doPost(req, null, element);

        Element errorUrl = null;

        Iterator iterator = element.getDescendants();

        while (iterator.hasNext()) {
            Object descendent = iterator.next();
            if (descendent instanceof Element) {
                Element descendentElement = (Element) descendent;

                String id = descendentElement.getAttributeValue("id");
                if (StringUtils.isNotBlank(id)) {
                    if (id.equalsIgnoreCase("errorUrl")) {
                        errorUrl = descendentElement;
                    }
                }
            }
        }
        assertNotNull(errorUrl);

        assertTrue(errorUrl.getText(), errorUrl.getText().contains("Trouble reaching the Hub server."));
    }

    @Test
    public void testDoPostTestConnectionPassThroughProxy() throws IOException {
        String configDir = "EmptyConfig";
        ServerHubConfigPersistenceManager persistenceManager = null;

        ServerPaths serverPaths = getMockedServerPaths(configDir);
        persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);

        HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);
        HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
        MockHttpServletRequest.requestHasParamters(req, true);

        MockHttpServletRequest.addGetParameter(req, "testConnection", "true");
        MockHttpServletRequest.addGetParameter(req, "hubUrl", testProperties.getProperty("TEST_HUB_SERVER_URL"));

        MockHttpServletRequest.addGetParameter(req, "hubUser", testProperties.getProperty("TEST_USERNAME"));

        String webEncryptedPassword = RSACipher.encryptDataForWeb(testProperties.getProperty("TEST_PASSWORD"));
        MockHttpServletRequest.addGetParameter(req,
                "encryptedHubPass", webEncryptedPassword);

        MockHttpServletRequest.addGetParameter(req, "hubProxyServer", testProperties.getProperty("TEST_PROXY_HOST_PASSTHROUGH"));

        MockHttpServletRequest.addGetParameter(req, "hubProxyPort", testProperties.getProperty("TEST_PROXY_PORT_PASSTHROUGH"));

        Element element = new Element("testElement");

        controller.doPost(req, null, element);

        assertTrue(element.getChildren("errors").isEmpty());
    }

    @Test
    public void testDoPostTestConnectionBasicAuthProxy() throws IOException {
        String configDir = "EmptyConfig";
        ServerHubConfigPersistenceManager persistenceManager = null;

        ServerPaths serverPaths = getMockedServerPaths(configDir);
        persistenceManager = new ServerHubConfigPersistenceManager(serverPaths);

        HubGlobalServerConfigController controller = new HubGlobalServerConfigController(persistenceManager);
        HttpServletRequest req = MockHttpServletRequest.getMockedHttpServletRequest();
        MockHttpServletRequest.requestHasParamters(req, true);

        MockHttpServletRequest.addGetParameter(req, "testConnection", "true");
        MockHttpServletRequest.addGetParameter(req, "hubUrl", testProperties.getProperty("TEST_HUB_SERVER_URL"));

        MockHttpServletRequest.addGetParameter(req, "hubUser", testProperties.getProperty("TEST_USERNAME"));

        String webEncryptedPassword = RSACipher.encryptDataForWeb(testProperties.getProperty("TEST_PASSWORD"));
        MockHttpServletRequest.addGetParameter(req,
                "encryptedHubPass", webEncryptedPassword);

        MockHttpServletRequest.addGetParameter(req, "hubProxyServer", testProperties.getProperty("TEST_PROXY_HOST_BASIC"));

        MockHttpServletRequest.addGetParameter(req, "hubProxyPort", testProperties.getProperty("TEST_PROXY_PORT_BASIC"));

        MockHttpServletRequest.addGetParameter(req, "hubProxyUser", testProperties.getProperty("TEST_PROXY_USER_BASIC"));

        String webEncryptedProxyPassword = RSACipher.encryptDataForWeb(testProperties.getProperty("TEST_PROXY_PASSWORD_BASIC"));
        MockHttpServletRequest.addGetParameter(req,
                "encryptedHubProxyPass", webEncryptedProxyPassword);

        Element element = new Element("testElement");

        controller.doPost(req, null, element);

        assertTrue(element.getChildren("errors").isEmpty());
    }
}
