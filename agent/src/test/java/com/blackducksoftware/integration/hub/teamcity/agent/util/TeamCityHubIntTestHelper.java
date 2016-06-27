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
	public TeamCityHubIntTestHelper(final String baseUrl) throws URISyntaxException {
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
		final ClientResource resource = new ClientResource(project.getMeta().getHref());
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
