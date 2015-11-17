package com.blackducksoftware.integration.hub.teamcity.agent.scan;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.List;

import com.blackducksoftware.integration.hub.ScanExecutor;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;

public class TeamCityScanExecutor extends ScanExecutor {

    protected TeamCityScanExecutor(String hubUrl, String hubUsername, String hubPassword, List<String> scanTargets, Integer buildNumber) {
        super(hubUrl, hubUsername, hubPassword, scanTargets, buildNumber);
    }

    @Override
    protected boolean isConfiguredCorrectly(String scanExec, String oneJarPath, String javaExec) {
        if (getLogger() == null) {
            System.out.println("Could not find a logger");
            return false;
        }
        if (scanExec == null) {
            getLogger().error("Please provide the Hub scan CLI.");
            return false;
        }
        else {
            File scanExecRemote = new File(scanExec);
            if (!scanExecRemote.exists()) {
                getLogger().error("The Hub scan CLI provided does not exist.");
                return false;
            }
        }

        if (oneJarPath == null) {
            getLogger().error("Please provide the path for the CLI cache.");
            return false;
        }

        if (javaExec == null) {
            getLogger().error("Please provide the java home directory.");
            return false;
        }
        else {
            File javaExecRemote = new File(javaExec);
            if (!javaExecRemote.exists()) {
                getLogger().error("The Java home provided does not exist.");
                return false;
            }
        }

        if (getScanMemory() <= 0) {
            getLogger().error("No memory set for the HUB CLI. Will use the default memory, " + DEFAULT_MEMORY);
            setScanMemory(DEFAULT_MEMORY);
        }
        return true;
    }

    @Override
    protected String getLogDirectoryPath() throws IOException {
        File logDirectory = new File(getWorkingDirectory());
        logDirectory = new File(logDirectory, "HubScanLogs");
        logDirectory = new File(logDirectory, String.valueOf(getBuildNumber()));
        logDirectory.mkdirs();

        return logDirectory.getAbsolutePath();
    }

    @Override
    protected Result executeScan(List<String> cmd, String logDirectoryPath) throws HubIntegrationException, InterruptedException {
        try {

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            ProcStarter ps = launcher.launch();
            if (ps != null) {
                // ////////////////////// Code to mask the password in the logs
                int indexOfPassword = cmd.indexOf("--password");
                int indexOfProxyPassword = -1;
                for (int i = 0; i < cmd.size(); i++) {
                    if (cmd.get(i).contains("-Dhttp.proxyPassword")) {
                        indexOfProxyPassword = i;
                        break;
                    }
                }
                boolean[] masks = new boolean[cmd.size()];
                Arrays.fill(masks, false);

                // The Users password should appear after --password
                masks[indexOfPassword + 1] = true;

                if (indexOfProxyPassword != -1) {
                    masks[indexOfProxyPassword] = true;
                }

                ps.masks(masks);
                // ///////////////////////

                ps.envs(build.getEnvironment(listener));
                ps.cmds(cmd);
                ps.stdout(byteStream);
                ps.join();

                ByteArrayOutputStream byteStreamOutput = (ByteArrayOutputStream) ps.stdout();

                // DO NOT close this PrintStream or Jenkins will not be able to log any more messages. Jenkins will
                // handle
                // closing it.
                String outputString = new String(byteStreamOutput.toByteArray(), "UTF-8");

                if (outputString.contains("Illegal character in path")
                        && (outputString.contains("Finished in") && outputString.contains("with status FAILURE"))) {
                    // This version of the CLI can not handle spaces in the log directory
                    // Not sure which version of the CLI this issue was fixed

                    int indexOfLogOption = cmd.indexOf("--logDir") + 1;

                    String logPath = cmd.get(indexOfLogOption);
                    logPath = logPath.replace(" ", "%20");
                    cmd.remove(indexOfLogOption);
                    cmd.add(indexOfLogOption, logPath);

                    byteStream = new ByteArrayOutputStream();

                    ps.cmds(cmd);
                    ps.stdout(byteStream);
                    ps.join();

                    byteStreamOutput = (ByteArrayOutputStream) ps.stdout();
                    outputString = new String(byteStreamOutput.toByteArray(), "UTF-8");
                }

                getLogger().info(outputString);

                if (logDirectoryPath != null) {
                    File logDirectory = new File(logDirectoryPath);
                    if (logDirectory.exists() && doesHubSupportLogOption()) {

                        getLogger().info(
                                "You can view the BlackDuck Scan CLI logs at : '" + logDirectory.getAbsolutePath()
                                        + "'");
                        getLogger().info("");
                    }
                }

                if (outputString.contains("Finished in") && outputString.contains("with status SUCCESS")) {
                    return Result.SUCCESS;
                } else {
                    return Result.FAILURE;
                }

            } else {
                getLogger().error("Could not find a ProcStarter to run the process!");
            }
        } catch (MalformedURLException e) {
            throw new HubIntegrationException("The server URL provided was not a valid", e);
        } catch (IOException e) {
            throw new HubIntegrationException(e.getMessage(), e);
        } catch (InterruptedException e) {
            throw new HubIntegrationException(e.getMessage(), e);
        }
        return Result.SUCCESS;
    }
}
