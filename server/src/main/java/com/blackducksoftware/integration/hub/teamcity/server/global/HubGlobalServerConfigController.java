/**
 * Black Duck Hub Plug-In for TeamCity Server
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.hub.teamcity.server.global;

import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;
import org.springframework.web.servlet.ModelAndView;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationCertificateException;
import com.blackducksoftware.integration.hub.Credentials;
import com.blackducksoftware.integration.hub.CredentialsField;
import com.blackducksoftware.integration.hub.configuration.HubServerConfig;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.configuration.HubServerConfigFieldEnum;
import com.blackducksoftware.integration.hub.proxy.ProxyInfoField;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.log.IntLogger;
import com.blackducksoftware.integration.validator.AbstractValidator;
import com.blackducksoftware.integration.validator.FieldEnum;
import com.blackducksoftware.integration.validator.ValidationResults;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.BaseFormXmlController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.crypt.RSACipher;

public class HubGlobalServerConfigController extends BaseFormXmlController {
    private final ServerHubConfigPersistenceManager configPersistenceManager;

    public HubGlobalServerConfigController(final ServerHubConfigPersistenceManager configPersistenceManager) {
        this.configPersistenceManager = configPersistenceManager;
    }

    @Override
    public ModelAndView doGet(final HttpServletRequest request, final HttpServletResponse response) {
        return null;
    }

    @Override
    public void doPost(final HttpServletRequest req, final HttpServletResponse resp, final Element xmlResponse) {
        if (isTestConnectionRequest(req)) {
            handleTestConnectionRequest(req, xmlResponse);
        } else if (isSavingRequest(req)) {
            handleSaveRequest(req, xmlResponse);
        }
    }

    private void handleTestConnectionRequest(final HttpServletRequest request, final Element xmlResponse) {
        ActionErrors errors = new ActionErrors();
        try {
            errors = testConnection(request);
            if (errors.hasErrors()) {
                errors.serialize(xmlResponse);
            }
        } catch (final Exception e) {
            Loggers.SERVER.error("Error testing Server connection", e);
            errors.addError("errorConnection", e.toString());
        }
    }

    private void handleSaveRequest(final HttpServletRequest request, final Element xmlResponse) {
        final ActionErrors errors = new ActionErrors();
        try {
            checkInput(request, errors);
        } catch (final Exception e) {
            Loggers.SERVER.error("Error with the Hub configuration", e);
            errors.addError("errorSaving", e.toString());
        }
        if (errors.hasErrors()) {
            errors.serialize(xmlResponse);
        }
        try {
            configPersistenceManager.persist();
        } catch (final Exception e) {
            errors.addError("errorSaving", e.toString());
        }

    }

    public void checkInput(final HttpServletRequest request, final ActionErrors errors) throws IllegalArgumentException, EncryptionException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final HubServerConfigBuilder builder = getHubServerConfigBuilderFromRequest(request);

        final AbstractValidator validator = builder.createValidator();

        final ValidationResults results = validator.assertValid();
        if (results.isSuccess()) {
            final HubServerConfig config = builder.buildObject();
            configPersistenceManager.setHubServerConfig(config);
            configPersistenceManager.setHubWorkspaceCheck(Boolean.valueOf(request.getParameter("hubWorkspaceCheck")));
        } else {
            checkForErrors(HubServerConfigFieldEnum.HUBURL, "errorUrl", results, errors);
            checkForErrors(HubServerConfigFieldEnum.HUBTIMEOUT, "errorTimeout", results, errors);

            checkForErrors(CredentialsField.USERNAME, "errorUserName", results, errors);
            checkForErrors(CredentialsField.PASSWORD, "errorPassword", results, errors);

            checkForErrors(ProxyInfoField.PROXYHOST, "errorHubProxyServer", results, errors);
            checkForErrors(ProxyInfoField.PROXYPORT, "errorHubProxyPort", results, errors);
            checkForErrors(ProxyInfoField.NOPROXYHOSTS, "errorHubNoProxyHost", results, errors);
            checkForErrors(ProxyInfoField.PROXYUSERNAME, "errorHubProxyUser", results, errors);
            checkForErrors(ProxyInfoField.PROXYPASSWORD, "errorHubProxyPass", results, errors);
        }
    }

    private void checkForErrors(final FieldEnum key, final String fieldId, final ValidationResults results, final ActionErrors errors) {
        if (!results.isSuccess()) {
            errors.addError(fieldId, results.getResultString(key));
        }
    }

    public ActionErrors testConnection(final HttpServletRequest request) throws IllegalArgumentException, EncryptionException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        ActionErrors errors = new ActionErrors();
        checkInput(request, errors);

        if (errors.hasNoErrors() || hasSSLErrors(errors)) {
            final HubServerLogger serverLogger = new HubServerLogger();
            String errorMsg = null;
            try {
                HubServerConfig config = null;
                try {
                    config = getHubServerConfigBuilderFromRequest(request).build();
                } catch (final IntegrationCertificateException e) {
                    errorMsg = e.getMessage();
                } catch (final IllegalStateException e) {
                    if (e.getMessage().toLowerCase().contains("ssl")) {
                        errorMsg = "Certificate could not be imported into the java keystore. Please ensure the correct user has read/write access.";
                    } else {
                        errorMsg = e.getMessage();
                    }
                }
                if (config != null) {
                    // if you can construct a CredentialsRestConnection, it
                    // calls setCookies and connects to the hub,
                    // throwing an Exception if things go wrong
                    serverLogger.info("Validating the credentials for the Server : " + config.getHubUrl());
                    getRestConnection(serverLogger, config).connect();
                    // If able to connect, it is likely that errors have been resolved
                    errors = new ActionErrors();
                    checkInput(request, errors);
                } else if (errorMsg != null) {
                    errors.addError("errorConnection", errorMsg);
                }
            } catch (final Exception e) {
                serverLogger.error(e);
                errors.addError("errorConnection", e.toString());
            }
        }
        return errors;
    }

    private boolean hasSSLErrors(final ActionErrors errors) {
        final ActionErrors.Error error = errors.findErrorById("errorUrl");
        if (null != error) {
            return error.getMessage().toLowerCase().contains("ssl");
        }
        return false;
    }

    private HubServerConfigBuilder getHubServerConfigBuilderFromRequest(final HttpServletRequest request) throws IllegalArgumentException, EncryptionException {
        final HubServerConfigBuilder serverConfigBuilder = new HubServerConfigBuilder();

        serverConfigBuilder.setHubUrl(request.getParameter("hubUrl"));
        serverConfigBuilder.setTimeout(request.getParameter("hubTimeout"));
        serverConfigBuilder.setUsername(request.getParameter("hubUser"));
        String hubPass = getDecryptedWebPassword(request.getParameter("encryptedHubPass"));
        if (isPasswordAstericks(hubPass) && configPersistenceManager.getHubServerConfig() != null && configPersistenceManager.getHubServerConfig().getGlobalCredentials() != null) {
            hubPass = configPersistenceManager.getHubServerConfig().getGlobalCredentials().getDecryptedPassword();
        }
        serverConfigBuilder.setPassword(hubPass);
        serverConfigBuilder.setAlwaysTrustServerCertificate(Boolean.valueOf(request.getParameter("alwaysTrustServerCertificates")));
        serverConfigBuilder.setProxyHost(request.getParameter("hubProxyServer"));
        serverConfigBuilder.setProxyPort(request.getParameter("hubProxyPort"));
        serverConfigBuilder.setIgnoredProxyHosts(request.getParameter("hubNoProxyHost"));
        serverConfigBuilder.setProxyUsername(request.getParameter("hubProxyUser"));
        String proxyPass = getDecryptedWebPassword(request.getParameter("encryptedHubProxyPass"));
        if (isPasswordAstericks(proxyPass) && configPersistenceManager.getHubServerConfig() != null && configPersistenceManager.getHubServerConfig().getProxyInfo() != null) {
            proxyPass = configPersistenceManager.getHubServerConfig().getProxyInfo().getDecryptedPassword();
        }
        serverConfigBuilder.setProxyPassword(proxyPass);

        return serverConfigBuilder;
    }

    public RestConnection getRestConnection(final IntLogger logger, final HubServerConfig hubServerConfig) throws EncryptionException {
        return hubServerConfig.createCredentialsRestConnection(logger);
    }

    public Credentials getCredentialsFromRequest(final HttpServletRequest request, final String usernameKey) throws IllegalArgumentException, EncryptionException {
        final String username = request.getParameter(usernameKey);
        String password = "";
        password = request.getParameter("encryptedHubPass");

        Credentials hubCredentials = null;
        final String decryptedPassword = getDecryptedWebPassword(password);
        if (isPasswordAstericks(decryptedPassword) && configPersistenceManager.getHubServerConfig() != null && configPersistenceManager.getHubServerConfig().getGlobalCredentials() != null) {
            final String savedPassword = configPersistenceManager.getHubServerConfig().getGlobalCredentials().getDecryptedPassword();
            hubCredentials = new Credentials(username, savedPassword);
        } else {
            if (StringUtils.isNotBlank(decryptedPassword)) {
                // Do not change the saved password unless the User has provided a new one
                final String decryptedWebPassword = getDecryptedWebPassword(password);
                if (StringUtils.isNotBlank(decryptedWebPassword)) {
                    hubCredentials = new Credentials(username, decryptedWebPassword);
                }
            }
        }

        return hubCredentials;
    }

    private String getDecryptedWebPassword(final String webEncryptedPass) throws IllegalArgumentException, EncryptionException {
        if (StringUtils.isNotBlank(webEncryptedPass)) {
            final String webDecryptedPass = RSACipher.decryptWebRequestData(webEncryptedPass);

            if (StringUtils.isNotBlank(webDecryptedPass)) {
                return webDecryptedPass;
            }
        }
        return "";
    }

    private boolean isPasswordAstericks(final String password) {
        if (StringUtils.isNotBlank(password)) {
            final String removedAstericks = password.replace("*", "");
            if (StringUtils.isBlank(removedAstericks)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTestConnectionRequest(final HttpServletRequest request) {
        if (!request.getParameterNames().hasMoreElements()) {
            return false;
        }
        final String testConnectionParamValue = request.getParameter("testConnection");
        return Boolean.valueOf(testConnectionParamValue);
    }

    private boolean isSavingRequest(final HttpServletRequest request) {
        if (!request.getParameterNames().hasMoreElements()) {
            return false;
        }
        final String savingParamValue = request.getParameter("saving");
        return Boolean.valueOf(savingParamValue);
    }

}
