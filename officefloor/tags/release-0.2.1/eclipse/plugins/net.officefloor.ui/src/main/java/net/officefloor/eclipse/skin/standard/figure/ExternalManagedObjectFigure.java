/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.eclipse.skin.standard.figure;

import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.frame.spi.managedobject.ManagedObject;

import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;

/**
 * {@link ManagedObject} {@link Figure}.
 *
 * @author Daniel Sagenschneider
 */
public class ExternalManagedObjectFigure extends Ellipse {

	/**
	 * {@link Label} for the name of the {@link ManagedObject}.
	 */
	private final Label name;

	/**
	 * Initiate.
	 *
	 * @param managedObjectName
	 *            Name of {@link ManagedObject}.
	 */
	public ExternalManagedObjectFigure(String managedObjectName) {

		// Ellipse
		this.setBackgroundColor(StandardOfficeFloorColours.EXTERNAL_OBJECT());
		this.setOpaque(true);
		this.setLayoutManager(new FlowLayout());

		// Name of managed object
		this.name = new Label(managedObjectName);
		this.name.setLayoutManager(new FlowLayout());
		this.name.setBorder(new MarginBorder(5));
		this.add(this.name);
	}

	/**
	 * Obtains the {@link Label} containing the {@link ManagedObject} name.
	 *
	 * @return {@link Label} containing the {@link ManagedObject} name.
	 */
	public Label getLabel() {
		return this.name;
	}

	/**
	 * Specifies the {@link ManagedObject} name.
	 *
	 * @param managedObjectName
	 *            {@link ManagedObject} name.
	 */
	public void setManagedObjectName(String managedObjectName) {
		this.name.setText(managedObjectName);
	}

}