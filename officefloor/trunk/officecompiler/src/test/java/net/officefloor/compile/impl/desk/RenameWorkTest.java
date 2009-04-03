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
package net.officefloor.compile.impl.desk;

import net.officefloor.compile.change.Change;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.WorkModel;

/**
 * Tests renaming the {@link WorkModel}.
 * 
 * @author Daniel
 */
public class RenameWorkTest extends AbstractDeskOperationsTestCase {

	/**
	 * Ensures handles {@link WorkModel} not being on the {@link DeskModel}.
	 */
	public void testRenameWorkNotOnDesk() {
		WorkModel work = new WorkModel("NOT_ON_DESK", null);
		Change<WorkModel> change = this.operations.renameWork(work, "NEW_NAME");
		this.assertChange(change, work, "Rename work NOT_ON_DESK to NEW_NAME",
				false, "Work NOT_ON_DESK not on desk");
	}

	/**
	 * Ensure can rename the {@link WorkModel}.
	 */
	public void testRenameWork() {
		WorkModel work = this.desk.getWorks().get(0);
		Change<WorkModel> change = this.operations.renameWork(work, "NEW_NAME");
		this.assertChange(change, work, "Rename work OLD_NAME to NEW_NAME",
				true);
	}

	/**
	 * Ensures on renaming the {@link WorkModel} that order is maintained.
	 */
	public void testRenameWorkCausingWorkOrderChange() {
		this.useTestSetupDesk();
		WorkModel work = this.desk.getWorks().get(0);
		Change<WorkModel> change = this.operations.renameWork(work, "WORK_C");
		this.assertChange(change, work, "Rename work WORK_A to WORK_C", true);
	}
}