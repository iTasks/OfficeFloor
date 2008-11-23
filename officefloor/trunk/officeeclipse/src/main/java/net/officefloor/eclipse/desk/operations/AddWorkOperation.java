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
package net.officefloor.eclipse.desk.operations;

import net.officefloor.eclipse.classpath.ClasspathUtil;
import net.officefloor.eclipse.common.action.AbstractOperation;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.desk.editparts.DeskEditPart;
import net.officefloor.eclipse.wizard.WizardUtil;
import net.officefloor.eclipse.wizard.workloader.WorkLoaderWizard;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskWorkModel;

/**
 * Adds a {@link DeskWorkModel} to the {@link DeskModel}.
 * 
 * @author Daniel
 */
public class AddWorkOperation extends AbstractOperation<DeskEditPart> {

	/**
	 * Initiate.
	 */
	public AddWorkOperation() {
		super("Add work", DeskEditPart.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.common.action.AbstractOperation#perform(net.
	 * officefloor.eclipse.common.action.AbstractOperation.Context)
	 */
	@Override
	protected void perform(Context context) {

		// Obtain the desk edit part
		final DeskEditPart editPart = context.getEditPart();

		// Create the work
		DeskWorkModel deskWork = null;
		try {
			WorkLoaderWizard workWizard = new WorkLoaderWizard(editPart);
			if (WizardUtil.runWizard(workWizard, editPart)) {
				deskWork = workWizard.getDeskWorkModel();
			}
		} catch (Throwable ex) {
			editPart.messageError(ex);
			return;
		}

		// Ensure have the work
		if (deskWork == null) {
			return;
		}

		// Set location
		context.positionModel(deskWork);

		// Update the class path to possibly include the loader
		ClasspathUtil.attemptUpdateOfficeFloorClasspath(editPart, null,
				deskWork.getLoader());

		// Add the work
		final DeskWorkModel work = deskWork;
		context.execute(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				editPart.getCastedModel().addWork(work);
			}

			@Override
			protected void undoCommand() {
				editPart.getCastedModel().removeWork(work);
			}
		});
	}

}
