/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
package com.blackducksoftware.integration.hub.teamcity.server.global;

import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;

import jetbrains.buildServer.log.Loggers;

public class HubServerLogger implements IntLogger {
	private LogLevel loggerLevel = LogLevel.INFO;

	public HubServerLogger() {
	}

	@Override
	public void setLogLevel(final LogLevel level) {
		loggerLevel = level;
	}

	@Override
	public LogLevel getLogLevel() {
		return loggerLevel;
	}

	@Override
	public void info(final String txt) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.INFO)) {
			Loggers.SERVER.info(txt);
		}
	}

	@Override
	public void error(final String txt, final Throwable e) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
			Loggers.SERVER.error(txt, e);
		}
	}

	@Override
	public void error(final String txt) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
			Loggers.SERVER.error(txt);
		}
	}

	@Override
	public void error(final Throwable e) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
			Loggers.SERVER.error(e);
		}
	}

	@Override
	public void warn(final String txt) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.WARN)) {
			Loggers.SERVER.warn(txt);
		}
	}

	@Override
	public void debug(final String txt) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.DEBUG)) {
			Loggers.SERVER.debug(txt);
		}
	}

	@Override
	public void debug(final String txt, final Throwable e) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.DEBUG)) {
			Loggers.SERVER.debug(txt, e);
		}
	}

	@Override
	public void trace(final String txt, final Throwable e) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.TRACE)) {
			Loggers.SERVER.debug(txt, e);
		}
	}

	@Override
	public void trace(final String txt) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.TRACE)) {
			Loggers.SERVER.debug(txt);
		}
	}

}
