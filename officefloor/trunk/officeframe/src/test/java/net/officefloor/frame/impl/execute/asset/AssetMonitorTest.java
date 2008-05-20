/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.impl.execute.asset;

import net.officefloor.frame.impl.execute.AbstractLinkedList;
import net.officefloor.frame.impl.execute.AssetMonitorImpl;
import net.officefloor.frame.impl.execute.JobActivateSetImpl;
import net.officefloor.frame.impl.execute.AssetNotifySetImplAccess;
import net.officefloor.frame.internal.structure.Asset;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.LinkedList;
import net.officefloor.frame.internal.structure.AssetMonitor;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link AssetMonitor}.
 * 
 * @author Daniel
 */
public class AssetMonitorTest extends OfficeFrameTestCase {

	/**
	 * {@link AssetMonitor} being tested.
	 */
	protected AssetMonitor assetMonitor;

	/**
	 * Lock for the {@link AssetMonitor}.
	 */
	protected final Object lock = new Object();

	/**
	 * {@link LinkedList} of the {@link AssetMonitor} instances.
	 */
	protected final LinkedList<AssetMonitor, Object> monitors = new AbstractLinkedList<AssetMonitor, Object>() {
		@Override
		public void lastLinkedListEntryRemoved(Object removeParameter) {
			// No action
		}
	};

	/**
	 * Mock {@link Asset}.
	 */
	protected Asset asset;

	/**
	 * Mock {@link AssetManager}.
	 */
	protected AssetManager assetManager;

	/**
	 * Setup.
	 */
	protected void setUp() throws Exception {
		// Create the Mock objects for testing
		this.asset = this.createMock(Asset.class);
		this.assetManager = this.createMock(AssetManager.class);

		// Create the Task Monitor
		this.assetMonitor = new AssetMonitorImpl(this.asset, this.lock,
				this.assetManager, this.monitors);
	}

	/**
	 * Ensure correct items are returned.
	 */
	public void testGetters() {
		assertEquals("Incorrect lock object", this.lock, this.assetMonitor
				.getAssetLock());
		assertEquals("Incorrect assset", this.asset, this.assetMonitor
				.getAsset());
	}

	/**
	 * Ensure notify no {@link Job} instances.
	 */
	public void testNotifyNoTasks() {
		// Notify tasks (should do nothing)
		this.replayMockObjects();
		this.doNotifyTasks();
		this.verifyMockObjects();
	}

	/**
	 * Ensure notifies the {@link Job} instances.
	 */
	public void testNotifyTasks() {
		// Create the Task Containers
		MockTaskContainer taskOne = new MockTaskContainer(this);
		MockTaskContainer taskTwo = new MockTaskContainer(this);

		// Record mock objects
		this.recordAssetManagerRegistration();
		this.recordTaskActivation(taskOne);
		this.recordTaskActivation(taskTwo);

		// Replay
		this.replayMockObjects();

		// Tasks to wait on monitor
		this.doWait(taskOne, true);
		this.doWait(taskTwo, true);
		assertFalse("Task one should not be activated", taskOne.isActivated());
		assertFalse("Task two should not be activated", taskTwo.isActivated());

		// Notify the tasks
		this.doNotifyTasks();
		assertTrue("Task one should be activated", taskOne.isActivated());
		assertTrue("Task two should be activated", taskTwo.isActivated());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to reuse the {@link AssetMonitor}.
	 */
	public void testNotifyTaskAgain() {
		// Create the Task Containers
		MockTaskContainer taskOne = new MockTaskContainer(this);
		MockTaskContainer taskTwo = new MockTaskContainer(this);

		// Record waiting on tasks twice (for each wait/notify)
		this.recordAssetManagerRegistration();
		this.recordTaskActivation(taskOne);
		this.recordAssetManagerRegistration();
		this.recordTaskActivation(taskTwo);

		// Replay
		this.replayMockObjects();

		// Wait and notify on first task
		this.doWait(taskOne, true);
		this.doNotifyTasks();
		assertTrue("Task one should be activated", taskOne.isActivated());

		// Wait and notify on second task
		this.doWait(taskTwo, true);
		this.doNotifyTasks();
		assertTrue("Task two should be activated", taskOne.isActivated());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure notifies the {@link Job} instances permanently.
	 */
	public void testNotifyPermantly() {
		// Create the Task Containers
		MockTaskContainer taskOne = new MockTaskContainer(this);
		MockTaskContainer taskTwo = new MockTaskContainer(this);

		// Record waiting on tasks only once
		this.recordAssetManagerRegistration();
		this.recordTaskActivation(taskOne);

		// Replay
		this.replayMockObjects();

		// Wait and permanently notify task one
		this.doWait(taskOne, true);
		assertFalse("Task one should not be activated", taskOne.isActivated());
		this.doNotifyPermanently();
		assertTrue("Task one should be activated", taskOne.isActivated());

		// Can no longer wait on monitor (and not register with Asset Manager)
		this.doWait(taskTwo, false);
		assertFalse("Task two should not be activated", taskTwo.isActivated());
		this.doNotifyTasks();
		assertFalse("Task two should not be activated", taskTwo.isActivated());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure fails the {@link Job} instances.
	 */
	public void testFailTasks() {

		// Create the failure
		Throwable failure = new Exception();

		// Create the Task Containers
		MockTaskContainer taskOne = new MockTaskContainer(this);
		MockTaskContainer taskTwo = new MockTaskContainer(this);

		// Record waiting on tasks only once
		this.recordAssetManagerRegistration();
		this.recordTaskActivation(taskOne, failure);
		this.recordTaskActivation(taskTwo, failure);

		// Replay
		this.replayMockObjects();

		// Wait on task containers
		this.doWait(taskOne, true);
		this.doWait(taskTwo, true);
		assertFalse("Task one should not be activated", taskOne.isActivated());
		assertFalse("Task two should not be activated", taskTwo.isActivated());

		// Notify the tasks of failure
		this.doFailTasks(failure);
		assertTrue("Task one should be activated", taskOne.isActivated());
		assertTrue("Task two should be activated", taskTwo.isActivated());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure fails the {@link Job} instances permanently.
	 */
	public void testFailPermanently() {

		// Create the failure
		Throwable failure = new Exception();

		// Create the Task Containers
		MockTaskContainer taskOne = new MockTaskContainer(this);
		MockTaskContainer taskTwo = new MockTaskContainer(this);

		// Record waiting on tasks only once
		this.recordAssetManagerRegistration();
		this.recordTaskActivation(taskOne, failure);

		// Replay
		this.replayMockObjects();

		// Wait and permanently fail task one
		this.doWait(taskOne, true);
		assertFalse("Task one should not be activated", taskOne.isActivated());
		this.doFailPermanently(failure);
		assertTrue("Task one should be activated", taskOne.isActivated());

		// Can no longer wait on monitor (and not register with Asset Manager)
		this.doWait(taskTwo, false);
		assertFalse("Task two should not be activated", taskTwo.isActivated());
		this.doNotifyTasks();
		assertFalse("Task two should not be activated", taskTwo.isActivated());

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Records {@link AssetManager} registration.
	 */
	private void recordAssetManagerRegistration() {
		this.assetManager.registerAssetMonitor(this.assetMonitor);
		this.assetManager.unregisterAssetMonitor(this.assetMonitor);
	}

	/**
	 * Records {@link Job} activation.
	 * 
	 * @param task
	 *            {@link Job}.
	 */
	private void recordTaskActivation(Job task) {
		task.getThreadState().getThreadLock();
		this.control(task.getThreadState()).setReturnValue(new Object());
	}

	/**
	 * Records {@link Job} activation.
	 * 
	 * @param task
	 *            {@link Job}.
	 * @param cause
	 *            {@link Throwable}.
	 */
	private void recordTaskActivation(Job task, Throwable cause) {
		this.recordTaskActivation(task);
		task.getThreadState().setFailure(cause);
	}

	/**
	 * Has the {@link Job} wait on the {@link AssetMonitor}.
	 * 
	 * @param taskContainer
	 *            {@link Job}.
	 * @param isExpectedWaitReturn
	 *            Expected return from {@link AssetMonitor#wait(Job)}.
	 */
	private void doWait(Job taskContainer,
			boolean isExpectedWaitReturn) {
		JobActivateSetImpl notifySet = new JobActivateSetImpl();
		synchronized (this.assetMonitor.getAssetLock()) {
			assertEquals("Incorrect waiting", isExpectedWaitReturn,
					this.assetMonitor.wait(taskContainer, notifySet));
		}
		if (isExpectedWaitReturn) {
			// Waiting so should not be added
			assertNull("Task should not be added for notifying",
					AssetNotifySetImplAccess.tasks(notifySet).getHead());
		} else {
			// Ensure added as task to notify
			assertNotNull("Task should be added for notifying",
					AssetNotifySetImplAccess.tasks(notifySet).getHead());
		}
	}

	/**
	 * Does the notifying of the {@link Job} instances within the
	 * {@link AssetMonitor}.
	 */
	private void doNotifyTasks() {
		JobActivateSetImpl notifySet = new JobActivateSetImpl();
		synchronized (this.assetMonitor.getAssetLock()) {
			this.assetMonitor.notifyTasks(notifySet);
		}
		notifySet.activateJobs();
	}

	/**
	 * Does the notifying of the {@link Job} instances within the
	 * {@link AssetMonitor} permanently.
	 */
	private void doNotifyPermanently() {
		JobActivateSetImpl notifySet = new JobActivateSetImpl();
		synchronized (this.assetMonitor.getAssetLock()) {
			this.assetMonitor.notifyPermanently(notifySet);
		}
		notifySet.activateJobs();
	}

	/**
	 * Does the failure notifying of the {@link Job} instances within
	 * the {@link AssetMonitor}.
	 * 
	 * @param failure
	 *            {@link Throwable} to fail {@link Job} instances
	 *            with.
	 */
	private void doFailTasks(Throwable failure) {
		JobActivateSetImpl notifySet = new JobActivateSetImpl();
		synchronized (this.assetMonitor.getAssetLock()) {
			this.assetMonitor.failTasks(notifySet, failure);
		}
		notifySet.activateJobs();
	}

	/**
	 * Does the failure notifying of the {@link Job} instances within
	 * the {@link AssetMonitor} permanently.
	 * 
	 * @param failure
	 *            {@link Throwable} to fail {@link Job} instances
	 *            with.
	 */
	private void doFailPermanently(Throwable failure) {
		JobActivateSetImpl notifySet = new JobActivateSetImpl();
		synchronized (this.assetMonitor.getAssetLock()) {
			this.assetMonitor.failPermanently(notifySet, failure);
		}
		notifySet.activateJobs();
	}

}
