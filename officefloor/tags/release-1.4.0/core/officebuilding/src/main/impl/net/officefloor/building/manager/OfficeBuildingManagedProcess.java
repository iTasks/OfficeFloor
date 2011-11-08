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
package net.officefloor.building.manager;

import java.util.Properties;

import net.officefloor.building.process.ManagedProcess;
import net.officefloor.building.process.ManagedProcessContext;
import net.officefloor.console.OfficeBuilding;

/**
 * {@link ManagedProcess} for spawning an {@link OfficeBuilding}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingManagedProcess implements ManagedProcess {

	/**
	 * Port for the {@link OfficeBuilding}.
	 */
	private final int port;

	/**
	 * Environment {@link Properties}.
	 */
	private final Properties environment;

	/**
	 * {@link ManagedProcessContext}.s
	 */
	private ManagedProcessContext context;

	/**
	 * {@link OfficeBuildingManager}.
	 */
	private OfficeBuildingManager manager;

	/**
	 * Initiate.
	 * 
	 * @param port
	 *            Port for the {@link OfficeBuilding}.
	 * @param environment
	 *            Environment {@link Properties}.
	 */
	public OfficeBuildingManagedProcess(int port, Properties environment) {
		this.port = port;
		this.environment = environment;
	}

	/*
	 * ====================== ManagedProcess ===========================
	 */

	@Override
	public void init(ManagedProcessContext context) throws Throwable {
		this.context = context;

		// Start the OfficeBuilding
		this.manager = OfficeBuildingManager.startOfficeBuilding(this.port,
				this.environment, null);
	}

	@Override
	public void main() throws Throwable {

		// Wait until the OfficeBuilding is stopped (or no longer processing)
		while ((!this.manager.isOfficeBuildingStopped())
				&& (this.context.continueProcessing())) {

			// Wait some time before checking again
			synchronized (this.manager) {
				this.manager.wait(1000);
			}
		}
	}

}