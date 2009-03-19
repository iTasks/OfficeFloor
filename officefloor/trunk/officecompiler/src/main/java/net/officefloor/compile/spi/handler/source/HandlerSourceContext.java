/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.spi.handler.source;

import java.util.Properties;

import net.officefloor.compile.spi.handler.HandlerType;
import net.officefloor.frame.api.execute.Handler;

/**
 * Context for loading a {@link HandlerType}.
 * 
 * @author Daniel
 */
public interface HandlerSourceContext {

	/**
	 * <p>
	 * Obtains the names of the available properties in the order they were
	 * defined. This allows for ability to provide variable number of properties
	 * identified by a naming convention and being able to maintain their order.
	 * <p>
	 * An example would be providing a listing of routing configurations, each
	 * entry named <code>route.[something]</code> and order indicating priority.
	 * 
	 * @return Names of the properties in the order defined.
	 */
	String[] getPropertyNames();

	/**
	 * Obtains a required property value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @return Value of the property.
	 * @throws HandlerUnknownPropertyError
	 *             If property was not configured. Let this propagate as the
	 *             framework will handle it.
	 */
	String getProperty(String name) throws HandlerUnknownPropertyError;

	/**
	 * Obtains the property value or subsequently the default value.
	 * 
	 * @param name
	 *            Name of the property.
	 * @param defaultValue
	 *            Default value if property not specified.
	 * @return Value of the property or the the default value if not specified.
	 */
	String getProperty(String name, String defaultValue);

	/**
	 * Properties to configure the {@link Handler}.
	 * 
	 * @return Properties specific for the {@link Handler}.
	 */
	Properties getProperties();

	/**
	 * Obtains the {@link ClassLoader} for loading the
	 * {@link HandlerTypeBuilder}.
	 * 
	 * @return {@link ClassLoader} for loading the {@link HandlerTypeBuilder}.
	 */
	ClassLoader getClassLoader();

}