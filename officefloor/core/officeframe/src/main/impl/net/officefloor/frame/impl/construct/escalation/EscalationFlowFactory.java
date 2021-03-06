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

package net.officefloor.frame.impl.construct.escalation;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.escalation.EscalationFlowImpl;
import net.officefloor.frame.internal.configuration.EscalationConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;

/**
 * {@link EscalationFlowFactory} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class EscalationFlowFactory {

	/**
	 * {@link OfficeMetaData}.
	 */
	private final OfficeMetaData officeMetaData;

	/**
	 * Instantiate.
	 * 
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 */
	public EscalationFlowFactory(OfficeMetaData officeMetaData) {
		this.officeMetaData = officeMetaData;
	}

	/**
	 * Creates the {@link EscalationFlow} instances.
	 * 
	 * @param configurations
	 *            {@link EscalationConfiguration} instances.
	 * @param assetType
	 *            {@link AssetType}.
	 * @param assetName
	 *            Name of the {@link Asset}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 * @return {@link EscalationFlow} instances.
	 */
	public EscalationFlow[] createEscalationFlows(EscalationConfiguration[] configurations, AssetType assetType,
			String assetName, OfficeFloorIssues issues) {

		// Obtain the function locator
		ManagedFunctionLocator functionLocator = this.officeMetaData.getManagedFunctionLocator();

		// Create the escalation flows
		EscalationFlow[] escalations = new EscalationFlow[configurations.length];
		for (int i = 0; i < escalations.length; i++) {
			EscalationConfiguration escalationConfiguration = configurations[i];

			// Obtain the type of cause
			Class<? extends Throwable> typeOfCause = escalationConfiguration.getTypeOfCause();
			if (typeOfCause == null) {
				issues.addIssue(assetType, assetName, "No escalation type for escalation index " + i);
				return null; // no escalation type
			}

			// Obtain the escalation handler
			ManagedFunctionReference escalationReference = escalationConfiguration.getManagedFunctionReference();
			if (escalationReference == null) {
				issues.addIssue(assetType, assetName, "No function referenced for escalation index " + i);
				return null; // no escalation handler referenced
			}
			ManagedFunctionMetaData<?, ?> escalationFunctionMetaData = ConstructUtil.getFunctionMetaData(
					escalationReference, functionLocator, issues, assetType, assetName, "escalation index " + i);
			if (escalationFunctionMetaData == null) {
				return null; // no escalation handler
			}

			// Create and add the escalation
			escalations[i] = new EscalationFlowImpl(typeOfCause, escalationFunctionMetaData);
		}

		// TODO sort the escalation flows into specific to more generic

		// Return the escalation flows
		return escalations;
	}

}
