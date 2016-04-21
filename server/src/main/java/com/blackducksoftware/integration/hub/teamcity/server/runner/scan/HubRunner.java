package com.blackducksoftware.integration.hub.teamcity.server.runner.scan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.blackducksoftware.integration.hub.teamcity.common.HubBundle;
import com.blackducksoftware.integration.hub.teamcity.server.global.HubServerListener;
import com.blackducksoftware.integration.hub.teamcity.server.runner.BaseRunType;

import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.PropertiesProcessor;
import jetbrains.buildServer.serverSide.RunTypeRegistry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;

public class HubRunner extends BaseRunType {
	public HubRunner(@NotNull final RunTypeRegistry runTypeRegistry,
			@NotNull final WebControllerManager webControllerManager, @NotNull final PluginDescriptor pluginDescriptor,
			@NotNull final HubServerListener serverListener) {
		super(webControllerManager, pluginDescriptor, serverListener.getConfigManager());
		runTypeRegistry.registerRunType(this);
		registerView("hubRunnerView.html", "bdHubRunner/hubRunnerView.jsp");
		registerEdit("hubRunnerEdit.html", "bdHubRunner/hubRunnerEdit.jsp");
	}

	@Override
	public String getDescription() {
		return HubBundle.RUNNER_DESCRIPTION;
	}

	@Override
	public String getDisplayName() {
		return HubBundle.RUNNER_DISPLAY_NAME;
	}

	@Override
	public String getType() {
		return HubBundle.RUNNER_TYPE;
	}

	@Override
	public Map<String, String> getDefaultRunnerProperties() {
		return null;
	}

	@Override
	@Nullable
	public PropertiesProcessor getRunnerPropertiesProcessor() {
		return new PropertiesProcessor() {
			@Override
			public Collection<InvalidProperty> process(final Map<String, String> properties) {
				final Collection<InvalidProperty> result = new ArrayList<InvalidProperty>();

				return result;
			}
		};
	}

}
