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
package com.blackducksoftware.integration.hub.teamcity.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.buildLog.BlockLogMessage;
import jetbrains.buildServer.serverSide.buildLog.BuildLog;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import jetbrains.buildServer.serverSide.buildLog.LogMessageFilter;
import jetbrains.buildServer.serverSide.buildLog.MessageAttrs;

public class TestBuildLog implements BuildLog {
    private final List<LogMessage> messages = new ArrayList<LogMessage>();

    @Override
    public List<LogMessage> getDefaultFilteredMessages() {
        return null;
    }

    @Override
    public List<LogMessage> getErrorMessages() {
        return null;
    }

    @Override
    public List<LogMessage> getFilteredMessages(final LogMessageFilter arg0) {
        return null;
    }

    @Override
    public Date getLastMessageTimestamp() {
        return null;
    }

    @NotNull
    @Override
    public String getCurrentPath() {
        return null;
    }

    @Override
    public List<LogMessage> getMessages() {
        return messages;
    }

    @Override
    public Iterator<LogMessage> getMessagesIterator() {
        return null;
    }

    @Override
    public Iterator<LogMessage> getVerboseIterator() {
        return null;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void close() {
    }

    @Override
    public void flush() {
    }

    @Override
    public BlockLogMessage closeBlock(final String arg0, final String arg1, final Date arg2, final String arg3) {
        return null;
    }

    @Override
    public BlockLogMessage closeProgressBlock(final Date arg0, final String arg1) {
        return null;
    }

    @Override
    public void dropProgressText(final String arg0) {
    }

    @Override
    public LogMessage error(final String arg0, final String arg1, final Date arg2, final String arg3,
            final String arg4) {
        return null;
    }

    @Override
    public LogMessage error(final String arg0, final String arg1, final Date arg2, final String arg3, final String arg4,
            @SuppressWarnings("rawtypes") final Collection arg5) {
        return null;
    }

    @Override
    public String getCurrentProgressText() {
        return null;
    }

    @Override
    public BlockLogMessage getLastBlockMessage(final String arg0) {
        return null;
    }

    @Override
    public LogMessage getLastMessage() {
        return null;
    }

    @Override
    public LogMessage message(final String text, final Status status, final Date date, final String renderingHint,
            final String arg4) {
        final LogMessage message = new LogMessage(text, status, date, arg4, false, 0);
        messages.add(message);
        return message;
    }

    @Override
    public LogMessage message(final String text, final Status status, final MessageAttrs attrs) {
        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public LogMessage message(final String text, final Status status, final Date date, final String renderingHint,
            final String arg4, final Collection tags) {
        final LogMessage message = new LogMessage(text, status, date, renderingHint, false, 0, tags);
        messages.add(message);
        return message;
    }

    @Override
    public BlockLogMessage openBlock(final String arg0, final String arg1, final Date arg2, final String arg3,
            final String arg4) {
        return null;
    }

    @Override
    public BlockLogMessage openBlock(final String blockName, final String blockType, final MessageAttrs attrs) {
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public BlockLogMessage openBlock(final String arg0, final String arg1, final Date arg2, final String arg3,
            final String arg4, final Collection arg5) {
        return null;
    }

    @Override
    public BlockLogMessage openProgressBlock(final String arg0, final Date arg1, final String arg2) {
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public BlockLogMessage openProgressBlock(final String arg0, final Date arg1, final String arg2,
            final Collection arg3) {
        return null;
    }

    @Override
    public LogMessage progressMessage(final String arg0, final Date arg1, final String arg2) {
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public LogMessage progressMessage(final String arg0, final Date arg1, final String arg2, final Collection arg3) {
        return null;
    }

    @Override
    public void flowFinished(final String arg0) {
    }

    @Override
    public Integer flowIdToInt(final String arg0) {
        return null;
    }

    @Override
    public void flowStarted(final String arg0, final String arg1) {
    }

    @Override
    public List<String> getFlowWithChildren(final String arg0) {
        return null;
    }

    @Override
    public Integer getParentFlow(final Integer arg0) {
        return null;
    }

    @Override
    public String getSizeEstimate() {
        return null;
    }

    @Override
    public long getSizeEstimateAsLong() {
        return 0;
    }

    @Override
    public File getMainLogFile() {
        return null;
    }

}
