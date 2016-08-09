/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
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
 *******************************************************************************/
package com.blackducksoftware.integration.hub.teamcity.server.global;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;
import org.springframework.web.servlet.ModelAndView;

import com.blackducksoftware.integration.hub.builder.HubServerConfigBuilder;
import com.blackducksoftware.integration.hub.builder.ValidationResultEnum;
import com.blackducksoftware.integration.hub.builder.ValidationResults;
import com.blackducksoftware.integration.hub.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.global.GlobalFieldKey;
import com.blackducksoftware.integration.hub.global.HubCredentialsFieldEnum;
import com.blackducksoftware.integration.hub.global.HubProxyInfoFieldEnum;
import com.blackducksoftware.integration.hub.global.HubServerConfig;
import com.blackducksoftware.integration.hub.global.HubServerConfigFieldEnum;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubCredentialsBean;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubProxyInfo;
import com.blackducksoftware.integration.hub.teamcity.common.beans.ServerHubConfigBean;

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
		} else {
			try {
				configPersistenceManager.persist();
			} catch (final Exception e) {
				Loggers.SERVER.error("Error saving Hub configuration", e);
				errors.addError("errorSaving", e.toString());
			}
		}
	}

	public void checkInput(final HttpServletRequest request, final ActionErrors errors) throws IllegalArgumentException,
	EncryptionException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		final HubCredentialsBean credentials = getCredentialsFromRequest(request, "hubUser");

		final String url = request.getParameter("hubUrl");
		final String proxyServer = request.getParameter("hubProxyServer");
		final String proxyPort = request.getParameter("hubProxyPort");
		final String noProxyHosts = request.getParameter("hubNoProxyHost");
		final String proxyUser = request.getParameter("hubProxyUser");
		final String encProxyPassword = request.getParameter("encryptedHubProxyPass");
		final String hubConnectionTimeout = request.getParameter("hubTimeout");

		final HubServerConfigBuilder builder = new HubServerConfigBuilder();
		builder.setHubUrl(url);
		builder.setUsername(credentials.getHubUser());
		builder.setPassword(credentials.getDecryptedPassword());
		builder.setProxyHost(proxyServer);
		builder.setProxyPort(proxyPort);
		builder.setIgnoredProxyHosts(noProxyHosts);
		builder.setProxyUsername(proxyUser);
		builder.setTimeout(hubConnectionTimeout);

		String proxyPass = getDecryptedWebPassword(encProxyPassword);
		if (StringUtils.isBlank(proxyPass)) {
			proxyPass = configPersistenceManager.getConfiguredServer().getProxyInfo().getProxyPassword();
		}
		builder.setProxyPassword(proxyPass);

		final ValidationResults<GlobalFieldKey, HubServerConfig> results = builder.build();

		if (results.isSuccess()) {
			final HubServerConfig config = results.getConstructedObject();
			configPersistenceManager.getConfiguredServer().setGlobalCredentials(credentials);
			configPersistenceManager.getConfiguredServer().setHubUrl(url);
			configPersistenceManager.getConfiguredServer().setHubTimeout(Integer.valueOf(hubConnectionTimeout));
			HubProxyInfo proxyInfo = null;
			if (config.getProxyInfo() != null && StringUtils.isNotBlank(config.getProxyInfo().getHost())) {
				proxyInfo = new HubProxyInfo(config.getProxyInfo().getHost(), config.getProxyInfo().getPort(),
						config.getProxyInfo().getIgnoredProxyHosts(), config.getProxyInfo().getUsername(),
						config.getProxyInfo().getDecryptedPassword());
			} else {
				proxyInfo = new HubProxyInfo();
			}
			// Need to add this here to make sure the proxy settings are used in
			// the checkUrl
			configPersistenceManager.getConfiguredServer().setProxyInfo(proxyInfo);
		} else {
			checkForErrors(HubServerConfigFieldEnum.HUBURL, "errorUrl", results, errors);
			checkForErrors(HubServerConfigFieldEnum.HUBTIMEOUT, "errorTimeout", results, errors);

			if (results.hasErrors(HubServerConfigFieldEnum.CREDENTIALS)) {
				checkForErrors(HubCredentialsFieldEnum.USERNAME, "errorUserName", results, errors);
				checkForErrors(HubCredentialsFieldEnum.PASSWORD, "errorPassword", results, errors);
			}

			if (results.hasErrors(HubServerConfigFieldEnum.PROXYINFO)) {
				checkForErrors(HubProxyInfoFieldEnum.PROXYHOST, "errorHubProxyServer", results, errors);
				checkForErrors(HubProxyInfoFieldEnum.PROXYPORT, "errorHubProxyPort", results, errors);
				checkForErrors(HubProxyInfoFieldEnum.NOPROXYHOSTS, "errorHubNoProxyHost", results, errors);
				checkForErrors(HubProxyInfoFieldEnum.PROXYUSERNAME, "errorHubProxyUser", results, errors);
				checkForErrors(HubProxyInfoFieldEnum.PROXYPASSWORD, "errorHubProxyPass", results, errors);
			}
		}
	}

	private void checkForErrors(final GlobalFieldKey key, final String fieldId,
			final ValidationResults<GlobalFieldKey, HubServerConfig> results, final ActionErrors errors) {
		if (results.hasErrors(key)) {
			errors.addError(fieldId, results.getResultString(key, ValidationResultEnum.ERROR));
		} else if (results.hasWarnings(key)) {
			errors.addError(fieldId, results.getResultString(key, ValidationResultEnum.WARN));
		}
	}

	public ActionErrors testConnection(final HttpServletRequest request)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, IOException, IllegalArgumentException, EncryptionException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException {
		final ActionErrors errors = new ActionErrors();
		checkInput(request, errors);
		final ServerHubConfigBean serverConfig = configPersistenceManager.getConfiguredServer();
		final HubProxyInfo proxyInfo = configPersistenceManager.getConfiguredServer().getProxyInfo();

		if (errors.hasNoErrors()) {
			final HubServerLogger serverLogger = new HubServerLogger();
			try {
				serverLogger.info("Validating the credentials for the Server : " + serverConfig.getHubUrl());

				final RestConnection restConnection = getRestConnection(serverConfig, proxyInfo, true);

				final int responseCode = restConnection.setCookies(serverConfig.getGlobalCredentials().getHubUser(),
						serverConfig.getGlobalCredentials().getDecryptedPassword());

				if (responseCode == 401) {
					// If User is Not Authorized, 401 error, an exception should
					// be thrown by the ClientResource
					errors.addError("errorConnection", "The provided credentials are not valid for the Hub server '"
							+ serverConfig.getHubUrl() + "'");
				} else if (responseCode != 200 && responseCode != 204 && responseCode != 202) {
					errors.addError("errorConnection", "Connection failed to the Hub server '"
							+ serverConfig.getHubUrl() + "'. With response code " + responseCode);
				}
			} catch (final Exception e) {
				serverLogger.error(e);
				errors.addError("errorConnection", e.toString());
			}
		}
		return errors;
	}

	public HubCredentialsBean getCredentialsFromRequest(final HttpServletRequest request, final String usernameKey)
			throws IllegalArgumentException, EncryptionException {
		final HubCredentialsBean credentialsBean = new HubCredentialsBean(request.getParameter(usernameKey));
		String password = "";
		password = request.getParameter("encryptedHubPass");

		if (StringUtils.isNotBlank(getDecryptedWebPassword(password))) {
			// Do not change the saved password unless the User has provided a
			// new one
			final String decryptedWebPassword = getDecryptedWebPassword(password);
			if (StringUtils.isNotBlank(decryptedWebPassword)) {
				credentialsBean.setEncryptedPassword(PasswordEncrypter.encrypt(decryptedWebPassword));
				credentialsBean.setActualPasswordLength(decryptedWebPassword.length());
			}
		} else {
			if (configPersistenceManager.getConfiguredServer().getGlobalCredentials() != null) {
				credentialsBean.setEncryptedPassword(
						configPersistenceManager.getConfiguredServer().getGlobalCredentials().getEncryptedPassword());

				credentialsBean.setActualPasswordLength(configPersistenceManager.getConfiguredServer()
						.getGlobalCredentials().getActualPasswordLength());
			}
		}

		return credentialsBean;
	}

	private String getDecryptedWebPassword(final String webEncryptedPass)
			throws IllegalArgumentException, EncryptionException {
		if (StringUtils.isNotBlank(webEncryptedPass)) {
			final String webDecryptedPass = RSACipher.decryptWebRequestData(webEncryptedPass);

			if (StringUtils.isNotBlank(webDecryptedPass)) {
				final String maskedString = webDecryptedPass.replace("*", "");

				if (StringUtils.isBlank(maskedString)) {
					// Then the string was just a masked version of the password
					// no new password to save
					return "";
				} else {
					return webDecryptedPass;
				}
			}
		}
		return "";
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

	private RestConnection getRestConnection(final ServerHubConfigBean serverConfig, final HubProxyInfo proxyInfo,
			final boolean isTestConnection) throws HubIntegrationException, URISyntaxException, IOException,
	NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
	BDRestException, EncryptionException {
		if (serverConfig == null) {
			return null;
		}
		final RestConnection restConnection = new RestConnection(serverConfig.getHubUrl());
		restConnection.setTimeout(serverConfig.getHubTimeout());
		if (proxyInfo != null) {
			Proxy proxy = null;
			if (StringUtils.isNotBlank(proxyInfo.getHost())
					&& StringUtils.isNotBlank(proxyInfo.getIgnoredProxyHosts())) {
				for (final Pattern p : proxyInfo.getNoProxyHostPatterns()) {
					if (p.matcher(proxyInfo.getHost()).matches()) {
						proxy = Proxy.NO_PROXY;
					}
				}
			}

			if (proxyInfo != null && (proxy == null || proxy != Proxy.NO_PROXY)) {
				if (StringUtils.isNotBlank(proxyInfo.getHost()) && proxyInfo.getPort() != 0) {
					if (StringUtils.isNotBlank(proxyInfo.getProxyUsername())
							&& StringUtils.isNotBlank(proxyInfo.getProxyPassword())) {
						restConnection.setProxyProperties(proxyInfo.getHost(), proxyInfo.getPort(), null,
								proxyInfo.getProxyUsername(), proxyInfo.getProxyPassword());
					} else {
						restConnection.setProxyProperties(proxyInfo.getHost(), proxyInfo.getPort(), null, null, null);
					}
				}
			}
		}

		if (serverConfig != null && !isTestConnection) {
			restConnection.setCookies(serverConfig.getGlobalCredentials().getHubUser(),
					serverConfig.getGlobalCredentials().getDecryptedPassword());
		}

		return restConnection;
	}

}
