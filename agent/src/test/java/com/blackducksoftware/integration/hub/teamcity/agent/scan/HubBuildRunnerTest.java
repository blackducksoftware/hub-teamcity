package com.blackducksoftware.integration.hub.teamcity.agent.scan;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import jetbrains.buildServer.agent.AgentBuildRunnerInfo;

import org.junit.Test;

import com.blackducksoftware.integration.hub.teamcity.agent.util.TestAgentRunningBuild;
import com.blackducksoftware.integration.hub.teamcity.agent.util.TestBuildRunnerContext;
import com.blackducksoftware.integration.hub.teamcity.common.HubBundle;

public class HubBuildRunnerTest {

    @Test
    public void testConstructor() {
        assertNotNull(new HubBuildRunner());

    }

    @Test
    public void testCreateBuildProcess() throws Exception {
        HubBuildRunner runner = new HubBuildRunner();
        assertNotNull(runner.createBuildProcess(new TestAgentRunningBuild(), new TestBuildRunnerContext()));
    }

    @Test
    public void testGetRunnerInfo() {
        HubBuildRunner runner = new HubBuildRunner();
        AgentBuildRunnerInfo runnerInfo = runner.getRunnerInfo();

        assertTrue(runnerInfo.canRun(null));

        assertEquals(HubBundle.RUNNER_TYPE, runnerInfo.getType());
    }

}
