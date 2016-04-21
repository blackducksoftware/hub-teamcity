package com.blackducksoftware.integration.hub.teamcity.agent;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.teamcity.common.beans.HubCredentialsBean;

public class HubParameterValidator {
	private final HubAgentBuildLogger logger;

	public HubParameterValidator(HubAgentBuildLogger logger) {
		this.logger = logger;
	}

	public boolean isServerUrlValid(final String url) {
		boolean validUrl = true;
		if (StringUtils.isBlank(url)) {
			logger.error("There is no Server URL specified");
			validUrl = false;
		} else {
			try {
				new URL(url);
			} catch (MalformedURLException e) {
				logger.error("The server URL specified is not a valid URL.");
				validUrl = false;
			}
		}
		return validUrl;
	}

	public boolean isHubCredentialConfigured(final HubCredentialsBean credential) {
		boolean validCredential = true;
		if (credential == null) {
			logger.error("There are no credentials configured.");
			validCredential = false;
		} else {
			if (StringUtils.isBlank(credential.getHubUser())) {
				logger.error("There is no Hub username specified");
				validCredential = false;
			}
			if (StringUtils.isBlank(credential.getEncryptedPassword())) {
				logger.error("There is no Hub password specified.");
				validCredential = false;
			}
		}
		return validCredential;
	}

}
