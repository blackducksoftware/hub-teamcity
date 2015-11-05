package com.blackducksoftware.integration.hub.teamcity.server.global;

import jetbrains.buildServer.log.Loggers;

import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;
import com.blackducksoftware.integration.suite.sdk.logging.LogLevel;

public class HubServerLogger implements IntLogger {

    private LogLevel loggerLevel = LogLevel.INFO;

    public HubServerLogger() {
    }

    public void setLogLevel(LogLevel level) {
        loggerLevel = level;
    }

    public LogLevel getLogLevel() {
        return loggerLevel;
    }

    public void info(String txt) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.INFO)) {
            Loggers.SERVER.info(txt);
        }
    }

    public void error(String txt, Throwable e) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
            Loggers.SERVER.error(txt, e);
        }
    }

    public void error(String txt) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
            Loggers.SERVER.error(txt);
        }
    }

    public void error(Throwable e) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
            Loggers.SERVER.error(e);
        }
    }

    public void warn(String txt) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.WARN)) {
            Loggers.SERVER.warn(txt);
        }
    }

    public void debug(String txt) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.DEBUG)) {
            Loggers.SERVER.debug(txt);
        }
    }

    public void debug(String txt, Throwable e) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.DEBUG)) {
            Loggers.SERVER.debug(txt, e);
        }
    }

    public void trace(String txt, Throwable e) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.TRACE)) {
            Loggers.SERVER.debug(txt, e);
        }
    }

    public void trace(String txt) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.TRACE)) {
            Loggers.SERVER.debug(txt);
        }
    }

}
