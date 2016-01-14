package com.blackducksoftware.integration.hub.teamcity.server.runner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jetbrains.buildServer.controllers.BaseFormXmlController;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import com.blackducksoftware.integration.hub.teamcity.server.global.ServerHubConfigPersistenceManager;

public class HubRunTypeConfigController extends BaseFormXmlController {

    private final ServerHubConfigPersistenceManager serverPeristanceManager;

    private final String actualUrl;

    private final String actualJsp;

    public HubRunTypeConfigController(@NotNull final String actualUrl, @NotNull final String actualJsp,
            @NotNull final ServerHubConfigPersistenceManager serverPeristanceManager) {
        this.actualUrl = actualUrl;
        this.actualJsp = actualJsp;
        this.serverPeristanceManager = serverPeristanceManager;
    }

    // This throws Exception because the super method throws exception
    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (isPost(request) && (request.getParameter("onServerChange") != null)) {
            return super.doHandle(request, response);
        }
        return doGet(request, response);
    }

    @Override
    protected ModelAndView doGet(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView(actualJsp);
        modelAndView.getModel().put("runnerType", request.getParameter("runnerType"));
        modelAndView.getModel().put("controllerUrl", actualUrl);
        modelAndView.getModel().put("hubConfigPersistenceManager", serverPeristanceManager);
        return modelAndView;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response, Element xmlResponse) {

    }

}