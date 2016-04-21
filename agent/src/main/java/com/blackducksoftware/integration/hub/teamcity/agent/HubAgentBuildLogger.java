package com.blackducksoftware.integration.hub.teamcity.agent;

import java.io.PrintWriter;
import java.io.StringWriter;

import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;

import jetbrains.buildServer.agent.BuildProgressLogger;

public class HubAgentBuildLogger implements IntLogger {
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
