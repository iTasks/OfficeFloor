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
package net.officefloor.frame.internal.structure;

/**
 * <p>
 * Latch on an {@link Asset} to only allow the {@link ThreadState} instances
 * proceed when {@link Asset} is ready.
 * <p>
 * May be used as a {@link LinkedListSetEntry} in a list of {@link AssetLatch}
 * instances for an {@link AssetManager}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AssetLatch {

	/**
	 * Obtains the {@link Asset} for this {@link AssetLatch}.
	 * 
	 * @return {@link Asset} for this {@link AssetLatch}.
	 */
	Asset getAsset();

	/**
	 * <p>
	 * Flags for the {@link JobNode} (and more specifically the
	 * {@link ThreadState} of the {@link JobNode}) to wait until the
	 * {@link Asset} is ready.
	 * <p>
	 * This is typically because the {@link Asset} is doing some processing that
	 * the {@link JobNode} requires completed before proceeding.
	 * 
	 * @param jobNode
	 *            {@link JobNode} to be released when the {@link Asset} is
	 *            ready.
	 * @return Optional {@link JobNode} to execute to wait on the {@link Asset}.
	 */
	JobNode awaitOnAsset(JobNode jobNode);

	/**
	 * Releases the {@link JobNode} instances waiting on the {@link Asset}.
	 * 
	 * @param isPermanent
	 *            <code>true</code> indicates that all {@link JobNode} instances
	 *            added to the {@link AssetLatch} from now on are activated
	 *            immediately. It is useful to flag an {@link AssetLatch} in
	 *            this state when the {@link Asset} is no longer being used to
	 *            stop a {@link JobNode} from waiting forever.
	 */
	void releaseJobNodes(boolean isPermanent);

	/**
	 * Fails the {@link JobNode} instances waiting on this {@link Asset}.
	 * 
	 * @param failure
	 *            Failure to propagate to the {@link ThreadState} of the
	 *            {@link JobNode} instances waiting on the {@link Asset}.
	 * @param isPermanent
	 *            <code>true</code> indicates that all {@link JobNode} instances
	 *            added to the {@link AssetLatch} from now on are activated
	 *            immediately with the input failure. It is useful to flag an
	 *            {@link AssetLatch} in this state when the {@link Asset} is in
	 *            a failed state that can not be recovered from.
	 */
	void failJobNodes(Throwable failure, boolean isPermanent);

}