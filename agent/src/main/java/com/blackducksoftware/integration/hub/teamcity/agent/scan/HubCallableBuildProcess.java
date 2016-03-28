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
