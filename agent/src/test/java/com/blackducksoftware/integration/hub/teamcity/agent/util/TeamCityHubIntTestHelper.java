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
package com.blackducksoftware.integration.hub.teamcity.agent.util;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.restlet.data.Cookie;
import org.restlet.data.Method;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.ProjectDoesNotExistException;
import com.blackducksoftware.integration.hub.project.api.ProjectItem;

public class TeamCityHubIntTestHelper extends HubIntRestService {
	public TeamCityHubIntTestHelper(final String baseUrl) {
		super(baseUrl);
	}

	/**
	 * Delete HubProject. For test purposes only!
	 *
	 */
	public boolean deleteHubProject(final ProjectItem project) throws BDRestException {
		if (project == null) {
			return false;
		}

		final Series<Cookie> cookies = getCookies();
		final ClientResource resource = new ClientResource(project.get_meta().getHref());
		resource.getRequest().setCookies(cookies);
		resource.setMethod(Method.DELETE);
		resource.delete();
		final int responseCode = resource.getResponse().getStatus().getCode();

		if (responseCode != 204) {
			throw new BDRestException(
					"Could not connect to the Hub server with the Given Url and credentials. Error Code: "
							+ responseCode,
							resource);
		} else {
			return true;
		}
	}

	/**
	 * Delete HubProject. For test purposes only!
	 *
	 */
	public boolean deleteHubProject(final String projectUrl) throws BDRestException {
		if (StringUtils.isBlank(projectUrl)) {
			return false;
		}

		final Series<Cookie> cookies = getCookies();
		final ClientResource resource = new ClientResource(projectUrl);
		resource.getRequest().setCookies(cookies);
		resource.setMethod(Method.DELETE);
		resource.delete();
		final int responseCode = resource.getResponse().getStatus().getCode();
		if (responseCode != 204) {
			throw new BDRestException(
					"Could not connect to the Hub server with the Given Url and credentials. Error Code: "
							+ responseCode,
							resource);
		} else {
			return true;
		}
	}

	@Override
	public ProjectItem getProjectByName(final String projectName) throws IOException, URISyntaxException {
		try {
			return super.getProjectByName(projectName);
		} catch (final ProjectDoesNotExistException e) {
			System.out.println(e.getMessage());
		} catch (final BDRestException e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

}
