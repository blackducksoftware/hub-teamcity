package com.blackducksoftware.integration.hub.teamcity.server.global;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jetbrains.buildServer.controllers.ActionErrors;
import jetbrains.buildServer.controllers.BaseFormXmlController;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.crypt.RSACipher;

import org.apache.commons.lang3.StringUtils;
import org.jdom.Element;
import org.springframework.web.servlet.ModelAndView;

import com.blackducksoftware.integration.hub.teamcity.common.beans.HubCredentialsBean;
import com.blackducksoftware.integration.hub.teamcity.common.beans.HubProxyInfo;
import com.blackducksoftware.integration.hub.teamcity.common.beans.ServerHubConfigBean;
import com.blackducksoftware.integration.suite.encryption.PasswordEncrypter;
import com.blackducksoftware.integration.suite.encryption.exceptions.EncryptionException;

public class HubGlobalServerConfigController extends BaseFormXmlController {

    private final ServerHubConfigPersistenceManager configPersistenceManager;

    public HubGlobalServerConfigController(final ServerHubConfigPersistenceManager configPersistenceManager)
    {
        this.configPersistenceManager = configPersistenceManager;
    }

    @Override
    public ModelAndView doGet(HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response, Element xmlResponse) {

        if (request.getParameterNames().hasMoreElements()) {

            if (isTestConnectionRequest(request)) {
                ActionErrors errors = new ActionErrors();
                try {
                    errors = testConnection(request);
                    if (errors.hasErrors()) {
                        errors.serialize(xmlResponse);
                    }
                } catch (Exception e) {
                    Loggers.SERVER.error("Error testing Server connection", e);
                    errors.addError("errorConnection", e.toString());
                }
                return;
            } else if (isSavingRequest(request)) {
                ActionErrors errors = new ActionErrors();
                try {
                    checkInput(request, errors);
                } catch (Exception e) {
                    Loggers.SERVER.error("Error with the Hub configuration", e);
                    errors.addError("errorSaving", e.toString());
                }
                if (errors.hasErrors()) {
                    errors.serialize(xmlResponse);
                    return;
                } else {
                    try {
                        configPersistenceManager.persist();
                    } catch (Exception e) {
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

    public void checkInput(final HttpServletRequest request, final ActionErrors errors) throws IllegalArgumentException, EncryptionException {
        HubCredentialsBean credentials = getCredentialsFromRequest(request, "hubUser");
        if (StringUtils.isBlank(credentials.getHubUser())) {
            errors.addError("errorUserName", "Please specify a UserName.");
        }
        if (StringUtils.isBlank(credentials.getEncryptedPassword())) {
            errors.addError("errorPassword", "There is no saved Password. Please specify a Password.");
        }
        String timeout = request.getParameter("hubTimeout");
        int timeoutInteger = 300;
        if (StringUtils.isBlank(timeout)) {
            errors.addError("errorTimeout", "Please specify a timeout.");
        } else {
            try {
                timeoutInteger = Integer.valueOf(timeout);
            } catch (NumberFormatException e) {
                timeoutInteger = configPersistenceManager.getConfiguredServer().getHubTimeout();
                errors.addError("errorTimeout", "Please specify the server timeout as an integer. " + e.toString());
            }
        }

        HubProxyInfo proxyInfo = null;
        if (configPersistenceManager.getConfiguredServer().getProxyInfo() != null) {
            proxyInfo = configPersistenceManager.getConfiguredServer().getProxyInfo();
        } else {
            proxyInfo = new HubProxyInfo();
        }

        String proxyServer = request.getParameter("hubProxyServer");
        if (StringUtils.isNotBlank(proxyServer)) {
            proxyInfo.setHost(proxyServer);
        } else {
            proxyInfo.setHost("");
        }
        String proxyPort = request.getParameter("hubProxyPort");
        if (StringUtils.isNotBlank(proxyPort)) {
            try {
                int port = Integer.valueOf(proxyPort);
                if (StringUtils.isNotBlank(proxyServer) && port < 0) {
                    errors.addError("errorHubProxyPort", "Please enter a valid Proxy port.");
                } else {
                    proxyInfo.setPort(port);
                }
            } catch (NumberFormatException e) {
                errors.addError("errorHubProxyPort", "Please enter a valid Proxy port. " + e.toString());
            }
        } else {
            proxyInfo.setPort(null);
        }
        String noProxyHosts = request.getParameter("hubNoProxyHost");
        if (StringUtils.isNotBlank(noProxyHosts)) {
            String[] ignoreHosts = null;
            List<Pattern> noProxyHostsPatterns = new ArrayList<Pattern>();
            boolean patternError = false;
            if (StringUtils.isNotBlank(noProxyHosts)) {
                if (noProxyHosts.contains(",")) {
                    ignoreHosts = noProxyHosts.split(",");
                    for (String ignoreHost : ignoreHosts) {
                        try {
                            Pattern pattern = Pattern.compile(ignoreHost);
                            noProxyHostsPatterns.add(pattern);
                        } catch (PatternSyntaxException e) {
                            patternError = true;
                            errors.addError("errorHubNoProxyHost", "The host : " + ignoreHost + " : is not a valid regular expression.");
                        }
                    }
                } else {
                    try {
                        Pattern pattern = Pattern.compile(noProxyHosts);
                        noProxyHostsPatterns.add(pattern);
                    } catch (PatternSyntaxException e) {
                        patternError = true;
                        errors.addError("errorHubNoProxyHost", "The host : " + noProxyHosts + " : is not a valid regular expression.");
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
        String proxyUser = request.getParameter("hubProxyUser");
        if (StringUtils.isNotBlank(proxyUser)) {
            proxyInfo.setProxyUsername(proxyUser);
        } else {
            proxyInfo.setProxyUsername("");
        }
        String encProxyPassword = request.getParameter("encryptedHubProxyPass");

        String webDecryptedProxyPass = RSACipher.decryptWebRequestData(encProxyPassword);
        if (StringUtils.isNotBlank(webDecryptedProxyPass)) {
            proxyInfo.setProxyPassword(webDecryptedProxyPass);
        } else {
            proxyInfo.setProxyPassword("");
        }

        if (errors.hasNoErrors()) {
            // Need to add this here to make sure the proxy settings are used in the checkUrl
            configPersistenceManager.getConfiguredServer().setProxyInfo(proxyInfo);
        }

        String url = request.getParameter("hubUrl");
        if (StringUtils.isBlank(url)) {
            errors.addError("errorUrl", "Please specify a URL.");
        } else {
            if (isTestConnectionRequest(request)) {
                checkUrl(url, errors, true);
            } else {
                checkUrl(url, errors, false);
            }
        }

        if (errors.hasNoErrors()) {
            configPersistenceManager.getConfiguredServer().setGlobalCredentials(credentials);
            configPersistenceManager.getConfiguredServer().setHubUrl(url);
            configPersistenceManager.getConfiguredServer().setHubTimeout(timeoutInteger);
        }
    }

    private ActionErrors checkUrl(final String url, final ActionErrors errors, boolean isTestConnection) {

        URL testUrl = null;
        try {
            testUrl = new URL(url);
            try {
                testUrl.toURI();
            } catch (URISyntaxException e) {
                errors.addError("errorUrl", "Please specify a valid URL of a Hub server. " + e.toString());
            }
        } catch (MalformedURLException e) {
            errors.addError("errorUrl", "Please specify a valid URL of a Hub server. " + e.toString());
        }
        if (isTestConnection) {
            if (testUrl != null) {
                try {
                    Proxy proxy = null;
                    if (!checkMatchingNoProxyHostPatterns(url, configPersistenceManager.getConfiguredServer().getProxyInfo().getNoProxyHostPatterns())) {

                        if (StringUtils.isNotBlank(configPersistenceManager.getConfiguredServer().getProxyInfo().getHost())
                                && configPersistenceManager.getConfiguredServer().getProxyInfo().getPort() != null) {
                            proxy = new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(
                                    configPersistenceManager.getConfiguredServer().getProxyInfo().getHost(),
                                    configPersistenceManager.getConfiguredServer().getProxyInfo().getPort()));
                            if (StringUtils.isNotBlank(configPersistenceManager.getConfiguredServer().getProxyInfo().getProxyUsername())
                                    && StringUtils.isNotBlank(configPersistenceManager.getConfiguredServer().getProxyInfo().getProxyPassword())) {
                                Authenticator.setDefault(
                                        new Authenticator() {
                                            @Override
                                            public PasswordAuthentication getPasswordAuthentication() {
                                                return new PasswordAuthentication(
                                                        configPersistenceManager.getConfiguredServer().getProxyInfo().getProxyUsername(),
                                                        configPersistenceManager
                                                                .getConfiguredServer().getProxyInfo().getProxyPassword().toCharArray());
                                            }
                                        }
                                        );
                            }
                        }
                    }
                    URLConnection connection = null;
                    if (proxy != null) {
                        connection = testUrl.openConnection(proxy);
                    } else {
                        connection = testUrl.openConnection();
                    }

                    connection.getContent();
                } catch (IOException ioe) {
                    errors.addError("errorUrl", "Trouble reaching the Hub server. " + ioe.toString());
                } catch (RuntimeException e) {
                    errors.addError("errorUrl", "Not a valid Hub server. " + e.toString());
                }
            }
        }
        return errors;
    }

    public ActionErrors testConnection(final HttpServletRequest request) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException,
            IllegalBlockSizeException, BadPaddingException, IOException, IllegalArgumentException, EncryptionException {
        ActionErrors errors = new ActionErrors();
        checkInput(request, errors);
        ServerHubConfigBean serverConfig = configPersistenceManager.getConfiguredServer();
        HubProxyInfo proxyInfo = configPersistenceManager.getConfiguredServer().getProxyInfo();

        if (errors.hasNoErrors()) {
            HubServerLogger serverLogger = new HubServerLogger();

            try {
                serverLogger.info("Validating the credentials for the Server : " + serverConfig.getHubUrl());

                // TODO test the connection

            } catch (Exception e) {
                Loggers.SERVER.error(e.toString(), e);
                errors.addError("errorConnection", e.toString());
            }
        }
        return errors;
    }

    public HubCredentialsBean getCredentialsFromRequest(HttpServletRequest request, String usernameKey) throws IllegalArgumentException, EncryptionException {
        HubCredentialsBean credentialsBean = new HubCredentialsBean(request.getParameter(usernameKey));
        String password = "";
        password = request.getParameter("encryptedHubPass");

        if (StringUtils.isNotBlank(getRealEncryptedPassword(password))) {
            // Do not change the saved password unless the User has provided a new one
            credentialsBean.setEncryptedPassword(getRealEncryptedPassword(password));
        } else {
            if (configPersistenceManager.getConfiguredServer().getGlobalCredentials() != null) {
                credentialsBean.setEncryptedPassword(configPersistenceManager.getConfiguredServer().getGlobalCredentials().getEncryptedPassword());
            }
        }
        return credentialsBean;
    }

    private String getRealEncryptedPassword(String webEncryptedPass) throws IllegalArgumentException, EncryptionException {
        if (StringUtils.isNotBlank(webEncryptedPass)) {
            String webDecryptedPass = RSACipher.decryptWebRequestData(webEncryptedPass);

            if (StringUtils.isNotBlank(webDecryptedPass)) {
                return PasswordEncrypter.publicEncrypt(webDecryptedPass);
            }
        }
        return "";
    }

    public boolean isTestConnectionRequest(final HttpServletRequest req) {
        String testConnectionParamValue = req.getParameter("testConnection");
        return StringUtils.isNotBlank(testConnectionParamValue) && Boolean.valueOf(testConnectionParamValue);
    }

    public boolean isSavingRequest(final HttpServletRequest req) {
        String savingParamValue = req.getParameter("saving");
        return StringUtils.isNotBlank(savingParamValue) && Boolean.valueOf(savingParamValue);
    }

    /**
     * Checks the list of user defined host names that should be connected to directly and not go through the proxy. If
     * the hostToMatch matches any of these hose names then this method returns true.
     *
     * @param hostToMatch
     *            String the hostName to check if it is in the list of hosts that should not go through the proxy.
     *
     * @return boolean
     */
    private boolean checkMatchingNoProxyHostPatterns(String hostToMatch, List<Pattern> noProxyHosts) {
        if (noProxyHosts.isEmpty()) {
            // User did not specify any hosts to ignore the proxy
            return false;
        }
        boolean match = false;
        if (!StringUtils.isBlank(hostToMatch) && !noProxyHosts.isEmpty()) {

            for (Pattern pattern : noProxyHosts) {
                if (!match) {
                    Matcher m = pattern.matcher(hostToMatch);
                    while (m.find()) {
                        match = true;
                    }
                }
            }
        }
        return match;
    }

}
