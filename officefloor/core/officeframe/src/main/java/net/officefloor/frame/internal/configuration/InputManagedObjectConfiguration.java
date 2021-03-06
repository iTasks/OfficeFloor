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

package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Configuration of a {@link ManagedObject} input into a {@link ProcessState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface InputManagedObjectConfiguration<O extends Enum<O>> {

	/**
	 * Obtains name the input {@link ManagedObject} is bound to within the
	 * {@link ProcessState}.
	 * 
	 * @return Name the input {@link ManagedObject} is bound to within the
	 *         {@link ProcessState}.
	 */
	String getBoundManagedObjectName();

	/**
	 * Obtains the listing of {@link ManagedObjectDependencyConfiguration}
	 * instances.
	 * 
	 * @return {@link ManagedObjectDependencyConfiguration} instances.
	 */
	ManagedObjectDependencyConfiguration<O>[] getDependencyConfiguration();

	/**
	 * Obtains the listing of {@link ManagedObjectGovernanceConfiguration}
	 * instances.
	 * 
	 * @return {@link ManagedObjectGovernanceConfiguration} instances.
	 */
	ManagedObjectGovernanceConfiguration[] getGovernanceConfiguration();

	/**
	 * Obtains the listing of the {@link Administration} to be done before the
	 * {@link ManagedObject} is loaded.
	 * 
	 * @return Listing of the {@link Administration} to be done before the
	 *         {@link ManagedObject} is loaded.
	 */
	AdministrationConfiguration<?, ?, ?>[] getPreLoadAdministration();

	/**
	 * Obtains the {@link ThreadLocalConfiguration}.
	 * 
	 * @return {@link ThreadLocalConfiguration} or <code>null</code> if not bound to
	 *         {@link Thread}.
	 */
	ThreadLocalConfiguration getThreadLocalConfiguration();

}
