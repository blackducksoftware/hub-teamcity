package com.blackducksoftware.integration.hub.teamcity.common.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("serializableHubProxyInfo")
public class HubProxyInfo implements Serializable {

	private String host = "";

	private Integer port = null;

	private String proxyUsername = "";

	private String proxyPassword = "";

	private String ignoredProxyHosts = "";

	public HubProxyInfo() {
	}

	public HubProxyInfo(String host, Integer port, List<Pattern> noProxyHostsPatterns, String proxyUsername,
			String proxyPassword) {
		this.host = host;
		this.port = port;

		StringBuilder builder = new StringBuilder();
		if (noProxyHostsPatterns != null && !noProxyHostsPatterns.isEmpty()) {
			for (Pattern pattern : noProxyHostsPatterns) {
				if (builder.length() == 0) {
					builder.append(pattern.pattern());
				} else {
					builder.append("," + pattern.pattern());
				}
			}
		}

		ignoredProxyHosts = builder.toString();

		this.proxyUsername = proxyUsername;
		this.proxyPassword = proxyPassword;
	}

	public HubProxyInfo(String host, Integer port, String noProxyHosts, String proxyUsername, String proxyPassword) {
		this.host = host;
		this.port = port;

		ignoredProxyHosts = noProxyHosts;

		this.proxyUsername = proxyUsername;
		this.proxyPassword = proxyPassword;
	}

	public HubProxyInfo(HubProxyInfo proxyInfo) {
		host = proxyInfo.getHost();

		port = proxyInfo.getPort();

		proxyUsername = proxyInfo.getProxyUsername();
		proxyPassword = proxyInfo.getProxyPassword();
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getProxyUsername() {
		return proxyUsername;
	}

	public void setProxyUsername(String username) {
		proxyUsername = username;
	}

	public String getProxyPassword() {
		return proxyPassword;
	}

	public void setProxyPassword(String password) {
		proxyPassword = password;
	}

	public String getIgnoredProxyHosts() {
		return ignoredProxyHosts;
	}

	public void setIgnoredProxyHosts(String ignoredProxyHosts) {
		this.ignoredProxyHosts = ignoredProxyHosts;
	}

	public List<Pattern> getNoProxyHostPatterns() {
		List<Pattern> noProxyHostsPatterns = new ArrayList<Pattern>();
		if (StringUtils.isNotBlank(ignoredProxyHosts)) {
			String[] ignoreHosts = null;
			if (StringUtils.isNotBlank(ignoredProxyHosts)) {
				if (ignoredProxyHosts.contains(",")) {
					ignoreHosts = ignoredProxyHosts.split(",");
					for (String ignoreHost : ignoreHosts) {
						Pattern pattern = Pattern.compile(ignoreHost.trim());
						noProxyHostsPatterns.add(pattern);
					}
				} else {
					Pattern pattern = Pattern.compile(ignoredProxyHosts);
					noProxyHostsPatterns.add(pattern);
				}
			}
		}
		return noProxyHostsPatterns;
	}

	/**
	 * Checks the list of user defined host names that should be connected to
	 * directly and not go through the proxy. If the hostToMatch matches any of
	 * these hose names then this method returns true.
	 *
	 * @param hostToMatch
	 *            String the hostName to check if it is in the list of hosts
	 *            that should not go through the proxy.
	 *
	 * @return boolean
	 */
	public static boolean checkMatchingNoProxyHostPatterns(String hostToMatch, List<Pattern> noProxyHosts) {
		if (noProxyHosts == null || noProxyHosts.isEmpty()) {
			// User did not specify any hosts to ignore the proxy
			return false;
		}
		boolean match = false;
		if (!StringUtils.isBlank(hostToMatch) && !noProxyHosts.isEmpty()) {

			for (Pattern pattern : noProxyHosts) {
				Matcher m = pattern.matcher(hostToMatch);
				while (m.find()) {
					match = true;
					break;
				}
				if (match) {
					break;
				}
			}
		}
		return match;
	}

	@Override
	public String toString() {
		return "CCProxyInfo [host=" + host + ", port=" + port + ", proxyUsername=" + proxyUsername + ", proxyPassword="
				+ proxyPassword + ", ignoredProxyHosts=" + ignoredProxyHosts + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((ignoredProxyHosts == null) ? 0 : ignoredProxyHosts.hashCode());
		result = prime * result + ((port == null) ? 0 : port);
		result = prime * result + ((proxyPassword == null) ? 0 : proxyPassword.hashCode());
		result = prime * result + ((proxyUsername == null) ? 0 : proxyUsername.hashCode());
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
		HubProxyInfo other = (HubProxyInfo) obj;
		if (host == null) {
			if (other.host != null) {
				return false;
			}
		} else if (!host.equals(other.host)) {
			return false;
		}
		if (ignoredProxyHosts == null) {
			if (other.ignoredProxyHosts != null) {
				return false;
			}
		} else if (!ignoredProxyHosts.equals(other.ignoredProxyHosts)) {
			return false;
		}
		if (port == null) {
			if (other.port != null) {
				return false;
			}
		} else if (!port.equals(other.port)) {
			return false;
		}

		if (proxyPassword == null) {
			if (other.proxyPassword != null) {
				return false;
			}
		} else if (!proxyPassword.equals(other.proxyPassword)) {
			return false;
		}
		if (proxyUsername == null) {
			if (other.proxyUsername != null) {
				return false;
			}
		} else if (!proxyUsername.equals(other.proxyUsername)) {
			return false;
		}
		return true;
	}

}
