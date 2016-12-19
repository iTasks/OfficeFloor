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

import net.officefloor.frame.internal.structure.JobNode;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link JobNode} to fail the {@link ThreadState}.
 *
 * @author Daniel Sagenschneider
 */
public class FailThreadStateJobNode implements JobNode {

	/**
	 * Failure for the {@link ThreadState}.
	 */
	private final Throwable failure;

	/**
	 * {@link ThreadState} to fail.
	 */
	private final ThreadState threadState;

	/**
	 * Instantiate.
	 * 
	 * @param failure
	 *            Failure for the {@link ThreadState}.
	 * @param threadState
	 *            {@link ThreadState} to fail.
	 */
	public FailThreadStateJobNode(Throwable failure, ThreadState threadState) {
		this.failure = failure;
		this.threadState = threadState;
	}

	/*
	 * ======================== JobNode ==========================
	 */

	@Override
	public ThreadState getThreadState() {
		return this.threadState;
	}

	@Override
	public JobNode doJob() {

		// Flag thread state as failed
		this.threadState.setFailure(this.failure);

		// Nothing further to flag the thread state in failure
		return null;
	}

}