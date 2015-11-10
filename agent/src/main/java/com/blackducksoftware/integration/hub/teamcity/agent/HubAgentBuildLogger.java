package com.blackducksoftware.integration.hub.teamcity.agent;

import java.io.PrintWriter;
import java.io.StringWriter;

import jetbrains.buildServer.agent.BuildProgressLogger;

import com.blackducksoftware.integration.suite.sdk.logging.IntLogger;
import com.blackducksoftware.integration.suite.sdk.logging.LogLevel;

public class HubAgentBuildLogger implements IntLogger {

    private final BuildProgressLogger logger;

    private LogLevel loggerLevel = LogLevel.INFO;

    public HubAgentBuildLogger(BuildProgressLogger logger) {
        this.logger = logger;
    }

    public BuildProgressLogger getLogger() {
        return logger;
    }

    public void setLogLevel(LogLevel level) {
        loggerLevel = level;
    }

    public LogLevel getLogLevel() {
        return loggerLevel;
    }

    public void targetStarted(String txt) {
        logger.targetStarted(txt);

    }

    public void targetFinished(String txt) {
        logger.targetFinished(txt);

    }

    public void info(String txt) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.INFO)) {
            logger.progressMessage("[INFO] " + txt);
        }
    }

    public void error(String txt, Throwable e) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
            logger.error("[ERROR] " + txt);
            if (e != null) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.error("[ERROR] " + sw.toString());
            }
        }
    }

    public void error(String txt) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
            logger.error("[ERROR] " + txt);
        }
    }

    public void warn(String txt) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.WARN)) {
            logger.progressMessage("[WARN] " + txt);
        }
    }

    public void trace(String txt) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.TRACE)) {
            logger.progressMessage("[TRACE] " + txt);
        }
    }

    public void debug(String txt) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.DEBUG)) {
            logger.progressMessage("[DEBUG] " + txt);
        }
    }

    public void debug(String txt, Throwable e) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.DEBUG)) {
            logger.progressMessage("[DEBUG] " + txt);
            if (e != null) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.progressMessage("[DEBUG] " + sw.toString());
            }
        }
    }

    public void error(Throwable e) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.ERROR)) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            logger.error("[ERROR] " + sw.toString());
        }
    }

    public void trace(String txt, Throwable e) {
        if (LogLevel.isLoggable(loggerLevel, LogLevel.TRACE)) {
            logger.progressMessage("[TRACE] " + txt);
            if (e != null) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.progressMessage("[TRACE] " + sw.toString());
            }
        }
    }

}
