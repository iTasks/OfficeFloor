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
package net.officefloor.eclipse.room.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToolbarLayout;

/**
 * {@link org.eclipse.draw2d.Figure} for the
 * {@link net.officefloor.model.room.SubRoomInputFlowModel}.
 * 
 * @author Daniel
 */
public class SubRoomInputFlowFigure extends Figure {

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name of the input flow.
	 * @param figureForIsPublic
	 *            {@link Figure} to handle whether public.
	 */
	public SubRoomInputFlowFigure(String name, IFigure figureForIsPublic) {
		this.setLayoutManager(new ToolbarLayout());
		this.setBackgroundColor(ColorConstants.lightBlue);
		this.setOpaque(true);
		this.setSize(60, 20);

		this.add(new Label(name));
		this.add(figureForIsPublic);
	}

}
