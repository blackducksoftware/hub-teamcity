package com.blackducksoftware.integration.hub.teamcity.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import jetbrains.buildServer.messages.Status;
import jetbrains.buildServer.serverSide.buildLog.BlockLogMessage;
import jetbrains.buildServer.serverSide.buildLog.BuildLog;
import jetbrains.buildServer.serverSide.buildLog.LogMessage;
import jetbrains.buildServer.serverSide.buildLog.LogMessageFilter;

public class TestBuildLog implements BuildLog {

    private List<LogMessage> messages = new ArrayList<LogMessage>();

    @Override
    public List<LogMessage> getDefaultFilteredMessages() {

        return null;
    }

    @Override
    public List<LogMessage> getErrorMessages() {

        return null;
    }

    @Override
    public List<LogMessage> getFilteredMessages(LogMessageFilter arg0) {

        return null;
    }

    @Override
    public Date getLastMessageTimestamp() {

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
    public BlockLogMessage closeBlock(String arg0, String arg1, Date arg2, String arg3) {

        return null;
    }

    @Override
    public BlockLogMessage closeProgressBlock(Date arg0, String arg1) {

        return null;
    }

    @Override
    public void dropProgressText(String arg0) {

    }

    @Override
    public LogMessage error(String arg0, String arg1, Date arg2, String arg3, String arg4) {

        return null;
    }

    @Override
    public LogMessage error(String arg0, String arg1, Date arg2, String arg3, String arg4, Collection arg5) {

        return null;
    }

    @Override
    public String getCurrentProgressText() {

        return null;
    }

    @Override
    public BlockLogMessage getLastBlockMessage(String arg0) {

        return null;
    }

    @Override
    public LogMessage getLastMessage() {

        return null;
    }

    @Override
    public LogMessage message(String text, Status status, Date date, String renderingHint, String arg4) {
        LogMessage message = new LogMessage(text, status, date, arg4, false, 0);
        messages.add(message);
        return message;
    }

    @Override
    public LogMessage message(String text, Status status, Date date, String renderingHint, String arg4, Collection tags) {
        LogMessage message = new LogMessage(text, status, date, renderingHint, false, 0, tags);
        messages.add(message);
        return message;
    }

    @Override
    public BlockLogMessage openBlock(String arg0, String arg1, Date arg2, String arg3, String arg4) {

        return null;
    }

    @Override
    public BlockLogMessage openBlock(String arg0, String arg1, Date arg2, String arg3, String arg4, Collection arg5) {

        return null;
    }

    @Override
    public BlockLogMessage openProgressBlock(String arg0, Date arg1, String arg2) {

        return null;
    }

    @Override
    public BlockLogMessage openProgressBlock(String arg0, Date arg1, String arg2, Collection arg3) {

        return null;
    }

    @Override
    public LogMessage progressMessage(String arg0, Date arg1, String arg2) {

        return null;
    }

    @Override
    public LogMessage progressMessage(String arg0, Date arg1, String arg2, Collection arg3) {

        return null;
    }

    @Override
    public void flowFinished(String arg0) {

    }

    @Override
    public Integer flowIdToInt(String arg0) {

        return null;
    }

    @Override
    public void flowStarted(String arg0, String arg1) {

    }

    @Override
    public List<String> getFlowWithChildren(String arg0) {

        return null;
    }

    @Override
    public Integer getParentFlow(Integer arg0) {

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

}
