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

package net.officefloor.eclipse.skin.standard.section;

import net.officefloor.eclipse.skin.section.ExternalFlowFigure;
import net.officefloor.eclipse.skin.section.ExternalFlowFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.section.ExternalFlowModel;
import net.officefloor.model.section.SectionManagedObjectSourceFlowToExternalFlowModel;
import net.officefloor.model.section.SubSectionOutputToExternalFlowModel;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link ExternalFlowFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardExternalFlowFigure extends AbstractOfficeFloorFigure
		implements ExternalFlowFigure {

	/**
	 * {@link Label} containing the {@link ExternalFlowModel} name.
	 */
	private final Label externalFlowName;

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link ExternalFlowFigureContext}.
	 */
	public StandardExternalFlowFigure(ExternalFlowFigureContext context) {
		LabelConnectorFigure figure = new LabelConnectorFigure(context
				.getExternalFlowName(), ConnectorDirection.WEST,
				StandardOfficeFloorColours.BLACK());
		this.externalFlowName = figure.getLabel();

		// Register connection anchor
		ConnectionAnchor anchor = figure.getConnectionAnchor();
		this.registerConnectionAnchor(
				SubSectionOutputToExternalFlowModel.class, anchor);
		this
				.registerConnectionAnchor(
						SectionManagedObjectSourceFlowToExternalFlowModel.class,
						anchor);

		this.setFigure(figure);
	}

	/*
	 * ================== ExternalFlowFigure ================================
	 */

	@Override
	public void setExternalFlowName(String externalFlowName) {
		this.externalFlowName.setText(externalFlowName);
	}

	@Override
	public IFigure getExternalFlowNameFigure() {
		return this.externalFlowName;
	}

}