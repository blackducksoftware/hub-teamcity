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
package com.blackducksoftware.integration.hub.teamcity.server.failure;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.blackducksoftware.integration.hub.teamcity.common.HubBundle;
import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;

import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

public class HubBuildFeature extends BuildFeature {
	public final static String DISPLAY_NAME = "Fail build on Black Duck Hub Failure Conditions";

	private final PluginDescriptor pluginDescriptor;

	public HubBuildFeature(@NotNull final PluginDescriptor pluginDescriptor) {
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
		return pluginDescriptor.getPluginResourcesPath("hubBuildFeatureEdit.html");
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
		String output = "";

		if (null != params && params.containsKey(HubConstantValues.HUB_FAILURE_TYPE)) {
			final String status = params.get(HubConstantValues.HUB_FAILURE_TYPE);
			final HubFailureType hubFailureType = HubFailureType.getHubFailureType(status);
			if (null != hubFailureType) {
				output = hubFailureType.getParameterDescription();
			}
		}

		return output;
	}

	@Override
	public Map<String, String> getDefaultParameters() {
		final Map<String, String> defaultParams = new HashMap<String, String>();
		defaultParams.put(HubConstantValues.HUB_FAILURE_TYPE, HubFailureType.POLICY_VIOLATIONS.getDescription());

		return defaultParams;
	}

	@Override
	public boolean isMultipleFeaturesPerBuildTypeAllowed() {
		return false;
	}

}
