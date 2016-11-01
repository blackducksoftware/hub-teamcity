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
package com.blackducksoftware.integration.hub.teamcity.server.failure;

public enum HubFailureType {
    POLICY_VIOLATIONS("Project has Hub Policy Violations", "If the specified Hub Project has policy violations after the Hub scan then the build will fail.", "Fail the build if there are any policy violations");

    private String displayName;

    private String description;

    private String parameterDescription;

    private HubFailureType(final String displayName, final String description, final String parameterDescription) {
        this.displayName = displayName;
        this.description = description;
        this.parameterDescription = parameterDescription;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getParameterDescription() {
        return parameterDescription;
    }

    public static HubFailureType getHubFailureType(final String status) {
        try {
            return HubFailureType.valueOf(status);
        } catch (final IllegalArgumentException e) {
            return null;
        }
    }

}
