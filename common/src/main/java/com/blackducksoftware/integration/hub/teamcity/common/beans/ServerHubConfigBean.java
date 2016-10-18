/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.teamcity.common.beans;

import java.io.Serializable;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("serializableHubServer")
public class ServerHubConfigBean implements Serializable {
    private static final long serialVersionUID = 435773839748832926L;

    private String hubUrl = "";

    private int hubTimeout = 5;

    @XStreamAlias("globalHubCredentials")
    private HubCredentialsBean globalCredentials = new HubCredentialsBean("", "");

    @XStreamAlias("serializableHubProxyInfo")
    private HubProxyInfo proxyInfo = new HubProxyInfo();

    public ServerHubConfigBean() {
    }

    public String getHubUrl() {
        return hubUrl;
    }

    public void setHubUrl(final String hubUrl) {
        this.hubUrl = hubUrl;
    }

    public HubCredentialsBean getGlobalCredentials() {
        return globalCredentials;
    }

    public void setGlobalCredentials(final HubCredentialsBean globalCredentials) {
        this.globalCredentials = globalCredentials;
    }

    public HubProxyInfo getProxyInfo() {
        return proxyInfo;
    }

    public void setProxyInfo(final HubProxyInfo proxyInfo) {
        this.proxyInfo = proxyInfo;
    }

    public int getHubTimeout() {
        return hubTimeout;
    }

    public void setHubTimeout(final int timeout) {
        this.hubTimeout = timeout;
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
        result = prime * result + hubTimeout;
        result = prime * result + ((globalCredentials == null) ? 0 : globalCredentials.hashCode());
        result = prime * result + ((proxyInfo == null) ? 0 : proxyInfo.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServerHubConfigBean other = (ServerHubConfigBean) obj;
        if (hubUrl == null) {
            if (other.hubUrl != null) {
                return false;
            }
        } else if (!hubUrl.equals(other.hubUrl)) {
            return false;
        }
        if (hubTimeout != other.getHubTimeout()) {
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
