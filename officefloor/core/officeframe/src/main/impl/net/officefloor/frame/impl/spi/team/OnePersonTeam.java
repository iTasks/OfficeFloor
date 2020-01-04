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

package net.officefloor.frame.impl.spi.team;

import java.util.concurrent.ThreadFactory;

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;

/**
 * {@link Team} having only one {@link Thread}.
 * 
 * @author Daniel Sagenschneider
 */
public class OnePersonTeam implements Team {

	/**
	 * {@link ThreadFactory}.
	 */
	private final ThreadFactory threadFactory;

	/**
	 * Time to wait in milliseconds for a {@link Job}.
	 */
	private final long waitTime;

	/**
	 * {@link JobQueue}.
	 */
	private final JobQueue jobQueue = new JobQueue();

	/**
	 * {@link OnePerson} of this {@link Team}.
	 */
	private OnePerson person = null;

	/**
	 * Single {@link Thread}.
	 */
	private Thread thread = null;

	/**
	 * Initiate.
	 * 
	 * @param threadFactory
	 *            {@link ThreadFactory}.
	 * @param waitTime
	 *            Time to wait in milliseconds for a {@link Job}.
	 */
	public OnePersonTeam(ThreadFactory threadFactory, long waitTime) {
		this.threadFactory = threadFactory;
		this.waitTime = waitTime;
	}

	/**
	 * Obtains the name of the single {@link Thread}.
	 * 
	 * @return Name of the single {@link Thread}. Will be <code>null</code> if
	 *         {@link Team} not started.
	 */
	public String getThreadName() {
		return (this.thread != null ? this.thread.getName() : null);
	}

	/*
	 * =================== Team =========================================
	 */

	@Override
	public void startWorking() {
		if (this.person != null) {
			throw new IllegalStateException("Team " + this.getClass().getName() + " has already started working");
		}

		// Hire the person for the team
		this.person = new OnePerson(this.jobQueue, this.waitTime);

		// Start the person working
		this.thread = this.threadFactory.newThread(this.person);
		this.thread.start();
	}

	@Override
	public void assignJob(Job job) {
		this.jobQueue.enqueue(job);
	}

	@Override
	public void stopWorking() {
		if (this.person != null) {
			try {
				// Stop the Person working
				this.person.continueWorking = false;

				// Wait on Person to stop working
				while (!this.person.finished) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException ex) {
						return;
					}
				}
			} finally {
				// Release the person and thread
				this.person = null;
				this.thread = null;
			}
		}
	}

	/**
	 * The individual comprising the {@link Team}.
	 */
	public class OnePerson implements Runnable {

		/**
		 * {@link JobQueue}.
		 */
		private final JobQueue jobQueue;

		/**
		 * Time to wait in milliseconds for a {@link Job}.
		 */
		private final long waitTime;

		/**
		 * Flag indicating to continue to work.
		 */
		private volatile boolean continueWorking = true;

		/**
		 * Flag to indicate finished.
		 */
		protected volatile boolean finished = false;

		/**
		 * Initiate.
		 * 
		 * @param jobQueue
		 *            {@link JobQueue}.
		 * @param waitTime
		 *            Time to wait in milliseconds for a {@link Job}.
		 */
		public OnePerson(JobQueue jobQueue, long waitTime) {
			this.jobQueue = jobQueue;
			this.waitTime = waitTime;
		}

		/*
		 * ======================== Runnable =============================
		 */

		@Override
		public void run() {
			try {
				while (this.continueWorking) {

					// Obtain the next job
					Job job = this.jobQueue.dequeue(this.waitTime);
					if (job != null) {
						// Have job therefore execute it
						job.run();
					}
				}
			} finally {
				// Flag finished
				this.finished = true;
			}
		}
	}

}
