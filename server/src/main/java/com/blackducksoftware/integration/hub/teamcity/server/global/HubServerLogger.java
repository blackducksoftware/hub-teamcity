package com.blackducksoftware.integration.hub.teamcity.server.global;

import jetbrains.buildServer.log.Loggers;

import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;

public class HubServerLogger implements IntLogger {
    private LogLevel loggerLevel = LogLevel.INFO;

    public HubServerLogger() {
    }

    @Override
    public void setLogLevel(LogLevel level) {
        loggerLevel = level;
    }

    @Override
    public LogLevel getLogLevel() {
        return loggerLevel;
    }

    @Override
    public void info(String txt) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.INFO)) {
            Loggers.SERVER.info(txt);
        }
    }

    @Override
    public void error(String txt, Throwable e) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
            Loggers.SERVER.error(txt, e);
        }
    }

    @Override
    public void error(String txt) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
            Loggers.SERVER.error(txt);
        }
    }

    @Override
    public void error(Throwable e) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
            Loggers.SERVER.error(e);
        }
    }

    @Override
    public void warn(String txt) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.WARN)) {
            Loggers.SERVER.warn(txt);
        }
    }

    @Override
    public void debug(String txt) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.DEBUG)) {
            Loggers.SERVER.debug(txt);
        }
    }

    @Override
    public void debug(String txt, Throwable e) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.DEBUG)) {
            Loggers.SERVER.debug(txt, e);
        }
    }

    @Override
    public void trace(String txt, Throwable e) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.TRACE)) {
            Loggers.SERVER.debug(txt, e);
        }
    }

    @Override
    public void trace(String txt) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.TRACE)) {
            Loggers.SERVER.debug(txt);
        }
    }

}
