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
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String message : messages) {
			if (i != 0) {
				sb.append("\n");
			}
			sb.append(message);
			i++;
		}

		return sb.toString();
	}

	public String getProgressMessagesString() {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String message : progressMessages) {
			if (i != 0) {
				sb.append("\n");
			}
			sb.append(message);
			i++;
		}

		return sb.toString();
	}

	public String getErrorMessagesString() {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String message : errorMessages) {
			if (i != 0) {
				sb.append("\n");
			}
			sb.append(message);
			i++;
		}

		return sb.toString();
	}

	public String getStartedMessagesString() {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String message : startedMessages) {
			if (i != 0) {
				sb.append("\n");
			}
			sb.append(message);
			i++;
		}
		return sb.toString();
	}

	public String getFinishedMessagesString() {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (String message : finishedMessages) {
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
	public void error(String text) {
		errorMessages.add(text);

	}

	@Override
	public void exception(Throwable e) {
		if (e != null) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			errorMessages.add(sw.toString());
		}

	}

	@Override
	public void message(String text) {
		messages.add(text);

	}

	@Override
	public void progressMessage(String text) {
		progressMessages.add(text);
	}

	@Override
	public void warning(String text) {
		// TODO Auto-generated function stub

	}

	@Override
	public void logComparisonFailure(String text, Throwable e, String text2, String text3) {
		// TODO Auto-generated function stub

	}

	@Override
	public void logSuiteFinished(String text) {
		// TODO Auto-generated function stub

	}

	@Override
	public void logSuiteFinished(String text, Date date) {
		// TODO Auto-generated function stub

	}

	@Override
	public void logSuiteStarted(String text) {
		// TODO Auto-generated function stub

	}

	@Override
	public void logSuiteStarted(String text, Date date) {
		// TODO Auto-generated function stub

	}

	@Override
	public void logTestFailed(String text, Throwable e) {
		// TODO Auto-generated function stub

	}

	@Override
	public void logTestFailed(String text, String text1, String text2) {
		// TODO Auto-generated function stub

	}

	@Override
	public void logTestFinished(String text) {
		// TODO Auto-generated function stub

	}

	@Override
	public void logTestFinished(String text, Date date) {
		// TODO Auto-generated function stub

	}

	@Override
	public void logTestIgnored(String text, String text1) {
		// TODO Auto-generated function stub

	}

	@Override
	public void logTestStarted(String text) {
		// TODO Auto-generated function stub

	}

	@Override
	public void logTestStarted(String text, Date date) {
		// TODO Auto-generated function stub

	}

	@Override
	public void logTestStdErr(String text, String text1) {
		// TODO Auto-generated function stub

	}

	@Override
	public void logTestStdOut(String text, String text1) {
		// TODO Auto-generated function stub

	}

	@Override
	public void activityFinished(String text, String text1) {
		// TODO Auto-generated function stub

	}

	@Override
	public void activityStarted(String text, String text1) {
		// TODO Auto-generated function stub

	}

	@Override
	public void activityStarted(String text, String text1, String text2) {
		// TODO Auto-generated function stub

	}

	@Override
	public void buildFailureDescription(String text) {
		// TODO Auto-generated function stub

	}

	@Override
	public void flush() {
		// TODO Auto-generated function stub

	}

	@Override
	public String getFlowId() {
		// TODO Auto-generated function stub
		return null;
	}

	@Override
	public FlowLogger getFlowLogger(String text) {
		// TODO Auto-generated function stub
		return null;
	}

	@Override
	public FlowLogger getThreadLogger() {
		// TODO Auto-generated function stub
		return null;
	}

	@Override
	public void ignoreServiceMessages(Runnable arg0) {
		// TODO Auto-generated function stub

	}

	@Override
	public void internalError(String text, String text1, Throwable e) {
		// TODO Auto-generated function stub

	}

	@Override
	public void logBuildProblem(BuildProblemData arg0) {
		// TODO Auto-generated function stub

	}

	@Override
	public void logMessage(BuildMessage1 arg0) {
		// TODO Auto-generated function stub

	}

	@Override
	public void progressFinished() {
		// TODO Auto-generated function stub

	}

	@Override
	public void progressStarted(String text) {
		// TODO Auto-generated function stub

	}

	@Override
	public void targetFinished(String text) {
		finishedMessages.add(text);
	}

	@Override
	public void targetStarted(String text) {
		startedMessages.add(text);

	}

}
