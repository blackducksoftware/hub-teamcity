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
