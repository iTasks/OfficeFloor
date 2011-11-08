/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.frame.impl.execute.governance;

import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.internal.structure.ActiveGovernance;
import net.officefloor.frame.internal.structure.ActiveGovernanceControl;
import net.officefloor.frame.internal.structure.ActiveGovernanceManager;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceControl;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectContainer;

/**
 * {@link ActiveGovernance} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ActiveGovernanceImpl<I, F extends Enum<F>> implements
		ActiveGovernanceManager, ActiveGovernance, ActiveGovernanceControl<F> {

	/**
	 * {@link GovernanceContainer}.
	 */
	private final GovernanceContainer<I> governanceContainer;

	/**
	 * {@link GovernanceMetaData}.
	 */
	private final GovernanceMetaData<I, F> metaData;

	/**
	 * {@link GovernanceControl}.
	 */
	private final GovernanceControl<I, F> governanceControl;

	/**
	 * Extension interface.
	 */
	private final I extensionInterface;

	/**
	 * {@link ManagedObjectContainer}.
	 */
	private final ManagedObjectContainer managedObject;

	/**
	 * Registered index within the {@link ManagedObjectContainer}.
	 */
	private final int registeredIndex;

	/**
	 * Initiate.
	 * 
	 * @param governanceContainer
	 *            {@link GovernanceContainer}.
	 * @param metaData
	 *            {@link GovernanceMetaData}.
	 * @param governanceControl
	 *            {@link GovernanceControl}.
	 * @param extensionInterface
	 *            Extension interface.
	 * @param managedObject
	 *            {@link ManagedObjectContainer}.
	 * @param registeredIndex
	 *            Registered index within the {@link ManagedObjectContainer}.
	 */
	public ActiveGovernanceImpl(GovernanceContainer<I> governanceContainer,
			GovernanceMetaData<I, F> metaData,
			GovernanceControl<I, F> governanceControl, I extensionInterface,
			ManagedObjectContainer managedObject, int registeredIndex) {
		this.governanceContainer = governanceContainer;
		this.metaData = metaData;
		this.governanceControl = governanceControl;
		this.extensionInterface = extensionInterface;
		this.managedObject = managedObject;
		this.registeredIndex = registeredIndex;
	}

	/*
	 * ================== ActiveGovernanceManager ========================
	 */

	@Override
	public ActiveGovernance getActiveGovernance() {
		return this;
	}

	@Override
	public void unregisterManagedObject() {
		this.managedObject.unregisterManagedObjectFromGovernance(this);
	}

	/*
	 * ================== ActiveGovernance ========================
	 */

	@Override
	public int getManagedObjectRegisteredIndex() {
		return this.registeredIndex;
	}

	@Override
	public boolean isActive() {
		return this.governanceContainer.isActive();
	}

	@Override
	public FlowMetaData<?> getFlowMetaData() {
		return this.metaData.getGovernFlowMetaData();
	}

	/*
	 * ================== ActiveGovernanceControl =====================
	 */

	@Override
	public void governManagedObject(TaskContext<?, ?, F> taskContext)
			throws Throwable {
		this.governanceControl.governManagedObject(this.extensionInterface,
				taskContext);
	}

}