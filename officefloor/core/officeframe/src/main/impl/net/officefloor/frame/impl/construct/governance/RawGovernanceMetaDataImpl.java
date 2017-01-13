/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.frame.impl.construct.governance;

import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.construct.util.ConstructUtil;
import net.officefloor.frame.impl.execute.escalation.EscalationFlowImpl;
import net.officefloor.frame.impl.execute.escalation.EscalationProcedureImpl;
import net.officefloor.frame.impl.execute.governance.GovernanceMetaDataImpl;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.internal.configuration.GovernanceEscalationConfiguration;
import net.officefloor.frame.internal.configuration.GovernanceFlowConfiguration;
import net.officefloor.frame.internal.configuration.ManagedFunctionReference;
import net.officefloor.frame.internal.construct.ManagedFunctionLocator;
import net.officefloor.frame.internal.construct.RawGovernanceMetaData;
import net.officefloor.frame.internal.construct.RawGovernanceMetaDataFactory;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * Raw meta-data for a {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public class RawGovernanceMetaDataImpl<I, F extends Enum<F>>
		implements RawGovernanceMetaDataFactory, RawGovernanceMetaData<I, F> {

	/**
	 * Obtains the {@link RawGovernanceMetaDataFactory}.
	 * 
	 * @return {@link RawGovernanceMetaDataFactory}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static RawGovernanceMetaDataFactory getFactory() {
		return new RawGovernanceMetaDataImpl(null, -1, null, null, null);
	}

	/**
	 * Name of the {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * Index of this {@link RawGovernanceMetaData} within the
	 * {@link ProcessState}.
	 */
	private final int governanceIndex;

	/**
	 * Extension interface type.
	 */
	private final Class<I> extensionInterfaceType;

	/**
	 * {@link GovernanceConfiguration}.
	 */
	private final GovernanceConfiguration<I, F> governanceConfiguration;

	/**
	 * {@link GovernanceMetaData}.
	 */
	private final GovernanceMetaDataImpl<I, F> governanceMetaData;

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceIndex
	 *            Index of this {@link RawGovernanceMetaData} within the
	 *            {@link ProcessState}.
	 * @param extensionInterfaceType
	 *            Extension interface type.
	 * @param governanceConfiguration
	 *            {@link GovernanceConfiguration}.
	 * @param governanceMetaData
	 *            {@link GovernanceMetaDataImpl}.
	 */
	public RawGovernanceMetaDataImpl(String governanceName, int governanceIndex, Class<I> extensionInterfaceType,
			GovernanceConfiguration<I, F> governanceConfiguration, GovernanceMetaDataImpl<I, F> governanceMetaData) {
		this.governanceName = governanceName;
		this.governanceIndex = governanceIndex;
		this.extensionInterfaceType = extensionInterfaceType;
		this.governanceConfiguration = governanceConfiguration;
		this.governanceMetaData = governanceMetaData;
	}

	/*
	 * ==================== RawGovernanceMetaDataFactory ==================
	 */

	@Override
	public <i, f extends Enum<f>> RawGovernanceMetaData<i, f> createRawGovernanceMetaData(
			GovernanceConfiguration<i, f> configuration, int governanceIndex, SourceContext sourceContext,
			Map<String, TeamManagement> officeTeams, String officeName, OfficeFloorIssues issues,
			FunctionLoop functionLoop) {

		// Obtain the governance name
		String governanceName = configuration.getGovernanceName();
		if (ConstructUtil.isBlank(governanceName)) {
			issues.addIssue(AssetType.OFFICE, officeName, "Governance added without a name");
			return null; // can not carry on
		}

		// Obtain the governance factory
		GovernanceFactory<? super i, f> governanceFactory = configuration.getGovernanceFactory();
		if (governanceFactory == null) {
			issues.addIssue(AssetType.GOVERNANCE, governanceName,
					"No " + GovernanceFactory.class.getSimpleName() + " provided");
			return null; // can not carry on
		}

		// Obtain the extension interface type
		Class<i> extensionInterfaceType = configuration.getExtensionInterface();
		if (extensionInterfaceType == null) {
			issues.addIssue(AssetType.GOVERNANCE, governanceName, "No extension interface type provided");
			return null; // can not carry on
		}

		// Obtain the team name for the governance
		String teamName = configuration.getTeamName();
		if (ConstructUtil.isBlank(teamName)) {
			issues.addIssue(AssetType.GOVERNANCE, governanceName, "Must specify " + Team.class.getSimpleName()
					+ " responsible for " + Governance.class.getSimpleName() + " activities");
			return null; // can not carry on
		}

		// Obtain the team
		TeamManagement responsibleTeam = officeTeams.get(teamName);
		if (responsibleTeam == null) {
			issues.addIssue(AssetType.GOVERNANCE, governanceName,
					"Can not find " + Team.class.getSimpleName() + " by name '" + teamName + "'");
			return null; // can not carry on
		}

		// Create the Governance Meta-Data
		GovernanceMetaDataImpl<i, f> governanceMetaData = new GovernanceMetaDataImpl<i, f>(governanceName,
				governanceFactory, responsibleTeam, functionLoop);

		// Create the raw Governance meta-data
		RawGovernanceMetaData<i, f> rawGovernanceMetaData = new RawGovernanceMetaDataImpl<i, f>(governanceName,
				governanceIndex, extensionInterfaceType, configuration, governanceMetaData);

		// Return the raw governance meta-data
		return rawGovernanceMetaData;
	}

	/*
	 * =================== RawGovernanceMetaData ==================
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

	@Override
	public Class<I> getExtensionInterfaceType() {
		return this.extensionInterfaceType;
	}

	@Override
	public int getGovernanceIndex() {
		return this.governanceIndex;
	}

	@Override
	public GovernanceMetaData<I, F> getGovernanceMetaData() {
		return this.governanceMetaData;
	}

	@Override
	public void linkOfficeMetaData(ManagedFunctionLocator functionLocator, OfficeFloorIssues issues) {

		// Obtain the listing of flow meta-data
		GovernanceFlowConfiguration<F>[] flowConfigurations = this.governanceConfiguration.getFlowConfiguration();
		FlowMetaData[] flowMetaDatas = new FlowMetaData[flowConfigurations.length];
		for (int i = 0; i < flowMetaDatas.length; i++) {
			GovernanceFlowConfiguration<F> flowConfiguration = flowConfigurations[i];

			// Ensure have flow configuration
			if (flowConfiguration == null) {
				continue;
			}

			// Obtain the function reference
			ManagedFunctionReference functionReference = flowConfiguration.getInitialFunction();
			if (functionReference == null) {
				issues.addIssue(AssetType.GOVERNANCE, this.governanceName,
						"No function referenced for flow index " + i);
				continue; // no reference task for flow
			}

			// Obtain the function meta-data
			ManagedFunctionMetaData<?, ?> functionMetaData = ConstructUtil.getFunctionMetaData(functionReference,
					functionLocator, issues, AssetType.GOVERNANCE, this.governanceName, "flow index " + i);
			if (functionMetaData == null) {
				continue; // no initial function for flow
			}

			// Obtain whether to spawn thread state
			boolean isSpawnThreadState = flowConfiguration.isSpawnThreadState();

			// Create and add the flow meta-data
			flowMetaDatas[i] = ConstructUtil.newFlowMetaData(functionMetaData, isSpawnThreadState);
		}

		// Create the escalation procedure
		GovernanceEscalationConfiguration[] escalationConfigurations = this.governanceConfiguration.getEscalations();
		EscalationFlow[] escalations = new EscalationFlow[escalationConfigurations.length];
		for (int i = 0; i < escalations.length; i++) {
			GovernanceEscalationConfiguration escalationConfiguration = escalationConfigurations[i];

			// Obtain the type of cause
			Class<? extends Throwable> typeOfCause = escalationConfiguration.getTypeOfCause();
			if (typeOfCause == null) {
				issues.addIssue(AssetType.GOVERNANCE, this.getGovernanceName(),
						"No escalation type for escalation index " + i);
				continue; // no escalation type
			}

			// Obtain the escalation handler
			ManagedFunctionReference escalationReference = escalationConfiguration.getTaskNodeReference();
			if (escalationReference == null) {
				issues.addIssue(AssetType.GOVERNANCE, this.getGovernanceName(),
						"No task referenced for escalation index " + i);
				continue; // no escalation handler referenced
			}
			ManagedFunctionMetaData<?, ?> escalationFunctionMetaData = ConstructUtil.getFunctionMetaData(
					escalationReference, functionLocator, issues, AssetType.GOVERNANCE, this.getGovernanceName(),
					"escalation index " + i);
			if (escalationFunctionMetaData == null) {
				continue; // no escalation handler
			}

			// Create and add the escalation
			escalations[i] = new EscalationFlowImpl(typeOfCause, escalationFunctionMetaData);
		}
		EscalationProcedure escalationProcedure = new EscalationProcedureImpl(escalations);

		// Load the remaining state
		this.governanceMetaData.loadRemainingState(flowMetaDatas, escalationProcedure);
	}

}