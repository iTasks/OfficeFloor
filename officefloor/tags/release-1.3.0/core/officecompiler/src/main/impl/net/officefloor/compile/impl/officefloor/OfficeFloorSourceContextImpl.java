/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.compile.impl.officefloor;

import net.officefloor.compile.impl.properties.PropertyListSourceProperties;
import net.officefloor.compile.impl.util.ConfigurationContextPropagateError;
import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.model.repository.ConfigurationItem;

/**
 * {@link OfficeFloorSourceContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorSourceContextImpl extends SourcePropertiesImpl
		implements OfficeFloorSourceContext {

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final String officeFloorLocation;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initiate.
	 * 
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @param propertyList
	 *            {@link PropertyList}.
	 * @param nodeContext
	 *            {@link NodeContext}.
	 */
	public OfficeFloorSourceContextImpl(String officeFloorLocation,
			PropertyList propertyList, NodeContext nodeContext) {
		super(new PropertyListSourceProperties(propertyList));
		this.officeFloorLocation = officeFloorLocation;
		this.context = nodeContext;
	}

	/*
	 * =============== OfficeFloorLoaderContext ============================
	 */

	@Override
	public String getOfficeFloorLocation() {
		return this.officeFloorLocation;
	}

	@Override
	public ConfigurationItem getConfiguration(String location) {
		try {
			return this.context.getConfigurationContext().getConfigurationItem(
					location);
		} catch (Throwable ex) {
			// Propagate failure to office floor loader
			throw new ConfigurationContextPropagateError(location, ex);
		}
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.context.getClassLoader();
	}

	@Override
	public PropertyList createPropertyList() {
		return this.context.createPropertyList();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedObjectType<?> loadManagedObjectType(
			String managedObjectSourceClassName, PropertyList properties) {

		// Obtain the managed object source class
		Class managedObjectSourceClass = this.context
				.getManagedObjectSourceClass(managedObjectSourceClassName,
						LocationType.OFFICE_FLOOR, this.officeFloorLocation,
						"loadManagedObjectType");

		// Ensure have the managed object source class
		if (managedObjectSourceClass == null) {
			throw new LoadTypeError(ManagedObjectType.class,
					managedObjectSourceClassName);
		}

		// Load the managed object type
		ManagedObjectLoader managedObjectLoader = this.context
				.getManagedObjectLoader(LocationType.OFFICE_FLOOR,
						this.officeFloorLocation, "loadManagedObjectType");
		ManagedObjectType<?> managedObjectType = managedObjectLoader
				.loadManagedObjectType(managedObjectSourceClass, properties);

		// Ensure have the managed object type
		if (managedObjectType == null) {
			throw new LoadTypeError(ManagedObjectType.class,
					managedObjectSourceClassName);
		}

		// Return the managed object type
		return managedObjectType;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public OfficeType loadOfficeType(String officeSourceClassName,
			String location, PropertyList properties) {

		// Obtain the office source class
		Class officeSourceClass = this.context.getOfficeSourceClass(
				officeSourceClassName, this.officeFloorLocation,
				"loadOfficeType");

		// Ensure have the office source class
		if (officeSourceClass == null) {
			throw new LoadTypeError(OfficeType.class, officeSourceClassName);
		}

		// Load the office type
		OfficeLoader officeLoader = this.context.getOfficeLoader();
		OfficeType officeType = officeLoader.loadOfficeType(officeSourceClass,
				location, properties);

		// Ensure have the office type
		if (officeType == null) {
			throw new LoadTypeError(OfficeType.class, officeSourceClassName);
		}

		// Return the office type
		return officeType;
	}

}