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
package net.officefloor.web;

import net.officefloor.server.http.HttpException;

/**
 * Dependency injected interface to send the {@link Object} response.
 * 
 * @author Daniel Sagenschneider
 */
public interface ObjectResponse<T> {

	/**
	 * Sends the {@link Object}.
	 * 
	 * @param object
	 *            {@link Object} to send as response.
	 * @throws HttpException
	 *             If fails to send the {@link Object}.
	 */
	void send(T object) throws HttpException;

}