/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.officefloor.plugin.socket.server.http.conversation.impl;

import java.util.List;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.stream.InputBufferStream;

/**
 * {@link HttpRequest} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class HttpRequestImpl implements HttpRequest {

	/**
	 * Method.
	 */
	private final String method;

	/**
	 * Request URI.
	 */
	private final String requestURI;

	/**
	 * Version.
	 */
	private final String version;

	/**
	 * Headers.
	 */
	private final List<HttpHeader> headers;

	/**
	 * Body.
	 */
	private final InputBufferStream body;

	/**
	 * Initiate.
	 *
	 * @param method
	 *            Method.
	 * @param requestURI
	 *            Request URI.
	 * @param httpVersion
	 *            HTTP version.
	 * @param headers
	 *            {@link HttpHeader} instances.
	 * @param body
	 *            {@link InputBufferStream} to the body.
	 */
	public HttpRequestImpl(String method, String requestURI,
			String httpVersion, List<HttpHeader> headers, InputBufferStream body) {
		this.method = method;
		this.requestURI = requestURI;
		this.version = httpVersion;
		this.headers = headers;
		this.body = body;
	}

	/**
	 * Cleans up this {@link HttpRequest}.
	 */
	void cleanup() {
		// Close the body to clean it up
		this.body.close();
	}

	/*
	 * ================ HttpRequest ================================
	 */

	@Override
	public String getMethod() {
		return this.method;
	}

	@Override
	public String getRequestURI() {
		return this.requestURI;
	}

	@Override
	public String getVersion() {
		return this.version;
	}

	@Override
	public List<HttpHeader> getHeaders() {
		return this.headers;
	}

	@Override
	public InputBufferStream getBody() {
		return this.body;
	}

}