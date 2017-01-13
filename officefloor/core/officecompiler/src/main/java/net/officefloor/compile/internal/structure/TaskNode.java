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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.OfficeTaskType;
import net.officefloor.compile.spi.office.OfficeSectionTask;
import net.officefloor.compile.spi.section.SectionTask;
import net.officefloor.compile.type.TypeContext;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.Work;

/**
 * {@link SectionTask} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface TaskNode extends LinkFlowNode, SectionTask, OfficeSectionTask {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Task";

	/**
	 * Initialises this {@link TaskNode}.
	 * 
	 * @param taskTypeName
	 *            {@link ManagedFunctionType} name.
	 * @param work
	 *            {@link WorkNode} for the {@link TaskNode}.
	 */
	void initialise(String taskTypeName, WorkNode work);

	/**
	 * Loads the {@link OfficeTaskType}.
	 * 
	 * @param parentSubSectionType
	 *            Containing {@link OfficeSubSectionType} to this
	 *            {@link OfficeSectionTask}.
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return {@link OfficeTaskType} or <code>null</code> with issues reported
	 *         to the {@link CompilerIssues}.
	 */
	OfficeTaskType loadOfficeTaskType(OfficeSubSectionType parentSubSectionType, TypeContext typeContext);

	/**
	 * Obtains the {@link WorkNode} containing this {@link TaskNode}.
	 * 
	 * @return {@link WorkNode} containing this {@link TaskNode}.
	 */
	WorkNode getWorkNode();

	/**
	 * Loads the {@link ManagedFunctionType} for this {@link TaskNode}.
	 * 
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return {@link ManagedFunctionType} for this {@link TaskNode}. May be
	 *         <code>null</code> if can not determine {@link ManagedFunctionType}.
	 */
	ManagedFunctionType<?, ?, ?> loadTaskType(TypeContext typeContext);

	/**
	 * Builds the {@link ManagedFunction} for this {@link TaskNode}.
	 * 
	 * @param <W>
	 *            {@link Work} type.
	 * @param workBuilder
	 *            {@link WorkBuilder} for the {@link Work} of this {@link ManagedFunction}.
	 * @param typeContext
	 *            {@link TypeContext}.
	 */
	<W extends Work> void buildTask(WorkBuilder<W> workBuilder, TypeContext typeContext);

}