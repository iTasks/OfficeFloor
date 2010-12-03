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

package net.officefloor.eclipse.common.commands;

import net.officefloor.model.change.Change;

/**
 * {@link OfficeFloorCommand} for a {@link Change}.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeCommand extends OfficeFloorCommand {

	/**
	 * {@link Change}.
	 */
	private Change<?> change;

	/**
	 * Initialise.
	 * 
	 * @param change
	 *            {@link Change}.
	 */
	public ChangeCommand(Change<?> change) {
		this.change = change;
	}

	/*
	 * ================== OfficeFloorCommand ===========================
	 */

	@Override
	protected void doCommand() {
		this.change.apply();
	}

	@Override
	protected void undoCommand() {
		this.change.revert();
	}

}