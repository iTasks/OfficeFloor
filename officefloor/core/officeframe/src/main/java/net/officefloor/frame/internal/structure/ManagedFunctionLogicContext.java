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

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Context for the execution of a {@link ManagedFunction}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionLogicContext {

	/**
	 * Specifies the next {@link FunctionLogic} to be executed before the next
	 * {@link ManagedFunctionLogic}.
	 * 
	 * @param function Next {@link FunctionLogic}.
	 */
	void next(FunctionLogic function);

	/**
	 * Obtains the {@link Object} from a {@link ManagedObject}.
	 * 
	 * @param index {@link ManagedObjectIndex} identifying the
	 *              {@link ManagedObject}.
	 * @return Object from the {@link ManagedObject}.
	 */
	Object getObject(ManagedObjectIndex index);

	/**
	 * Invokes a {@link Flow}.
	 * 
	 * @param flowMetaData {@link FlowMetaData} for the {@link Flow}.
	 * @param parameter    Parameter for the initial {@link ManagedFunction} of the
	 *                     {@link Flow}.
	 * @param callback     Optional {@link FlowCallback}. May be <code>null</code>.
	 */
	void doFlow(FlowMetaData flowMetaData, Object parameter, FlowCallback callback);

	/**
	 * Creates an {@link AsynchronousFlow}.
	 * 
	 * @return {@link AsynchronousFlow}.
	 */
	AsynchronousFlow createAsynchronousFlow();

	/**
	 * Allows to asynchronously overwrite the next {@link ManagedFunction} argument.
	 * 
	 * @param argument Argument for the next {@link ManagedFunction}.
	 */
	void setNextFunctionArgument(Object argument);

}
