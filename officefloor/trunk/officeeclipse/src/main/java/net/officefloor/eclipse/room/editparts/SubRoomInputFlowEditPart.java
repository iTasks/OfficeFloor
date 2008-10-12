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
package net.officefloor.eclipse.room.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.eclipse.OfficeFloorPlugin;
import net.officefloor.eclipse.common.commands.OfficeFloorCommand;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.skin.room.SubRoomInputFlowFigure;
import net.officefloor.eclipse.skin.room.SubRoomInputFlowFigureContext;
import net.officefloor.model.room.SubRoomInputFlowModel;
import net.officefloor.model.room.SubRoomInputFlowModel.SubRoomInputFlowEvent;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.room.SubRoomInputFlowModel}.
 * 
 * @author Daniel
 */
public class SubRoomInputFlowEditPart
		extends
		AbstractOfficeFloorNodeEditPart<SubRoomInputFlowModel, SubRoomInputFlowFigure>
		implements SubRoomInputFlowFigureContext {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart
	 * #populateConnectionSourceModels(java.util.List)
	 */
	@Override
	protected void populateConnectionSourceModels(List<Object> models) {
		// Not a source
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart
	 * #populateConnectionTargetModels(java.util.List)
	 */
	@Override
	protected void populateConnectionTargetModels(List<Object> models) {
		models.addAll(this.getCastedModel().getOutputs());
		models.addAll(this.getCastedModel().getEscalations());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * populatePropertyChangeHandlers(java.util.List)
	 */
	@Override
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<SubRoomInputFlowEvent>(
				SubRoomInputFlowEvent.values()) {
			@Override
			protected void handlePropertyChange(SubRoomInputFlowEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case CHANGE_IS_PUBLIC:
					SubRoomInputFlowEditPart.this.getOfficeFloorFigure()
							.setIsPublic(
									SubRoomInputFlowEditPart.this
											.getCastedModel().getIsPublic());
					break;
				case ADD_OUTPUT:
				case REMOVE_OUTPUT:
				case ADD_ESCALATION:
				case REMOVE_ESCALATION:
					SubRoomInputFlowEditPart.this.refreshTargetConnections();
					break;
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#
	 * createOfficeFloorFigure()
	 */
	@Override
	protected SubRoomInputFlowFigure createOfficeFloorFigure() {
		return OfficeFloorPlugin.getSkin().getRoomFigureFactory()
				.createSubRoomInputFlowFigure(this);
	}

	/*
	 * ================= SubRoomInputFlowFigureContext =====================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.eclipse.skin.room.SubRoomInputFlowFigureContext#
	 * getSubRoomInputFlowName()
	 */
	@Override
	public String getSubRoomInputFlowName() {
		return this.getCastedModel().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.room.SubRoomInputFlowFigureContext#isPublic
	 * ()
	 */
	@Override
	public boolean isPublic() {
		return this.getCastedModel().getIsPublic();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.room.SubRoomInputFlowFigureContext#setIsPublic
	 * (boolean)
	 */
	@Override
	public void setIsPublic(final boolean isPublic) {

		// Maintain current is public
		final boolean currentIsPublic = this.getCastedModel().getIsPublic();

		// Make change
		this.executeCommand(new OfficeFloorCommand() {

			@Override
			protected void doCommand() {
				SubRoomInputFlowEditPart.this.getCastedModel().setIsPublic(
						isPublic);
			}

			@Override
			protected void undoCommand() {
				SubRoomInputFlowEditPart.this.getCastedModel().setIsPublic(
						currentIsPublic);
			}
		});
	}

}
