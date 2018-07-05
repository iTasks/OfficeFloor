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
package net.officefloor.web.escalation;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.WritableHttpHeader;

/**
 * Bad request {@link HttpException}.
 * 
 * @author Daniel Sagenschneider
 */
public class BadRequestHttpException extends HttpException {

	/**
	 * Instantiate.
	 * 
	 * @param headers
	 *            {@link WritableHttpHeader} instances.
	 * @param entity
	 *            {@link HttpResponse} entity.
	 */
	public BadRequestHttpException(WritableHttpHeader[] headers, String entity) {
		super(HttpStatus.BAD_REQUEST, headers, entity);
	}

}