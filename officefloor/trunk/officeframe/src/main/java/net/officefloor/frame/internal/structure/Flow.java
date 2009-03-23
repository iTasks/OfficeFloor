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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.spi.team.Job;

/**
 * Represents a sequence of {@link Job} instances that are completed one after
 * another.
 * 
 * @author Daniel
 */
public interface Flow extends FlowFuture, LinkedListEntry<Flow, JobActivateSet> {

	/**
	 * Creates a new {@link JobNode} bound to this {@link Flow}.
	 * 
	 * @param taskMetaData
	 *            {@link TaskMetaData} for the new {@link JobNode}.
	 * @param parallelNodeOwner
	 *            {@link JobNode} that is the parallel owner of the new
	 *            {@link JobNode}.
	 * @param parameter
	 *            Parameter for the {@link JobNode}.
	 * @return New configured {@link JobNode}.
	 */
	JobNode createJobNode(TaskMetaData<?, ?, ?> taskMetaData,
			JobNode parallelNodeOwner, Object parameter);

	/**
	 * Flags that the input {@link Job} has completed.
	 * 
	 * @param job
	 *            {@link Job} that has completed.
	 * @param notifySet
	 *            {@link JobActivateSet} to add {@link Job} instances waiting on
	 *            this {@link Flow}.
	 */
	void jobComplete(Job job, JobActivateSet notifySet);

	/**
	 * Obtains the {@link ThreadState} that this {@link Flow} is bound.
	 * 
	 * @return {@link ThreadState} that this {@link Flow} is bound.
	 */
	ThreadState getThreadState();

}
