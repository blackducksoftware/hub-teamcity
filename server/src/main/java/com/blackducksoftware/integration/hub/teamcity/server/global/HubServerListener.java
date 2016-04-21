/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *******************************************************************************/
package com.blackducksoftware.integration.hub.teamcity.server.global;

import org.jetbrains.annotations.NotNull;

import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.serverSide.BuildServerAdapter;
import jetbrains.buildServer.serverSide.BuildServerListener;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.EventDispatcher;

public class HubServerListener extends BuildServerAdapter {
	private final SBuildServer server;
	private final ServerHubConfigPersistenceManager configPersistenceManager;

	public HubServerListener(@NotNull final EventDispatcher<BuildServerListener> dispatcher,
			@NotNull final SBuildServer server, @NotNull final ServerPaths serverPaths) {
		this.server = server;

		dispatcher.addListener(this);

		configPersistenceManager = new ServerHubConfigPersistenceManager(serverPaths);
	}

	@Override
	public void serverStartup() {
		Loggers.SERVER.info("The Black Duck Software Hub Plugin is running on server version '"
				+ server.getFullServerVersion() + "'.");
	}

	public ServerHubConfigPersistenceManager getConfigManager() {
		return configPersistenceManager;
	}

}
