package com.blackducksoftware.integration.hub.teamcity.server.failure;

public enum HubFailureType {
	POLICY_VIOLATIONS(
			"Project has Hub Policy Violations",
			"If the specified Hub Project has policy violations after the Hub scan then the build will fail.",
			"Fail the build if there are any policy violations"),
	LICENSE_VIOLATIONS(
			"Project has Hub License Violations",
			"If the specified Hub Project has license violations after the Hub scan then the build will fail.",
			"Fail the build if there are any license violations"),
	SECURITY_VIOLATIONS(
			"Project has Hub Security Violations",
			"If the specified Hub Project has security violations after the Hub scan then the build will fail.",
			"Fail the build if there are any security violations");

	private String displayName;
	private String description;
	private String parameterDescription;

	private HubFailureType(final String displayName, final String description, final String parameterDescription) {
		this.displayName = displayName;
		this.description = description;
		this.parameterDescription = parameterDescription;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	public String getParameterDescription() {
		return parameterDescription;
	}

	public static HubFailureType getHubFailureType(final String status) {
		try {
			return HubFailureType.valueOf(status);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

}
