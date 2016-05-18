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
import com.blackducksoftware.integration.hub.teamcity.server.failure.BDPolicyViolationBuildFeature;

import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

public class BDPolicyViolationBuildFeatureTest {
	private PluginDescriptor getMockedPluginDescriptor() {
		return MockPluginDescriptor.getMockedPluginDescriptor();
	}

	@Test
	public void testConstructor() {
		final BDPolicyViolationBuildFeature feature = new BDPolicyViolationBuildFeature(getMockedPluginDescriptor());
		assertNotNull(feature);
	}

	@Test
	public void testDisplayName() {
		final BDPolicyViolationBuildFeature feature = new BDPolicyViolationBuildFeature(getMockedPluginDescriptor());
		assertEquals(BDPolicyViolationBuildFeature.DISPLAY_NAME, feature.getDisplayName());
	}

	@Test
	public void testEditParametersUrl() {
		final BDPolicyViolationBuildFeature feature = new BDPolicyViolationBuildFeature(getMockedPluginDescriptor());
		assertEquals(feature.getEditParametersUrl(), feature.getEditParametersUrl());
	}

	@Test
	public void testType() {
		final BDPolicyViolationBuildFeature feature = new BDPolicyViolationBuildFeature(getMockedPluginDescriptor());
		assertEquals(HubBundle.POLICY_FAILURE_CONDITION, feature.getType());
	}

	@Test
	public void testPlaceToShow() {
		final BDPolicyViolationBuildFeature feature = new BDPolicyViolationBuildFeature(getMockedPluginDescriptor());
		assertEquals(BuildFeature.PlaceToShow.FAILURE_REASON, feature.getPlaceToShow());
	}

	@Test
	public void testDescribeParameters() {
		final BDPolicyViolationBuildFeature feature = new BDPolicyViolationBuildFeature(getMockedPluginDescriptor());
		assertEquals(BDPolicyViolationBuildFeature.DESCRIPTION, feature.describeParameters(null));
	}

	@Test
	public void testMultipleFeaturesPerBuildTypeAllowed() {
		final BDPolicyViolationBuildFeature buildFeature = new BDPolicyViolationBuildFeature(null);
		assertFalse(buildFeature.isMultipleFeaturesPerBuildTypeAllowed());
	}

}
