/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.server.http.impl;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.HttpServerLocation;

/**
 * {@link HttpServerLocation} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServerLocationImpl implements HttpServerLocation {

	/**
	 * Default HTTP port.
	 */
	public static final int DEFAULT_HTTP_PORT = 7878;

	/**
	 * Default HTTPS port.
	 */
	public static final int DEFAULT_HTTPS_PORT = 7979;

	/**
	 * Obtains the default host name.
	 * 
	 * @return Default Host name.
	 */
	public static String getDefaultHostName() {
		return "localhost"; // default only local connections
	}

	/**
	 * Domain.
	 */
	private final String domain;

	/**
	 * HTTP port.
	 */
	private final int httpPort;

	/**
	 * HTTPS port.
	 */
	private final int httpsPort;

	/**
	 * Cluster host name.
	 */
	private final String clusterHostName;

	/**
	 * Cluster HTTP port.
	 */
	private final int clusterHttpPort;

	/**
	 * Cluster HTTPS port.
	 */
	private final int clusterHttpsPort;

	/**
	 * Client URL to the {@link HttpServer}.
	 */
	private final String clientHttpUrl;

	/**
	 * Client secure URL to the {@link HttpServer}.
	 */
	private final String clientHttpsUrl;

	/**
	 * Instantiate with defaults for testing.
	 */
	public HttpServerLocationImpl() {
		this(getDefaultHostName(), 7878, 7979);
	}

	/**
	 * Instantiate from {@link Property} values configured firstly from
	 * {@link SourceContext}, then {@link System} (then defaults).
	 * 
	 * @param context
	 *            {@link SourceContext}.
	 */
	public HttpServerLocationImpl(SourceContext context) {

		// Obtain the public client configuration
		this.domain = HttpServer.getPropertyString(PROPERTY_DOMAIN, context, () -> getDefaultHostName());
		this.httpPort = HttpServer.getPropertyInteger(PROPERTY_HTTP_PORT, context, () -> DEFAULT_HTTP_PORT);
		this.httpsPort = HttpServer.getPropertyInteger(PROPERTY_HTTPS_PORT, context, () -> DEFAULT_HTTPS_PORT);

		// Obtain the cluster configuration
		this.clusterHostName = HttpServer.getPropertyString(PROPERTY_CLUSTER_HOST_NAME, context, () -> this.domain);
		this.clusterHttpPort = HttpServer.getPropertyInteger(PROPERTY_CLUSTER_HTTP_PORT, context, () -> this.httpPort);
		this.clusterHttpsPort = HttpServer.getPropertyInteger(PROPERTY_CLUSTER_HTTPS_PORT, context,
				() -> this.httpsPort);

		// Create the client URLs
		this.clientHttpUrl = this.getClientHttpUrl();
		this.clientHttpsUrl = this.getClientHttpsUrl();
	}

	/**
	 * Instantiate for running single instance.
	 * 
	 * @param domain
	 *            Domain.
	 * @param httpPort
	 *            HTTP port.
	 * @param httpsPort
	 *            HTTPS port.
	 */
	public HttpServerLocationImpl(String domain, int httpPort, int httpsPort) {
		this(domain, httpPort, httpsPort, domain, httpPort, httpsPort);
	}

	/**
	 * Instantiate for running in a cluster with same port mappings.
	 * 
	 * @param domain
	 *            Domain.
	 * @param httpPort
	 *            HTTP port.
	 * @param httpsPort
	 *            HTTPS port.
	 * @param clusterHostName
	 *            Cluster host name.
	 */
	public HttpServerLocationImpl(String domain, int httpPort, int httpsPort, String clusterHostName) {
		this(domain, httpPort, httpsPort, clusterHostName, httpPort, httpsPort);
	}

	/**
	 * Instantiate for running in a cluster.
	 * 
	 * @param domain
	 *            Domain.
	 * @param httpPort
	 *            HTTP port.
	 * @param httpsPort
	 *            HTTPS port.
	 * @param clusterHostName
	 *            Cluster host name.
	 * @param clusterHttpPort
	 *            Cluster HTTP port.
	 * @param clusterHttpsPort
	 *            Cluster HTTPS port.
	 */
	public HttpServerLocationImpl(String domain, int httpPort, int httpsPort, String clusterHostName,
			int clusterHttpPort, int clusterHttpsPort) {
		this.domain = domain;
		this.httpPort = httpPort;
		this.httpsPort = httpsPort;
		this.clusterHostName = clusterHostName;
		this.clusterHttpPort = clusterHttpPort;
		this.clusterHttpsPort = clusterHttpsPort;

		// Create the client URLs
		this.clientHttpUrl = this.getClientHttpUrl();
		this.clientHttpsUrl = this.getClientHttpsUrl();
	}

	/**
	 * Obtains the Client HTTP URL.
	 * 
	 * @return Client HTTP URL.
	 */
	private String getClientHttpUrl() {
		return "http://" + this.domain + (this.httpPort == 80 ? "" : ":" + String.valueOf(this.httpPort));
	}

	/**
	 * Obtains the Client HTTPS URL.
	 * 
	 * @return Client HTTPS URL.
	 */
	private String getClientHttpsUrl() {
		return "https://" + this.domain + (this.httpsPort == 443 ? "" : ":" + String.valueOf(this.httpsPort));
	}

	/*
	 * ================= HttpServerImplementation =======================
	 */

	@Override
	public String getDomain() {
		return this.domain;
	}

	@Override
	public int getHttpPort() {
		return this.httpPort;
	}

	@Override
	public int getHttpsPort() {
		return this.httpsPort;
	}

	@Override
	public String getClusterHostName() {
		return this.clusterHostName;
	}

	@Override
	public int getClusterHttpPort() {
		return this.clusterHttpPort;
	}

	@Override
	public int getClusterHttpsPort() {
		return this.clusterHttpsPort;
	}

	@Override
	public String createClientUrl(boolean isSecure, String path) {
		return (isSecure ? this.clientHttpsUrl : this.clientHttpUrl) + path;
	}

}