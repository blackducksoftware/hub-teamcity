/**
 * Black Duck Hub Plug-In for TeamCity Server
 *
 * Copyright (C) 2017 Black Duck Software, Inc.
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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;
import org.springframework.web.servlet.ModelAndView;

import com.blackducksoftware.integration.exception.EncryptionException;
import com.blackducksoftware.integration.exception.IntegrationCertificateException;
import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.global.HubCredentials;
import com.blackducksoftware.integration.hub.global.HubCredentialsFieldEnum;
import com.blackducksoftware.integration.hub.global.HubProxyInfoFieldEnum;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.global.HubServerConfigFieldEnum;
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

    public void checkInput(final HttpServletRequest request, final ActionErrors errors) throws IllegalArgumentException,
            EncryptionException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final HubCredentials credentials = getCredentialsFromRequest(request, "hubUser");

        final String url = request.getParameter("hubUrl");
        final String proxyServer = request.getParameter("hubProxyServer");
        final String proxyPort = request.getParameter("hubProxyPort");
        final String noProxyHosts = request.getParameter("hubNoProxyHost");
        final String proxyUser = request.getParameter("hubProxyUser");
        final String encProxyPassword = request.getParameter("encryptedHubProxyPass");
        final String hubConnectionTimeout = request.getParameter("hubTimeout");

        final HubServerConfigBuilder builder = new HubServerConfigBuilder();
        if (credentials != null) {
            builder.setUsername(credentials.getUsername());
            builder.setPassword(credentials.getDecryptedPassword());
        }
        builder.setHubUrl(url);
        builder.setProxyHost(proxyServer);
        builder.setProxyPort(proxyPort);
        builder.setIgnoredProxyHosts(noProxyHosts);
        builder.setProxyUsername(proxyUser);
        builder.setTimeout(hubConnectionTimeout);

        String proxyPass = getDecryptedWebPassword(encProxyPassword);
        if (isPasswordAstericks(proxyPass) && configPersistenceManager.getHubServerConfig() != null
                && configPersistenceManager.getHubServerConfig().getProxyInfo() != null) {
            proxyPass = configPersistenceManager.getHubServerConfig().getProxyInfo().getDecryptedPassword();
        }
        builder.setProxyPassword(proxyPass);

        final AbstractValidator validator = builder.createValidator();

        final ValidationResults results = validator.assertValid();
        if (results.isSuccess()) {
            final HubServerConfig config = builder.buildObject();
            configPersistenceManager.setHubServerConfig(config);
            configPersistenceManager.setHubImportSSLCert(Boolean.valueOf(request.getParameter("hubImportSSLCert")));
            configPersistenceManager.setHubWorkspaceCheck(Boolean.valueOf(request.getParameter("hubWorkspaceCheck")));
        } else {
            checkForErrors(HubServerConfigFieldEnum.HUBURL, "errorUrl", results, errors);
            checkForErrors(HubServerConfigFieldEnum.HUBTIMEOUT, "errorTimeout", results, errors);

            checkForErrors(HubCredentialsFieldEnum.USERNAME, "errorUserName", results, errors);
            checkForErrors(HubCredentialsFieldEnum.PASSWORD, "errorPassword", results, errors);

            checkForErrors(HubProxyInfoFieldEnum.PROXYHOST, "errorHubProxyServer", results, errors);
            checkForErrors(HubProxyInfoFieldEnum.PROXYPORT, "errorHubProxyPort", results, errors);
            checkForErrors(HubProxyInfoFieldEnum.NOPROXYHOSTS, "errorHubNoProxyHost", results, errors);
            checkForErrors(HubProxyInfoFieldEnum.PROXYUSERNAME, "errorHubProxyUser", results, errors);
            checkForErrors(HubProxyInfoFieldEnum.PROXYPASSWORD, "errorHubProxyPass", results, errors);
        }
    }

    private void checkForErrors(final FieldEnum key, final String fieldId,
            final ValidationResults results, final ActionErrors errors) {
        if (!results.isSuccess()) {
            errors.addError(fieldId, results.getResultString(key));
        }
    }

    public ActionErrors testConnection(final HttpServletRequest request)
            throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException, IOException, IllegalArgumentException, EncryptionException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
        ActionErrors errors = new ActionErrors();
        checkInput(request, errors);

        if (errors.hasNoErrors() || hasSSLErrors(errors)) {
            final HubServerLogger serverLogger = new HubServerLogger();
            String errorMsg = null;
            try {
                HubServerConfig config = null;
                try {
                    config = getHubServerConfigBuilderFromRequest(request).build();
                } catch (final IllegalStateException e) {
                    if (e instanceof IntegrationCertificateException || e.getMessage().toLowerCase().contains("ssl")) {
                        errorMsg = "Certificate could not be imported into the java keystore. Please ensure the correct user has read/write access.";
                    } else {
                        errorMsg = e.getMessage();
                    }
                }
                if (config != null) {
                    // if you can construct a CredentialsRestConnection, it calls setCookies and connects to the hub,
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
        if (isPasswordAstericks(hubPass) && configPersistenceManager.getHubServerConfig() != null
                && configPersistenceManager.getHubServerConfig().getGlobalCredentials() != null) {
            hubPass = configPersistenceManager.getHubServerConfig().getGlobalCredentials().getDecryptedPassword();
        }
        serverConfigBuilder.setPassword(hubPass);
        serverConfigBuilder.setAutoImportHttpsCertificates(Boolean.valueOf(request.getParameter("hubImportSSLCert")));
        serverConfigBuilder.setProxyHost(request.getParameter("hubProxyServer"));
        serverConfigBuilder.setProxyPort(request.getParameter("hubProxyPort"));
        serverConfigBuilder.setIgnoredProxyHosts(request.getParameter("hubNoProxyHost"));
        serverConfigBuilder.setProxyUsername(request.getParameter("hubProxyUser"));
        String proxyPass = getDecryptedWebPassword(request.getParameter("encryptedHubProxyPass"));
        if (isPasswordAstericks(proxyPass) && configPersistenceManager.getHubServerConfig() != null
                && configPersistenceManager.getHubServerConfig().getProxyInfo() != null) {
            proxyPass = configPersistenceManager.getHubServerConfig().getProxyInfo().getDecryptedPassword();
        }
        serverConfigBuilder.setProxyPassword(proxyPass);

        return serverConfigBuilder;
    }

    public RestConnection getRestConnection(final IntLogger logger, final HubServerConfig hubServerConfig) throws EncryptionException {
        return hubServerConfig.createCredentialsRestConnection(logger);
    }

    public HubCredentials getCredentialsFromRequest(final HttpServletRequest request, final String usernameKey)
            throws IllegalArgumentException, EncryptionException {
        final String username = request.getParameter(usernameKey);
        String password = "";
        password = request.getParameter("encryptedHubPass");

        HubCredentials hubCredentials = null;
        final String decryptedPassword = getDecryptedWebPassword(password);
        if (isPasswordAstericks(decryptedPassword) && configPersistenceManager.getHubServerConfig() != null
                && configPersistenceManager.getHubServerConfig().getGlobalCredentials() != null) {
            final String savedPassword = configPersistenceManager.getHubServerConfig().getGlobalCredentials().getDecryptedPassword();
            hubCredentials = new HubCredentials(username, savedPassword);
        } else {
            if (StringUtils.isNotBlank(decryptedPassword)) {
                // Do not change the saved password unless the User has provided a
                // new one
                final String decryptedWebPassword = getDecryptedWebPassword(password);
                if (StringUtils.isNotBlank(decryptedWebPassword)) {
                    hubCredentials = new HubCredentials(username, decryptedWebPassword);
                }
            }
        }

        return hubCredentials;
    }

    private String getDecryptedWebPassword(final String webEncryptedPass)
            throws IllegalArgumentException, EncryptionException {
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
