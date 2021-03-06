/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.extension;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * Provides means to auto-wire aspects of the {@link Office} within testing.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireOfficeExtensionService implements OfficeExtensionService, OfficeExtensionServiceFactory {

	/**
	 * Indicates whether to auto-wire the objects.
	 */
	private static boolean isEnableAutoWireObjects = false;

	/**
	 * Indicates whether to auto-wire the teams.
	 */
	private static boolean isEnableAutoWireTeams = false;

	/**
	 * Resets the state for next test.
	 */
	public static void reset() {
		isEnableAutoWireObjects = false;
		isEnableAutoWireTeams = false;
	}

	/**
	 * Enable auto-wire of the objects.
	 */
	public static void enableAutoWireObjects() {
		isEnableAutoWireObjects = true;
	}

	/**
	 * Enable auto-wire of the teams.
	 */
	public static void enableAutoWireTeams() {
		isEnableAutoWireTeams = true;
	}

	/*
	 * ======================== OfficeExtensionService ========================
	 */

	@Override
	public OfficeExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		// Enable auto-wiring as appropriate
		if (isEnableAutoWireObjects) {
			officeArchitect.enableAutoWireObjects();
		}
		if (isEnableAutoWireTeams) {
			officeArchitect.enableAutoWireTeams();
		}
	}

}