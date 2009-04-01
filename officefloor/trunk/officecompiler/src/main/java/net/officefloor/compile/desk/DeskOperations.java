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
package net.officefloor.compile.desk;

import net.officefloor.compile.change.Change;
import net.officefloor.compile.spi.work.TaskType;
import net.officefloor.compile.spi.work.WorkType;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.ExternalFlowModel;
import net.officefloor.model.desk.ExternalManagedObjectModel;
import net.officefloor.model.desk.TaskModel;
import net.officefloor.model.desk.WorkModel;
import net.officefloor.model.desk.WorkTaskModel;

/**
 * Changes that can be made to a {@link DeskModel}.
 * 
 * @author Daniel
 */
public interface DeskOperations {

	<W extends Work> Change<WorkModel> addWork(String workName,
			WorkType<W> workType, String... taskNames);

	Change<WorkModel> removeWork(WorkModel workModel);

	Change<WorkModel> renameWork(WorkModel workModel, String newWorkName);

	<W extends Work> Change<WorkModel> conformWork(WorkModel workModel,
			WorkType<W> workType);

	<W extends Work, D extends Enum<D>, F extends Enum<F>> Change<WorkTaskModel> addWorkTask(
			WorkModel workModel, TaskType<W, D, F> taskType);

	Change<WorkTaskModel> removeWorkTask(WorkModel workModel,
			WorkTaskModel taskModel);

	Change<WorkTaskModel> setObjectAsParameter(boolean isParameter,
			String objectName, WorkTaskModel workTaskModel);

	<W extends Work, D extends Enum<D>, F extends Enum<F>> Change<TaskModel> addTask(
			String taskName, WorkTaskModel workTaskModel,
			TaskType<W, D, F> taskType);

	Change<TaskModel> removeTask(TaskModel taskModel);

	Change<TaskModel> renameTask(TaskModel taskModel, String newTaskName);

	Change<TaskModel> setTaskAsPublic(boolean isPublic, TaskModel taskModel);

	<W extends Work, D extends Enum<D>, F extends Enum<F>> Change<WorkTaskModel> conformTask(
			WorkTaskModel taskModel, TaskType<W, D, F> taskType);

	Change<ExternalFlowModel> addExternalFlow(String externalFlowName,
			String argumentType);

	Change<ExternalFlowModel> removeExternalFlow(ExternalFlowModel externalFlow);

	Change<ExternalManagedObjectModel> addExternalManagedObject(
			String externalManagedObjectName, String argumentType);

	Change<ExternalManagedObjectModel> removeExternalManagedObject(
			ExternalManagedObjectModel externalManagedObject);

}