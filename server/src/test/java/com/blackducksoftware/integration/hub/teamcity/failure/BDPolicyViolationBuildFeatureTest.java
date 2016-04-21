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
