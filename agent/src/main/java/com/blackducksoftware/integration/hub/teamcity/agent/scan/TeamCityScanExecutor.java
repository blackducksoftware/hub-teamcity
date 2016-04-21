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

public class TeamCityScanExecutor extends ScanExecutor {
	protected TeamCityScanExecutor(final String hubUrl, final String hubUsername, final String hubPassword,
			final List<String> scanTargets, final Integer buildNumber, final HubSupportHelper supportHelper) {
		super(hubUrl, hubUsername, hubPassword, scanTargets, buildNumber, supportHelper);
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
			// The User's password will be at the next index
			indexToMask.add(cmdToOutput.indexOf("--password") + 1);

			for (int i = 0; i < cmdToOutput.size(); i++) {
				if (cmdToOutput.get(i).contains("-Dhttp") && cmdToOutput.get(i).contains("proxyPassword")) {
					indexToMask.add(i);
				}
			}
			for (final Integer index : indexToMask) {
				maskIndex(cmdToOutput, index);
			}

			getLogger().info("Hub CLI command :");
			for (final String current : cmdToOutput) {
				getLogger().info(current);
			}

			// Should use the split stream for the process
			final FileOutputStream outputFileStream = new FileOutputStream(standardOutFile);

			String outputString = "";
			ScannerSplitStream splitOutputStream = new ScannerSplitStream(getLogger(), outputFileStream);

			Process hubCliProcess = new ProcessBuilder(cmd).redirectError(PIPE).redirectOutput(PIPE).start();

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
			getLogger().info(readStream(hubCliProcess.getInputStream()));

			if (outputString.contains("Illegal character in path")
					&& (outputString.contains("Finished in") && outputString.contains("with status FAILURE"))) {
				standardOutFile.delete();
				standardOutFile.createNewFile();

				splitOutputStream = new ScannerSplitStream(getLogger(), outputFileStream);

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
				getLogger().info(readStream(hubCliProcess.getInputStream()));
			} else if (outputString.contains("Illegal character in opaque")
					&& (outputString.contains("Finished in") && outputString.contains("with status FAILURE"))) {
				standardOutFile.delete();
				standardOutFile.createNewFile();

				splitOutputStream = new ScannerSplitStream(getLogger(), outputFileStream);

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
				getLogger().info(readStream(hubCliProcess.getInputStream()));
			}

			getLogger().info("Hub CLI return code : " + returnCode);
			if (logDirectoryPath != null) {
				final File logDirectory = new File(logDirectoryPath);
				if (logDirectory.exists()) {
					getLogger().info(
							"You can view the BlackDuck Scan CLI logs at : '" + logDirectory.getAbsolutePath() + "'");
					getLogger().info("");
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
