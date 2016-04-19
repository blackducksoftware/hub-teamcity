package com.blackducksoftware.integration.hub.teamcity.agent.util;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.restlet.data.Cookie;
import org.restlet.data.Method;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
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
	 * @param projectId
	 *            String
	 * @return boolean true if deleted successfully
	 * @throws BDRestException
	 */
	public boolean deleteHubProject(final String projectId) {
		if (StringUtils.isEmpty(projectId)) {
			return false;
		}
		try {
			final Series<Cookie> cookies = getCookies();
			final String url = getBaseUrl() + "/api/v1/projects/" + projectId;
			final ClientResource resource = new ClientResource(url);

			resource.getRequest().setCookies(cookies);
			resource.setMethod(Method.DELETE);

			resource.delete();
			final int responseCode = resource.getResponse().getStatus().getCode();

			if (responseCode != 204) {
				System.out.println("Could not connect to Hub server. Response Code : " + responseCode);
			} else {
				return true;
			}
		} catch (final ResourceException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public ProjectItem getProjectByName(final String projectName)
			throws IOException, BDRestException, URISyntaxException, ProjectDoesNotExistException {
		try {
			return super.getProjectByName(projectName);
		} catch (final BDRestException e) {
			e.printStackTrace();
		}

		return null;
	}

}
