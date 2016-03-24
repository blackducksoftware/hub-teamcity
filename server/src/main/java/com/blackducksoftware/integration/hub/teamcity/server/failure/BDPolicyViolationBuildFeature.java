package com.blackducksoftware.integration.hub.teamcity.server.failure;

import java.util.Map;

import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.web.openapi.PluginDescriptor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.blackducksoftware.integration.hub.teamcity.common.HubBundle;

public class BDPolicyViolationBuildFeature extends BuildFeature {

    public final static String DISPLAY_NAME = "Fail Build on Black Duck Hub Policy Violations";

    public final static String DESCRIPTION = "Fail the build if there are any policy violations";

    public final static String PARAMETER_URL = "hubPolicyViolationFeatureEdit.html";

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
        return pluginDescriptor.getPluginResourcesPath(PARAMETER_URL);
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
    public String describeParameters(Map<String, String> params) {
        return DESCRIPTION;
    }

    @Override
    public boolean isMultipleFeaturesPerBuildTypeAllowed() {
        return false;
    }

}
