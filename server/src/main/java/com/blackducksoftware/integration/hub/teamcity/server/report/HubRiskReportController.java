package com.blackducksoftware.integration.hub.teamcity.server.report;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.BuildDataExtensionUtil;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

import org.springframework.web.servlet.ModelAndView;

import com.blackducksoftware.integration.hub.report.api.HubRiskReportData;
import com.blackducksoftware.integration.hub.teamcity.common.HubConstantValues;
import com.google.gson.Gson;

public class HubRiskReportController extends BaseController {
    private PluginDescriptor pluginDescriptor;

    private SBuildServer server;

    public HubRiskReportController(WebControllerManager manager, PluginDescriptor pluginDescriptor, SBuildServer server) {
        manager.registerController(HubConstantValues.HUB_RISK_REPORT_TAB_PATH, this);

        this.pluginDescriptor = pluginDescriptor;
        this.server = server;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        SBuild build = BuildDataExtensionUtil.retrieveBuild(request, server);

        String hubBomReportPath = build.getArtifactsDirectory().getCanonicalPath() + File.separator + HubConstantValues.HUB_RISK_REPORT_FILENAME;
        FileReader fileReader = new FileReader(hubBomReportPath);

        Gson gson = new Gson();
        HubRiskReportData hubRiskReportData = gson.fromJson(fileReader, HubRiskReportData.class);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put("hubRiskReportData", hubRiskReportData);

        return new ModelAndView(pluginDescriptor.getPluginResourcesPath("hubRiskReportTab.jsp"), model);
    }

}
