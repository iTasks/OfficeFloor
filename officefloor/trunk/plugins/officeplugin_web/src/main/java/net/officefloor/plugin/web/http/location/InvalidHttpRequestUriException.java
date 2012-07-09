/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.location;

import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.InvalidHttpRequestException;

/**
 * Indicates the {@link HttpRequest} request URI is invalid.
 *
 * @author Daniel Sagenschneider
 */
public class InvalidHttpRequestUriException extends InvalidHttpRequestException {

	/**
	 * Initiate.
	 *
	 * @param httpStatus
	 *            HTTP status of exception.
	 * @param message
	 *            Message.
	 */
	public InvalidHttpRequestUriException(int httpStatus, String message) {
		super(httpStatus, message);
	}

}