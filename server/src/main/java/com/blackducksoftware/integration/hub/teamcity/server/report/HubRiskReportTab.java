package com.blackducksoftware.integration.hub.teamcity.server.report;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import jetbrains.buildServer.controllers.BuildDataExtensionUtil;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.SimpleCustomTab;
import jetbrains.buildServer.web.openapi.WebControllerManager;

import org.jetbrains.annotations.NotNull;

import com.blackducksoftware.integration.hub.report.api.HubRiskReportData;
import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;
import com.google.gson.Gson;

public class HubRiskReportTab extends SimpleCustomTab {
    private SBuildServer server;

    public HubRiskReportTab(@NotNull final WebControllerManager webControllerManager, SBuildServer server) {
        super(webControllerManager, PlaceId.BUILD_RESULTS_TAB, "hub", "hubRiskReportTab.jsp", "Black Duck HUB Risk Report");

        this.server = server;

        register();
    }

    @Override
    public void fillModel(Map<String, Object> model, HttpServletRequest request) {
        try {
            File riskReportFile = getRiskReportFile(request, server);
            FileReader fileReader = new FileReader(riskReportFile);

            Gson gson = new Gson();
            HubRiskReportData hubRiskReportData = gson.fromJson(fileReader, HubRiskReportData.class);

            model.put("hubRiskReportData", hubRiskReportData);
        } catch (IOException e) {
            Loggers.SERVER.error("Could not read the risk report file: " + e.getMessage());
        }
    }

    @Override
    public boolean isAvailable(HttpServletRequest request) {
        File riskReportFile = getRiskReportFile(request, server);

        return null != riskReportFile && riskReportFile.exists();
    }

    private File getRiskReportFile(HttpServletRequest request, SBuildServer server) {
        try {
            SBuild build = BuildDataExtensionUtil.retrieveBuild(request, server);
            return new File(build.getArtifactsDirectory().getCanonicalPath() + File.separator + HubConstantValues.HUB_RISK_REPORT_FILENAME);
        } catch (IOException e) {
            Loggers.SERVER.error("Could not create the risk report file: " + e.getMessage());
            return null;
        }
    }

}
