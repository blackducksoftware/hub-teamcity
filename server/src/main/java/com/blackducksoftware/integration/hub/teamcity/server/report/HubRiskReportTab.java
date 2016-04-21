/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
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
				"Black Duck HUB Risk Report");

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
