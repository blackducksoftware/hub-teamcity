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
package com.blackducksoftware.integration.hub.teamcity.server.failure;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.blackducksoftware.integration.hub.teamcity.common.HubBundle;

import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

public class BDPolicyViolationBuildFeature extends BuildFeature {
	public final static String DISPLAY_NAME = "Fail Build on Black Duck Hub Policy Violations";
	public final static String DESCRIPTION = "Fail the build if there are any policy violations";

	private final PluginDescriptor pluginDescriptor;

	public BDPolicyViolationBuildFeature(@NotNull final PluginDescriptor pluginDescriptor) {
		this.pluginDescriptor = pluginDescriptor;
	}

	@Override
	@NotNull
	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	@Override
	@Nullable
	public String getEditParametersUrl() {
		return pluginDescriptor.getPluginResourcesPath();
	}

	@Override
	@NotNull
	public String getType() {
		return HubBundle.POLICY_FAILURE_CONDITION;
	}

	@Override
	public BuildFeature.PlaceToShow getPlaceToShow() {
		return BuildFeature.PlaceToShow.FAILURE_REASON;
	}

	@Override
	public String describeParameters(final Map<String, String> params) {
		return DESCRIPTION;
	}

	@Override
	public boolean isMultipleFeaturesPerBuildTypeAllowed() {
		return false;
	}

}
