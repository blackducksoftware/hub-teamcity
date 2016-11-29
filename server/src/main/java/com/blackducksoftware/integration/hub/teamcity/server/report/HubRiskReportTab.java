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
package com.blackducksoftware.integration.hub.teamcity.server.report;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;
import com.blackducksoftware.integration.hub.teamcity.server.UrlUtil;
import com.blackducksoftware.integration.hub.util.HubResourceBundleHelper;

import jetbrains.buildServer.controllers.BuildDataExtensionUtil;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;
import jetbrains.buildServer.web.openapi.WebControllerManager;

public class HubRiskReportTab extends SimpleCustomTab {
    private final SBuildServer server;

    public HubRiskReportTab(@NotNull final WebControllerManager webControllerManager, final SBuildServer server) {
        super(webControllerManager, PlaceId.BUILD_RESULTS_TAB, "hub", "hubRiskReportTab.jsp",
                "Black Duck Hub Risk Report");
        this.server = server;
        register();
    }

    @Override
    public void fillModel(final Map<String, Object> model, final HttpServletRequest request) {
        try {
            final String hubRiskReportUrl = getRiskReportUrl(request, server);
            model.put("hubRiskReportUrl", hubRiskReportUrl);
            final HubResourceBundleHelper bundle = new HubResourceBundleHelper();
            bundle.setKeyPrefix("hub.riskreport");
            if (request.getLocale() != null) {
                bundle.setLocale(request.getLocale());
            }
            model.put("bundle", bundle);
        } catch (final Exception e) {
            Loggers.SERVER.error("Could not read the risk report file: " + e.getMessage());
        }
        model.put("teamcityBaseUrl", UrlUtil.createTeamcityBaseUrl(request));
    }

    @Override
    public boolean isAvailable(final HttpServletRequest request) {
        final File riskReportFile = getRiskReportFile(request, server);
        return riskReportFile != null && riskReportFile.exists();
    }

    private File getRiskReportFile(final HttpServletRequest request, final SBuildServer server) {
        try {
            final SBuild build = BuildDataExtensionUtil.retrieveBuild(request, server);
            if (build.getArtifactsDirectory() == null) {
                return null;
            }
            return new File(build.getArtifactsDirectory().getCanonicalPath() + File.separator + HubConstantValues.HUB_RISK_REPORT_DIRECTORY_NAME
                    + File.separator + HubConstantValues.HUB_RISK_REPORT_FILENAME);
        } catch (final IOException e) {
            Loggers.SERVER.error("Could not create the risk report file: " + e.getMessage());
            return null;
        }
    }

    private String getRiskReportUrl(final HttpServletRequest request, final SBuildServer server) {
        final SBuild build = BuildDataExtensionUtil.retrieveBuild(request, server);
        if (build.getArtifactsDirectory() == null) {
            return null;
        }
        String externalId = build.getBuildTypeExternalId();
        // based on information found in the TeamCity blog:
        // https://blog.jetbrains.com/teamcity/2012/06/teamcity-ivy-gradle-maven/
        final String prefix = "/repository/download/";
        final String indexHtml = HubConstantValues.HUB_RISK_REPORT_DIRECTORY_NAME + "/riskreport.html";
        final String reportUrl = prefix + externalId + "/" + build.getBuildId() + ":id/" + indexHtml;
        return reportUrl;
    }
}
