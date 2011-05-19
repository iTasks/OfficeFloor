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

package net.officefloor.compile.integrate.task;

import net.officefloor.compile.integrate.AbstractCompileTestCase;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.TaskFlow;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.compile.work.TaskEscalationType;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;
import net.officefloor.plugin.work.clazz.ClassTaskFactory;
import net.officefloor.plugin.work.clazz.ClassWork;
import net.officefloor.plugin.work.clazz.ClassWorkFactory;
import net.officefloor.plugin.work.clazz.ClassWorkSource;
import net.officefloor.plugin.work.clazz.FlowInterface;
import net.officefloor.plugin.work.clazz.ParameterFactory;

/**
 * Tests compiling a {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileTaskTest extends AbstractCompileTestCase {

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
	 * Tests compiling a {@link Task} with a differentiator.
	 */
	public void testDifferentiatorTask() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		this.record_workBuilder_addTask("TASK", "OFFICE_TEAM");
		this
				.record_taskBuilder_setDifferentiator(DifferentiatorWorkSource.DIFFERENTIATOR);

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling {@link Work} with an initial {@link Task}.
	 */
	public void testInitialTaskForWork() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		WorkBuilder<Work> work = this
				.record_officeBuilder_addWork("SECTION.WORK");
		this.record_workBuilder_addTask("TASK", "OFFICE_TEAM");
		work.setInitialTask("TASK");

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
	 * Ensures compiling a {@link Task} linking to next {@link Task} on the same
	 * {@link Work}.
	 */
	public void testLinkTaskNextToTaskOnSameWork() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK_A",
				"OFFICE_TEAM");
		this.record_workBuilder_addTask("TASK_B", "OFFICE_TEAM");
		task.setNextTaskInFlow("TASK_B", Integer.class);

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Ensures compiling a {@link Task} linking to next {@link Task} in a
	 * different {@link OfficeSection}.
	 */
	public void testLinkTaskNextToTaskInDifferentOfficeSection() {

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
		task.setNextTaskInFlow("SECTION_B.WORK", "INPUT", Integer.class);

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Ensures compiling a {@link Task} linking to next {@link Task} that is
	 * through a parent {@link SubSection}.
	 */
	public void testLinkTaskNextToTaskThroughParentSection() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION_A.desk-one.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK",
				"OFFICE_TEAM");
		this.record_officeBuilder_addWork("SECTION_B.WORK");
		this.record_workBuilder_addTask("INPUT", "OFFICE_TEAM");
		task.setNextTaskInFlow("SECTION_B.WORK", "INPUT", Integer.class);

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Ensures compiling a {@link Task} linking to next {@link Task} that is
	 * through a child {@link SubSection}.
	 */
	public void testLinkTaskNextToTaskThroughChildSection() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION_A.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK",
				"OFFICE_TEAM");
		this.record_officeBuilder_addWork("SECTION_B.desk-two.WORK");
		this.record_workBuilder_addTask("INPUT", "OFFICE_TEAM");
		task.setNextTaskInFlow("SECTION_B.desk-two.WORK", "INPUT",
				Integer.class);

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Ensures issue if {@link TaskObject} not linked.
	 */
	public void testTaskObjectNotLinked() throws Exception {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		this.record_workBuilder_addTask("TASK", "OFFICE_TEAM");
		this.issues
				.addIssue(LocationType.SECTION, "desk", AssetType.TASK, "TASK",
						"Object CompileManagedObject is not linked to a BoundManagedObjectNode");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Ensure compiling a {@link Task} link the {@link TaskObject} as a
	 * parameter.
	 */
	public void testLinkTaskObjectAsParameter() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK",
				"OFFICE_TEAM");
		task.linkParameter(0, CompileManagedObject.class);

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Ensure compiling a {@link Task} linking the {@link TaskObject} to a
	 * {@link OfficeFloorManagedObject}.
	 */
	public void testLinkTaskObjectToOfficeFloorManagedObject() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice(
				"OFFICE", "OFFICE_TEAM", "TEAM");
		office.registerManagedObjectSource("MANAGED_OBJECT",
				"MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT",
				"MANAGED_OBJECT");
		this.record_officeBuilder_addWork("SECTION.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK",
				"OFFICE_TEAM");
		task.linkManagedObject(0, "MANAGED_OBJECT", CompileManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Ensure compiling a {@link Task} linking the {@link TaskObject} to an
	 * {@link OfficeFloorInputManagedObject}.
	 */
	public void testLinkTaskObjectToOfficeFloorInputManagedObject() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE");
		this.record_officeBuilder_registerTeam("OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		TaskBuilder<Work, ?, ?> task = this.record_workBuilder_addTask("TASK",
				"OFFICE_TEAM");
		task.linkManagedObject(0, "INPUT_MANAGED_OBJECT",
				InputManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", InputManagedObject.class.getName());
		ManagingOfficeBuilder<?> managingOffice = this
				.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this
				.record_managingOfficeBuilder_setInputManagedObjectName("INPUT_MANAGED_OBJECT");
		managingOffice.linkProcess(0, "SECTION.WORK", "TASK");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Ensure compiling a {@link Task} linking the {@link TaskObject} to an
	 * {@link OfficeManagedObject}.
	 */
	public void testLinkTaskObjectToOfficeManagedObject() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice(
				"OFFICE", "OFFICE_TEAM", "TEAM");
		office.registerManagedObjectSource("OFFICE.MANAGED_OBJECT",
				"OFFICE.MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject(
				"OFFICE.MANAGED_OBJECT", "OFFICE.MANAGED_OBJECT");
		this.record_officeBuilder_addWork("SECTION.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK",
				"OFFICE_TEAM");
		task.linkManagedObject(0, "OFFICE.MANAGED_OBJECT",
				CompileManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class,
				0, "class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Ensure compiling a {@link Task} linking the {@link TaskObject} to a
	 * {@link SectionManagedObject}.
	 */
	public void testLinkTaskObjectToSectionManagedObject() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice(
				"OFFICE", "OFFICE_TEAM", "TEAM");
		office.registerManagedObjectSource("OFFICE.SECTION.MANAGED_OBJECT",
				"OFFICE.SECTION.MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject(
				"OFFICE.SECTION.MANAGED_OBJECT",
				"OFFICE.SECTION.MANAGED_OBJECT");
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.SECTION.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");
		this.record_officeBuilder_addWork("SECTION.DESK.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK",
				"OFFICE_TEAM");
		task.linkManagedObject(0, "OFFICE.SECTION.MANAGED_OBJECT",
				CompileManagedObject.class);

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Ensure compiling a {@link Task} linking the {@link TaskObject} to a
	 * {@link DeskManagedObject}.
	 */
	public void testLinkTaskObjectToDeskManagedObject() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice(
				"OFFICE", "OFFICE_TEAM", "TEAM");
		this.record_officeBuilder_addWork("SECTION.DESK.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK",
				"OFFICE_TEAM");
		task.linkManagedObject(0, "OFFICE.SECTION.DESK.MANAGED_OBJECT",
				CompileManagedObject.class);
		office.registerManagedObjectSource(
				"OFFICE.SECTION.DESK.MANAGED_OBJECT",
				"OFFICE.SECTION.DESK.MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject(
				"OFFICE.SECTION.DESK.MANAGED_OBJECT",
				"OFFICE.SECTION.DESK.MANAGED_OBJECT");
		this.record_officeFloorBuilder_addManagedObject(
				"OFFICE.SECTION.DESK.MANAGED_OBJECT_SOURCE",
				ClassManagedObjectSource.class, 0, "class.name",
				CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Ensures compiling a {@link TaskObject} linking
	 * {@link OfficeManagedObject} that is through a parent {@link SubSection}.
	 */
	public void testLinkTaskObjectThroughParentSection() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		OfficeBuilder office = this.record_officeFloorBuilder_addOffice(
				"OFFICE", "OFFICE_TEAM", "TEAM");
		office.registerManagedObjectSource("MANAGED_OBJECT",
				"MANAGED_OBJECT_SOURCE");
		this.record_officeBuilder_addThreadManagedObject("MANAGED_OBJECT",
				"MANAGED_OBJECT");
		this.record_officeBuilder_addWork("SECTION.DESK.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK",
				"OFFICE_TEAM");
		task.linkManagedObject(0, "MANAGED_OBJECT", CompileManagedObject.class);
		this.record_officeFloorBuilder_addManagedObject(
				"MANAGED_OBJECT_SOURCE", ClassManagedObjectSource.class, 0,
				"class.name", CompileManagedObject.class.getName());
		this.record_managedObjectBuilder_setManagingOffice("OFFICE");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link Task} linking a {@link Escalation} to another
	 * {@link Task} on the same {@link Work}.
	 */
	public void testLinkEscalationToTaskOnSameWork() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		TaskBuilder<?, ?, ?> task = this.record_workBuilder_addTask("TASK_A",
				"OFFICE_TEAM");
		this.record_workBuilder_addTask("TASK_B", "OFFICE_TEAM");
		task.addEscalation(Exception.class, "TASK_B");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link Task} linking a {@link Escalation} to a
	 * {@link Task} in a different {@link OfficeSection}.
	 */
	public void testLinkEscalationToTaskInDifferentOfficeSection() {

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
		task.addEscalation(Exception.class, "SECTION_B.WORK", "INPUT");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Ensures issue if {@link TaskEscalationType} not linked.
	 */
	public void testEscalationNotPropagatedToOffice() {

		// Record building the office floor
		this.record_officeFloorBuilder_addTeam("TEAM",
				OnePersonTeamSource.class);
		this.record_officeFloorBuilder_addOffice("OFFICE", "OFFICE_TEAM",
				"TEAM");
		this.record_officeBuilder_addWork("SECTION.WORK");
		this.record_workBuilder_addTask("TASK", "OFFICE_TEAM");
		this.issues
				.addIssue(
						LocationType.SECTION,
						"desk",
						AssetType.TASK,
						"TASK",
						"Escalation "
								+ Exception.class.getName()
								+ " not handled by a Task nor propagated to the Office");

		// Compile the office floor
		this.compile(true);
	}

	/**
	 * Tests compiling a {@link Task} that propagates the {@link Escalation} to
	 * the {@link Office}.
	 */
	public void testEscalationPropagatedToOffice() {

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
			fail("Should not be invoked in compiling");
		}

		public void flowTask(Flows flows) {
			fail("Should not be invoked in compiling");
		}

		public Integer nextTask() {
			fail("Should not be invoked in compiling");
			return null;
		}

		public void objectTask(CompileManagedObject object) {
			fail("Should not be invoked in compiling");
		}

		public void inputObjectTask(InputManagedObject object) {
			fail("Should not be invoked in compiling");
		}

		public void escalationTask() throws Exception {
			fail("Should not be invoked in compiling");
		}
	}

	/**
	 * {@link WorkSource} to load differentiator for {@link Task}.
	 */
	public static class DifferentiatorWorkSource extends
			AbstractWorkSource<ClassWork> {

		public static final String DIFFERENTIATOR = "DIFFERENTIATOR";

		/*
		 * ================== WorkSource ==============================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void sourceWork(WorkTypeBuilder<ClassWork> workTypeBuilder,
				WorkSourceContext context) throws Exception {
			workTypeBuilder.setWorkFactory(new ClassWorkFactory(
					CompileTaskWork.class));
			TaskTypeBuilder<Indexed, Indexed> task = workTypeBuilder
					.addTaskType("task", new ClassTaskFactory(
							CompileTaskWork.class.getMethod("simpleTask"),
							false, new ParameterFactory[0]), Indexed.class,
							Indexed.class);
			task.setDifferentiator(DIFFERENTIATOR);
		}
	}

	/**
	 * Class for {@link ClassManagedObjectSource}.
	 */
	public static class CompileManagedObject {
		// No dependencies as focused on testing task
	}

	/**
	 * Class for {@link ClassManagedObjectSource}.
	 */
	public static class InputManagedObject {

		@FlowInterface
		public static interface InputProcesses {
			void doProcess(Integer parameter);
		}

		InputProcesses processes;
	}

}