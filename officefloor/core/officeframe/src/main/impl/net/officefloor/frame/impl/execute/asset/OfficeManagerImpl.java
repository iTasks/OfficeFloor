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

package net.officefloor.frame.impl.execute.asset;

import java.util.Timer;
import java.util.TimerTask;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.MonitorClock;
import net.officefloor.frame.internal.structure.OfficeManager;

/**
 * Implementation of the {@link OfficeManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeManagerImpl extends TimerTask implements OfficeManager {

	/**
	 * Interval in milliseconds between each check of the {@link Office}.
	 */
	private final long monitorInterval;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop functionLoop;

	/**
	 * {@link Timer} to monitor the {@link Office}.
	 */
	private final Timer timer;

	/**
	 * {@link MonitorClockImpl}.
	 */
	private final MonitorClockImpl monitorClock;

	/**
	 * {@link AssetManager} instances of the {@link Office}.
	 */
	private AssetManager[] assetManagers;

	/**
	 * Initiate.
	 * 
	 * @param monitorInterval Interval in milliseconds between each check of the
	 *                        {@link Office}. Setting this high reduces overhead of
	 *                        managing the {@link Office}, however setting lower
	 *                        increases responsiveness of the {@link Office}.
	 * @param monitorClock    {@link MonitorClock} for the {@link Office}.
	 * @param functionLoop    {@link FunctionLoop} for the {@link Office}.
	 * @param timer           {@link Timer} to monitor the {@link Office}.
	 */
	public OfficeManagerImpl(long monitorInterval, MonitorClockImpl monitorClock, FunctionLoop functionLoop,
			Timer timer) {
		this.monitorInterval = monitorInterval;
		this.monitorClock = monitorClock;
		this.functionLoop = functionLoop;
		this.timer = timer;
	}

	/**
	 * Loads the remaining {@link AssetManager} instances.
	 * 
	 * @param assetManagers {@link AssetManager} instances.
	 */
	public void loadRemainingState(AssetManager[] assetManagers) {
		this.assetManagers = assetManagers;
	}

	/*
	 * ================= OfficeManager =======================================
	 */

	@Override
	public void startManaging() {
		if (this.monitorInterval > 0) {
			this.timer.scheduleAtFixedRate(this, 0, this.monitorInterval);
		}
	}

	@Override
	public void runAssetChecks() {

		// Trigger the monitoring of the office
		for (int i = 0; i < this.assetManagers.length; i++) {
			AssetManager assetManager = this.assetManagers[i];
			this.functionLoop.delegateFunction(assetManager);
		}
	}

	@Override
	public void stopManaging() {
		this.timer.cancel();
	}

	/*
	 * ================== TimerTask ==========================================
	 */

	@Override
	public void run() {

		// Update the approximate time for the office
		if (this.monitorClock != null) {
			this.monitorClock.updateTime();
		}

		// Run checks on the assets
		this.runAssetChecks();
	}

}
