/*-
 * #%L
 * OfficeFrame
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

/*
 * Created on Nov 30, 2005
 */
package net.officefloor.frame.api.managedobject;

/**
 * <p>
 * Registry providing the dependent Object instances for a
 * {@link CoordinatingManagedObject} instance.
 * <p>
 * This is provided by the OfficeFloor implementation.
 * 
 * @author Daniel Sagenschneider
 */
public interface ObjectRegistry<O extends Enum<O>> {

	/**
	 * Obtains the dependency {@link Object} for the dependency key.
	 * 
	 * @param key
	 *            Key identifying the dependency {@link Object}.
	 * @return Dependency {@link Object} for the key.
	 */
	Object getObject(O key);

	/**
	 * <p>
	 * Obtains the dependency {@link Object} by its index.
	 * <p>
	 * This enables a dynamic number of dependencies for the
	 * {@link ManagedObject}.
	 * 
	 * @param index
	 *            Index identifying the dependency {@link Object}.
	 * @return Dependency {@link Object} for the index.
	 */
	Object getObject(int index);

}
