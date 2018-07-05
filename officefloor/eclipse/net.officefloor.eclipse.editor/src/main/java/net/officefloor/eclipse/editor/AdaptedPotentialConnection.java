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
package net.officefloor.eclipse.editor;

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Potential {@link AdaptedConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedPotentialConnection {

	/**
	 * Obtains the source {@link Model} {@link Class}.
	 * 
	 * @return Source {@link Model} {@link Class}.
	 */
	Class<?> getSourceModelClass();

	/**
	 * Obtains the target {@link Model} {@link Class}.
	 * 
	 * @return Target {@link Model} {@link Class}.
	 */
	Class<?> getTargetModelClass();

	/**
	 * Indicates whether can create the {@link ConnectionModel}.
	 * 
	 * @return <code>true</code> if able to create the {@link ConnectionModel}.
	 */
	boolean canCreateConnection();

}