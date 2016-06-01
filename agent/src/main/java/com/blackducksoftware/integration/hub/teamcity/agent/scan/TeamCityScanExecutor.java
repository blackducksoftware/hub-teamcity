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

import static java.lang.ProcessBuilder.Redirect.PIPE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.hub.HubSupportHelper;
import com.blackducksoftware.integration.hub.ScanExecutor;
import com.blackducksoftware.integration.hub.ScannerSplitStream;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.teamcity.agent.HubAgentBuildLogger;

public class TeamCityScanExecutor extends ScanExecutor {
	private final HubAgentBuildLogger logger;

	protected TeamCityScanExecutor(final String hubUrl, final String hubUsername, final String hubPassword,
			final List<String> scanTargets, final Integer buildNumber, final HubSupportHelper supportHelper,
			final HubAgentBuildLogger logger) {
		super(hubUrl, hubUsername, hubPassword, scanTargets, buildNumber, supportHelper);
		this.logger = logger;
		setLogger(logger);
	}

	@Override
	public IntLogger getLogger() {
		return logger;
	}

	@Override
	protected String getLogDirectoryPath() throws IOException {
		File logDirectory = new File(getWorkingDirectory());
		logDirectory = new File(logDirectory, "HubScanLogs");
		logDirectory = new File(logDirectory, String.valueOf(getBuildNumber()));
		logDirectory.mkdirs();

		return logDirectory.getAbsolutePath();
	}

	@Override
	protected Result executeScan(final List<String> cmd, final String logDirectoryPath)
			throws HubIntegrationException, InterruptedException {
		try {
			final File logBaseDirectory = new File(getLogDirectoryPath());
			logBaseDirectory.mkdirs();
			final File standardOutFile = new File(logBaseDirectory, "CLI_Output.txt");
			standardOutFile.createNewFile();

			// ////////////////////// Code to mask the password in the logs
			final List<String> cmdToOutput = new ArrayList<String>();
			cmdToOutput.addAll(cmd);

			final ArrayList<Integer> indexToMask = new ArrayList<Integer>();
			if (cmdToOutput.indexOf("--password") != -1) {
				// The User's password will be at the next index
				indexToMask.add(cmdToOutput.indexOf("--password") + 1);
			}

			for (int i = 0; i < cmdToOutput.size(); i++) {
				if (cmdToOutput.get(i).contains("-Dhttp") && cmdToOutput.get(i).contains("proxyPassword")) {
					indexToMask.add(i);
				}
			}
			for (final Integer index : indexToMask) {
				maskIndex(cmdToOutput, index);
			}

			logger.alwaysLog("Hub CLI command :");
			for (final String current : cmdToOutput) {
				logger.alwaysLog(current);
			}

			// Should use the split stream for the process
			final FileOutputStream outputFileStream = new FileOutputStream(standardOutFile);

			String outputString = "";
			ScannerSplitStream splitOutputStream = new ScannerSplitStream(logger, outputFileStream);

			final ProcessBuilder processBuilder = new ProcessBuilder(cmd).redirectError(PIPE).redirectOutput(PIPE);

			processBuilder.environment().put("BD_HUB_PASSWORD", getHubPassword());

			Process hubCliProcess = processBuilder.start();

			// The Cli logs go the error stream for some reason
			StreamRedirectThread redirectThread = new StreamRedirectThread(hubCliProcess.getErrorStream(),
					splitOutputStream);
			redirectThread.start();

			int returnCode = hubCliProcess.waitFor();

			// the join method on the redirect thread will wait until the thread
			// is dead
			// the thread will die when it reaches the end of stream and the run
			// method is finished
			redirectThread.join();

			splitOutputStream.flush();
			splitOutputStream.close();

			if (splitOutputStream.hasOutput()) {
				outputString = splitOutputStream.getOutput();
			}
			logger.info(readStream(hubCliProcess.getInputStream()));

			if (outputString.contains("Illegal character in path")
					&& (outputString.contains("Finished in") && outputString.contains("with status FAILURE"))) {
				standardOutFile.delete();
				standardOutFile.createNewFile();

				splitOutputStream = new ScannerSplitStream(logger, outputFileStream);

				// This version of the CLI can not handle spaces in the log
				// directory
				// Not sure which version of the CLI this issue was fixed
				final int indexOfLogOption = cmd.indexOf("--logDir") + 1;

				String logPath = cmd.get(indexOfLogOption);
				logPath = logPath.replace(" ", "%20");
				cmd.remove(indexOfLogOption);
				cmd.add(indexOfLogOption, logPath);

				hubCliProcess = new ProcessBuilder(cmd).redirectError(PIPE).redirectOutput(PIPE).start();
				// The Cli logs go the error stream for some reason
				redirectThread = new StreamRedirectThread(hubCliProcess.getErrorStream(), splitOutputStream);
				redirectThread.start();

				returnCode = hubCliProcess.waitFor();

				// the join method on the redirect thread will wait until the
				// thread is dead
				// the thread will die when it reaches the end of stream and the
				// run method is finished
				redirectThread.join();

				splitOutputStream.flush();
				splitOutputStream.close();

				if (splitOutputStream.hasOutput()) {
					outputString = splitOutputStream.getOutput();
				}
				logger.info(readStream(hubCliProcess.getInputStream()));
			} else if (outputString.contains("Illegal character in opaque")
					&& (outputString.contains("Finished in") && outputString.contains("with status FAILURE"))) {
				standardOutFile.delete();
				standardOutFile.createNewFile();

				splitOutputStream = new ScannerSplitStream(logger, outputFileStream);

				final int indexOfLogOption = cmd.indexOf("--logDir") + 1;

				String logPath = cmd.get(indexOfLogOption);

				final File logFile = new File(logPath);

				logPath = logFile.toURI().toString();
				cmd.remove(indexOfLogOption);
				cmd.add(indexOfLogOption, logPath);

				hubCliProcess = new ProcessBuilder(cmd).redirectError(PIPE).redirectOutput(PIPE).start();
				// The Cli logs go the error stream for some reason
				redirectThread = new StreamRedirectThread(hubCliProcess.getErrorStream(), splitOutputStream);
				redirectThread.start();

				returnCode = hubCliProcess.waitFor();

				// the join method on the redirect thread will wait until the
				// thread is dead
				// the thread will die when it reaches the end of stream and the
				// run method is finished
				redirectThread.join();

				splitOutputStream.flush();
				splitOutputStream.close();

				if (splitOutputStream.hasOutput()) {
					outputString = splitOutputStream.getOutput();
				}
				logger.info(readStream(hubCliProcess.getInputStream()));
			}

			logger.alwaysLog("Hub CLI return code : " + returnCode);
			if (logDirectoryPath != null) {
				final File logDirectory = new File(logDirectoryPath);
				if (logDirectory.exists()) {
					logger.alwaysLog(
							"You can view the BlackDuck Scan CLI logs at : '" + logDirectory.getAbsolutePath() + "'");
					logger.alwaysLog("");
				}
			}

			if (outputString.contains("Finished in") && outputString.contains("with status SUCCESS")) {
				return Result.SUCCESS;
			} else {
				return Result.FAILURE;
			}
		} catch (final MalformedURLException e) {
			throw new HubIntegrationException("The server URL provided was not a valid", e);
		} catch (final IOException e) {
			throw new HubIntegrationException(e.getMessage(), e);
		} catch (final InterruptedException e) {
			throw new HubIntegrationException(e.getMessage(), e);
		}
	}

	private void maskIndex(final List<String> cmd, final int indexToMask) {
		final String cmdToMask = cmd.get(indexToMask);
		final String[] maskedArray = new String[cmdToMask.length()];
		Arrays.fill(maskedArray, "*");
		final StringBuilder stringBuilder = new StringBuilder();
		for (final String current : maskedArray) {
			stringBuilder.append(current);
		}
		final String maskedCmd = stringBuilder.toString();

		cmd.remove(indexToMask);
		cmd.add(indexToMask, maskedCmd);
	}

	private String readStream(final InputStream stream) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		final StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line + System.lineSeparator());
		}
		return stringBuilder.toString();
	}

}
