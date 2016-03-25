package com.blackducksoftware.integration.hub.teamcity.server.report;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;
import jetbrains.buildServer.web.openapi.WebControllerManager;

import org.jetbrains.annotations.NotNull;

import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;

public class HubRiskReportTab extends SimpleCustomTab {
    public HubRiskReportTab(@NotNull final WebControllerManager webControllerManager) {
        super(webControllerManager, PlaceId.BUILD_RESULTS_TAB, "hub", HubConstantValues.HUB_RISK_REPORT_TAB_PATH, "Black Duck HUB Risk Report");
        register();
    }

    @Override
    public void fillModel(Map<String, Object> model, HttpServletRequest request) {
        super.fillModel(model, request);
    }

    @Override
    public boolean isVisible() {
        return super.isVisible();
    }

}
