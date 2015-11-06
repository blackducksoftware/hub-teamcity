package com.blackducksoftware.integration.hub.teamcity.common.beans;

import java.io.IOException;
import java.io.Serializable;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.commons.lang3.StringUtils;
import org.apache.ws.security.WSPasswordCallback;

import com.blackducksoftware.integration.suite.sdk.util.ProgrammedEncryptedPasswordCallback;
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

    public String getDecryptedPassword() throws IOException, UnsupportedCallbackException {
        ProgrammedEncryptedPasswordCallback passwordCallback = new ProgrammedEncryptedPasswordCallback();
        passwordCallback.addUserNameAndPassword(hubUser, hubPass);
        Callback[] callbacks = new Callback[1];
        WSPasswordCallback callback = new WSPasswordCallback(hubUser, 2);
        callbacks[0] = callback;
        passwordCallback.handle(callbacks);

        return callback.getPassword();
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
