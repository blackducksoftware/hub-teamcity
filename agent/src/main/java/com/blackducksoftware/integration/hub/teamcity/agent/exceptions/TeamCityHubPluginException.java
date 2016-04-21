package com.blackducksoftware.integration.hub.teamcity.agent.exceptions;

public class TeamCityHubPluginException extends Exception {
	public TeamCityHubPluginException() {
	}

	public TeamCityHubPluginException(final String message) {
		super(message);
	}

	public TeamCityHubPluginException(final Throwable cause) {
		super(cause);
	}

	public TeamCityHubPluginException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
