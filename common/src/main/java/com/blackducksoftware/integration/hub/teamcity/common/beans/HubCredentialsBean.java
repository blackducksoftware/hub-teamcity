package com.blackducksoftware.integration.hub.teamcity.common.beans;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("globalCredentials")
public class HubCredentialsBean implements Serializable {

    private String hubUser;

    private String hubPass;

    public HubCredentialsBean(String hubUser) {
        this.hubUser = hubUser;
    }

    public HubCredentialsBean(String hubUser, String password) {
        this.hubUser = hubUser;
        hubPass = password;
    }

    public HubCredentialsBean(HubCredentialsBean credentials) {
        if ((credentials != null) && !credentials.isEmpty()) {
            hubUser = credentials.getHubUser();

            hubPass = credentials.getEncryptedPassword();
        }
    }

    public String getHubUser() {
        return hubUser;
    }

    public void setHubUser(String hubUser) {
        this.hubUser = hubUser;
    }

    public String getEncryptedPassword() {
        return hubPass;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        hubPass = encryptedPassword;
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(hubUser) && StringUtils.isBlank(hubPass);
    }

    @Override
    public String toString() {
        return "CCCredentialsBean [hubUser=" + hubUser + ", hubPass=" + hubPass + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((hubPass == null) ? 0 : hubPass.hashCode());
        result = prime * result + ((hubUser == null) ? 0 : hubUser.hashCode());
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
        HubCredentialsBean other = (HubCredentialsBean) obj;
        if (hubPass == null) {
            if (other.hubPass != null) {
                return false;
            }
        } else if (!hubPass.equals(other.hubPass)) {
            return false;
        }
        if (hubUser == null) {
            if (other.hubUser != null) {
                return false;
            }
        } else if (!hubUser.equals(other.hubUser)) {
            return false;
        }
        return true;
    }

}
