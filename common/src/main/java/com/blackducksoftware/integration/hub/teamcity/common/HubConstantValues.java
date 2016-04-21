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
package com.blackducksoftware.integration.hub.teamcity.common;

public final class HubConstantValues {
	public static final String PLUGIN_PREFIX = "com.blackducksoftware.integration.hub.";
	public static final String PLUGIN_MAVEN_ENABLED = PLUGIN_PREFIX + "mavenEnabled";
	public static final String PLUGIN_GRADLE_ENABLED = PLUGIN_PREFIX + "gradleEnabled";
	public static final String HUB_PROJECT_NAME = PLUGIN_PREFIX + "projectName";
	public static final String HUB_PROJECT_VERSION = PLUGIN_PREFIX + "projectVersion";
	public static final String HUB_VERSION_PHASE = PLUGIN_PREFIX + "phase";
	public static final String HUB_VERSION_DISTRIBUTION = PLUGIN_PREFIX + "distribution";
	public static final String HUB_GENERATE_RISK_REPORT = PLUGIN_PREFIX + "generateRiskReport";
	public static final String HUB_MAX_WAIT_TIME_FOR_RISK_REPORT = PLUGIN_PREFIX + "maxWaitTimeForRiskReport";
	public static final String HUB_CLI_ENV_VAR = "BD_HUB_SCAN";
	public static final String HUB_SCAN_MEMORY = PLUGIN_PREFIX + "scanMemory";
	public static final String HUB_SCAN_TARGETS = PLUGIN_PREFIX + "targets";
	public static final String HUB_URL = PLUGIN_PREFIX + "hubUrl";
	public static final String HUB_USERNAME = PLUGIN_PREFIX + "hubUser";
	public static final String HUB_PASSWORD = PLUGIN_PREFIX + "hubPass";
	public static final String HUB_PROXY_HOST = PLUGIN_PREFIX + "hubProxyServer";
	public static final String HUB_PROXY_PORT = PLUGIN_PREFIX + "hubProxyPort";
	public static final String HUB_NO_PROXY_HOSTS = PLUGIN_PREFIX + "hubNoProxyHost";
	public static final String HUB_PROXY_USER = PLUGIN_PREFIX + "hubProxyUser";
	public static final String HUB_PROXY_PASS = PLUGIN_PREFIX + "hubProxyPass";
	public static final String HUB_MAVEN_SCOPES = PLUGIN_PREFIX + "mavenScopes";
	public static final String HUB_GRADLE_CONFIGS = PLUGIN_PREFIX + "gradleConfigs";
	public static final String PLUGIN_LOG = "[Hub Plugin] ";
	public static final String HUB_BUILD_INFO = "build-info.json";
	public static final String HUB_RISK_REPORT_FILENAME = "hub_risk_report.json";

}
