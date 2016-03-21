package com.blackducksoftware.integration.hub.teamcity.server.report;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;
import jetbrains.buildServer.web.openapi.WebControllerManager;

import org.jetbrains.annotations.NotNull;

public class HubBomReportTab extends SimpleCustomTab {
    public HubBomReportTab(@NotNull final WebControllerManager webControllerManager) {
        super(webControllerManager, PlaceId.BUILD_RESULTS_TAB, "hub", "hubBomReportTab.jsp", "HUB BOM Report");
        register();
    }

    @Override
    public void fillModel(Map<String, Object> model, HttpServletRequest request) {
        super.fillModel(model, request);
    }

}
