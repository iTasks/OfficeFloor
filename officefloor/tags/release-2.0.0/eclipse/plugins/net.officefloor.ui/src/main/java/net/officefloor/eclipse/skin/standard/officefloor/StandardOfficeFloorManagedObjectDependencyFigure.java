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

package net.officefloor.eclipse.skin.standard.officefloor;

import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectDependencyFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorManagedObjectDependencyFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.LabelConnectorFigure;
import net.officefloor.eclipse.skin.standard.figure.ConnectorFigure.ConnectorDirection;
import net.officefloor.model.officefloor.OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel;

import org.eclipse.draw2d.ConnectionAnchor;

/**
 * Standard {@link OfficeFloorManagedObjectDependencyFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardOfficeFloorManagedObjectDependencyFigure extends
		AbstractOfficeFloorFigure implements
		OfficeFloorManagedObjectDependencyFigure {

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link OfficeFloorManagedObjectDependencyFigureContext}.
	 */
	public StandardOfficeFloorManagedObjectDependencyFigure(
			OfficeFloorManagedObjectDependencyFigureContext context) {
		LabelConnectorFigure figure = new LabelConnectorFigure(context
				.getOfficeFloorManagedObjectDependencyName(),
				ConnectorDirection.EAST, StandardOfficeFloorColours.BLACK());

		// Register connections
		ConnectionAnchor anchor = figure.getConnectionAnchor();
		this
				.registerConnectionAnchor(
						OfficeFloorManagedObjectDependencyToOfficeFloorManagedObjectModel.class,
						anchor);

		this.setFigure(figure);
	}

}