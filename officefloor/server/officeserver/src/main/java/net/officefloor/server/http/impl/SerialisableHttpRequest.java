/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.http.impl;

import java.io.Serializable;

import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpRequestHeaders;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.stream.ServerInputStream;
import net.officefloor.server.stream.impl.ByteArrayByteSequence;
import net.officefloor.server.stream.impl.ByteSequence;
import net.officefloor.server.stream.impl.ByteSequenceServerInputStream;

/**
 * {@link Serializable} {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public class SerialisableHttpRequest implements HttpRequest, Serializable {

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * {@link HttpMethod}.
	 */
	private final HttpMethod method;

	/**
	 * Request URI.
	 */
	private final String requestUri;

	/**
	 * {@link HttpVersion}.
	 */
	private transient final HttpVersion version;

	/**
	 * {@link HttpRequestHeaders}.
	 */
	private final SerialisableHttpRequestHeaders headers;

	/**
	 * {@link ByteArrayByteSequence} for the HTTP entity.
	 */
	private final ByteArrayByteSequence entity;

	/**
	 * {@link ByteSequenceServerInputStream}.
	 */
	private transient ByteSequenceServerInputStream entityStream;

	/**
	 * Instantiate from existing {@link HttpRequest}.
	 * 
	 * @param request
	 *            {@link HttpRequest}.
	 * @param entity
	 *            {@link ByteSequence} to entity of {@link HttpRequest}.
	 */
	public SerialisableHttpRequest(HttpRequest request, ByteSequence entity) {
		this.method = request.getHttpMethod();
		this.requestUri = request.getRequestURI();
		this.version = request.getHttpVersion();

		// Ensure have serializable headers
		HttpRequestHeaders headers = request.getHttpHeaders();
		if (headers instanceof SerialisableHttpRequestHeaders) {
			// Use immutable serialisable instance
			this.headers = (SerialisableHttpRequestHeaders) headers;
		} else {
			// Create serialisable copy
			this.headers = new SerialisableHttpRequestHeaders(headers);
		}

		// Create the entity
		if (entity instanceof ByteArrayByteSequence) {
			// Use immutable serialisable instance
			this.entity = (ByteArrayByteSequence) entity;
		} else {
			// Create serialisable copy
			byte[] data = new byte[entity.length()];
			for (int i = 0; i < data.length; i++) {
				data[i] = entity.byteAt(i);
			}
			this.entity = new ByteArrayByteSequence(data);
		}

		// Create the stream
		this.entityStream = new ByteSequenceServerInputStream(this.entity, 0);
	}

	/**
	 * Instantiate.
	 * 
	 * @param method
	 *            {@link HttpMethod}.
	 * @param requestUri
	 *            Request URI.
	 * @param version
	 *            {@link HttpVersion}.
	 * @param headers
	 *            {@link SerialisableHttpRequestHeaders}.
	 * @param entity
	 *            {@link ByteArrayByteSequence} for the entity.
	 */
	public SerialisableHttpRequest(HttpMethod method, String requestUri, HttpVersion version,
			SerialisableHttpRequestHeaders headers, ByteArrayByteSequence entity) {
		this.method = method;
		this.requestUri = requestUri;
		this.version = version;
		this.headers = headers;
		this.entity = entity;
		this.entityStream = new ByteSequenceServerInputStream(this.entity, 0);
	}

	/**
	 * Creates a {@link SerialisableHttpRequest} from this {@link Serializable}
	 * state.
	 * 
	 * @param clientHttpVersion
	 *            {@link HttpVersion} that the client is currently using.
	 * @return {@link SerialisableHttpRequest}.
	 */
	public SerialisableHttpRequest createHttpRequest(HttpVersion clientHttpVersion) {
		return new SerialisableHttpRequest(this.method, this.requestUri, clientHttpVersion, this.headers, entity);
	}

	/**
	 * Obtains the {@link ByteSequence} for the entity.
	 * 
	 * @return {@link ByteSequence} for the entity.
	 */
	protected ByteSequence getEntityByteSequence() {
		return this.entity;
	}

	/*
	 * ================ HttpRequest =================
	 */

	@Override
	public HttpMethod getHttpMethod() {
		return this.method;
	}

	@Override
	public String getRequestURI() {
		return this.requestUri;
	}

	@Override
	public HttpVersion getHttpVersion() {
		return this.version;
	}

	@Override
	public HttpRequestHeaders getHttpHeaders() {
		return this.headers;
	}

	@Override
	public ServerInputStream getEntity() {
		return this.entityStream;
	}

}
