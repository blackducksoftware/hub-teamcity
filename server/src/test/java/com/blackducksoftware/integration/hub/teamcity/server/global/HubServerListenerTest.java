/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.teamcity.server.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.blackducksoftware.integration.hub.teamcity.common.beans.ServerHubConfigBean;
import com.blackducksoftware.integration.hub.teamcity.mocks.MockSBuildServer;
import com.blackducksoftware.integration.hub.teamcity.mocks.MockServerPaths;

import jetbrains.buildServer.log.LogInitializer;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.EventDispatcher;

public class HubServerListenerTest {
	private static final String parentDir = "config";

	private static PrintStream orgStream = null;
	private static PrintStream orgErrStream = null;
	private static ByteArrayOutputStream byteOutput = null;
	private static PrintStream currStream = null;
	private static Properties testProperties;

	@Mock
	private EventDispatcher<BuildServerListener> mockedEventDispatcher;

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
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final InputStream is = classLoader.getResourceAsStream("test.properties");
		try {
			testProperties.load(is);
		} catch (final IOException e) {
			System.err.println("reading test.properties failed!");
		}
	}

	@Before
	public void testSetup() throws Exception {
		currStream.flush();
		byteOutput.flush();
		byteOutput.reset();
		MockitoAnnotations.initMocks(this);
	}

	@AfterClass
	public static void tearDown() {
		System.setOut(orgStream);
		System.setErr(orgErrStream);
	}

	private SBuildServer getMockedBuildServer(final String serverVersion) {
		return MockSBuildServer.getMockedSBuildServer(serverVersion);
	}

	private ServerPaths getMockedServerPaths(final String configDir) {
		return MockServerPaths.getMockedServerPaths(parentDir, configDir);
	}

	private EventDispatcher<BuildServerListener> getEventDispatcher() {
		Mockito.doNothing().when(mockedEventDispatcher).addListener(Mockito.any(BuildServerAdapter.class));
		return mockedEventDispatcher;
	}

	@Test
	public void testConstructor() throws Exception {
		final ServerPaths serverPaths = getMockedServerPaths("ValidConfig");
		final SBuildServer buildServer = getMockedBuildServer("TestVersion");
		final EventDispatcher<BuildServerListener> dispatcher = getEventDispatcher();

		final HubServerListener listener = new HubServerListener(dispatcher, buildServer, serverPaths);

		final ServerHubConfigPersistenceManager persistenceManager = listener.getConfigManager();
		final File config = persistenceManager.getConfigFile();
		assertTrue(config.getCanonicalPath(), config.getCanonicalPath().contains("test-workspace/config/ValidConfig"));

		final ServerHubConfigBean globalConfig = persistenceManager.getConfiguredServer();

		globalConfig.getHubUrl();
		globalConfig.getGlobalCredentials();
		globalConfig.getProxyInfo();

		assertNotNull(globalConfig.getHubUrl());
		assertEquals(testProperties.getProperty("TEST_HUB_SERVER_URL"), globalConfig.getHubUrl());

		assertNotNull(globalConfig.getGlobalCredentials().getHubUser());
		assertEquals(testProperties.getProperty("TEST_USERNAME"), globalConfig.getGlobalCredentials().getHubUser());

		assertNotNull(globalConfig.getGlobalCredentials().getDecryptedPassword());
		assertEquals(testProperties.getProperty("TEST_PASSWORD"),
				globalConfig.getGlobalCredentials().getDecryptedPassword());

		assertNotNull(globalConfig.getProxyInfo().getHost());
		assertEquals(testProperties.getProperty("TEST_PROXY_HOST_BASIC"), globalConfig.getProxyInfo().getHost());

		assertNotNull(globalConfig.getProxyInfo().getPort());
		assertEquals(Integer.valueOf(testProperties.getProperty("TEST_PROXY_PORT_BASIC")),
				globalConfig.getProxyInfo().getPort());

		assertNotNull(globalConfig.getProxyInfo().getProxyUsername());
		assertEquals(testProperties.getProperty("TEST_PROXY_USER_BASIC"),
				globalConfig.getProxyInfo().getProxyUsername());

		assertNotNull(globalConfig.getProxyInfo().getProxyPassword());
		assertEquals(testProperties.getProperty("TEST_PROXY_PASSWORD_BASIC"),
				globalConfig.getProxyInfo().getProxyPassword());

		assertEquals("testIgnore", globalConfig.getProxyInfo().getIgnoredProxyHosts());
	}

	@Test
	public void testServerStartup() throws Exception {
		final ServerPaths serverPaths = getMockedServerPaths("EmptyConfig");
		final SBuildServer buildServer = getMockedBuildServer("TestVersion");
		final EventDispatcher<BuildServerListener> dispatcher = getEventDispatcher();

		final HubServerListener listener = new HubServerListener(dispatcher, buildServer, serverPaths);
		listener.serverStartup();

		final String output = byteOutput.toString();

		assertTrue(output,
				output.contains("The Black Duck Software Hub Plugin is running on server version 'TestVersion'."));
	}

}
