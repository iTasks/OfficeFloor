/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.skin.standard.officefloor;

import net.officefloor.eclipse.skin.officefloor.DeployedOfficeFigure;
import net.officefloor.eclipse.skin.officefloor.DeployedOfficeFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.RoundedContainerFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.officefloor.DeployedOfficeModel;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectSourceToDeployedOfficeModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * Standard {@link DeployedOfficeFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardDeployedOfficeFigure extends AbstractOfficeFloorFigure
		implements DeployedOfficeFigure {

	/**
	 * {@link Label} containing the {@link DeployedOfficeModel} name.
	 */
	private final Label deployedOfficeName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link DeployedOfficeFigureContext}.
	 */
	public StandardDeployedOfficeFigure(DeployedOfficeFigureContext context) {

		Color officeColour = new Color(null, 130, 255, 150);

		// Create the figure
		Figure figure = new Figure();
		NoSpacingGridLayout figureLayout = new NoSpacingGridLayout(2);
		figure.setLayoutManager(figureLayout);

		// Create the office
		RoundedContainerFigure office = new RoundedContainerFigure(context
				.getDeployedOfficeName(), officeColour, 20, false);
		this.deployedOfficeName = office.getContainerName();
		figure.add(office);

		// Create the managing object connection
		ConnectorFigure managingObject = new ConnectorFigure(
				ConnectorDirection.EAST, ColorConstants.black);
		managingObject.setBorder(new MarginBorder(10, 0, 0, 0));
		this.registerConnectionAnchor(
				OfficeFloorManagedObjectSourceToDeployedOfficeModel.class,
				managingObject.getConnectionAnchor());
		figure.add(managingObject);
		figureLayout.setConstraint(managingObject, new GridData(SWT.BEGINNING,
				SWT.BEGINNING, false, false));

		// Specify figures
		this.setFigure(figure);
		this.setContentPane(office.getContentPane());
	}

	/*
	 * ============== DeployedOfficeFigure ==================================
	 */

	@Override
	public void setDeployedOfficeName(String deployedOfficeName) {
		this.deployedOfficeName.setText(deployedOfficeName);
	}

	@Override
	public IFigure getDeployedOfficeNameFigure() {
		return this.deployedOfficeName;
	}

}