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

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.governance.Governance;

/**
 * {@link OfficeObject} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeObjectNode extends LinkObjectNode, OfficeObject {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Office Object";

	/**
	 * Initialises this {@link OfficeManagedObjectType}.
	 * 
	 * @param objectType
	 *            Object type.
	 */
	void initialise(String objectType);

	/**
	 * <p>
	 * Adds an {@link AdministrationNode} for this
	 * {@link OfficeManagedObjectType}.
	 * <p>
	 * This allows the {@link OfficeManagedObjectType} to report the extension
	 * types required to be supported by the {@link OfficeFloorManagedObject}
	 * for the {@link OfficeObject}.
	 * 
	 * @param administrator
	 *            {@link AdministrationNode}.
	 */
	void addAdministrator(AdministrationNode administrator);

	/**
	 * Obtains the {@link AdministrationNode} instances to provide pre-load
	 * {@link Administration} over {@link BoundManagedObjectNode} linked to this
	 * {@link OfficeObjectNode}.
	 * 
	 * @return {@link AdministrationNode} instances.
	 */
	AdministrationNode[] getPreLoadAdministrations();

	/**
	 * <p>
	 * Adds a {@link GovernanceNode} providing {@link Governance} for this
	 * {@link OfficeObject}.
	 * <p>
	 * This also allows the {@link OfficeManagedObjectType} to report the
	 * extension interfaces required to be supported by the
	 * {@link OfficeFloorManagedObject} for the {@link OfficeObject}.
	 * 
	 * @param governance
	 *            {@link GovernanceNode}.
	 */
	void addGovernance(GovernanceNode governance);

	/**
	 * Obtains the {@link GovernanceNode} instances to provide
	 * {@link Governance} over {@link BoundManagedObjectNode} linked to this
	 * {@link OfficeObjectNode}.
	 * 
	 * @return {@link GovernanceNode} instances.
	 */
	GovernanceNode[] getGovernances();

	/**
	 * Obtains the type of the {@link OfficeObject}.
	 * 
	 * @return Type of the {@link OfficeObject}.
	 */
	String getOfficeObjectType();

	/**
	 * Obtains the type qualifier for the {@link OfficeObject}.
	 * 
	 * @return Type qualifier for the {@link OfficeObject}.
	 */
	String getTypeQualifier();

	/**
	 * Loads the {@link OfficeManagedObjectType} for this
	 * {@link OfficeObjectNode}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link OfficeManagedObjectType} or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues}.
	 */
	OfficeManagedObjectType loadOfficeManagedObjectType(CompileContext compileContext);

}
