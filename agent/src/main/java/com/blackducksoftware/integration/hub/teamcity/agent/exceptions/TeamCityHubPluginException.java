/**
 * Black Duck Hub Plug-In for TeamCity Agent
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
 */
package com.blackducksoftware.integration.hub.teamcity.agent.exceptions;

public class TeamCityHubPluginException extends Exception {
    private static final long serialVersionUID = -1471078296882978823L;

    public TeamCityHubPluginException() {
    }

    public TeamCityHubPluginException(final String message) {
        super(message);
    }

    public TeamCityHubPluginException(final Throwable cause) {
        super(cause);
    }

    public TeamCityHubPluginException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
