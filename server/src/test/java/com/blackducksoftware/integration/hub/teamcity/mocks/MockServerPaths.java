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
package com.blackducksoftware.integration.hub.teamcity.mocks;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.apache.commons.lang3.StringUtils;
import org.mockito.Mockito;

import jetbrains.buildServer.serverSide.ServerPaths;

public class MockServerPaths {
	public static ServerPaths getMockedServerPaths(final String parentDir, final String configDir)
			throws UnsupportedEncodingException {
		final ServerPaths mockedServerPaths = Mockito.mock(ServerPaths.class);

		final String confDir = getConfigDirectory(parentDir, configDir);

		Mockito.when(mockedServerPaths.getConfigDir()).thenReturn(confDir);
		return mockedServerPaths;
	}

	public static String getConfigDirectory(final String parentDir, final String configDir)
			throws UnsupportedEncodingException {
		String confDir = URLDecoder
				.decode(MockServerPaths.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
		confDir = confDir.substring(0, confDir.indexOf("/target"));
		confDir = confDir + "/test-workspace";

		if (StringUtils.isNotBlank(parentDir)) {
			confDir = confDir + "/" + parentDir;
		}

		if (StringUtils.isNotBlank(configDir)) {
			confDir = confDir + "/" + configDir;
		}
		return confDir;
	}

}
