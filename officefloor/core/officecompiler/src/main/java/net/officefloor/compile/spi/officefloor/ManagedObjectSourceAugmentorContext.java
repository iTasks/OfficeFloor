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

package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.issues.SourceIssues;
import net.officefloor.compile.managedobject.ManagedObjectExecutionStrategyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Context for the {@link ManagedObjectSourceAugmentor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceAugmentorContext extends SourceIssues {

	/**
	 * Obtains the name of the {@link ManagedObjectSource}.
	 * 
	 * @return Name of the {@link ManagedObjectSource}.
	 */
	String getManagedObjectSourceName();

	/**
	 * Obtains the {@link ManagedObjectType} of the {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectType} of the {@link ManagedObjectSource}.
	 */
	ManagedObjectType<?> getManagedObjectType();

	/**
	 * Obtains the {@link AugmentedManagedObjectFlow} for the
	 * {@link ManagedObjectFlowType}.
	 * 
	 * @param managedObjectSourceFlowName Name of the {@link ManagedObjectFlowType}.
	 * @return {@link AugmentedManagedObjectFlow}.
	 */
	AugmentedManagedObjectFlow getManagedObjectFlow(String managedObjectSourceFlowName);

	/**
	 * Obtains the {@link AugmentedManagedObjectTeam} for the
	 * {@link ManagedObjectTeamType}.
	 * 
	 * @param managedObjectSourceTeamName Name of the {@link ManagedObjectTeamType}.
	 * @return {@link AugmentedManagedObjectTeam}.
	 */
	AugmentedManagedObjectTeam getManagedObjectTeam(String managedObjectSourceTeamName);

	/**
	 * Obtains the {@link AugmentedManagedObjectExecutionStrategy} for the
	 * {@link ManagedObjectExecutionStrategyType}.
	 * 
	 * @param managedObjectSourceExecutionStrategyName Name of the
	 *                                                 {@link ManagedObjectExecutionStrategyType}.
	 * @return {@link AugmentedManagedObjectExecutionStrategy}.
	 */
	AugmentedManagedObjectExecutionStrategy getManagedObjectExecutionStrategy(
			String managedObjectSourceExecutionStrategyName);

	/**
	 * Links the {@link AugmentedManagedObjectFlow} to the
	 * {@link DeployedOfficeInput}.
	 * 
	 * @param flow        {@link AugmentedManagedObjectFlow}.
	 * @param officeInput {@link DeployedOfficeInput}.
	 */
	void link(AugmentedManagedObjectFlow flow, DeployedOfficeInput officeInput);

	/**
	 * Links the {@link AugmentedManagedObjectTeam} to the {@link OfficeFloorTeam}.
	 * 
	 * @param responsibility {@link AugmentedManagedObjectTeam}.
	 * @param team           {@link OfficeFloorTeam}.
	 */
	void link(AugmentedManagedObjectTeam responsibility, OfficeFloorTeam team);

	/**
	 * Links the {@link AugmentedManagedObjectExecutionStrategy} to the
	 * {@link OfficeFloorExecutionStrategy}.
	 * 
	 * @param requiredStrategy  {@link AugmentedManagedObjectExecutionStrategy}.
	 * @param executionStrategy {@link OfficeFloorExecutionStrategy}.
	 */
	void link(AugmentedManagedObjectExecutionStrategy requiredStrategy, OfficeFloorExecutionStrategy executionStrategy);

}
