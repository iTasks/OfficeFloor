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

package net.officefloor.eclipse.woof.editparts;

import net.officefloor.eclipse.WoofPlugin;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorConnectionEditPart;
import net.officefloor.eclipse.skin.woof.TemplateOutputToTemplateFigureContext;
import net.officefloor.model.woof.WoofTemplateOutputToWoofTemplateModel;
import net.officefloor.model.woof.WoofTemplateOutputToWoofTemplateModel.WoofTemplateOutputToWoofTemplateEvent;

import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;

/**
 * {@link EditPart} for the {@link WoofTemplateOutputToWoofTemplateEditPart}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateOutputToWoofTemplateEditPart
		extends
		AbstractOfficeFloorConnectionEditPart<WoofTemplateOutputToWoofTemplateModel, WoofTemplateOutputToWoofTemplateEvent>
		implements TemplateOutputToTemplateFigureContext {

	/*
	 * ============= AbstractOfficeFloorConnectionEditPart =====================
	 */

	@Override
	protected void decorateFigure(PolylineConnection figure) {
		WoofPlugin.getSkin().getWoofFigureFactory()
				.decorateTemplateOutputToTemplateFigure(figure, this);
	}

}