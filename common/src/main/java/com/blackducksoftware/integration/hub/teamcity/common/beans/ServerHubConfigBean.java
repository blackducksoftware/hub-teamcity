package com.blackducksoftware.integration.hub.teamcity.common.beans;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("serializableHubServer")
public class ServerHubConfigBean implements Serializable {

	private String hubUrl = "";

	@XStreamAlias("globalHubCredentials")
	private HubCredentialsBean globalCredentials = new HubCredentialsBean("", "");

	@XStreamAlias("serializableHubProxyInfo")
	private HubProxyInfo proxyInfo = new HubProxyInfo();

	public ServerHubConfigBean() {
	}

	public String getHubUrl() {
		return hubUrl;
	}

	public void setHubUrl(String hubUrl) {
		this.hubUrl = hubUrl;
	}

	public HubCredentialsBean getGlobalCredentials() {
		return globalCredentials;
	}

	public void setGlobalCredentials(HubCredentialsBean globalCredentials) {
		this.globalCredentials = globalCredentials;
	}

	public HubProxyInfo getProxyInfo() {
		return proxyInfo;
	}

	public void setProxyInfo(HubProxyInfo proxyInfo) {
		this.proxyInfo = proxyInfo;
	}

	@Override
	public String toString() {
		return "ServerHubConfigBean [hubUrl=" + hubUrl + ", globalCredentials=" + globalCredentials + ", proxyInfo="
				+ proxyInfo + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hubUrl == null) ? 0 : hubUrl.hashCode());
		result = prime * result + ((globalCredentials == null) ? 0 : globalCredentials.hashCode());
		result = prime * result + ((proxyInfo == null) ? 0 : proxyInfo.hashCode());
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
		ServerHubConfigBean other = (ServerHubConfigBean) obj;
		if (hubUrl == null) {
			if (other.hubUrl != null) {
				return false;
			}
		} else if (!hubUrl.equals(other.hubUrl)) {
			return false;
		}
		if (globalCredentials == null) {
			if (other.globalCredentials != null) {
				return false;
			}
		} else if (!globalCredentials.equals(other.globalCredentials)) {
			return false;
		}
		if (proxyInfo == null) {
			if (other.proxyInfo != null) {
				return false;
			}
		} else if (!proxyInfo.equals(other.proxyInfo)) {
			return false;
		}
		return true;
	}

}
