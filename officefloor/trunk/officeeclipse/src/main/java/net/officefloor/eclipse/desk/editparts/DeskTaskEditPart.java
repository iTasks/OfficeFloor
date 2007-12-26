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
package net.officefloor.eclipse.desk.editparts;

import java.beans.PropertyChangeEvent;
import java.util.List;

import net.officefloor.desk.TaskToFlowItemSynchroniser;
import net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart;
import net.officefloor.eclipse.common.editparts.ButtonEditPart;
import net.officefloor.eclipse.common.editparts.PropertyChangeHandler;
import net.officefloor.eclipse.common.editpolicies.ConnectionModelFactory;
import net.officefloor.eclipse.desk.figure.DeskTaskFigure;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.desk.DeskModel;
import net.officefloor.model.desk.DeskTaskModel;
import net.officefloor.model.desk.DeskTaskToFlowItemModel;
import net.officefloor.model.desk.DeskWorkModel;
import net.officefloor.model.desk.FlowItemModel;
import net.officefloor.model.desk.DeskTaskModel.DeskTaskEvent;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.requests.CreateConnectionRequest;

/**
 * {@link org.eclipse.gef.EditPart} for the
 * {@link net.officefloor.model.desk.DeskTaskModel}.
 * 
 * @author Daniel
 */
public class DeskTaskEditPart extends
		AbstractOfficeFloorSourceNodeEditPart<DeskTaskModel> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
	 */
	protected IFigure createFigure() {
		// Button to add as flow item
		final ButtonEditPart addAsFlowItem = new ButtonEditPart(
				"Add as Flow Item") {
			protected void handleButtonClick() {

				// Obtain the work
				DeskWorkEditPart workEditPart = (DeskWorkEditPart) DeskTaskEditPart.this
						.getParent();
				DeskWorkModel work = workEditPart.getCastedModel();

				// Create the flow item for this task
				DeskTaskModel task = DeskTaskEditPart.this.getCastedModel();
				String flowItemName = work.getId() + "-" + task.getName();
				FlowItemModel flowItem = new FlowItemModel(flowItemName, false,
						work.getId(), task.getName(), task.getTask(), null,
						null, null, null, null, null);
				flowItem.setX(300);
				flowItem.setY(100);

				// Ensure synchronised to the task
				TaskToFlowItemSynchroniser.synchroniseTaskOntoFlowItem(task
						.getTask(), flowItem);

				// Obtain the desk
				// Note parent is work listing then desk
				DeskEditPart deskEditPart = (DeskEditPart) workEditPart
						.getParent().getParent();
				DeskModel desk = deskEditPart.getCastedModel();

				// Add the flow item to the desk
				desk.addFlowItem(flowItem);

				// Link the flow item with the task
				DeskTaskToFlowItemModel conn = new DeskTaskToFlowItemModel(
						flowItem, task);
				conn.connect();
			}
		};

		// Return the Desk Task figure
		return new DeskTaskFigure(this.getCastedModel().getName(),
				addAsFlowItem.getFigure());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populateModelChildren(java.util.List)
	 */
	protected void populateModelChildren(List<Object> childModels) {
		childModels.addAll(this.getCastedModel().getObjects());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorEditPart#populatePropertyChangeHandlers(java.util.List)
	 */
	protected void populatePropertyChangeHandlers(
			List<PropertyChangeHandler<?>> handlers) {
		handlers.add(new PropertyChangeHandler<DeskTaskEvent>(DeskTaskEvent
				.values()) {
			protected void handlePropertyChange(DeskTaskEvent property,
					PropertyChangeEvent evt) {
				switch (property) {
				case ADD_FLOW_ITEM:
				case REMOVE_FLOW_ITEM:
					DeskTaskEditPart.this.refreshSourceConnections();
					break;
				}
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart#createConnectionModelFactory()
	 */
	protected ConnectionModelFactory createConnectionModelFactory() {
		return new ConnectionModelFactory() {
			public ConnectionModel createConnection(Object source,
					Object target, CreateConnectionRequest request) {
				DeskTaskToFlowItemModel conn = new DeskTaskToFlowItemModel();
				conn.setTask((DeskTaskModel) source);
				conn.setFlowItem((FlowItemModel) target);
				conn.connect();
				return conn;
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorSourceNodeEditPart#populateConnectionTargetTypes(java.util.List)
	 */
	protected void populateConnectionTargetTypes(List<Class<?>> types) {
		types.add(FlowItemModel.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionSourceModels(java.util.List)
	 */
	protected void populateConnectionSourceModels(List<Object> models) {
		models.addAll(this.getCastedModel().getFlowItems());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.eclipse.common.editparts.AbstractOfficeFloorNodeEditPart#populateConnectionTargetModels(java.util.List)
	 */
	protected void populateConnectionTargetModels(List<Object> models) {
		// Not a target
	}

}
