/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.plugin.socket.server.http;

import java.io.OutputStream;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.impl.socket.server.AbstractServerSocketManagedObjectSource;
import net.officefloor.plugin.socket.server.http.HttpServer.HttpServerFlows;
import net.officefloor.plugin.socket.server.http.api.HttpRequest;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.ConnectionHandler;
import net.officefloor.plugin.socket.server.spi.MessageSegment;
import net.officefloor.plugin.socket.server.spi.Server;
import net.officefloor.plugin.socket.server.spi.ServerSocketHandler;

/**
 * {@link ManagedObjectSource} for a {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServerSocketManagedObjectSource extends
		AbstractServerSocketManagedObjectSource<HttpServerFlows> implements
		ServerSocketHandler<HttpServerFlows> {

	/**
	 * Initial size in bytes of the buffer to contain the {@link HttpRequest}
	 * body.
	 */
	// TODO provide via property
	protected int initialRequestBodyBufferLength = 1024;

	/**
	 * Obtains the initial size in bytes of the buffer to contain the
	 * {@link HttpRequest} body.
	 * 
	 * @return Initial buffer size.
	 */
	protected int getInitialRequestBodyBufferLength() {
		return this.initialRequestBodyBufferLength;
	}

	/**
	 * Maximum length in bytes of the {@link HttpRequest} body.
	 */
	// TODO provide via property
	protected int maximumRequestBodyLength = (1024 * 1024);

	/**
	 * Obtains the maximum length in bytes of the {@link HttpRequest} body.
	 * 
	 * @return Maximum length.
	 */
	protected int getMaximumRequestBodyLength() {
		return this.maximumRequestBodyLength;
	}

	/**
	 * Response buffer length for each {@link MessageSegment} being appended to
	 * by the {@link OutputStream} to populate the body.
	 */
	// TODO provide via property
	protected int responseBufferLength = 1024;

	/**
	 * Obtains the response buffer length for each {@link MessageSegment} being
	 * appended to by the {@link OutputStream} to populate the body.
	 * 
	 * @return Response buffer length.
	 */
	protected int getResponseBufferLength() {
		return this.responseBufferLength;
	}

	/**
	 * Timeout of the {@link Connection} in milliseconds.
	 */
	// TODO provide via property
	protected long connectionTimeout = 5 * 60 * 1000;

	/**
	 * Returns the {@link Connection} timeout in milliseconds.
	 * 
	 * @return {@link Connection} timeout.
	 */
	protected long getConnectionTimeout() {
		return this.connectionTimeout;
	}

	/*
	 * ============= AbstractServerSocketManagedObjectSource ===============
	 */

	@Override
	protected ServerSocketHandler<HttpServerFlows> createServerSocketHandler(
			MetaDataContext<None, HttpServerFlows> context) throws Exception {

		// Specify types
		context.setManagedObjectClass(HttpManagedObject.class);
		context.setObjectClass(ServerHttpConnection.class);

		// Provide the flow to handle the HTTP request
		context.addFlow(HttpServerFlows.HANDLE_HTTP_REQUEST,
				ServerHttpConnection.class);

		// Return this as the server socket handler
		return this;
	}

	/*
	 * =================== ServerSocketHandler ===========================
	 */

	@Override
	public Server<HttpServerFlows> createServer() {
		return new HttpServer();
	}

	@Override
	public ConnectionHandler createConnectionHandler(Connection connection) {
		return new HttpConnectionHandler(this, connection);
	}

}