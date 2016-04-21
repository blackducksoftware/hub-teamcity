/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
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

}
