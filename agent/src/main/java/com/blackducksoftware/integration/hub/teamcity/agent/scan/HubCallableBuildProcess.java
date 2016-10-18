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
