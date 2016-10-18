package com.blackducksoftware.integration.hub.teamcity.server;

import javax.servlet.http.HttpServletRequest;

public class UrlUtil {

    public static String createTeamcityBaseUrl(final HttpServletRequest request) {
        final String scheme = request.getScheme();
        final String host = request.getHeader("Host");
        final String contextPath = request.getContextPath();
        final String baseUrl = scheme + "://" + host + contextPath;

        return baseUrl;
    }
}
