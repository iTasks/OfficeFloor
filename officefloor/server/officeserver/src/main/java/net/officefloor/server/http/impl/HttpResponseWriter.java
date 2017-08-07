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

import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.stream.PooledBuffer;

/**
 * Writes the {@link HttpResponse}.
 * 
 * @param <B>
 *            Type of buffer.
 * @author Daniel Sagenschneider
 */
public interface HttpResponseWriter<B> {

	/**
	 * Writes the {@link HttpResponse}.
	 * 
	 * @param version
	 *            {@link HttpVersion}.
	 * @param status
	 *            {@link HttpStatus}.
	 * @param responseHttpheaders
	 *            {@link WritableHttpHeader} instances for the
	 *            {@link HttpResponse}.
	 * @param responseHttpEntity
	 *            {@link PooledBuffer} instances containing the
	 *            {@link HttpResponse} entity.
	 */
	void writeHttpResponse(HttpVersion version, HttpStatus status, Iterable<WritableHttpHeader> responseHttpheaders,
			Iterable<PooledBuffer<B>> responseHttpEntity);

}
