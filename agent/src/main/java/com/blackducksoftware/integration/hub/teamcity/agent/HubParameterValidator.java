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
            String targetPath = target.getCanonicalPath();

            if (!targetPath.startsWith(workingDirectory)) {
                logger.error("Can not scan targets outside the working directory.");
                validTargetPath = false;
            } else if (!target.exists()) {
                logger.error("The scan target '" + targetPath + "' does not exist.");
                validTargetPath = false;
            }
        }
        return validTargetPath;
    }

    public boolean validateCLIPath(final File cliHomeDirectory) throws IOException {
        boolean validCLIPath = false;

        if (cliHomeDirectory == null) {
            logger.error("The Hub CLI path has not been set.");
            validCLIPath = false;
        } else {
            if (cliHomeDirectory.exists()) {
                File[] files = cliHomeDirectory.listFiles();
                if (files != null && files.length != 0) {
                    File libFolder = null;
                    for (File subDirectory : files) {
                        if ("lib".equalsIgnoreCase(subDirectory.getName())) {
                            libFolder = subDirectory;
                            break;
                        }
                    }
                    if (libFolder != null) {
                        File[] cliFiles = libFolder.listFiles();

                        if (cliFiles == null || cliFiles.length == 0) {
                            logger.error("The lib directory in the Hub CLI home is empty!");
                            validCLIPath = false;
                        } else {
                            for (File file : cliFiles) {
                                if (file.getName().contains("scan.cli")) {
                                    validCLIPath = true;
                                    break;
                                }
                            }
                            if (!validCLIPath) {
                                logger.error("Could not find the Hub CLI in the lib directory.");
                            }
                        }
                    } else {
                        logger.error("Could not find the lib directory in the Hub CLI home directory.");
                    }
                } else {
                    logger.error("The Hub CLI home directory is empty!");
                }

            } else {
                logger.error("The Hub CLI home directory does not exist at : " + cliHomeDirectory.getCanonicalPath());
                validCLIPath = false;
            }
        }
        return validCLIPath;
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

}
