/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.compile.spi.office.OfficeManagedObjectFlow;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectFlow;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionManagedObjectFlow;

/**
 * {@link ManagedObjectFlow} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFlowNode extends LinkFlowNode, AugmentedManagedObjectFlow, SectionManagedObjectFlow,
		OfficeManagedObjectFlow, OfficeFloorManagedObjectFlow {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Managed Object Source Flow";

	/**
	 * Initialises the {@link ManagedObjectFlowNode}.
	 */
	void initialise();

}
