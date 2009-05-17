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
package net.officefloor.compile.integrate.administrator;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.test.issues.StderrCompilerIssuesWrapper;
import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.plugin.administrator.clazz.ClassAdministratorSource;

/**
 * Tests compiling the {@link Administrator}.
 * 
 * @author Daniel
 */
public class CompileAdministratorTest extends AbstractCompileTestCase {

	// TODO remove once tests written and passing
	@Override
	protected CompilerIssues enhanceIssues(CompilerIssues issues) {
		return new StderrCompilerIssuesWrapper(issues);
	}

	/**
	 * Tests compiling a simple {@link Administrator}.
	 */
	public void testSimpleAdministrator() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addThreadAdministrator("ADMIN",
				"OFFICE_TEAM", ClassAdministratorSource.class,
				ClassAdministratorSource.CLASS_NAME_PROPERTY_NAME,
				SimpleAdmin.class.getName());

		// Compile
		this.compile(true);
	}

	/**
	 * Tests {@link Administrator} pre-administering a {@link Task}.
	 */
	public void testPreAdministerTask() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("DESK.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK",
				"OFFICE_TEAM");
		task.linkPreTaskAdministration("ADMIN", "duty");
		AdministratorBuilder<?> admin = this
				.record_officeBuilder_addThreadAdministrator("ADMIN",
						"OFFICE_TEAM", ClassAdministratorSource.class,
						ClassAdministratorSource.CLASS_NAME_PROPERTY_NAME,
						SimpleAdmin.class.getName());
		this.recordReturn(admin, admin.addDuty("duty"), null);

		// Compile
		this.compile(true);
	}

	/**
	 * Tests {@link Administrator} post-administering a {@link Task}.
	 */
	public void testPostAdministerTask() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("DESK.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK",
				"OFFICE_TEAM");
		task.linkPostTaskAdministration("ADMIN", "duty");
		AdministratorBuilder<?> admin = this
				.record_officeBuilder_addThreadAdministrator("ADMIN",
						"OFFICE_TEAM", ClassAdministratorSource.class,
						ClassAdministratorSource.CLASS_NAME_PROPERTY_NAME,
						SimpleAdmin.class.getName());
		this.recordReturn(admin, admin.addDuty("duty"), null);

		// Compile
		this.compile(true);
	}

	/**
	 * Simple {@link Administrator}.
	 */
	public static class SimpleAdmin {

		public void duty(Object[] extensions) {
		}
	}

	/**
	 * Simple {@link Work}.
	 */
	public static class SimpleWork {

		public void task() {
		}
	}
}