/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.extension;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeExtensionService} to configure the {@link Office} within tests.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileOffice implements OfficeExtensionService {

	/**
	 * {@link OfficeExtensionService} logic.
	 */
	private static OfficeExtensionService extender = null;

	/**
	 * {@link OfficeFloorCompiler}.
	 */
	private final OfficeFloorCompiler compiler;

	/**
	 * Instantiate.
	 */
	public CompileOffice() {
		this.compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		this.compiler.setCompilerIssues(new FailTestCompilerIssues());
	}

	/**
	 * Obtains the {@link OfficeFloorCompiler}.
	 * 
	 * @return {@link OfficeFloorCompiler}.
	 */
	public OfficeFloorCompiler getOfficeFloorCompiler() {
		return this.compiler;
	}

	/**
	 * Compiles the {@link Office}.
	 * 
	 * @param officeConfiguration
	 *            {@link OfficeExtensionService} to configure the
	 *            {@link Office}.
	 * @return {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to compile the {@link OfficeFloor}.
	 */
	public OfficeFloor compileOffice(OfficeExtensionService officeConfiguration) throws Exception {

		// Compile the solution
		try {
			extender = officeConfiguration;

			// Compile and return the office
			return this.compiler.compile("OfficeFloor");

		} finally {
			// Ensure the extender is cleared for other tests
			extender = null;
		}

	}

	/**
	 * Compiles and opens the {@link Office}.
	 * 
	 * @param officeConfiguration
	 *            {@link OfficeExtensionService} to configure the
	 *            {@link Office}.
	 * @return {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to compile and open the {@link OfficeFloor}.
	 */
	public OfficeFloor compileAndOpenOffice(OfficeExtensionService officeConfiguration) throws Exception {

		// Compile the OfficeFloor
		OfficeFloor officeFloor = this.compileOffice(officeConfiguration);

		// Open the OfficeFloor
		officeFloor.openOfficeFloor();

		// Return the OfficeFloor
		return officeFloor;
	}

	/*
	 * ====================== OfficeExtensionService =====================
	 */

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {
		if (extender != null) {
			extender.extendOffice(officeArchitect, context);
		}
	}

}