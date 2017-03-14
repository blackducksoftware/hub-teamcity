/**
 * Black Duck Hub Plug-In for TeamCity Common
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.hub.teamcity.common;

public final class HubConstantValues {
    public static final String PLUGIN_PREFIX = "com.blackducksoftware.integration.hub.";

    public static final String PLUGIN_MAVEN_ENABLED = PLUGIN_PREFIX + "mavenEnabled";

    public static final String PLUGIN_GRADLE_ENABLED = PLUGIN_PREFIX + "gradleEnabled";

    public static final String HUB_PROJECT_NAME = PLUGIN_PREFIX + "projectName";

    public static final String HUB_PROJECT_VERSION = PLUGIN_PREFIX + "projectVersion";

    public static final String HUB_GENERATE_RISK_REPORT = PLUGIN_PREFIX + "generateRiskReport";

    public static final String HUB_MAX_WAIT_TIME_FOR_RISK_REPORT = PLUGIN_PREFIX + "maxWaitTimeForRiskReport";

    public static final String HUB_SCAN_MEMORY = PLUGIN_PREFIX + "scanMemory";

    public static final String HUB_CODE_LOCATION_NAME = PLUGIN_PREFIX + "codeLocationName";

    public static final String HUB_DRY_RUN = PLUGIN_PREFIX + "hubDryRun";

    public static final String HUB_SCAN_TARGETS = PLUGIN_PREFIX + "targets";

    public static final String HUB_CLEANUP_LOGS_ON_SUCCESS = PLUGIN_PREFIX + "cleanupOnSuccessfulScan";

    public static final String HUB_UNMAP_PREVIOUS_CODE_LOCATIONS = PLUGIN_PREFIX + "unmapPreviousCodeLocations";

    public static final String HUB_DELETE_PREVIOUS_CODE_LOCATIONS = PLUGIN_PREFIX + "deletePreviousCodeLocations";

    public static final String HUB_EXCLUDE_PATTERNS = PLUGIN_PREFIX + "excludePatterns";

    public static final String HUB_URL = PLUGIN_PREFIX + "hubUrl";

    public static final String HUB_USERNAME = PLUGIN_PREFIX + "hubUser";

    public static final String HUB_PASSWORD = PLUGIN_PREFIX + "hubPass";

    public static final String HUB_PASSWORD_LENGTH = PLUGIN_PREFIX + "hubPassLength";

    public static final String HUB_PROXY_HOST = PLUGIN_PREFIX + "hubProxyServer";

    public static final String HUB_PROXY_PORT = PLUGIN_PREFIX + "hubProxyPort";

    public static final String HUB_NO_PROXY_HOSTS = PLUGIN_PREFIX + "hubNoProxyHost";

    public static final String HUB_PROXY_USER = PLUGIN_PREFIX + "hubProxyUser";

    public static final String HUB_PROXY_PASS = PLUGIN_PREFIX + "hubProxyPass";

    public static final String HUB_PROXY_PASS_LENGTH = PLUGIN_PREFIX + "hubProxyPassLength";

    public static final String HUB_WORKSPACE_CHECK = PLUGIN_PREFIX + "hubWorkspaceCheck";

    public static final String HUB_MAVEN_SCOPES = PLUGIN_PREFIX + "mavenScopes";

    public static final String HUB_GRADLE_CONFIGS = PLUGIN_PREFIX + "gradleConfigs";

    public static final String PLUGIN_LOG = "[Hub Plugin] ";

    public static final String HUB_BUILD_INFO = "build-info.json";

    public static final String HUB_RISK_REPORT_FILENAME = "riskreport.html";

    public static final String HUB_CLI_ENV_VAR = "BD_HUB_SCAN";

    public static final String HUB_LOG_LEVEL = "HUB_LOG_LEVEL";

    public static final String HUB_FAILURE_TYPE = PLUGIN_PREFIX + "hubFailureType";

    public static final String HUB_CONNECTION_TIMEOUT = "hubTimeout";

    public static final String PLUGIN_VERSION = PLUGIN_PREFIX + "hubPluginVersion";

    public static final String PLUGIN_NAME = PLUGIN_PREFIX + "hubPluginName";

    public static final String HUB_RISK_REPORT_DIRECTORY_NAME = "Hub_Risk_Report";
}
