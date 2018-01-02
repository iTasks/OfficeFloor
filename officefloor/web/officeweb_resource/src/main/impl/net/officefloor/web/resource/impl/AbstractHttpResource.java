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
package net.officefloor.web.resource.impl;

import net.officefloor.web.resource.HttpResource;

/**
 * Abstract {@link HttpResource}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpResource implements HttpResource {

	/**
	 * Path.
	 */
	protected String path;

	/**
	 * Initiate.
	 * 
	 * @param path
	 *            Resource path.
	 */
	public AbstractHttpResource(String path) {
		this.path = path;
	}

	/*
	 * ======================= HttpResource ==========================
	 */

	@Override
	public String getPath() {
		return this.path;
	}

	/*
	 * ========================= Object ===========================
	 */

	@Override
	public boolean equals(Object obj) {

		// Check if same object
		if (this == obj) {
			return true;
		}

		// Ensure same type
		if (!(obj instanceof AbstractHttpResource)) {
			return false;
		}
		AbstractHttpResource that = (AbstractHttpResource) obj;

		// Return whether same resource by path
		return this.path.equals(that.path);
	}

	@Override
	public int hashCode() {
		return this.path.hashCode();
	}

}