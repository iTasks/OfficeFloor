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

package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Container of {@link ManagedFunctionLogic}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionContainer extends BlockState {

	/**
	 * Obtains the {@link Flow} containing this {@link ManagedObjectContainer}.
	 * 
	 * @return {@link Flow} containing this {@link ManagedObjectContainer}.
	 */
	Flow getFlow();

	/**
	 * Obtains the {@link ManagedObjectContainer} bound to this
	 * {@link ManagedFunctionContainer}.
	 * 
	 * @param index Index of the {@link ManagedObjectContainer}.
	 * @return {@link ManagedObjectContainer} bound to this
	 *         {@link ManagedFunctionContainer}.
	 */
	ManagedObjectContainer getManagedObjectContainer(int index);

	/**
	 * <p>
	 * Creates a {@link ManagedFunctionInterest} in this
	 * {@link ManagedFunctionContainer}.
	 * <p>
	 * The {@link ManagedFunctionContainer} will not unload its
	 * {@link ManagedFunction} bound {@link ManagedObject} instances until all
	 * registered {@link ManagedFunctionInterest} instances have been unregistered.
	 * 
	 * @return New {@link ManagedFunctionInterest} in this
	 *         {@link ManagedFunctionContainer}.
	 */
	ManagedFunctionInterest createInterest();

}
