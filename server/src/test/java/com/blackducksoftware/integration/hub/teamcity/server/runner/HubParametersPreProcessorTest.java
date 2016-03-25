package com.blackducksoftware.integration.hub.teamcity.server.runner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SRunningBuild;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import jetbrains.buildServer.util.EventDispatcher;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubCredentialsBean;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubProxyInfo;
import com.blackducksoftware.integration.hub.teamcity.helper.TestBuildLog;
import com.blackducksoftware.integration.hub.teamcity.mocks.MockSBuildServer;
import com.blackducksoftware.integration.hub.teamcity.mocks.MockSRunningBuild;
import com.blackducksoftware.integration.hub.teamcity.mocks.MockServerPaths;
import com.blackducksoftware.integration.hub.teamcity.server.global.HubServerListener;
import com.blackducksoftware.integration.hub.version.api.DistributionEnum;
import com.blackducksoftware.integration.hub.version.api.PhaseEnum;

public class HubParametersPreProcessorTest {

    private final static String parentDir = "runner";

    private SBuildServer getMockedBuildServer(final String serverVersion) {

        return MockSBuildServer.getMockedSBuildServer(serverVersion);
    }

    private ServerPaths getMockedServerPaths(final String configDir) {

        return MockServerPaths.getMockedServerPaths(parentDir, configDir);
    }

    private SRunningBuild getMockedSRunningBuild(final TestBuildLog buildLog) {

        return MockSRunningBuild.getMockedSRunningBuild(buildLog);
    }

    private EventDispatcher getEventDispatcher() {
        EventDispatcher mockedEventDispatcher = Mockito.mock(EventDispatcher.class);
        Mockito.doNothing().when(mockedEventDispatcher).addListener(Mockito.any(BuildServerAdapter.class));
        return mockedEventDispatcher;
    }

    private HubServerListener getHubServerListener() {
        ServerPaths serverPaths = getMockedServerPaths("NoConfig");
        SBuildServer buildServer = getMockedBuildServer("TestVersion");
        EventDispatcher dispatcher = getEventDispatcher();

        return new HubServerListener(dispatcher, buildServer, serverPaths);
    }

    @Test
    public void testConstructor() {
        HubServerListener listener = getHubServerListener();
        assertNotNull(new HubParametersPreprocessor(listener));
    }

    @Test
    public void testFixRunBuildParametersNoParameters() {
        HubServerListener listener = getHubServerListener();
        HubParametersPreprocessor preprocessor = new HubParametersPreprocessor(listener);

        Map<String, String> runParameters = new HashMap<String, String>();
        Map<String, String> buildParameters = new HashMap<String, String>();

        TestBuildLog buildLog = new TestBuildLog();
        SRunningBuild build = getMockedSRunningBuild(buildLog);

        preprocessor.fixRunBuildParameters(build, runParameters, buildParameters);

        assertTrue(String.valueOf(buildLog.getMessages().size()), buildLog.getMessages().size() == 0);
    }

    @Test
    public void testFixRunBuildParametersBlankPhaseAndDistribution() {
        HubServerListener listener = getHubServerListener();
        HubParametersPreprocessor preprocessor = new HubParametersPreprocessor(listener);

        Map<String, String> runParameters = new HashMap<String, String>();
        Map<String, String> buildParameters = new HashMap<String, String>();

        TestBuildLog buildLog = new TestBuildLog();
        SRunningBuild build = getMockedSRunningBuild(buildLog);

        runParameters.put(HubConstantValues.HUB_VERSION_PHASE, "");
        runParameters.put(HubConstantValues.HUB_VERSION_DISTRIBUTION, "");

        preprocessor.fixRunBuildParameters(build, runParameters, buildParameters);
        assertTrue(String.valueOf(buildLog.getMessages().size()), buildLog.getMessages().size() == 0);
    }

    @Test
    public void testFixRunBuildParametersOnlyPhaseAndDistribution() {
        HubServerListener listener = getHubServerListener();
        HubParametersPreprocessor preprocessor = new HubParametersPreprocessor(listener);

        Map<String, String> runParameters = new HashMap<String, String>();
        Map<String, String> buildParameters = new HashMap<String, String>();

        TestBuildLog buildLog = new TestBuildLog();
        SRunningBuild build = getMockedSRunningBuild(buildLog);

        runParameters.put(HubConstantValues.HUB_VERSION_PHASE, PhaseEnum.DEVELOPMENT.name());
        runParameters.put(HubConstantValues.HUB_VERSION_DISTRIBUTION, DistributionEnum.INTERNAL.name());

        preprocessor.fixRunBuildParameters(build, runParameters, buildParameters);
        assertTrue(String.valueOf(buildLog.getMessages().size()), buildLog.getMessages().size() == 1);

        assertTrue(runParameters.containsKey(HubConstantValues.HUB_VERSION_PHASE));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_VERSION_DISTRIBUTION));

        assertTrue(runParameters.containsKey(HubConstantValues.HUB_URL));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_USERNAME));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_PASSWORD));
        assertTrue(!runParameters.containsKey(HubConstantValues.HUB_PROXY_HOST));
        assertTrue(!runParameters.containsKey(HubConstantValues.HUB_PROXY_PORT));
        assertTrue(!runParameters.containsKey(HubConstantValues.HUB_NO_PROXY_HOSTS));
        assertTrue(!runParameters.containsKey(HubConstantValues.HUB_PROXY_USER));
        assertTrue(!runParameters.containsKey(HubConstantValues.HUB_PROXY_PASS));

        assertTrue(StringUtils.isBlank(runParameters.get(HubConstantValues.HUB_URL)));
        assertTrue(StringUtils.isBlank(runParameters.get(HubConstantValues.HUB_USERNAME)));
        assertTrue(StringUtils.isBlank(runParameters.get(HubConstantValues.HUB_PASSWORD)));
    }

    @Test
    public void testFixRunBuildParametersNoProxy() {
        HubServerListener listener = getHubServerListener();
        HubParametersPreprocessor preprocessor = new HubParametersPreprocessor(listener);

        HubCredentialsBean credentials = new HubCredentialsBean("TestUser", "TestPassword");
        HubProxyInfo proxyInfo = new HubProxyInfo();

        listener.getConfigManager().getConfiguredServer().setHubUrl("testServerUrl");
        listener.getConfigManager().getConfiguredServer().setGlobalCredentials(credentials);
        listener.getConfigManager().getConfiguredServer().setProxyInfo(proxyInfo);

        Map<String, String> runParameters = new HashMap<String, String>();
        Map<String, String> buildParameters = new HashMap<String, String>();

        TestBuildLog buildLog = new TestBuildLog();
        SRunningBuild build = getMockedSRunningBuild(buildLog);

        runParameters.put(HubConstantValues.HUB_VERSION_PHASE, PhaseEnum.DEVELOPMENT.name());
        runParameters.put(HubConstantValues.HUB_VERSION_DISTRIBUTION, DistributionEnum.EXTERNAL.name());

        preprocessor.fixRunBuildParameters(build, runParameters, buildParameters);

        assertTrue(String.valueOf(buildLog.getMessages().size()), buildLog.getMessages().size() == 1);

        boolean protexEnabledMessage = false;

        for (LogMessage message : buildLog.getMessages()) {
            if (message.getText().contains("Hub Plugin enabled.")) {
                protexEnabledMessage = true;
            }
        }
        assertTrue(protexEnabledMessage);

        assertTrue(runParameters.containsKey(HubConstantValues.HUB_VERSION_PHASE));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_VERSION_DISTRIBUTION));

        assertTrue(runParameters.containsKey(HubConstantValues.HUB_URL));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_USERNAME));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_PASSWORD));
        assertTrue(!runParameters.containsKey(HubConstantValues.HUB_PROXY_HOST));
        assertTrue(!runParameters.containsKey(HubConstantValues.HUB_PROXY_PORT));
        assertTrue(!runParameters.containsKey(HubConstantValues.HUB_NO_PROXY_HOSTS));
        assertTrue(!runParameters.containsKey(HubConstantValues.HUB_PROXY_USER));
        assertTrue(!runParameters.containsKey(HubConstantValues.HUB_PROXY_PASS));

        assertEquals("testServerUrl", runParameters.get(HubConstantValues.HUB_URL));
        assertEquals("TestUser", runParameters.get(HubConstantValues.HUB_USERNAME));
        assertEquals("TestPassword", runParameters.get(HubConstantValues.HUB_PASSWORD));
    }

    @Test
    public void testFixRunBuildParametersPassThroughProxy() {
        HubServerListener listener = getHubServerListener();
        HubParametersPreprocessor preprocessor = new HubParametersPreprocessor(listener);

        HubCredentialsBean credentials = new HubCredentialsBean("TestUser", "TestPassword");
        HubProxyInfo proxyInfo = new HubProxyInfo();
        proxyInfo.setHost("testProxyHost");
        proxyInfo.setPort(3128);
        proxyInfo.setIgnoredProxyHosts("ignoreHost, host");

        listener.getConfigManager().getConfiguredServer().setHubUrl("testServerUrl");
        listener.getConfigManager().getConfiguredServer().setGlobalCredentials(credentials);
        listener.getConfigManager().getConfiguredServer().setProxyInfo(proxyInfo);

        Map<String, String> runParameters = new HashMap<String, String>();
        Map<String, String> buildParameters = new HashMap<String, String>();

        TestBuildLog buildLog = new TestBuildLog();
        SRunningBuild build = getMockedSRunningBuild(buildLog);

        runParameters.put(HubConstantValues.HUB_VERSION_PHASE, PhaseEnum.DEVELOPMENT.name());
        runParameters.put(HubConstantValues.HUB_VERSION_DISTRIBUTION, DistributionEnum.EXTERNAL.name());

        preprocessor.fixRunBuildParameters(build, runParameters, buildParameters);

        assertTrue(String.valueOf(buildLog.getMessages().size()), buildLog.getMessages().size() == 1);

        boolean protexEnabledMessage = false;

        for (LogMessage message : buildLog.getMessages()) {
            if (message.getText().contains("Hub Plugin enabled.")) {
                protexEnabledMessage = true;
            }
        }
        assertTrue(protexEnabledMessage);

        assertTrue(runParameters.containsKey(HubConstantValues.HUB_VERSION_PHASE));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_VERSION_DISTRIBUTION));

        assertTrue(runParameters.containsKey(HubConstantValues.HUB_URL));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_USERNAME));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_PASSWORD));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_PROXY_HOST));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_PROXY_PORT));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_NO_PROXY_HOSTS));
        assertTrue(!runParameters.containsKey(HubConstantValues.HUB_PROXY_USER));
        assertTrue(!runParameters.containsKey(HubConstantValues.HUB_PROXY_PASS));

        assertEquals("testServerUrl", runParameters.get(HubConstantValues.HUB_URL));
        assertEquals("TestUser", runParameters.get(HubConstantValues.HUB_USERNAME));
        assertEquals("TestPassword", runParameters.get(HubConstantValues.HUB_PASSWORD));
        assertEquals("testProxyHost", runParameters.get(HubConstantValues.HUB_PROXY_HOST));
        assertEquals("3128", runParameters.get(HubConstantValues.HUB_PROXY_PORT));
        assertEquals("ignoreHost, host", runParameters.get(HubConstantValues.HUB_NO_PROXY_HOSTS));
    }

    @Test
    public void testFixRunBuildParametersAuthenticatedProxy() {
        HubServerListener listener = getHubServerListener();
        HubParametersPreprocessor preprocessor = new HubParametersPreprocessor(listener);

        HubCredentialsBean credentials = new HubCredentialsBean("TestUser", "TestPassword");
        HubProxyInfo proxyInfo = new HubProxyInfo();
        proxyInfo.setHost("testProxyHost");
        proxyInfo.setPort(3128);
        proxyInfo.setIgnoredProxyHosts("ignoreHost, host");
        proxyInfo.setProxyUsername("proxyUser");
        proxyInfo.setProxyPassword("proxyPassword");

        listener.getConfigManager().getConfiguredServer().setHubUrl("testServerUrl");
        listener.getConfigManager().getConfiguredServer().setGlobalCredentials(credentials);
        listener.getConfigManager().getConfiguredServer().setProxyInfo(proxyInfo);

        Map<String, String> runParameters = new HashMap<String, String>();
        Map<String, String> buildParameters = new HashMap<String, String>();

        TestBuildLog buildLog = new TestBuildLog();
        SRunningBuild build = getMockedSRunningBuild(buildLog);

        runParameters.put(HubConstantValues.HUB_VERSION_PHASE, PhaseEnum.DEVELOPMENT.name());
        runParameters.put(HubConstantValues.HUB_VERSION_DISTRIBUTION, DistributionEnum.EXTERNAL.name());

        preprocessor.fixRunBuildParameters(build, runParameters, buildParameters);

        assertTrue(String.valueOf(buildLog.getMessages().size()), buildLog.getMessages().size() == 1);

        boolean protexEnabledMessage = false;

        for (LogMessage message : buildLog.getMessages()) {
            if (message.getText().contains("Hub Plugin enabled.")) {
                protexEnabledMessage = true;
            }
        }
        assertTrue(protexEnabledMessage);

        assertTrue(runParameters.containsKey(HubConstantValues.HUB_VERSION_PHASE));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_VERSION_DISTRIBUTION));

        assertTrue(runParameters.containsKey(HubConstantValues.HUB_URL));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_USERNAME));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_PASSWORD));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_PROXY_HOST));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_PROXY_PORT));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_NO_PROXY_HOSTS));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_PROXY_USER));
        assertTrue(runParameters.containsKey(HubConstantValues.HUB_PROXY_PASS));

        assertEquals("testServerUrl", runParameters.get(HubConstantValues.HUB_URL));
        assertEquals("TestUser", runParameters.get(HubConstantValues.HUB_USERNAME));
        assertEquals("TestPassword", runParameters.get(HubConstantValues.HUB_PASSWORD));
        assertEquals("testProxyHost", runParameters.get(HubConstantValues.HUB_PROXY_HOST));
        assertEquals("3128", runParameters.get(HubConstantValues.HUB_PROXY_PORT));
        assertEquals("ignoreHost, host", runParameters.get(HubConstantValues.HUB_NO_PROXY_HOSTS));
        assertEquals("proxyUser", runParameters.get(HubConstantValues.HUB_PROXY_USER));
        assertEquals("proxyPassword", runParameters.get(HubConstantValues.HUB_PROXY_PASS));
    }

}
