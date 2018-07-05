/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.maven.stubs;

import java.io.File;
import java.util.Arrays;

import org.apache.maven.plugin.testing.stubs.MavenProjectStub;
import org.apache.maven.project.MavenProject;

/**
 * Mock {@link MavenProject}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorProjectStub extends MavenProjectStub {

	/**
	 * Instantiate.
	 */
	public OfficeFloorProjectStub() {

		// Load the runtime class
		String javaClassPath = System.getProperty("java.class.path");
		this.setRuntimeClasspathElements(Arrays.asList(javaClassPath.split(File.pathSeparator)));
	}

}