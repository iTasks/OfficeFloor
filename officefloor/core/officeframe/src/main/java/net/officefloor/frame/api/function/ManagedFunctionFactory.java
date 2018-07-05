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
package net.officefloor.frame.api.function;

/**
 * Creates the {@link ManagedFunction} to be executed.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionFactory<O extends Enum<O>, F extends Enum<F>> {

	/**
	 * Creates the {@link ManagedFunction}.
	 * 
	 * @return {@link ManagedFunction}.
	 * @throws Throwable
	 *             If fails to create the {@link ManagedFunction}.
	 */
	ManagedFunction<O, F> createManagedFunction() throws Throwable;

}