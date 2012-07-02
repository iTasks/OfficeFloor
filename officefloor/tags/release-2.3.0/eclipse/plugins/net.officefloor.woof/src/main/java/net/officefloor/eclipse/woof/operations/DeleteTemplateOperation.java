/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.eclipse.woof.operations;

import net.officefloor.eclipse.woof.editparts.WoofTemplateEditPart;
import net.officefloor.model.change.Change;
import net.officefloor.model.woof.WoofChanges;
import net.officefloor.model.woof.WoofModel;
import net.officefloor.model.woof.WoofTemplateModel;

/**
 * Deletes a {@link WoofTemplateModel} from the {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DeleteTemplateOperation extends
		AbstractWoofChangeOperation<WoofTemplateEditPart> {

	/**
	 * Initiate.
	 * 
	 * @param woofChanges
	 *            {@link WoofChanges}.
	 */
	public DeleteTemplateOperation(WoofChanges woofChanges) {
		super("Delete template", WoofTemplateEditPart.class, woofChanges);
	}

	/*
	 * ================== AbstractWoofChangeOperation ======================
	 */

	@Override
	protected Change<?> getChange(WoofChanges changes, Context context) {

		// Obtain the template to remove
		WoofTemplateModel template = context.getEditPart().getCastedModel();

		// Create the change
		Change<WoofTemplateModel> change = changes.removeTemplate(template);

		// Return the change
		return change;
	}

}