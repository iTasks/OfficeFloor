/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.web.resource.spi;

import java.util.ServiceLoader;

/**
 * <p>
 * Factory for the creation of a {@link ResourceTransformer}.
 * <p>
 * This is loaded by the {@link ServiceLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResourceTransformerFactory {

	/**
	 * Obtains the name of transformation.
	 * 
	 * @return Name of transformation.
	 */
	String getName();

	/**
	 * Creates the {@link ResourceTransformer}.
	 * 
	 * @return {@link ResourceTransformer}.
	 */
	ResourceTransformer createResourceTransformer();

}