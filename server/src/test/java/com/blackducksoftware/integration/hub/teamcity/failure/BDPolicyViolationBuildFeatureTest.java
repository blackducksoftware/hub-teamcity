package com.blackducksoftware.integration.hub.teamcity.failure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

import org.junit.Test;

import com.blackducksoftware.integration.hub.teamcity.common.HubBundle;
import com.blackducksoftware.integration.hub.teamcity.mocks.MockPluginDescriptor;
import com.blackducksoftware.integration.hub.teamcity.server.failure.BDPolicyViolationBuildFeature;

public class BDPolicyViolationBuildFeatureTest {

    private PluginDescriptor getMockedPluginDescriptor() {

        return MockPluginDescriptor.getMockedPluginDescriptor();
    }

    @Test
    public void testConstructor() {
        BDPolicyViolationBuildFeature feature = new BDPolicyViolationBuildFeature(getMockedPluginDescriptor());
        assertNotNull(feature);
    }

    @Test
    public void testDisplayName() {
        BDPolicyViolationBuildFeature feature = new BDPolicyViolationBuildFeature(getMockedPluginDescriptor());
        assertEquals(BDPolicyViolationBuildFeature.DISPLAY_NAME, feature.getDisplayName());
    }

    @Test
    public void testEditParametersUrl() {
        BDPolicyViolationBuildFeature feature = new BDPolicyViolationBuildFeature(getMockedPluginDescriptor());
        assertEquals(feature.getEditParametersUrl(), feature.getEditParametersUrl());
    }

    @Test
    public void testType() {
        BDPolicyViolationBuildFeature feature = new BDPolicyViolationBuildFeature(getMockedPluginDescriptor());
        assertEquals(HubBundle.POLICY_FAILURE_CONDITION, feature.getType());
    }

    @Test
    public void testPlaceToShow() {
        BDPolicyViolationBuildFeature feature = new BDPolicyViolationBuildFeature(getMockedPluginDescriptor());
        assertEquals(BuildFeature.PlaceToShow.FAILURE_REASON, feature.getPlaceToShow());
    }

    @Test
    public void testDescribeParameters() {
        BDPolicyViolationBuildFeature feature = new BDPolicyViolationBuildFeature(getMockedPluginDescriptor());
        assertEquals(BDPolicyViolationBuildFeature.DESCRIPTION, feature.describeParameters(null));
    }

    @Test
    public void testMultipleFeaturesPerBuildTypeAllowed() {
        BDPolicyViolationBuildFeature buildFeature = new BDPolicyViolationBuildFeature(null);
        assertFalse(buildFeature.isMultipleFeaturesPerBuildTypeAllowed());
    }
}
