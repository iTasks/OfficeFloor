/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.frame.spi.administration;

import java.util.List;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Context in which the {@link Duty} executes.
 * 
 * @author Daniel Sagenschneider
 */
public interface DutyContext<I extends Object, F extends Enum<F>> {

	/**
	 * Obtains the particular extension interfaces.
	 * 
	 * @return Extension interfaces for the {@link ManagedObject} instances to
	 *         be administered.
	 */
	List<I> getExtensionInterfaces();

	/**
	 * Instigates a {@link Flow} to be run in parallel to the {@link Task} being
	 * administered.
	 * 
	 * @param key
	 *            Key identifying the {@link Flow} to instigate.
	 * @param parameter
	 *            Parameter for the first {@link Task} of the {@link Flow}.
	 */
	void doFlow(F key, Object parameter);

	/**
	 * <p>
	 * Similar to {@link #doFlow(F, Object)} except that allows dynamic
	 * instigation of flows.
	 * <p>
	 * In other words, an {@link Enum} is not required to define the possible
	 * flows available.
	 * 
	 * @param flowIndex
	 *            Index identifying the {@link Flow} to instigate.
	 * @param parameter
	 *            Parameter for the first {@link Task} of the {@link Flow}.
	 */
	void doFlow(int flowIndex, Object parameter);

}