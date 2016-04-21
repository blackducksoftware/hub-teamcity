package com.blackducksoftware.integration.hub.teamcity.agent.exceptions;

public class TeamCityHubPluginException extends Exception {
	public TeamCityHubPluginException() {
	}

	public TeamCityHubPluginException(String message) {
		super(message);
	}

	public TeamCityHubPluginException(Throwable cause) {
		super(cause);
	}

	public TeamCityHubPluginException(String message, Throwable cause) {
		super(message, cause);
	}

}
