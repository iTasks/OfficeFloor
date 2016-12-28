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
package net.officefloor.frame.internal.structure;

/**
 * Container of {@link ManagedFunctionLogic}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionContainer extends FunctionState {

	/**
	 * Obtains the {@link Flow} containing this {@link ManagedObjectContainer}.
	 * 
	 * @return {@link Flow} containing this {@link ManagedObjectContainer}.
	 */
	Flow getFlow();

	/**
	 * Specifies a {@link ManagedFunctionContainer} to be sequentially executed
	 * after this {@link ManagedFunctionContainer}.
	 * 
	 * @param container
	 *            {@link ManagedFunctionContainer} to be sequentially executed
	 *            after this {@link ManagedFunctionContainer}.
	 */
	void setNextManagedFunctionContainer(ManagedFunctionContainer container);

	/**
	 * Obtains the {@link ManagedObjectContainer} bound to this
	 * {@link ManagedFunctionContainer}.
	 * 
	 * @param index
	 *            Index of the {@link ManagedObjectContainer}.
	 * @return {@link ManagedObjectContainer} bound to this
	 *         {@link ManagedFunctionContainer}.
	 */
	ManagedObjectContainer getManagedObjectContainer(int index);

}