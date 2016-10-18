/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.teamcity.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.blackducksoftware.integration.hub.teamcity.agent.util.TestBuildProgressLogger;
import com.blackducksoftware.integration.log.LogLevel;

public class HubAgentBuildLoggerTest {
    @Test
    public void testConstructorNoLogger() {
        assertNotNull(new HubAgentBuildLogger(null));
    }

    @Test
    public void testConstructorWithLogger() {
        assertNotNull(new HubAgentBuildLogger(new TestBuildProgressLogger()));
    }

    @Test
    public void testSetLoggerLevel() {
        final HubAgentBuildLogger logger = new HubAgentBuildLogger(new TestBuildProgressLogger());
        logger.setLogLevel(LogLevel.WARN);
        assertEquals(LogLevel.WARN, logger.getLogLevel());
        logger.setLogLevel(LogLevel.INFO);
        assertEquals(LogLevel.INFO, logger.getLogLevel());
        logger.setLogLevel(LogLevel.DEBUG);
        assertEquals(LogLevel.DEBUG, logger.getLogLevel());
        logger.setLogLevel(LogLevel.ERROR);
        assertEquals(LogLevel.ERROR, logger.getLogLevel());
        logger.setLogLevel(LogLevel.TRACE);
        assertEquals(LogLevel.TRACE, logger.getLogLevel());
    }

    @Test
    public void testTargetStarted() {
        final TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        final HubAgentBuildLogger logger = new HubAgentBuildLogger(testLogger);
        logger.targetStarted("Should be logged");

        final String output = testLogger.getStartedMessagesString();

        assertTrue(output, output.contains("Should be logged"));
    }

    @Test
    public void testTargetFinished() {
        final TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        final HubAgentBuildLogger logger = new HubAgentBuildLogger(testLogger);
        logger.targetFinished("Should be logged");

        final String output = testLogger.getFinishedMessagesString();
        assertTrue(output, output.contains("Should be logged"));
    }

    @Test
    public void testAlwaysLog() {
        final TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        final HubAgentBuildLogger logger = new HubAgentBuildLogger(testLogger);
        logger.setLogLevel(LogLevel.OFF);
        logger.error("Should not be logged");
        logger.warn("Should not be logged");
        logger.info("Should not be logged");
        logger.debug("Should not be logged");
        logger.trace("Should not be logged");

        logger.alwaysLog("Should be logged");

        final String output = testLogger.getProgressMessagesString();

        assertTrue(output, !output.contains("Should not be logged"));
        assertTrue(output, output.contains("Should be logged"));
    }

    @Test
    public void testInfo() {
        final TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        final HubAgentBuildLogger logger = new HubAgentBuildLogger(testLogger);
        logger.setLogLevel(LogLevel.ERROR);
        logger.info("Should not be logged");

        logger.setLogLevel(LogLevel.INFO);
        logger.info("Should be logged");

        logger.setLogLevel(LogLevel.TRACE);
        logger.info("Definitely should be logged");

        final String output = testLogger.getProgressMessagesString();

        assertTrue(output, !output.contains("Should not be logged"));
        assertTrue(output, output.contains("Should be logged"));
        assertTrue(output, output.contains("Definitely should be logged"));
    }

    @Test
    public void testErrorMessageOnly() {
        final TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        final HubAgentBuildLogger logger = new HubAgentBuildLogger(testLogger);
        logger.setLogLevel(LogLevel.ERROR);
        logger.error("Should be logged");

        logger.setLogLevel(LogLevel.INFO);
        logger.error("Totally got logged");

        logger.setLogLevel(LogLevel.TRACE);
        logger.error("Definitely should be logged");

        final String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("Should be logged"));
        assertTrue(output, output.contains("Totally got logged"));
        assertTrue(output, output.contains("Definitely should be logged"));
    }

    @Test
    public void testErrorExceptionOnly() {
        final TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        final HubAgentBuildLogger logger = new HubAgentBuildLogger(testLogger);
        logger.setLogLevel(LogLevel.ERROR);
        logger.error(new Exception("Should be logged"));

        logger.setLogLevel(LogLevel.INFO);
        logger.error(new Exception("Totally got logged"));

        logger.setLogLevel(LogLevel.TRACE);
        logger.error(new Exception("Definitely should be logged"));

        final String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("Should be logged"));
        assertTrue(output, output.contains("Totally got logged"));
        assertTrue(output, output.contains("Definitely should be logged"));
    }

    @Test
    public void testErrorMessageAndException() {
        final TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        final HubAgentBuildLogger logger = new HubAgentBuildLogger(testLogger);
        logger.setLogLevel(LogLevel.ERROR);
        logger.error("Should be logged", new Exception("Error Should be logged"));

        logger.setLogLevel(LogLevel.INFO);
        logger.error("Totally got logged", new Exception("This error Should be logged"));

        logger.setLogLevel(LogLevel.TRACE);
        logger.error("Definitely should be logged", new Exception("This error definitely Should be logged"));

        final String output = testLogger.getErrorMessagesString();

        assertTrue(output, output.contains("Should be logged"));
        assertTrue(output, output.contains("Error Should be logged"));

        assertTrue(output, output.contains("Totally got logged"));
        assertTrue(output, output.contains("This error Should be logged"));

        assertTrue(output, output.contains("Definitely should be logged"));
        assertTrue(output, output.contains("This error definitely Should be logged"));
    }

    @Test
    public void testWarn() {
        final TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        final HubAgentBuildLogger logger = new HubAgentBuildLogger(testLogger);
        logger.setLogLevel(LogLevel.ERROR);
        logger.warn("Should not be logged");

        logger.setLogLevel(LogLevel.WARN);
        logger.warn("Should be logged");

        logger.setLogLevel(LogLevel.INFO);
        logger.warn("Definitely should be logged");

        final String output = testLogger.getProgressMessagesString();

        assertTrue(output, !output.contains("Should not be logged"));
        assertTrue(output, output.contains("Should be logged"));
        assertTrue(output, output.contains("Definitely should be logged"));
    }

    @Test
    public void testDebugMessageOnly() {
        final TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        final HubAgentBuildLogger logger = new HubAgentBuildLogger(testLogger);
        logger.setLogLevel(LogLevel.ERROR);
        logger.debug("Should not be logged");

        logger.setLogLevel(LogLevel.INFO);
        logger.debug("Should be logged either");

        logger.setLogLevel(LogLevel.DEBUG);
        logger.debug("Should be logged");

        logger.setLogLevel(LogLevel.TRACE);
        logger.debug("Definitely should be logged");

        final String output = testLogger.getProgressMessagesString();

        assertTrue(output, !output.contains("Should not be logged"));
        assertTrue(output, !output.contains("Should not be logged either"));
        assertTrue(output, output.contains("Should be logged"));
        assertTrue(output, output.contains("Definitely should be logged"));
    }

    @Test
    public void testDebugMessageAndException() {
        final TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        final HubAgentBuildLogger logger = new HubAgentBuildLogger(testLogger);
        logger.setLogLevel(LogLevel.ERROR);
        logger.debug("Should not be logged", new Exception("Error Should not be logged"));

        logger.setLogLevel(LogLevel.INFO);
        logger.debug("Should not be logged either", new Exception("This error Should not be logged either"));

        logger.setLogLevel(LogLevel.DEBUG);
        logger.debug("Should be logged", new Exception("This error Should be logged"));

        logger.setLogLevel(LogLevel.TRACE);
        logger.debug("Definitely should be logged", new Exception("This error definitely Should be logged"));

        final String output = testLogger.getProgressMessagesString();

        assertTrue(output, !output.contains("Should not be logged"));
        assertTrue(output, !output.contains("Error Should not be logged"));

        assertTrue(output, !output.contains("Should not be logged either"));
        assertTrue(output, !output.contains("This error Should not be logged either"));

        assertTrue(output, output.contains("Should be logged"));
        assertTrue(output, output.contains("This error Should be logged"));

        assertTrue(output, output.contains("Definitely should be logged"));
        assertTrue(output, output.contains("This error definitely Should be logged"));
    }

    @Test
    public void testTraceMessageOnly() {
        final TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        final HubAgentBuildLogger logger = new HubAgentBuildLogger(testLogger);
        logger.setLogLevel(LogLevel.ERROR);
        logger.trace("Should not be logged");

        logger.setLogLevel(LogLevel.DEBUG);
        logger.trace("Should not be logged either");

        logger.setLogLevel(LogLevel.TRACE);
        logger.trace("Definitely should be logged");

        final String output = testLogger.getProgressMessagesString();

        assertTrue(output, !output.contains("Should not be logged"));
        assertTrue(output, !output.contains("Should not be logged either"));
        assertTrue(output, output.contains("Definitely should be logged"));
    }

    @Test
    public void testTraceMessageAndException() {
        final TestBuildProgressLogger testLogger = new TestBuildProgressLogger();
        final HubAgentBuildLogger logger = new HubAgentBuildLogger(testLogger);
        logger.setLogLevel(LogLevel.ERROR);
        logger.trace("Should not be logged", new Exception("Error Should not be logged"));

        logger.setLogLevel(LogLevel.DEBUG);
        logger.trace("Should not be logged either", new Exception("This error Should not be logged either"));

        logger.setLogLevel(LogLevel.TRACE);
        logger.trace("Definitely should be logged", new Exception("This error definitely Should be logged"));

        final String output = testLogger.getProgressMessagesString();

        assertTrue(output, !output.contains("Should not be logged"));
        assertTrue(output, !output.contains("Error Should not be logged"));

        assertTrue(output, !output.contains("Should not be logged either"));
        assertTrue(output, !output.contains("This error Should not be logged either"));

        assertTrue(output, output.contains("Definitely should be logged"));
        assertTrue(output, output.contains("This error definitely Should be logged"));
    }

}
