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
package com.blackducksoftware.integration.hub.teamcity.agent.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jetbrains.buildServer.BuildProblemData;
import jetbrains.buildServer.agent.BuildProgressLogger;
import jetbrains.buildServer.agent.FlowLogger;
import jetbrains.buildServer.messages.BuildMessage1;

public class TestBuildProgressLogger implements BuildProgressLogger {
    private List<String> messages = new ArrayList<String>();

    private List<String> progressMessages = new ArrayList<String>();

    private List<String> errorMessages = new ArrayList<String>();

    private List<String> startedMessages = new ArrayList<String>();

    private List<String> finishedMessages = new ArrayList<String>();

    public String getMessagesString() {
        final StringBuilder sb = new StringBuilder();
        int i = 0;
        for (final String message : messages) {
            if (i != 0) {
                sb.append("\n");
            }
            sb.append(message);
            i++;
        }

        return sb.toString();
    }

    public String getProgressMessagesString() {
        final StringBuilder sb = new StringBuilder();
        int i = 0;
        for (final String message : progressMessages) {
            if (i != 0) {
                sb.append("\n");
            }
            sb.append(message);
            i++;
        }

        return sb.toString();
    }

    public String getErrorMessagesString() {
        final StringBuilder sb = new StringBuilder();
        int i = 0;
        for (final String message : errorMessages) {
            if (i != 0) {
                sb.append("\n");
            }
            sb.append(message);
            i++;
        }

        return sb.toString();
    }

    public String getStartedMessagesString() {
        final StringBuilder sb = new StringBuilder();
        int i = 0;
        for (final String message : startedMessages) {
            if (i != 0) {
                sb.append("\n");
            }
            sb.append(message);
            i++;
        }
        return sb.toString();
    }

    public String getFinishedMessagesString() {
        final StringBuilder sb = new StringBuilder();
        int i = 0;
        for (final String message : finishedMessages) {
            if (i != 0) {
                sb.append("\n");
            }
            sb.append(message);
            i++;
        }
        return sb.toString();
    }

    public void clearMessages() {
        messages = new ArrayList<String>();
    }

    public void clearProgressMessages() {
        progressMessages = new ArrayList<String>();
    }

    public void clearErrorMessages() {
        errorMessages = new ArrayList<String>();
    }

    public void clearStartedMessages() {
        startedMessages = new ArrayList<String>();
    }

    public void clearFinishedMessages() {
        finishedMessages = new ArrayList<String>();
    }

    public void clearAllOutput() {
        clearMessages();
        clearProgressMessages();
        clearErrorMessages();
        clearStartedMessages();
        clearFinishedMessages();
    }

    public List<String> getMessages() {
        return messages;
    }

    public List<String> getProgressMessages() {
        return progressMessages;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public List<String> getStartedMessages() {
        return startedMessages;
    }

    public List<String> getFinishedMessages() {
        return finishedMessages;
    }

    @Override
    public void error(final String text) {
        errorMessages.add(text);
    }

    @Override
    public void exception(final Throwable e) {
        if (e != null) {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            errorMessages.add(sw.toString());
        }
    }

    @Override
    public void message(final String text) {
        messages.add(text);
    }

    @Override
    public void progressMessage(final String text) {
        progressMessages.add(text);
    }

    @Override
    public void warning(final String text) {
    }

    @Override
    public void logComparisonFailure(final String text, final Throwable e, final String text2, final String text3) {
    }

    @Override
    public void logSuiteFinished(final String text) {
    }

    @Override
    public void logSuiteFinished(final String text, final Date date) {
    }

    @Override
    public void logSuiteStarted(final String text) {
    }

    @Override
    public void logSuiteStarted(final String text, final Date date) {
    }

    @Override
    public void logTestFailed(final String text, final Throwable e) {
    }

    @Override
    public void logTestFailed(final String text, final String text1, final String text2) {
    }

    @Override
    public void logTestFinished(final String text) {
    }

    @Override
    public void logTestFinished(final String text, final Date date) {
    }

    @Override
    public void logTestIgnored(final String text, final String text1) {
    }

    @Override
    public void logTestStarted(final String text) {
    }

    @Override
    public void logTestStarted(final String text, final Date date) {
    }

    @Override
    public void logTestStdErr(final String text, final String text1) {
    }

    @Override
    public void logTestStdOut(final String text, final String text1) {
    }

    @Override
    public void activityFinished(final String text, final String text1) {
    }

    @Override
    public void activityStarted(final String text, final String text1) {
    }

    @Override
    public void activityStarted(final String text, final String text1, final String text2) {
    }

    @Override
    public void buildFailureDescription(final String text) {
    }

    @Override
    public void flush() {
    }

    @Override
    public String getFlowId() {
        return null;
    }

    @Override
    public FlowLogger getFlowLogger(final String text) {
        return null;
    }

    @Override
    public FlowLogger getThreadLogger() {
        return null;
    }

    @Override
    public void ignoreServiceMessages(final Runnable arg0) {
    }

    @Override
    public void internalError(final String text, final String text1, final Throwable e) {
    }

    @Override
    public void logBuildProblem(final BuildProblemData arg0) {
    }

    @Override
    public void logMessage(final BuildMessage1 arg0) {
    }

    @Override
    public void progressFinished() {
    }

    @Override
    public void progressStarted(final String text) {
    }

    @Override
    public void targetFinished(final String text) {
        finishedMessages.add(text);
    }

    @Override
    public void targetStarted(final String text) {
        startedMessages.add(text);
    }

}
