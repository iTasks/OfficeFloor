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
package net.officefloor.compile.managedobject;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectType<D extends Enum<D>> {

	/**
	 * Obtains the {@link Class} name of the object returned from
	 * {@link ManagedObject}.
	 * 
	 * @return The {@link Class} name of the object being managed by the
	 *         {@link ManagedObject}.
	 */
	String getObjectType();

	/**
	 * Obtains the {@link ManagedObjectDependencyType} definitions of the required
	 * dependencies for the {@link ManagedObject}.
	 * 
	 * @return {@link ManagedObjectDependencyType} definitions of the required
	 *         dependencies for the {@link ManagedObject}.
	 */
	ManagedObjectDependencyType<D>[] getDependencyTypes();

	/**
	 * <p>
	 * Indicates if the {@link ManagedObjectSource} may trigger a {@link Flow}.
	 * <p>
	 * Note that a {@link ManagedObjectSource} can provide no
	 * {@link ManagedObjectFlowType} instances yet still be input (as
	 * {@link ManagedObjectSource} provides the {@link ManagedFunction} for the
	 * {@link Flow}).
	 * 
	 * @return <code>true</code> if input.
	 */
	boolean isInput();

	/**
	 * Obtains the {@link ManagedObjectFlowType} definitions of the {@link Flow}
	 * instances required to be linked for the {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectFlowType} definitions of the {@link Flow}
	 *         instances required to be linked for the {@link ManagedObjectSource}.
	 */
	ManagedObjectFlowType<?>[] getFlowTypes();

	/**
	 * Obtains the {@link ManagedObjectTeamType} definitions of {@link Team}
	 * instances required by the {@link ManagedObject}.
	 * 
	 * @return {@link ManagedObjectTeamType} definitions of {@link Team} instances
	 *         required by the {@link ManagedObject}.
	 */
	ManagedObjectTeamType[] getTeamTypes();

	/**
	 * Obtains the extension types supported by the {@link ManagedObject}.
	 * 
	 * @return Extension types supported by the {@link ManagedObject}.
	 */
	String[] getExtensionTypes();

}