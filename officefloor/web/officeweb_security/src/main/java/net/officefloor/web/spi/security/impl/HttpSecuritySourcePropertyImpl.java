/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.spi.security.impl;

import net.officefloor.web.spi.security.HttpSecuritySourceProperty;

/**
 * {@link HttpSecuritySourceProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecuritySourcePropertyImpl implements
		HttpSecuritySourceProperty {

	/**
	 * Name of property.
	 */
	protected final String name;

	/**
	 * Label of property.
	 */
	protected final String label;

	/**
	 * Initiate with name and label of property.
	 * 
	 * @param name
	 *            Name of property.
	 * @param label
	 *            Label of property.
	 */
	public HttpSecuritySourcePropertyImpl(String name, String label) {
		this.name = name;
		this.label = label;
	}

	/*
	 * ================== HttpSecuritySourceProperty ==============
	 */

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}
