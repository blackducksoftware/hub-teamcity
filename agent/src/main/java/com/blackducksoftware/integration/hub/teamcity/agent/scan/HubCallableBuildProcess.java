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
package com.blackducksoftware.integration.hub.teamcity.agent.scan;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildFinishedStatus;
import jetbrains.buildServer.agent.BuildProcess;

abstract public class HubCallableBuildProcess implements BuildProcess, Callable<BuildFinishedStatus> {
	private Future<BuildFinishedStatus> future;

	@Override
	public void interrupt() {
		future.cancel(true);
	}

	@Override
	public boolean isFinished() {
		return future.isDone();
	}

	@Override
	public boolean isInterrupted() {
		return future.isCancelled() && isFinished();
	}

	@Override
	public void start() throws RunBuildException {
		try {
			future = Executors.newSingleThreadExecutor().submit(this);
		} catch (final RejectedExecutionException e) {
			throw new RunBuildException(e);
		}
	}

	@Override
	public BuildFinishedStatus waitFor() throws RunBuildException {
		try {
			final BuildFinishedStatus status = future.get();
			return status;
		} catch (final ExecutionException e) {
			throw new RunBuildException(e);
		} catch (final InterruptedException e) {
			throw new RunBuildException(e);
		} catch (final CancellationException e) {
			return BuildFinishedStatus.INTERRUPTED;
		}
	}

}
