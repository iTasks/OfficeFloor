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
package net.officefloor.compile.spi.office;

import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectSource} within the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeManagedObjectSource {

	/**
	 * Obtains the name of this {@link OfficeManagedObjectSource}.
	 * 
	 * @return Name of this {@link OfficeManagedObjectSource}.
	 */
	String getOfficeManagedObjectSourceName();

	/**
	 * Adds a {@link Property} to source the {@link ManagedObject} from the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param name
	 *            Name of the {@link Property}.
	 * @param value
	 *            Value of the {@link Property}.
	 */
	void addProperty(String name, String value);

	/**
	 * Obtains the {@link ManagedObjectFlow} for the
	 * {@link ManagedObjectFlowType}.
	 * 
	 * @param managedObjectSourceFlowName
	 *            Name of the {@link ManagedObjectFlowType}.
	 * @return {@link ManagedObjectFlow}.
	 */
	ManagedObjectFlow getManagedObjectFlow(String managedObjectSourceFlowName);

	/**
	 * Obtains the {@link ManagedObjectTeam} for the
	 * {@link ManagedObjectTeamType}.
	 * 
	 * @param managedObjectSourceTeamName
	 *            Name of the {@link ManagedObjectTeamType}.
	 * @return {@link ManagedObjectTeam}.
	 */
	ManagedObjectTeam getManagedObjectTeam(String managedObjectSourceTeamName);

	/**
	 * Obtains the {@link OfficeManagedObject} representing an instance use of a
	 * {@link ManagedObject} from the {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link OfficeManagedObject}. Typically this will
	 *            be the name under which the {@link ManagedObject} will be
	 *            registered to the {@link Office}.
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope} of the {@link OfficeManagedObject}
	 *            within the {@link Office}.
	 * @return {@link OfficeManagedObject}.
	 */
	OfficeManagedObject addOfficeManagedObject(String managedObjectName,
			ManagedObjectScope managedObjectScope);

}