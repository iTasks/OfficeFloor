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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.plugin.socket.server.http.api.HttpRequest;
import net.officefloor.plugin.socket.server.http.api.HttpResponse;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParser;

/**
 * {@link ManagedObject} for the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpManagedObject implements ServerHttpConnection, ManagedObject,
		EscalationHandler {

	/**
	 * {@link HttpConnectionHandler}.
	 */
	private HttpConnectionHandler connectionHandler;

	/**
	 * {@link HttpRequest}.
	 */
	private final HttpRequest request;

	/**
	 * {@link HttpResponse}.
	 */
	private final HttpResponseImpl response;

	/**
	 * Initiate.
	 * 
	 * @param connectionHandler
	 *            {@link HttpConnectionHandler} that has just received a HTTP
	 *            request.
	 */
	public HttpManagedObject(HttpConnectionHandler connectionHandler) {
		this.connectionHandler = connectionHandler;

		// Create the request
		this.request = new HttpRequestImpl(this.connectionHandler
				.getHttpRequestParser());

		// Create the response to fill out
		this.response = new HttpResponseImpl(connectionHandler, this.request);
	}

	/*
	 * =============== ServerHttpConnection =================================
	 */

	@Override
	public Object getObject() throws Exception {
		return this;
	}

	/*
	 * ================== ServerHttpConnection =========================
	 */

	@Override
	public synchronized HttpRequest getHttpRequest() {
		return this.request;
	}

	@Override
	public HttpResponse getHttpResponse() {
		return this.response;
	}

	/*
	 * ================== EscalationHandler =============================
	 */

	@Override
	public void handleEscalation(Throwable escalation) throws Throwable {
		// Indicate failure to handle request
		this.response.sendFailure(escalation);
	}

	/**
	 * {@link HttpRequest} implementation.
	 */
	private class HttpRequestImpl implements HttpRequest {

		/**
		 * Method.
		 */
		private final String method;

		/**
		 * Path.
		 */
		private final String path;

		/**
		 * Version.
		 */
		private final String version;

		/**
		 * Headers.
		 */
		private final Map<String, String> headers;

		/**
		 * Body.
		 */
		private final byte[] body;

		/**
		 * Initiate from {@link HttpRequestParser}.
		 * 
		 * @param parser
		 *            {@link HttpRequestParser}.
		 */
		public HttpRequestImpl(HttpRequestParser parser) {
			this.method = parser.getMethod();
			this.path = parser.getPath();
			this.version = parser.getVersion();
			this.headers = parser.getHeaders();
			this.body = parser.getBody();
		}

		/*
		 * ================ HttpRequest ================================
		 */
		
		@Override
		public String getMethod() {
			return this.method;
		}

		@Override
		public String getPath() {
			return this.path;
		}

		@Override
		public String getVersion() {
			return this.version;
		}

		@Override
		public Set<String> getHeaderNames() {
			return this.headers.keySet();
		}

		@Override
		public String getHeader(String name) {
			return this.headers.get(name.toUpperCase());
		}

		@Override
		public InputStream getBody() {
			return new ByteArrayInputStream(this.body);
		}
	}

}