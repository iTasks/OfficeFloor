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

import java.util.List;

import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Container managing a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectContainer {

	/**
	 * Obtains the {@link ThreadState} responsible for changes to this
	 * {@link ManagedObjectContainer}.
	 * 
	 * @return {@link ThreadState} responsible for changes to this
	 *         {@link ManagedObjectContainer}.
	 */
	ThreadState getResponsibleThreadState();

	/**
	 * Creates a {@link FunctionState} to load the {@link ManagedObject}.
	 * 
	 * @param managedFunctionContainer
	 *            {@link ManagedFunctionContainer} requiring the
	 *            {@link ManagedObject}.
	 * @return Optional {@link FunctionState} to load the {@link ManagedObject}.
	 *         Should this return </code>null</code>, the
	 *         {@link ManagedFunctionContainer} should not then be executed, as
	 *         it is expecting to wait. This will return the
	 *         {@link ManagedFunctionContainer} when the {@link ManagedObject}
	 *         is loaded.
	 */
	FunctionState loadManagedObject(ManagedFunctionContainer managedFunctionContainer);

	/**
	 * <p>
	 * Creates a {@link FunctionState} to check if the {@link ManagedObject}
	 * contained within this {@link ManagedObjectContainer} is ready.
	 * <p>
	 * Should the {@link ManagedObject} not be ready, then will latch to wait
	 * for the {@link ManagedObject} to be ready.
	 * 
	 * @param check
	 *            {@link ManagedObjectReadyCheck}.
	 * @return {@link FunctionState} to check if the {@link ManagedObject}
	 *         contained within this {@link ManagedObjectContainer} is ready.
	 */
	FunctionState checkReady(ManagedObjectReadyCheck check);

	/**
	 * <p>
	 * Extracts the {@link ManagedObject} extension from the
	 * {@link ManagedObject} contained in this {@link ManagedObjectContainer}.
	 * <p>
	 * Should the {@link ManagedObject} not be loaded, then no
	 * {@link ManagedObject} extension will be loaded.
	 * 
	 * @param extractor
	 *            {@link ExtensionInterfaceExtractor}.
	 * @param managedObjectExtensions
	 *            {@link List} to load the {@link ManagedObject} extension.
	 * @return {@link FunctionState} to load the {@link ManagedObject}
	 *         extension.
	 */
	<E> FunctionState extractExtensionInterface(ExtensionInterfaceExtractor<E> extractor,
			List<E> managedObjectExtensions);

	/**
	 * Obtains the object being managed by the {@link ManagedObject}.
	 * 
	 * @return Object being managed by the {@link ManagedObject}.
	 */
	Object getObject();

	/**
	 * Creates a {@link FunctionState} to unload the {@link ManagedObject}.
	 * 
	 * @return {@link FunctionState} to unload the {@link ManagedObject}.
	 */
	FunctionState unloadManagedObject();

}