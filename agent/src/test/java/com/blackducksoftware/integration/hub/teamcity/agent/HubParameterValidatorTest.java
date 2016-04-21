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
package com.blackducksoftware.integration.hub.teamcity.agent;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
		final HubParameterValidator validator = new HubParameterValidator(buildLogger);

		assertTrue(!validator.isServerUrlValid(null));

		final String output = testLogger.getErrorMessagesString();

		assertTrue(output, output.contains("There is no Server URL specified"));
	}

	@Test
	public void testIsServerUrlValidBlankUrl() {
		final HubParameterValidator validator = new HubParameterValidator(buildLogger);

		assertTrue(!validator.isServerUrlValid(""));

		final String output = testLogger.getErrorMessagesString();

		assertTrue(output, output.contains("There is no Server URL specified"));
	}

	@Test
	public void testIsServerUrlValidUrlInvalid() {
		final HubParameterValidator validator = new HubParameterValidator(buildLogger);

		assertTrue(!validator.isServerUrlValid("testUrl"));
		final String output = testLogger.getErrorMessagesString();
		assertTrue(output, output.contains("The server URL specified is not a valid URL."));
	}

	@Test
	public void testIsServerUrlValidUrlValid() {
		final HubParameterValidator validator = new HubParameterValidator(buildLogger);

		assertTrue(validator.isServerUrlValid("http://testUrl"));
		assertTrue(testLogger.getErrorMessages().size() == 0);
	}

	@Test
	public void testIsHubCredentialConfiguredEmptyCredentials() {
		final HubParameterValidator validator = new HubParameterValidator(buildLogger);
		assertTrue(!validator.isHubCredentialConfigured(new HubCredentialsBean("")));

		final String output = testLogger.getErrorMessagesString();

		assertTrue(output, output.contains("There is no Hub username specified"));
		assertTrue(output, output.contains("There is no Hub password specified."));
	}

	@Test
	public void testIsHubCredentialConfiguredValidCredentials() {
		final HubParameterValidator validator = new HubParameterValidator(buildLogger);
		final HubCredentialsBean credential = new HubCredentialsBean("user", "password");
		assertTrue(validator.isHubCredentialConfigured(credential));
		assertTrue(testLogger.getErrorMessages().size() == 0);
	}

}
