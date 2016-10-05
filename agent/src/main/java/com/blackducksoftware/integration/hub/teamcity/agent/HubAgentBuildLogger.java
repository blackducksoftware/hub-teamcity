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

import java.io.PrintWriter;
import java.io.StringWriter;

import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;

import jetbrains.buildServer.agent.BuildProgressLogger;

public class HubAgentBuildLogger extends IntLogger {
	private final BuildProgressLogger logger;
	private LogLevel loggerLevel = LogLevel.INFO;

	public HubAgentBuildLogger(final BuildProgressLogger logger) {
		this.logger = logger;
	}

	public BuildProgressLogger getLogger() {
		return logger;
	}

	@Override
	public void setLogLevel(final LogLevel level) {
		loggerLevel = level;
	}

	@Override
	public LogLevel getLogLevel() {
		return loggerLevel;
	}

	public void targetStarted(final String txt) {
		logger.targetStarted(txt);
	}

	public void targetFinished(final String txt) {
		logger.targetFinished(txt);
	}

	/**
	 * Prints the message regardless of the log level
	 */
	public void alwaysLog(final String txt) {
		logger.progressMessage(txt);
	}

	@Override
	public void info(final String txt) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.INFO)) {
			logger.progressMessage(txt);
		}
	}

	@Override
	public void error(final String txt, final Throwable e) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
			logger.error(txt);
			if (e != null) {
				final StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.error(sw.toString());
			}
		}
	}

	@Override
	public void error(final String txt) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
			logger.error(txt);
		}
	}

	@Override
	public void warn(final String txt) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.WARN)) {
			logger.progressMessage(txt);
		}
	}

	@Override
	public void trace(final String txt) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.TRACE)) {
			logger.progressMessage(txt);
		}
	}

	@Override
	public void debug(final String txt) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.DEBUG)) {
			logger.progressMessage(txt);
		}
	}

	@Override
	public void debug(final String txt, final Throwable e) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.DEBUG)) {
			logger.progressMessage(txt);
			if (e != null) {
				final StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.progressMessage(sw.toString());
			}
		}
	}

	@Override
	public void error(final Throwable e) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
			final StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
		}
	}

	@Override
	public void trace(final String txt, final Throwable e) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.TRACE)) {
			logger.progressMessage(txt);
			if (e != null) {
				final StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.progressMessage(sw.toString());
			}
		}
	}
}
