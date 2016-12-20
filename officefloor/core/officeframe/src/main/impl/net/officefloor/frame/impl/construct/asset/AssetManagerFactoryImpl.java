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
package net.officefloor.frame.impl.construct.asset;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.execute.asset.AssetManagerImpl;
import net.officefloor.frame.internal.construct.AssetManagerFactory;
import net.officefloor.frame.internal.structure.AssetManager;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.OfficeClock;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link AssetManagerFactory} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AssetManagerFactoryImpl implements AssetManagerFactory {

	/**
	 * Registry of the {@link AssetManager} instances by their name.
	 */
	private final Map<String, AssetManager> registry = new HashMap<String, AssetManager>();

	/**
	 * {@link ProcessState} for managing the {@link Office} where all mutations
	 * of the {@link Office} are undertaken on its main {@link ThreadState}.
	 */
	private final ProcessState officeManagerProcessState;

	/**
	 * {@link OfficeClock}.
	 */
	private final OfficeClock officeClock;

	/**
	 * {@link FunctionLoop}.
	 */
	private final FunctionLoop functionLoop;

	/**
	 * Instantiate.
	 * 
	 * @param officeManagerProcessState
	 *            {@link ProcessState} for managing the {@link Office} where all
	 *            mutations of the {@link Office} are undertaken on its main
	 *            {@link ThreadState}.
	 * @param officeClock
	 *            {@link OfficeClock}.
	 * @param functionLoop
	 *            {@link FunctionLoop}.
	 */
	public AssetManagerFactoryImpl(ProcessState officeManagerProcessState, OfficeClock officeClock,
			FunctionLoop functionLoop) {
		this.officeManagerProcessState = officeManagerProcessState;
		this.officeClock = officeClock;
		this.functionLoop = functionLoop;
	}

	/*
	 * ================= AssetManagerFactory =================================
	 */

	@Override
	public AssetManager createAssetManager(AssetType assetType, String assetName, String responsibility,
			OfficeFloorIssues issues) {

		// Create the name of the asset manager
		String assetManagerName = assetType.toString() + ":" + assetName + "[" + responsibility + "]";

		// Determine if manager already available for responsibility over asset
		if (this.registry.get(assetManagerName) != null) {
			issues.addIssue(assetType, assetName,
					AssetManager.class.getSimpleName() + " already responsible for '" + responsibility + "'");
			return null; // can not carry on
		}

		// Create the asset manager
		AssetManager assetManager = new AssetManagerImpl(this.officeManagerProcessState, this.officeClock,
				this.functionLoop);

		// Register the asset manager
		this.registry.put(assetManagerName, assetManager);

		// Return the asset manager
		return assetManager;
	}

}