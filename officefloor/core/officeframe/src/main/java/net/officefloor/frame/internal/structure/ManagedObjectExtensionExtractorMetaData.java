/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Meta-data to extract the extension from the {@link ManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExtensionExtractorMetaData<E extends Object> {

	/**
	 * Obtains the {@link ManagedObjectIndex} to identify the
	 * {@link ManagedObject} to extract the extension interface from.
	 *
	 * @return {@link ManagedObjectIndex} to identify the {@link ManagedObject}
	 *         to extract the extension interface from.
	 */
	ManagedObjectIndex getManagedObjectIndex();

	/**
	 * Obtains the {@link ManagedObjectExtensionExtractor} to extract the
	 * Extension Interface from the {@link ManagedObject}.
	 *
	 * @return {@link ManagedObjectExtensionExtractor} to extract the Extension
	 *         Interface from the {@link ManagedObject}.
	 */
	ManagedObjectExtensionExtractor<E> getManagedObjectExtensionExtractor();

}