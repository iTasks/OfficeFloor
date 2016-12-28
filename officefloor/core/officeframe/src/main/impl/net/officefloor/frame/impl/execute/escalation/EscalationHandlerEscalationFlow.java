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
package net.officefloor.frame.impl.execute.escalation;

import net.officefloor.frame.api.build.ManagedFunctionFactory;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionLogicImpl;
import net.officefloor.frame.impl.execute.managedfunction.ManagedFunctionMetaDataImpl;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationProcedure;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.ManagedFunctionDutyAssociation;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * {@link EscalationFlow} for an {@link EscalationHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public class EscalationHandlerEscalationFlow implements EscalationFlow {

	/**
	 * No required {@link ManagedObject} instances for
	 * {@link EscalationHandlerManagedFunction}.
	 */
	private static final ManagedObjectIndex[] NO_MANAGED_OBJECTS = new ManagedObjectIndex[0];

	/**
	 * No {@link ManagedObject} instances bound to
	 * {@link EscalationHandlerManagedFunction}.
	 */
	private static final ManagedObjectMetaData<?>[] NO_MANAGED_OBJECT_META_DATA = new ManagedObjectMetaData[0];

	/**
	 * No required {@link Governance} for
	 * {@link EscalationHandlerManagedFunction}.
	 */
	private static final boolean[] NO_GOVERNANCE = null;

	/**
	 * No {@link Administrator} instances bound to
	 * {@link EscalationHandlerManagedFunction}.
	 */
	private static final AdministratorMetaData<?, ?>[] NO_ADMINISTRATOR_META_DATA = new AdministratorMetaData[0];

	/**
	 * NO {@link ManagedFunctionDutyAssociation} instances for
	 * {@link EscalationHandlerManagedFunction}.
	 */
	private static final ManagedFunctionDutyAssociation<?>[] NO_FUNCTION_DUTY_ASSOCIATIONS = new ManagedFunctionDutyAssociation[0];

	/**
	 * No {@link FlowMetaData} instances for
	 * {@link EscalationHandlerManagedFunction}.
	 */
	private static final FlowMetaData[] NO_FLOW_META_DATA = new FlowMetaData[0];

	/**
	 * No {@link Escalation} handling from
	 * {@link EscalationHandlerManagedFunction}.
	 */
	private static final EscalationProcedure FURTHER_ESCALATION_PROCEDURE = new EscalationProcedureImpl();

	/**
	 * {@link ManagedObjectIndex} instances for dependencies.
	 */
	private static final ManagedObjectIndex[] MANGED_OBJECT_DEPENDENCIES = new ManagedObjectIndex[1];

	/**
	 * Initiate static state.
	 */
	static {
		// Specify dependency on parameter for escalation to handle
		MANGED_OBJECT_DEPENDENCIES[EscalationKey.EXCEPTION
				.ordinal()] = ManagedFunctionLogicImpl.PARAMETER_MANAGED_OBJECT_INDEX;
	}

	/**
	 * {@link ManagedFunctionMetaData} for the {@link EscalationHandler}
	 * {@link ManagedFunction}.
	 */
	private final ManagedFunctionMetaData<EscalationKey, None> functionMetaData;

	/**
	 * Initiate.
	 * 
	 * @param escalationHandler
	 *            {@link EscalationHandler}.
	 * @param functionLoop
	 *            {@link FunctionLoop}.
	 */
	public EscalationHandlerEscalationFlow(EscalationHandler escalationHandler, FunctionLoop functionLoop) {

		// Create the escalation function meta-data
		TeamManagement anyTeam = null;
		EscalationHandlerManagedFunctionFactory functionFactory = new EscalationHandlerManagedFunctionFactory(
				escalationHandler);
		ManagedFunctionMetaDataImpl<EscalationKey, None> functionMetaData = new ManagedFunctionMetaDataImpl<>(
				EscalationHandler.class.getSimpleName(), functionFactory, null, Throwable.class, anyTeam,
				MANGED_OBJECT_DEPENDENCIES, NO_MANAGED_OBJECT_META_DATA, NO_MANAGED_OBJECTS, NO_GOVERNANCE,
				NO_ADMINISTRATOR_META_DATA, NO_FUNCTION_DUTY_ASSOCIATIONS, NO_FUNCTION_DUTY_ASSOCIATIONS, functionLoop);
		functionMetaData.loadRemainingState(NO_FLOW_META_DATA, null, FURTHER_ESCALATION_PROCEDURE);
		this.functionMetaData = functionMetaData;
	}

	/*
	 * ==================== EscalationFlow =====================================
	 */

	@Override
	public Class<? extends Throwable> getTypeOfCause() {
		return Throwable.class;
	}

	@Override
	public ManagedFunctionMetaData<?, ?> getManagedFunctionMetaData() {
		return this.functionMetaData;
	}

	/**
	 * Key identifying the {@link Exception} for the {@link EscalationFlow}.
	 */
	private enum EscalationKey {
		EXCEPTION
	}

	/**
	 * {@link ManagedFunctionFactory} to execute the {@link EscalationHandler}.
	 */
	private static class EscalationHandlerManagedFunctionFactory
			implements ManagedFunctionFactory<EscalationKey, None>, ManagedFunction<EscalationKey, None> {

		/**
		 * {@link EscalationHandler}.
		 */
		private final EscalationHandler escalationHandler;

		/**
		 * Initiate.
		 * 
		 * @param escalationHandler
		 *            {@link EscalationHandler}.
		 */
		public EscalationHandlerManagedFunctionFactory(EscalationHandler escalationHandler) {
			this.escalationHandler = escalationHandler;
		}

		/*
		 * ================== ManagedFunctionFactory ==================
		 */

		@Override
		public ManagedFunction<EscalationKey, None> createManagedFunction() {
			return this;
		}

		/*
		 * ================== ManagedFunction ==================
		 */

		@Override
		public Object execute(ManagedFunctionContext<EscalationKey, None> context) throws Throwable {

			// Obtain the exception
			Throwable exception = (Throwable) context.getObject(EscalationKey.EXCEPTION);

			// Handle the exception
			this.escalationHandler.handleEscalation(exception);

			// Nothing to return
			return null;
		}
	}

}