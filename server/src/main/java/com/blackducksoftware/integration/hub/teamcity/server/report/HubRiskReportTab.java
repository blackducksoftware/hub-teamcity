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
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jetbrains.annotations.NotNull;

import com.blackducksoftware.integration.hub.report.api.HubRiskReportData;
import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;
import com.blackducksoftware.integration.hub.util.HubResourceBundleHelper;
import com.google.gson.Gson;

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
			final File riskReportFile = getRiskReportFile(request, server);
			final FileReader fileReader = new FileReader(riskReportFile);

			final Gson gson = new Gson();
			final HubRiskReportData hubRiskReportData = gson.fromJson(fileReader, HubRiskReportData.class);

			model.put("hubRiskReportData", hubRiskReportData);

			final HubResourceBundleHelper bundle = new HubResourceBundleHelper();
			bundle.setKeyPrefix("hub.riskreport");
			if (null != request.getLocale()) {
				bundle.setLocale(request.getLocale());
			}
			model.put("bundle", bundle);
		} catch (final IOException e) {
			Loggers.SERVER.error("Could not read the risk report file: " + e.getMessage());
		}
	}

	@Override
	public boolean isAvailable(final HttpServletRequest request) {
		final File riskReportFile = getRiskReportFile(request, server);

		return null != riskReportFile && riskReportFile.exists();
	}

	private File getRiskReportFile(final HttpServletRequest request, final SBuildServer server) {
		try {
			final SBuild build = BuildDataExtensionUtil.retrieveBuild(request, server);
			return new File(build.getArtifactsDirectory().getCanonicalPath() + File.separator
					+ HubConstantValues.HUB_RISK_REPORT_FILENAME);
		} catch (final IOException e) {
			Loggers.SERVER.error("Could not create the risk report file: " + e.getMessage());
			return null;
		}
	}

}
