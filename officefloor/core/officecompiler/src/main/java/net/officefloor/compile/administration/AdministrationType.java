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

package net.officefloor.compile.administration;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;

/**
 * <code>Type definition</code> of an {@link Administration}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationType<E, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * Obtains the {@link Class} that the {@link ManagedObject} must provide as
	 * an extension interface to be administered.
	 * 
	 * @return Extension interface for the {@link ManagedObject}.
	 */
	Class<E> getExtensionType();

	/**
	 * Obtains the {@link AdministrationFactory} to create the
	 * {@link Administration}.
	 * 
	 * @return {@link AdministrationFactory} to create the
	 *         {@link Administration}.
	 */
	AdministrationFactory<E, F, G> getAdministrationFactory();

	/**
	 * Obtains the {@link Enum} providing the keys for the {@link Flow}
	 * instances instigated by the {@link Administration}.
	 * 
	 * @return {@link Enum} providing instigated {@link Flow} keys or
	 *         <code>null</code> if {@link Indexed} or no instigated
	 *         {@link Flow} instances.
	 */
	Class<F> getFlowKeyClass();

	/**
	 * Obtains the {@link AdministrationFlowType} definitions for the possible
	 * {@link Flow} instances instigated by the {@link Administration}.
	 * 
	 * @return {@link AdministrationFlowType} definitions for the possible
	 *         {@link Flow} instances instigated by the {@link Administration}.
	 */
	AdministrationFlowType<F>[] getFlowTypes();

	/**
	 * Obtains the {@link AdministrationEscalationType} definitions for the
	 * possible {@link EscalationFlow} instances by the {@link Administration}.
	 * 
	 * @return {@link AdministrationEscalationType} definitions for the possible
	 *         {@link EscalationFlow} instances by the {@link Administration}.
	 */
	AdministrationEscalationType[] getEscalationTypes();

	/**
	 * Obtains the {@link Enum} providing the keys for the {@link Governance}
	 * instances used by this {@link Administration}.
	 * 
	 * @return {@link Enum} providing {@link Governance} keys or
	 *         <code>null</code> if {@link Indexed} or no {@link Governance}
	 *         used.s
	 */
	Class<G> getGovernanceKeyClass();

	/**
	 * Obtains the {@link AdministrationGovernanceType} instances for the
	 * {@link Governance} used by this {@link Administration}.
	 * 
	 * @return {@link AdministrationGovernanceType} instances for the
	 *         {@link Governance} used by this {@link Administration}.
	 */
	AdministrationGovernanceType<G>[] getGovernanceTypes();

}
