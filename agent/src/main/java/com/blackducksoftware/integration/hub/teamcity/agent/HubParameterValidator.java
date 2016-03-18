package com.blackducksoftware.integration.hub.teamcity.agent;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.teamcity.common.beans.HubCredentialsBean;

public class HubParameterValidator {
    private final HubAgentBuildLogger logger;

    public HubParameterValidator(HubAgentBuildLogger logger) {
        this.logger = logger;
    }

    public boolean isServerUrlValid(final String url) {
        boolean validUrl = true;
        if (StringUtils.isBlank(url)) {
            logger.error("There is no Server URL specified");
            validUrl = false;
        } else {
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                logger.error("The server URL specified is not a valid URL.");
                validUrl = false;
            }
        }
        return validUrl;
    }

    public boolean isHubCredentialConfigured(final HubCredentialsBean credential) {
        boolean validCredential = true;
        if (credential == null) {
            logger.error("There are no credentials configured.");
            validCredential = false;
        } else {
            if (StringUtils.isBlank(credential.getHubUser())) {
                logger.error("There is no Hub username specified");
                validCredential = false;
            }
            if (StringUtils.isBlank(credential.getEncryptedPassword())) {
                logger.error("There is no Hub password specified.");
                validCredential = false;
            }
        }
        return validCredential;
    }

    public boolean validateTargetPath(final String targetAbsolutePath, final String workingDirectory) throws IOException {
        if (null == targetAbsolutePath) {
            logger.error("Can not scan null target.");
            return false;
        }

        File target = new File(targetAbsolutePath);
        if (null == target || !target.exists()) {
            logger.error("The scan target '" + target.getAbsolutePath() + "' does not exist.");
            return false;
        }

        String targetCanonicalPath = target.getCanonicalPath();
        if (!targetCanonicalPath.startsWith(workingDirectory)) {
            logger.error("Can not scan targets outside the working directory.");
            return false;
        }

        return true;
    }

    public boolean validateProjectNameAndVersion(final String projectName, final String version) {
        boolean validProjectConfig = true;

        if (StringUtils.isBlank(projectName) && StringUtils.isBlank(version)) {
            logger.warn("No Project Name or Version were found. Any scans run will not be mapped to a Version.");
            validProjectConfig = true;
        } else if (StringUtils.isBlank(projectName)) {
            logger.error("No Project Name was found.");
            validProjectConfig = false;
        } else if (StringUtils.isBlank(version)) {
            logger.error("No Project Version were found.");
            validProjectConfig = false;
        }

        return validProjectConfig;
    }

}
