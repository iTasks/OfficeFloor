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
package net.officefloor.compile.integrate.task;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.test.issues.StderrCompilerIssuesWrapper;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.plugin.work.clazz.ClassWorkSource;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Tests compiling a {@link Task}.
 * 
 * @author Daniel
 */
public class CompileTaskTest extends AbstractCompileTestCase {

	@Override
	protected CompilerIssues enhanceIssues(CompilerIssues issues) {
		return new StderrCompilerIssuesWrapper(issues);
	}

	/**
	 * Tests compiling a simple {@link Task}.
	 */
	public void testSimpleTask() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		this.record_workBuilder_addTask("TASK", "OFFICE_TEAM");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Ensures issue if {@link TaskFlow} not linked.
	 */
	public void testTaskFlowNotLinked() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		this.record_workBuilder_addTask("TASK", "OFFICE_TEAM");
		this.issues.addIssue(LocationType.SECTION, "desk", AssetType.TASK,
				"TASK", "Flow flow is not linked to a TaskNode");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Ensures issue if unknown {@link FlowInstigationStrategyEnum}.
	 */
	public void testUnknownFlowInstigationStrategy() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		this.record_workBuilder_addTask("TASK_A", "OFFICE_TEAM");
		this.record_workBuilder_addTask("TASK_B", "OFFICE_TEAM");
		this.issues.addIssue(LocationType.SECTION, "desk", AssetType.TASK,
				"TASK_A",
				"Unknown flow instigation strategy 'unknown' for flow flow");
		this.issues.addIssue(LocationType.SECTION, "desk", AssetType.TASK,
				"TASK_A", "No instigation strategy provided for flow flow");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link Task} linking a {@link Flow} to another
	 * {@link Task} on the same {@link Work}.
	 */
	public void testLinkFlowToTaskOnSameWork() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		TaskBuilder<?, ?, ?> taskOne = this.record_workBuilder_addTask(
				"TASK_A", "OFFICE_TEAM");
		this.record_workBuilder_addTask("TASK_B", "OFFICE_TEAM");
		taskOne.linkFlow(0, "TASK_B", FlowInstigationStrategyEnum.PARALLEL,
				String.class);

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link Task} linking a {@link Flow} to different
	 * {@link Work} in the same {@link OfficeSection}.
	 */
	public void testLinkFlowToTaskOnDifferentWorkInSameSection() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK_A");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK_A",
				"OFFICE_TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK_B");
		this.record_workBuilder_addTask("TASK_B", "OFFICE_TEAM");
		task.linkFlow(0, "SECTION.WORK_B", "TASK_B",
				FlowInstigationStrategyEnum.SEQUENTIAL, String.class);

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link Task} linking a {@link Flow} to a {@link Task}
	 * in a different {@link SubSection}.
	 */
	public void testLinkFlowToTaskInDifferentSubSection() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION.SUB_SECTION_A.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK",
				"OFFICE_TEAM");
		this.record_officeBuilder_addWork("SECTION.SUB_SECTION_B.WORK");
		this.record_workBuilder_addTask("INPUT", "OFFICE_TEAM");
		task.linkFlow(0, "SECTION.SUB_SECTION_B.WORK", "INPUT",
				FlowInstigationStrategyEnum.ASYNCHRONOUS, String.class);

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link Task} linking a {@link Flow} to a {@link Task}
	 * in a different {@link OfficeSection}.
	 */
	public void testLinkFlowToTaskInDifferentOfficeSection() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION_A.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK",
				"OFFICE_TEAM");
		this.record_officeBuilder_addWork("SECTION_B.WORK");
		this.record_workBuilder_addTask("INPUT", "OFFICE_TEAM");
		task.linkFlow(0, "SECTION_B.WORK", "INPUT",
				FlowInstigationStrategyEnum.ASYNCHRONOUS, String.class);

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * {@link FlowInterface} for {@link CompileTaskWork}.
	 */
	@FlowInterface
	public static interface Flows {

		void flow(String parameter);
	}

	/**
	 * Class for {@link ClassWorkSource}.
	 */
	public static class CompileTaskWork {

		public void simpleTask() {
		}

		public void flowTask(Flows flows) {
		}
	}

}