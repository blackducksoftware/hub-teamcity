/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.teamcity.agent;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.teamcity.common.beans.HubCredentialsBean;

public class HubParameterValidator {
	private final HubAgentBuildLogger logger;

	public HubParameterValidator(final HubAgentBuildLogger logger) {
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
			} catch (final MalformedURLException e) {
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
