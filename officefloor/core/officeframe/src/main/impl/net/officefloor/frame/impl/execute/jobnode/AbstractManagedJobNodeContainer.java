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
package net.officefloor.frame.impl.execute.jobnode;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.execute.escalation.PropagateEscalationError;
import net.officefloor.frame.internal.structure.ActiveGovernance;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.EscalationLevel;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowCallbackJobNodeFactory;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceContainer;
import net.officefloor.frame.internal.structure.GovernanceDeactivationStrategy;
import net.officefloor.frame.internal.structure.JobMetaData;
import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ManagedJobNode;
import net.officefloor.frame.internal.structure.ManagedJobNodeContext;
import net.officefloor.frame.internal.structure.ManagedObjectIndex;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.WorkContainer;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.team.Job;

/**
 * Abstract implementation of the {@link Job} that provides the additional
 * {@link JobNode} functionality.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractManagedJobNodeContainer<W extends Work, N extends JobMetaData>
		implements ManagedJobNode, ManagedJobNodeContext {

	/**
	 * {@link Logger}.
	 */
	private static final Logger LOGGER = Logger.getLogger(OfficeFrame.class.getName());

	/**
	 * {@link Flow}.
	 */
	private final Flow flow;

	/**
	 * {@link WorkContainer}.
	 */
	private final WorkContainer<W> workContainer;

	/**
	 * {@link JobMetaData}.
	 */
	private final N nodeMetaData;

	/**
	 * State of this {@link Job}.
	 */
	private JobState jobState = JobState.LOAD_MANAGED_OBJECTS;

	/**
	 * {@link work} {@link ManagedObjectIndex} instances to the
	 * {@link ManagedObject} instances that must be loaded before the
	 * {@link Task} may be executed.
	 */
	private final ManagedObjectIndex[] requiredManagedObjects;

	/**
	 * <p>
	 * Array identifying which {@link Governance} instances are required to be
	 * active for this {@link Job}. The {@link Governance} is identified by the
	 * index into the array.For each {@link Governance}:
	 * <ol>
	 * <li><code>true</code> indicates the {@link Governance} is to be activated
	 * (if not already activated)</li>
	 * <li><code>false</code> indicates to deactivate the {@link Governance}
	 * should it be active. The strategy for deactivation is defined by the
	 * {@link GovernanceDeactivationStrategy}.</li>
	 * </ol>
	 * <p>
	 * Should this array be <code>null</code> no change is undertaken with the
	 * {@link Governance} for the {@link Job}.
	 */
	private final boolean[] requiredGovernance;

	/**
	 * {@link GovernanceDeactivationStrategy}.
	 */
	private final GovernanceDeactivationStrategy governanceDeactivationStrategy;

	/**
	 * <p>
	 * {@link JobNode} that is the escalation parent of this {@link JobNode}.
	 * <p>
	 * It is possible for multiple parallel {@link JobNode} instances to be
	 * added. This allows determining which parallel owner is the escalation
	 * parent.
	 */
	// TODO implement with deprecation of WorkContainer
	// private final JobNode escalationParent;

	/**
	 * Next {@link AbstractManagedJobNodeContainer} in the sequential listing.
	 */
	private AbstractManagedJobNodeContainer<?, ?> nextTaskNode = null;

	/**
	 * Parallel {@link AbstractManagedJobNodeContainer} that must be executed
	 * before this {@link AbstractManagedJobNodeContainer} may be executed.
	 */
	private AbstractManagedJobNodeContainer<?, ?> parallelNode = null;

	/**
	 * <p>
	 * Owner if this {@link AbstractManagedJobNodeContainer} is a parallel
	 * {@link AbstractManagedJobNodeContainer}.
	 * <p>
	 * This is the {@link AbstractManagedJobNodeContainer} that is executed once
	 * the sequence from this {@link AbstractManagedJobNodeContainer} is
	 * complete.
	 */
	private AbstractManagedJobNodeContainer<?, ?> parallelOwner;

	/**
	 * Index of the {@link Governance} to be configured.
	 */
	private int index_governance = 0;

	/**
	 * Parameter for the next {@link JobNode}.
	 */
	private Object nextJobParameter;

	/**
	 * Flag indicating if a sequential {@link JobNode} was invoked.
	 */
	private boolean isSequentialJobInvoked = false;

	/**
	 * Spawn {@link ThreadState} {@link JobNode}.
	 */
	private SpawnThreadStateJobNode spawnThreadStateJobNode = null;

	/**
	 * Initiate.
	 * 
	 * @param flow
	 *            {@link Flow} containing this {@link Job}.
	 * @param workContainer
	 *            {@link WorkContainer} of the {@link Work} for this
	 *            {@link Task}.
	 * @param nodeMetaData
	 *            {@link JobMetaData} for this node.
	 * @param parallelOwner
	 *            If this is invoked as or a parallel {@link Task} or from a
	 *            parallel {@link Task} this will be the invoker. If not
	 *            parallel then will be <code>null</code>.
	 * @param requiredManagedObjects
	 *            {@link Work} {@link ManagedObjectIndex} instances to the
	 *            {@link ManagedObject} instances that must be loaded before the
	 *            {@link Task} may be executed.
	 * @param requiredGovernance
	 *            Identifies the required activation state of the
	 *            {@link Governance} for this {@link Job}.
	 * @param governanceDeactivationStrategy
	 *            {@link GovernanceDeactivationStrategy} for
	 *            {@link ActiveGovernance}.
	 */
	public AbstractManagedJobNodeContainer(Flow flow, WorkContainer<W> workContainer, N nodeMetaData,
			AbstractManagedJobNodeContainer<?, ?> parallelOwner, ManagedObjectIndex[] requiredManagedObjects,
			boolean[] requiredGovernance, GovernanceDeactivationStrategy governanceDeactivationStrategy) {
		this.flow = flow;
		this.workContainer = workContainer;
		this.nodeMetaData = nodeMetaData;
		this.parallelOwner = parallelOwner;
		this.requiredManagedObjects = requiredManagedObjects;
		this.requiredGovernance = requiredGovernance;
		this.governanceDeactivationStrategy = governanceDeactivationStrategy;
	}

	/**
	 * Loads the {@link Job} name to the message.
	 * 
	 * @param message
	 *            Message to receive the {@link Job} name.
	 */
	protected abstract void loadJobName(StringBuilder message);

	/**
	 * Overridden by specific container to execute the {@link JobNode}.
	 * 
	 * @param context
	 *            {@link ManagedJobNodeContext}.
	 * @return Parameter for the next {@link JobNode}.
	 * @throws Throwable
	 *             If failure in executing the {@link JobNode}.
	 */
	protected abstract Object executeJobNode(ManagedJobNodeContext context) throws Throwable;

	/*
	 * ======================== JobNode =======================================
	 */

	@Override
	public TeamManagement getResponsibleTeam() {
		return this.nodeMetaData.getResponsibleTeam();
	}

	@Override
	public ThreadState getThreadState() {
		return this.flow.getThreadState();
	}

	@Override
	public final JobNode doJob() {

		// Obtain the thread and process state (as used throughout method)
		ThreadState threadState = this.flow.getThreadState();
		ProcessState processState = threadState.getProcessState();

		// Profile job being executed
		threadState.profile(this.nodeMetaData);

		// Escalation cause
		Throwable escalationCause = null;
		try {
			// Handle failure on thread
			// (possibly from waiting for a managed object)
			escalationCause = threadState.getFailure();
			if (escalationCause != null) {
				// Clear failure on the thread, as escalating
				threadState.setFailure(null);

				// Escalate the failure on the thread
				throw escalationCause;
			}

			// Determine if alter governance
			if (this.requiredGovernance != null) {
				// Alter governance for the job
				while (this.index_governance < this.requiredGovernance.length) {
					try {
						// Determine if this governance is required
						boolean isGovernanceRequired = this.requiredGovernance[this.index_governance];

						// Determine if governance in correct state
						if (isGovernanceRequired != threadState.isGovernanceActive(this.index_governance)) {

							// Obtain the governance
							GovernanceContainer<?, ?> governance = threadState
									.getGovernanceContainer(this.index_governance);

							// Incorrect state, so correct
							if (isGovernanceRequired) {
								// Activate the governance
								return governance.activateGovernance().then(this);

							} else {
								// De-activate the governance
								switch (this.governanceDeactivationStrategy) {
								case ENFORCE:
									return governance.enforceGovernance().then(this);

								case DISREGARD:
									return governance.disregardGovernance().then(this);

								default:
									// Unknown de-activation strategy
									throw new IllegalStateException("Unknown "
											+ GovernanceDeactivationStrategy.class.getSimpleName() + " "
											+ AbstractManagedJobNodeContainer.this.governanceDeactivationStrategy);
								}
							}
						}
					} finally {
						// Increment for next governance
						this.index_governance++;
					}
				}
			}

			// Load the managed objects
			JobNode loadJobNode = this.workContainer.loadManagedObjects(this.requiredManagedObjects, this);
			if (loadJobNode != null) {
				// Execute the job once managed objects loaded
				this.jobState = JobState.EXECUTE_JOB;
				return loadJobNode;
			}

			// Synchronise process state to this thread (if required)
			if (threadState != processState.getMainThreadState()) {
				return new SynchroniseProcessStateJobNode(threadState).then(this);
			}

			switch (this.jobState) {
			case EXECUTE_JOB:

				// Log execution of the Job
				if (LOGGER.isLoggable(Level.FINER)) {
					StringBuilder msg = new StringBuilder();
					msg.append("Executing job ");
					this.loadJobName(msg);
					msg.append(" (thread=");
					msg.append(threadState);
					msg.append(" process=");
					msg.append(processState);
					msg.append(", team=");
					msg.append(Thread.currentThread().getName());
					msg.append(")");
					LOGGER.log(Level.FINER, msg.toString());
				}

				// Execute the job
				this.nextJobParameter = this.executeJobNode(this);

				// Job executed, so now to activate the next job
				this.jobState = JobState.ACTIVATE_NEXT_JOB_NODE;

				// Spawn any threads
				if (this.spawnThreadStateJobNode != null) {
					JobNode spawn = this.spawnThreadStateJobNode;
					this.spawnThreadStateJobNode = null;
					return spawn;
				}

			case ACTIVATE_NEXT_JOB_NODE:

				// Load next job if no sequential job invoked
				if (!this.isSequentialJobInvoked) {
					// No sequential node, load next task of flow
					TaskMetaData<?, ?, ?> nextTaskMetaData = this.nodeMetaData.getNextTaskInFlow();
					if (nextTaskMetaData != null) {
						// Create next task
						AbstractManagedJobNodeContainer<?, ?> job = (AbstractManagedJobNodeContainer<?, ?>) this.flow
								.createManagedJobNode(nextTaskMetaData, this.parallelOwner, this.nextJobParameter,
										GovernanceDeactivationStrategy.ENFORCE);

						// Load for sequential execution
						this.loadSequentialJobNode(job);
					}

					// Sequential job now invoked
					this.isSequentialJobInvoked = true;
				}

				// Complete this job (flags state complete)
				JobNode completeJob = this.completeJobNode();
				if (completeJob != null) {
					return completeJob.then(this);
				}

				// Obtain next job to execute
				JobNode nextJob = this.getNextJobNodeToExecute();
				if (nextJob != null) {
					return nextJob;
				}

			case COMPLETED:
				// Already complete, thus return immediately
				return null;

			case FAILED:
				// Carry on to handle the failure
				break;

			default:
				throw new IllegalStateException("Should not be in state " + this.jobState);
			}
		} catch (PropagateEscalationError ex) {
			// Obtain the cause of the escalation
			escalationCause = ex.getCause();
			if (escalationCause == null) {
				// May have been thrown by application code
				escalationCause = ex;
			}

		} catch (Throwable ex) {
			// Flag for escalation
			escalationCause = ex;

			// Log execution of the Job
			if (LOGGER.isLoggable(Level.FINE)) {
				StringBuilder msg = new StringBuilder();
				msg.append("EXCEPTION from job ");
				this.loadJobName(msg);
				msg.append(" (thread=");
				msg.append(threadState);
				msg.append(" process=");
				msg.append(processState);
				msg.append(", team=");
				msg.append(Thread.currentThread().getName());
				msg.append(")");
				LOGGER.log(Level.FINE, msg.toString(), ex);
			}
		}

		// Job failure
		this.jobState = JobState.FAILED;
		try {

			/*
			 * FIXME escalation path takes account many sequential.
			 * 
			 * DETAILS: Adding another sequential flow transforms the previous
			 * sequential flow into a parallel flow. This allows the flows to be
			 * executed in order and maintain the sequential nature of
			 * invocation. This however may result in this flow becoming an
			 * escalation handler for the invoked sequential flow (which it
			 * should not). Some identifier is to be provided to know when
			 * actually invoked as parallel rather than sequential transformed
			 * to parallel.
			 * 
			 * MITIGATION: This is an edge case where two sequential flows are
			 * invoked and the first flow throws an Escalation that is handled
			 * by this Node. Require 'real world' example to model tests for
			 * this scenario. Note that in majority of cases that exception
			 * handling is undertaken by input ManagedObjectSource or at
			 * Office/OfficeFloor level.
			 */

			// Obtain the node to handle the escalation
			JobNode escalationNode = null;

			// Inform thread of escalation search
			threadState.escalationStart(this);
			try {
				// Escalation from this node, so nothing further
				JobNode clearJobNode = this.clearNodes();
				if (clearJobNode != null) {
					return clearJobNode.then(this);
				}

				// Search upwards for an escalation handler
				AbstractManagedJobNodeContainer<?, ?> node = this;
				AbstractManagedJobNodeContainer<?, ?> escalationOwnerNode = this.parallelOwner;
				do {
					EscalationFlow escalation = node.nodeMetaData.getEscalationProcedure()
							.getEscalation(escalationCause);
					if (escalation == null) {
						// Clear node as not handles escalation
						JobNode parentClearJobNode = node.clearNodes();
						if (parentClearJobNode != null) {
							return parentClearJobNode.then(this);
						}

					} else {
						// Create the node for the escalation
						escalationNode = this.createEscalationJobNode(escalation.getTaskMetaData(), escalationCause,
								escalationOwnerNode);
					}

					// Move to parallel owner for next try
					if (node != escalationOwnerNode) {
						// Direct parallel owner of job escalating
						node = escalationOwnerNode;
					} else {
						// Ancestor parallel owner
						node = node.parallelOwner;
						escalationOwnerNode = node;
					}

				} while ((escalationNode == null) && (escalationOwnerNode != null));

			} finally {
				// Inform thread escalation search over
				threadState.escalationComplete(this);
			}

			// Determine if require a global escalation
			if (escalationNode == null) {
				// No escalation, so use global escalation
				EscalationFlow globalEscalation = null;
				switch (threadState.getEscalationLevel()) {
				case FLOW:
					// Obtain the Office escalation
					globalEscalation = processState.getOfficeEscalationProcedure().getEscalation(escalationCause);
					if (globalEscalation != null) {
						threadState.setEscalationLevel(EscalationLevel.OFFICE);
						break;
					}

				case OFFICE:
					// Tried office, now at invocation
					globalEscalation = processState.getInvocationEscalation();
					if (globalEscalation != null) {
						threadState.setEscalationLevel(EscalationLevel.INVOCATION_HANDLER);
						break;
					}

				case INVOCATION_HANDLER:
					// Tried invocation, always at office floor
					threadState.setEscalationLevel(EscalationLevel.OFFICE_FLOOR);
					globalEscalation = processState.getOfficeFloorEscalation();
					if (globalEscalation != null) {
						break;
					}

				case OFFICE_FLOOR:
					// Should not be escalating at office floor.
					// Allow stderr failure to pick up issue.
					throw escalationCause;

				default:
					throw new IllegalStateException("Should not be in state " + threadState.getEscalationLevel());
				}

				// Create the global escalation
				escalationNode = this.createEscalationJobNode(globalEscalation.getTaskMetaData(), escalationCause,
						null);
			}

			// Activate escalation node
			return escalationNode;

		} catch (Throwable ex) {
			// Should not receive failure here.
			// If so likely something has corrupted - eg OOM.
			if (LOGGER.isLoggable(Level.SEVERE)) {
				LOGGER.log(Level.SEVERE, "FAILURE: please restart OfficeFloor as likely become corrupt", ex);
			}
		}

		// Now complete
		JobNode completeJobNode = this.completeJobNode();
		if (completeJobNode != null) {
			return completeJobNode.then(this);
		}

		// Nothing further
		return null;
	}

	/**
	 * Obtains the parallel {@link JobNode} to execute.
	 * 
	 * @return Parallel {@link JobNode} to execute.
	 */
	private JobNode getParallelJobNodeToExecute() {

		// Determine furthest parallel node
		AbstractManagedJobNodeContainer<?, ?> currentTask = this;
		AbstractManagedJobNodeContainer<?, ?> nextTask = null;
		while ((nextTask = currentTask.parallelNode) != null) {
			currentTask = nextTask;
		}

		// Determine if a parallel task
		if (currentTask == this) {
			// No parallel task
			return null;
		} else {
			// Return the furthest parallel task
			return currentTask;
		}
	}

	/**
	 * Obtains the next {@link JobNode} to execute.
	 * 
	 * @return Next {@link JobNode} to execute.
	 */
	private JobNode getNextJobNodeToExecute() {

		// Determine if have parallel node
		JobNode nextTaskContainer = this.getParallelJobNodeToExecute();
		if (nextTaskContainer != null) {
			// Parallel node
			return nextTaskContainer;
		}

		// Determine if have sequential node
		if (this.nextTaskNode != null) {
			// Sequential node
			return this.nextTaskNode;
		}

		// Determine if have parallel owner
		if (this.parallelOwner != null) {
			// Returning to owner, therefore unlink parallel node
			this.parallelOwner.parallelNode = null;

			// Parallel owner
			return this.parallelOwner;
		}

		// No further tasks
		return null;
	}

	/*
	 * ======================= ManagedJobNodeContext ==========================
	 */

	@Override
	public final void doFlow(FlowMetaData<?> flowMetaData, Object parameter, FlowCallback callback) {

		// Obtain the task meta-data for instigating the flow
		TaskMetaData<?, ?, ?> initTaskMetaData = flowMetaData.getInitialTaskMetaData();

		// Instigate the flow
		switch (flowMetaData.getInstigationStrategy()) {

		case SEQUENTIAL:
			// Flag sequential job invoked
			this.isSequentialJobInvoked = true;

			// Create the job node on the same flow as this job node
			AbstractManagedJobNodeContainer<?, ?> sequentialJobNode = (AbstractManagedJobNodeContainer<?, ?>) this.flow
					.createManagedJobNode(initTaskMetaData, this.parallelOwner, parameter,
							GovernanceDeactivationStrategy.ENFORCE);

			// Load the sequential node
			this.loadSequentialJobNode(sequentialJobNode);
			break;

		case PARALLEL:
			// Create a new flow for execution
			Flow parallelFlow = this.flow.getThreadState().createFlow();

			// Create the job node
			AbstractManagedJobNodeContainer<?, ?> parallelJobNode = (AbstractManagedJobNodeContainer<?, ?>) parallelFlow
					.createManagedJobNode(initTaskMetaData, this, parameter, GovernanceDeactivationStrategy.ENFORCE);

			// Load the parallel node
			this.loadParallelJobNode(parallelJobNode);
			break;

		case ASYNCHRONOUS:
			JobNode continueJobNode = (this.spawnThreadStateJobNode != null) ? this.spawnThreadStateJobNode : this;
			this.spawnThreadStateJobNode = new SpawnThreadStateJobNode(this.flow.getThreadState().getProcessState(),
					flowMetaData, parameter, new FlowCallbackJobNodeFactory() {
						@Override
						public JobNode createJobNode(Throwable exception) {
							// TODO implement Type1481920593530.createJobNode
							throw new UnsupportedOperationException("TODO implement Type1481920593530.createJobNode");

						}
					}, this.nodeMetaData.getJobNodeDelegator(), continueJobNode);
			break;

		default:
			// Unknown instigation strategy
			throw new IllegalStateException("Unknown instigation strategy");
		}
	}

	/**
	 * Loads a sequential {@link JobNode} relative to this {@link JobNode}
	 * within the tree of {@link JobNode} instances.
	 * 
	 * @param sequentialJobNode
	 *            {@link AbstractManagedJobNodeContainer} to load to tree.
	 */
	private final void loadSequentialJobNode(AbstractManagedJobNodeContainer<?, ?> sequentialJobNode) {

		// Obtain the next sequential node
		if (this.nextTaskNode != null) {
			// Move current sequential node to parallel node
			this.loadParallelJobNode(this.nextTaskNode);
		}

		// Set next sequential node
		this.nextTaskNode = sequentialJobNode;
	}

	/**
	 * Loads a parallel {@link JobNode} relative to this {@link JobNode} within
	 * the tree of {@link JobNode} instances.
	 * 
	 * @param parallelJobNode
	 *            {@link JobNode} to load to tree.
	 */
	private final void loadParallelJobNode(AbstractManagedJobNodeContainer<?, ?> parallelJobNode) {

		// Move possible next parallel node out
		if (this.parallelNode != null) {
			parallelJobNode.parallelNode = this.parallelNode;
			this.parallelNode.parallelOwner = parallelJobNode;
		}

		// Set next parallel node
		this.parallelNode = parallelJobNode;
		parallelJobNode.parallelOwner = this;
	}

	/**
	 * Creates an {@link EscalationFlow} {@link JobNode} from the input
	 * {@link TaskMetaData}.
	 * 
	 * @param taskMetaData
	 *            {@link TaskMetaData}.
	 * @param parameter
	 *            Parameter.
	 * @param parallelOwner
	 *            Parallel owner for the {@link EscalationFlow} {@link JobNode}.
	 * @return {@link JobNode}.
	 */
	private final JobNode createEscalationJobNode(TaskMetaData<?, ?, ?> taskMetaData, Object parameter,
			ManagedJobNode parallelOwner) {

		// Create a new flow for execution
		ThreadState threadState = this.flow.getThreadState();
		Flow parallelFlow = threadState.createFlow();

		// Create the job node
		JobNode escalationJobNode = parallelFlow.createManagedJobNode(taskMetaData, parallelOwner, parameter,
				GovernanceDeactivationStrategy.DISREGARD);

		// Return the escalation job node
		return escalationJobNode;
	}

	/**
	 * Clears this {@link JobNode}.
	 */
	private final JobNode clearNodes() {

		// Complete this job
		JobNode completeJobNode = this.completeJobNode();
		if (completeJobNode != null) {
			return completeJobNode.then(this);
		}

		// Clear all the parallel jobs from this node
		if (this.parallelNode != null) {
			JobNode parallelJobNode = this.parallelNode.clearNodes();
			if (parallelJobNode != null) {
				return parallelJobNode.then(this);
			}
			this.parallelNode = null;
		}

		// Clear all the sequential jobs from this node
		if (this.nextTaskNode != null) {
			JobNode sequentialJobNode = this.nextTaskNode.clearNodes();
			if (sequentialJobNode != null) {
				return sequentialJobNode.then(this);
			}
			this.nextTaskNode = null;
		}

		// Nodes cleared
		return null;
	}

	/**
	 * Completes this {@link JobNode}.
	 */
	private JobNode completeJobNode() {

		// Do nothing if already complete
		if (this.jobState == JobState.COMPLETED) {
			return null;
		}

		// Clean up work container
		JobNode unloadJob = this.workContainer.unloadWork();
		if (unloadJob != null) {
			return unloadJob.then(this);
		}

		// Clean up job node
		JobNode flowJob = this.flow.managedJobNodeComplete(this);
		if (flowJob != null) {
			return flowJob.then(this);
		}

		// Complete the job
		this.jobState = JobState.COMPLETED;
		return null;
	}

	/**
	 * State of this {@link Job}.
	 */
	private static enum JobState {

		/**
		 * Initial state requiring the {@link ManagedObject} instances to be
		 * loaded.
		 */
		LOAD_MANAGED_OBJECTS,

		/**
		 * Indicates the {@link Job} is to be executed.
		 */
		EXECUTE_JOB,

		/**
		 * Indicates to activate the next {@link ManagedJobNode}.
		 */
		ACTIVATE_NEXT_JOB_NODE,

		/**
		 * Failure in executing.
		 */
		FAILED,

		/**
		 * Completed.
		 */
		COMPLETED
	}

}