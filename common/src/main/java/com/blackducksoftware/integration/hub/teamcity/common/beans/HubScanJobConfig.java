package com.blackducksoftware.integration.hub.teamcity.common.beans;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HubScanJobConfig {
    private String projectName = "";

    private String version = "";

    private String phase = "";

    private String distribution = "";

    private File hubCLIPath = null;

    private String generateRiskReport = "";

    private String maxWaitTimeForRiskReport = "";

    private String hubScanMemory = "";

    private List<File> hubScanTargets = new ArrayList<File>();

    private String workingDirectory = "";

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public File getHubCLIPath() {
        return hubCLIPath;
    }

    public void setHubCLIPath(File hubCLIPath) {
        this.hubCLIPath = hubCLIPath;
    }

    public String getHubScanMemory() {
        return hubScanMemory;
    }

    public void setHubScanMemory(String hubScanMemory) {
        this.hubScanMemory = hubScanMemory;
    }

    public List<File> getHubScanTargets() {
        return hubScanTargets;
    }

    public List<String> getHubScanTargetPaths() {
        List<String> scanTargets = new ArrayList<String>();
        for (File currTarget : hubScanTargets) {
            scanTargets.add(currTarget.getAbsolutePath());
        }
        return scanTargets;
    }

    public void setHubScanTargets(List<File> hubScanTargets) {
        this.hubScanTargets = hubScanTargets;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getGenerateRiskReport() {
        return generateRiskReport;
    }

    public void setGenerateRiskReport(String generateRiskReport) {
        this.generateRiskReport = generateRiskReport;
    }

    public String getMaxWaitTimeForRiskReport() {
        return maxWaitTimeForRiskReport;
    }

    public void setMaxWaitTimeForRiskReport(String maxWaitTimeForRiskReport) {
        this.maxWaitTimeForRiskReport = maxWaitTimeForRiskReport;
    }

    @Override
    public String toString() {
        return "HubScanJobConfig [projectName=" + projectName + ", version=" + version + ", phase=" + phase + ", distribution=" + distribution
                + ", hubCLIPath=" + hubCLIPath + ", generateRiskReport=" + generateRiskReport + ", maxWaitTimeForRiskReport=" + maxWaitTimeForRiskReport
                + ", hubScanMemory=" + hubScanMemory + ", hubScanTargets=" + hubScanTargets + ", workingDirectory=" + workingDirectory + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((distribution == null) ? 0 : distribution.hashCode());
        result = prime * result + ((generateRiskReport == null) ? 0 : generateRiskReport.hashCode());
        result = prime * result + ((hubCLIPath == null) ? 0 : hubCLIPath.hashCode());
        result = prime * result + ((hubScanMemory == null) ? 0 : hubScanMemory.hashCode());
        result = prime * result + ((hubScanTargets == null) ? 0 : hubScanTargets.hashCode());
        result = prime * result + ((maxWaitTimeForRiskReport == null) ? 0 : maxWaitTimeForRiskReport.hashCode());
        result = prime * result + ((phase == null) ? 0 : phase.hashCode());
        result = prime * result + ((projectName == null) ? 0 : projectName.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        result = prime * result + ((workingDirectory == null) ? 0 : workingDirectory.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HubScanJobConfig other = (HubScanJobConfig) obj;
        if (distribution == null) {
            if (other.distribution != null) {
                return false;
            }
        } else if (!distribution.equals(other.distribution)) {
            return false;
        }
        if (generateRiskReport == null) {
            if (other.generateRiskReport != null) {
                return false;
            }
        } else if (!generateRiskReport.equals(other.generateRiskReport)) {
            return false;
        }
        if (hubCLIPath == null) {
            if (other.hubCLIPath != null) {
                return false;
            }
        } else if (!hubCLIPath.equals(other.hubCLIPath)) {
            return false;
        }
        if (hubScanMemory == null) {
            if (other.hubScanMemory != null) {
                return false;
            }
        } else if (!hubScanMemory.equals(other.hubScanMemory)) {
            return false;
        }
        if (hubScanTargets == null) {
            if (other.hubScanTargets != null) {
                return false;
            }
        } else if (!hubScanTargets.equals(other.hubScanTargets)) {
            return false;
        }
        if (maxWaitTimeForRiskReport == null) {
            if (other.maxWaitTimeForRiskReport != null) {
                return false;
            }
        } else if (!maxWaitTimeForRiskReport.equals(other.maxWaitTimeForRiskReport)) {
            return false;
        }
        if (phase == null) {
            if (other.phase != null) {
                return false;
            }
        } else if (!phase.equals(other.phase)) {
            return false;
        }
        if (projectName == null) {
            if (other.projectName != null) {
                return false;
            }
        } else if (!projectName.equals(other.projectName)) {
            return false;
        }
        if (version == null) {
            if (other.version != null) {
                return false;
            }
        } else if (!version.equals(other.version)) {
            return false;
        }
        if (workingDirectory == null) {
            if (other.workingDirectory != null) {
                return false;
            }
        } else if (!workingDirectory.equals(other.workingDirectory)) {
            return false;
        }
        return true;
    }

}
