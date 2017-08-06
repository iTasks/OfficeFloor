/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.server.http;

import net.officefloor.server.stream.ServerInputStream;

/**
 * HTTP request from the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequest {

	/**
	 * Obtains the {@link HttpMethod}.
	 * 
	 * @return {@link HttpMethod}.
	 */
	HttpMethod getHttpMethod();

	/**
	 * Obtains the Request URI as provided on the request.
	 * 
	 * @return Request URI as provided on the request.
	 */
	String getRequestURI();

	/**
	 * Obtains the {@link HttpVersion}.
	 * 
	 * @return {@link HttpVersion}.
	 */
	HttpVersion getHttpVersion();

	/**
	 * Obtains the {@link HttpRequestHeaders}.
	 * 
	 * @return {@link HttpRequestHeaders}.
	 */
	HttpRequestHeaders getHttpHeaders();

	/**
	 * Obtains the {@link ServerInputStream} to the entity of the HTTP request.
	 * 
	 * @return {@link ServerInputStream} to the entity of the HTTP request.
	 */
	ServerInputStream getEntity();

}