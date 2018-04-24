/**
 * Black Duck Hub Plug-In for TeamCity Server
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.hub.teamcity.server.global;

import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.log.LogLevel;

import jetbrains.buildServer.log.Loggers;

public class HubServerLogger extends IntLogger {
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

    @Override
    public void alwaysLog(String txt) {
        Loggers.SERVER.info(txt);
    }
}
