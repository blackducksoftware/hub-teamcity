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
package com.blackducksoftware.integration.hub.teamcity.agent.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.blackducksoftware.integration.hub.teamcity.agent.util.TestAgentRunningBuild;
import com.blackducksoftware.integration.hub.teamcity.agent.util.TestArtifactsWatcher;
import com.blackducksoftware.integration.hub.teamcity.agent.util.TestBuildRunnerContext;
import com.blackducksoftware.integration.hub.teamcity.common.HubBundle;

import jetbrains.buildServer.agent.AgentBuildRunnerInfo;

public class HubBuildRunnerTest {
	@Test
	public void testConstructor() {
		assertNotNull(new HubBuildRunner(new TestArtifactsWatcher()));
	}

	@Test
	public void testCreateBuildProcess() throws Exception {
		final HubBuildRunner runner = new HubBuildRunner(new TestArtifactsWatcher());
		assertNotNull(runner.createBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext()));
	}

	@Test
	public void testGetRunnerInfo() {
		final HubBuildRunner runner = new HubBuildRunner(new TestArtifactsWatcher());
		final AgentBuildRunnerInfo runnerInfo = runner.getRunnerInfo();

		assertTrue(runnerInfo.canRun(null));

		assertEquals(HubBundle.RUNNER_TYPE, runnerInfo.getType());
	}

}
