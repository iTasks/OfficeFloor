/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.compile.impl.office;

import java.util.Properties;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.ConfigurationContextPropagateError;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.OfficeUnknownPropertyError;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.model.repository.ConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link OfficeSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSourceContextImpl implements OfficeSourceContext {

	/**
	 * Location of the {@link Office}.
	 */
	private final String officeLocation;

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * Initiate.
	 * 
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	public OfficeSourceContextImpl(String officeLocation,
			ConfigurationContext configurationContext,
			PropertyList propertyList, ClassLoader classLoader) {
		this.officeLocation = officeLocation;
		this.configurationContext = configurationContext;
		this.propertyList = propertyList;
		this.classLoader = classLoader;
	}

	/*
	 * ================= OfficeLoaderContext ================================
	 */

	@Override
	public String getOfficeLocation() {
		return this.officeLocation;
	}

	@Override
	public ConfigurationItem getConfiguration(String location) {
		try {
			return this.configurationContext.getConfigurationItem(location);
		} catch (Throwable ex) {
			// Propagate failure to office loader
			throw new ConfigurationContextPropagateError(location, ex);
		}
	}

	@Override
	public String[] getPropertyNames() {
		return this.propertyList.getPropertyNames();
	}

	@Override
	public String getProperty(String name) throws OfficeUnknownPropertyError {
		String value = this.getProperty(name, null);
		if (value == null) {
			throw new OfficeUnknownPropertyError("Unknown property '" + name
					+ "'", name);
		}
		return value;
	}

	@Override
	public String getProperty(String name, String defaultValue) {
		Property property = this.propertyList.getProperty(name);
		String value = (property != null ? property.getValue() : null);
		if (CompileUtil.isBlank(value)) {
			return defaultValue;
		}
		return value;
	}

	@Override
	public Properties getProperties() {
		return this.propertyList.getProperties();
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

}