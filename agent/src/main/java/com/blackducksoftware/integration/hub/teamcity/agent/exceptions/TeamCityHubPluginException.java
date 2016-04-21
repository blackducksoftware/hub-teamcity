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
package com.blackducksoftware.integration.hub.teamcity.agent.exceptions;

public class TeamCityHubPluginException extends Exception {
	private static final long serialVersionUID = -1471078296882978823L;

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
