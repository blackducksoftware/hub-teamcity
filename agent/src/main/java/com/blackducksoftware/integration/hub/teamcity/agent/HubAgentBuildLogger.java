package com.blackducksoftware.integration.hub.teamcity.agent;

import java.io.PrintWriter;
import java.io.StringWriter;

import jetbrains.buildServer.agent.BuildProgressLogger;

import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;

public class HubAgentBuildLogger implements IntLogger {
	private final BuildProgressLogger logger;

	private LogLevel loggerLevel = LogLevel.INFO;

	public HubAgentBuildLogger(BuildProgressLogger logger) {
		this.logger = logger;
	}

	public BuildProgressLogger getLogger() {
		return logger;
	}

	@Override
	public void setLogLevel(LogLevel level) {
		loggerLevel = level;
	}

	@Override
	public LogLevel getLogLevel() {
		return loggerLevel;
	}

	public void targetStarted(String txt) {
		logger.targetStarted(txt);
	}

	public void targetFinished(String txt) {
		logger.targetFinished(txt);
	}

	@Override
	public void info(String txt) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.INFO)) {
			logger.progressMessage(txt);
		}
	}

	@Override
	public void error(String txt, Throwable e) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
			logger.error(txt);
			if (e != null) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.error(sw.toString());
			}
		}
	}

	@Override
	public void error(String txt) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
			logger.error(txt);
		}
	}

	@Override
	public void warn(String txt) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.WARN)) {
			logger.progressMessage(txt);
		}
	}

	@Override
	public void trace(String txt) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.TRACE)) {
			logger.progressMessage(txt);
		}
	}

	@Override
	public void debug(String txt) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.DEBUG)) {
			logger.progressMessage(txt);
		}
	}

	@Override
	public void debug(String txt, Throwable e) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.DEBUG)) {
			logger.progressMessage(txt);
			if (e != null) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.progressMessage(sw.toString());
			}
		}
	}

	@Override
	public void error(Throwable e) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			logger.error(sw.toString());
		}
	}

	@Override
	public void trace(String txt, Throwable e) {
		if (LogLevel.isLoggable(loggerLevel, LogLevel.TRACE)) {
			logger.progressMessage(txt);
			if (e != null) {
				StringWriter sw = new StringWriter();
				e.printStackTrace(new PrintWriter(sw));
				logger.progressMessage(sw.toString());
			}
		}
	}

}
