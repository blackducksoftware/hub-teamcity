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
package com.blackducksoftware.integration.hub.teamcity.failure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.blackducksoftware.integration.hub.teamcity.common.HubBundle;
import com.blackducksoftware.integration.hub.teamcity.mocks.MockPluginDescriptor;
import com.blackducksoftware.integration.hub.teamcity.server.failure.HubBuildFeature;

import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

public class HubBuildFeatureTest {
	private PluginDescriptor getMockedPluginDescriptor() {
		return MockPluginDescriptor.getMockedPluginDescriptor();
	}

	@Test
	public void testConstructor() {
		final HubBuildFeature feature = new HubBuildFeature(getMockedPluginDescriptor());
		assertNotNull(feature);
	}

	@Test
	public void testDisplayName() {
		final HubBuildFeature feature = new HubBuildFeature(getMockedPluginDescriptor());
		assertEquals(HubBuildFeature.DISPLAY_NAME, feature.getDisplayName());
	}

	@Test
	public void testEditParametersUrl() {
		final HubBuildFeature feature = new HubBuildFeature(getMockedPluginDescriptor());
		assertEquals(feature.getEditParametersUrl(), feature.getEditParametersUrl());
	}

	@Test
	public void testType() {
		final HubBuildFeature feature = new HubBuildFeature(getMockedPluginDescriptor());
		assertEquals(HubBundle.POLICY_FAILURE_CONDITION, feature.getType());
	}

	@Test
	public void testPlaceToShow() {
		final HubBuildFeature feature = new HubBuildFeature(getMockedPluginDescriptor());
		assertEquals(BuildFeature.PlaceToShow.FAILURE_REASON, feature.getPlaceToShow());
	}

	@Test
	public void testDescribeParameters() {
		final HubBuildFeature feature = new HubBuildFeature(getMockedPluginDescriptor());
		assertEquals("", feature.describeParameters(null));
	}

	@Test
	public void testMultipleFeaturesPerBuildTypeAllowed() {
		final HubBuildFeature buildFeature = new HubBuildFeature(null);
		assertFalse(buildFeature.isMultipleFeaturesPerBuildTypeAllowed());
	}

}
