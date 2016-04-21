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
