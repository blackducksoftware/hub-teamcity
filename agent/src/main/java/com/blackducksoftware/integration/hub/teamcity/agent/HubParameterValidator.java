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

    public boolean validateTargetPath(final File target, final String workingDirectory) throws IOException {
        boolean validTargetPath = true;

        if (target == null) {
            logger.error("Can not scan null target.");
            validTargetPath = false;
        } else {
            if (!target.exists()) {
                logger.error("The scan target '" + target.getAbsolutePath() + "' does not exist.");
                validTargetPath = false;
            }

            String targetPath = target.getCanonicalPath();

            if (!targetPath.startsWith(workingDirectory)) {
                logger.error("Can not scan targets outside the working directory.");
                validTargetPath = false;
            }
        }
        return validTargetPath;
    }

    public boolean validateScanMemory(final String memory) {
        boolean validMemory = true;
        if (StringUtils.isBlank(memory)) {
            logger.error("There is no memory specified for the Hub scan. The scan requires a minimum of 4096 MB.");
            validMemory = false;
        } else {
            try {
                Integer scanMemory = Integer.valueOf(memory);
                if (scanMemory < 4096) {
                    logger.error("The Hub scan requires at least 4096 MB of memory.");
                    validMemory = false;
                }
            } catch (NumberFormatException e) {
                logger.error("The amount of memory provided must be in the form of an Integer. Ex: 4096, 4608, etc.");
                validMemory = false;
            }
        }

        return validMemory;
    }

    public boolean validateRiskReportProperties(final String generateRiskReport, final String maxWaitTimeForRiskReport) {
        boolean reportPropertiesValid = true;

        boolean shouldGenerateRiskReport = Boolean.valueOf(generateRiskReport);
        if (shouldGenerateRiskReport) {
            reportPropertiesValid = false;
            if (StringUtils.isNumeric(maxWaitTimeForRiskReport)) {
                try {
                    int waitTime = Integer.parseInt(maxWaitTimeForRiskReport);
                    if (waitTime > 0) {
                        reportPropertiesValid = true;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }

        if (!reportPropertiesValid) {
            logger.error("If the Black Duck Risk Report is requested, the Maximum time to wait for report must also be set to a numeric value greater than zero.");
        }

        return reportPropertiesValid;
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
