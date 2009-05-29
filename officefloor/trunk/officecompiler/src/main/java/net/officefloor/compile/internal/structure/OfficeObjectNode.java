/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeObject} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeObjectNode extends OfficeManagedObjectType,
		OfficeObject, LinkObjectNode {

	/**
	 * Indicates if this {@link OfficeManagedObjectType} has been initialised.
	 * 
	 * @return <code>true</code> if initialised.
	 */
	boolean isInitialised();

	/**
	 * Initialises this {@link OfficeManagedObjectType}.
	 * 
	 * @param objectType
	 *            Object type.
	 */
	void initialise(String objectType);

	/**
	 * <p>
	 * Adds an {@link AdministratorNode} for this
	 * {@link OfficeManagedObjectType}.
	 * <p>
	 * This allows the {@link OfficeManagedObjectType} to report the extension
	 * interfaces required to be supported by the
	 * {@link OfficeFloorManagedObject} for the {@link OfficeObject}.
	 * 
	 * @param administrator
	 *            {@link AdministratorNode}.
	 */
	void addAdministrator(AdministratorNode administrator);

	/**
	 * Adds the context of the {@link OfficeFloor} containing this
	 * {@link OfficeObject}.
	 * 
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 */
	void addOfficeFloorContext(String officeFloorLocation);

}