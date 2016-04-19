package com.blackducksoftware.integration.hub.teamcity.server.global;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;
import org.springframework.web.servlet.ModelAndView;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.EncryptionException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
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
	public void doPost(final HttpServletRequest request, final HttpServletResponse response,
			final Element xmlResponse) {
		if (request.getParameterNames().hasMoreElements()) {
			if (isTestConnectionRequest(request)) {
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
				return;
			} else if (isSavingRequest(request)) {
				final ActionErrors errors = new ActionErrors();
				try {
					checkInput(request, errors);
				} catch (final Exception e) {
					Loggers.SERVER.error("Error with the Hub configuration", e);
					errors.addError("errorSaving", e.toString());
				}
				if (errors.hasErrors()) {
					errors.serialize(xmlResponse);
					return;
				} else {
					try {
						configPersistenceManager.persist();
					} catch (final Exception e) {
						Loggers.SERVER.error("Error saving Hub configuration", e);
						errors.addError("errorSaving", e.toString());
					}
				}
			}
		}
		// else {
		// blank request??
		// }
	}

	public void checkInput(final HttpServletRequest request, final ActionErrors errors)
			throws IllegalArgumentException, EncryptionException {
		final HubCredentialsBean credentials = getCredentialsFromRequest(request, "hubUser");
		if (StringUtils.isBlank(credentials.getHubUser())) {
			errors.addError("errorUserName", "Please specify a UserName.");
		}
		if (StringUtils.isBlank(credentials.getEncryptedPassword())) {
			errors.addError("errorPassword", "There is no saved Password. Please specify a Password.");
		}

		HubProxyInfo proxyInfo = null;
		if (configPersistenceManager.getConfiguredServer().getProxyInfo() != null) {
			proxyInfo = configPersistenceManager.getConfiguredServer().getProxyInfo();
		} else {
			proxyInfo = new HubProxyInfo();
		}

		checkProxySettings(request, proxyInfo, errors);

		if (errors.hasNoErrors()) {
			// Need to add this here to make sure the proxy settings are used in
			// the checkUrl
			configPersistenceManager.getConfiguredServer().setProxyInfo(proxyInfo);
		}

		final String url = request.getParameter("hubUrl");
		if (StringUtils.isBlank(url)) {
			errors.addError("errorUrl", "Please specify a URL.");
		} else {
			// if (isTestConnectionRequest(request)) {
			// checkUrl(url, errors, true);
			// } else {
			// checkUrl(url, errors, false);
			// }
			checkUrl(url, errors);
		}

		if (errors.hasNoErrors()) {
			configPersistenceManager.getConfiguredServer().setGlobalCredentials(credentials);
			configPersistenceManager.getConfiguredServer().setHubUrl(url);
		}
	}

	private ActionErrors checkUrl(final String url, final ActionErrors errors) { // ,
																					// boolean
																					// isTestConnection)
																					// {

		URL testUrl = null;
		try {
			testUrl = new URL(url);
			try {
				testUrl.toURI();
			} catch (final URISyntaxException e) {
				errors.addError("errorUrl", "Please specify a valid URL of a Hub server. " + e.toString());
			}
		} catch (final MalformedURLException e) {
			errors.addError("errorUrl", "Please specify a valid URL of a Hub server. " + e.toString());
		}
		// if (isTestConnection) {
		if (testUrl != null) {
			try {
				if (StringUtils.isBlank(System.getProperty("http.maxRedirects"))) {
					// If this property is not set the default is 20
					// When not set the Authenticator redirects in a loop and
					// results in an error for too many redirects
					System.setProperty("http.maxRedirects", "3");
				}
				Proxy proxy = null;
				if (configPersistenceManager.getConfiguredServer().getProxyInfo() != null) {

					final HubProxyInfo proxyInfo = configPersistenceManager.getConfiguredServer().getProxyInfo();

					if (StringUtils.isNotBlank(proxyInfo.getHost())
							&& StringUtils.isNotBlank(proxyInfo.getIgnoredProxyHosts())) {
						for (final Pattern p : proxyInfo.getNoProxyHostPatterns()) {
							if (p.matcher(proxyInfo.getHost()).matches()) {
								proxy = Proxy.NO_PROXY;
							}
						}
					}
					if (proxy == null && StringUtils.isNotBlank(proxyInfo.getHost()) && proxyInfo.getPort() != null) {
						proxy = new Proxy(Proxy.Type.HTTP,
								new InetSocketAddress(proxyInfo.getHost(), proxyInfo.getPort()));
					}
				}
				attemptResetProxyCache();

				if (proxy != null && proxy != Proxy.NO_PROXY) {

					if (StringUtils.isNotBlank(
							configPersistenceManager.getConfiguredServer().getProxyInfo().getProxyUsername())
							&& StringUtils.isNotBlank(
									configPersistenceManager.getConfiguredServer().getProxyInfo().getProxyPassword())) {
						Authenticator.setDefault(new Authenticator() {
							@Override
							public PasswordAuthentication getPasswordAuthentication() {
								return new PasswordAuthentication(
										configPersistenceManager.getConfiguredServer().getProxyInfo()
												.getProxyUsername(),
										configPersistenceManager.getConfiguredServer().getProxyInfo().getProxyPassword()
												.toCharArray());
							}
						});
					} else {
						Authenticator.setDefault(null);
					}
				}
				URLConnection connection = null;
				if (proxy != null) {
					connection = testUrl.openConnection(proxy);
				} else {
					connection = testUrl.openConnection();
				}

				connection.getContent();
			} catch (final IOException ioe) {
				errors.addError("errorUrl", "Trouble reaching the Hub server. " + ioe.toString());
			} catch (final RuntimeException e) {
				errors.addError("errorUrl", "Not a valid Hub server. " + e.toString());
			}
		}
		// }
		return errors;
	}

	private void attemptResetProxyCache() {
		try {
			// works, and resets the cache when using sun classes
			// sun.net.www.protocol.http.AuthCacheValue.setAuthCache(new
			// sun.net.www.protocol.http.AuthCacheImpl());

			// Attempt the same thing using reflection in case they are not
			// using a jdk with sun classes

			Class<?> sunAuthCacheValue;
			Class<?> sunAuthCache;
			Class<?> sunAuthCacheImpl;
			try {
				sunAuthCacheValue = Class.forName("sun.net.www.protocol.http.AuthCacheValue");
				sunAuthCache = Class.forName("sun.net.www.protocol.http.AuthCache");
				sunAuthCacheImpl = Class.forName("sun.net.www.protocol.http.AuthCacheImpl");
			} catch (final Exception e) {
				// Must not be using a JDK with sun classes so we abandon this
				// reset since it is sun specific
				return;
			}

			final Method m = sunAuthCacheValue.getDeclaredMethod("setAuthCache", sunAuthCache);

			final Constructor<?> authCacheImplConstr = sunAuthCacheImpl.getConstructor();
			final Object authCachImp = authCacheImplConstr.newInstance();

			m.invoke(null, authCachImp);

		} catch (final Exception e) {
			Loggers.SERVER.error(e);
		}
	}

	private ActionErrors checkProxySettings(final HttpServletRequest request, final HubProxyInfo proxyInfo,
			final ActionErrors errors) {

		final String proxyServer = request.getParameter("hubProxyServer");
		if (StringUtils.isNotBlank(proxyServer)) {
			proxyInfo.setHost(proxyServer);
		} else {
			proxyInfo.setHost("");
		}
		final String proxyPort = request.getParameter("hubProxyPort");
		if (StringUtils.isNotBlank(proxyPort)) {
			try {
				final int port = Integer.valueOf(proxyPort);
				if (StringUtils.isNotBlank(proxyServer) && port < 0) {
					errors.addError("errorHubProxyPort", "Please enter a valid Proxy port.");
				} else {
					proxyInfo.setPort(port);
				}
			} catch (final NumberFormatException e) {
				errors.addError("errorHubProxyPort", "Please enter a valid Proxy port. " + e.toString());
			}
		} else {
			proxyInfo.setPort(null);
		}
		final String noProxyHosts = request.getParameter("hubNoProxyHost");
		if (StringUtils.isNotBlank(noProxyHosts)) {
			String[] ignoreHosts = null;
			final List<Pattern> noProxyHostsPatterns = new ArrayList<Pattern>();
			boolean patternError = false;
			if (StringUtils.isNotBlank(noProxyHosts)) {
				if (noProxyHosts.contains(",")) {
					ignoreHosts = noProxyHosts.split(",");
					for (final String ignoreHost : ignoreHosts) {
						try {
							final Pattern pattern = Pattern.compile(ignoreHost);
							noProxyHostsPatterns.add(pattern);
						} catch (final PatternSyntaxException e) {
							patternError = true;
							errors.addError("errorHubNoProxyHost",
									"The host : " + ignoreHost + " : is not a valid regular expression.");
						}
					}
				} else {
					try {
						final Pattern pattern = Pattern.compile(noProxyHosts);
						noProxyHostsPatterns.add(pattern);
					} catch (final PatternSyntaxException e) {
						patternError = true;
						errors.addError("errorHubNoProxyHost",
								"The host : " + noProxyHosts + " : is not a valid regular expression.");
					}
				}
			}
			if (!patternError) {
				proxyInfo.setIgnoredProxyHosts(noProxyHosts);
			} else {
				proxyInfo.setIgnoredProxyHosts("");
			}
		} else {
			proxyInfo.setIgnoredProxyHosts("");
		}
		final String proxyUser = request.getParameter("hubProxyUser");
		if (StringUtils.isNotBlank(proxyUser)) {
			proxyInfo.setProxyUsername(proxyUser);
		} else {
			proxyInfo.setProxyUsername("");
		}
		final String encProxyPassword = request.getParameter("encryptedHubProxyPass");

		final String webDecryptedProxyPass = RSACipher.decryptWebRequestData(encProxyPassword);
		if (StringUtils.isNotBlank(webDecryptedProxyPass)) {
			proxyInfo.setProxyPassword(webDecryptedProxyPass);
		} else {
			proxyInfo.setProxyPassword("");
		}

		return errors;
	}

	public ActionErrors testConnection(final HttpServletRequest request)
			throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException,
			BadPaddingException, IOException, IllegalArgumentException, EncryptionException {
		final ActionErrors errors = new ActionErrors();
		checkInput(request, errors);
		final ServerHubConfigBean serverConfig = configPersistenceManager.getConfiguredServer();
		final HubProxyInfo proxyInfo = configPersistenceManager.getConfiguredServer().getProxyInfo();

		if (errors.hasNoErrors()) {
			final HubServerLogger serverLogger = new HubServerLogger();

			try {
				serverLogger.info("Validating the credentials for the Server : " + serverConfig.getHubUrl());

				final HubIntRestService service = getRestService(serverConfig, proxyInfo, true);

				final int responseCode = service.setCookies(serverConfig.getGlobalCredentials().getHubUser(),
						serverConfig.getGlobalCredentials().getDecryptedPassword());

				if (responseCode == 401) {
					// If User is Not Authorized, 401 error, an exception should
					// be thrown by the ClientResource
					errors.addError("errorConnection", "The provided credentials are not valid for the Hub server '"
							+ serverConfig.getHubUrl() + "'");
				} else if (responseCode != 200 && responseCode != 204 && responseCode != 202) {
					// return
					// FormValidation.ok(Messages.HubBuildScan_getCredentialsValidFor_0_(serverUrl));
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
				credentialsBean
						.setEncryptedPassword(PasswordEncrypter.encrypt(new HubServerLogger(), decryptedWebPassword));
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

	public boolean isTestConnectionRequest(final HttpServletRequest req) {
		final String testConnectionParamValue = req.getParameter("testConnection");
		return StringUtils.isNotBlank(testConnectionParamValue) && Boolean.valueOf(testConnectionParamValue);
	}

	public boolean isSavingRequest(final HttpServletRequest req) {
		final String savingParamValue = req.getParameter("saving");
		return StringUtils.isNotBlank(savingParamValue) && Boolean.valueOf(savingParamValue);
	}

	private HubIntRestService getRestService(final ServerHubConfigBean serverConfig, final HubProxyInfo proxyInfo,
			final boolean isTestConnection)
					throws HubIntegrationException, URISyntaxException, IOException, NoSuchMethodException,
					IllegalAccessException, IllegalArgumentException, InvocationTargetException, BDRestException {
		if (serverConfig == null) {
			return null;
		}
		final HubIntRestService service = new HubIntRestService(serverConfig.getHubUrl());
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
						service.setProxyProperties(proxyInfo.getHost(), proxyInfo.getPort(), null,
								proxyInfo.getProxyUsername(), proxyInfo.getProxyPassword());
					} else {
						service.setProxyProperties(proxyInfo.getHost(), proxyInfo.getPort(), null, null, null);
					}
				}
			}
		}
		if (serverConfig != null && !isTestConnection) {
			service.setCookies(serverConfig.getGlobalCredentials().getHubUser(),
					serverConfig.getGlobalCredentials().getDecryptedPassword());
		}

		return service;
	}

}
