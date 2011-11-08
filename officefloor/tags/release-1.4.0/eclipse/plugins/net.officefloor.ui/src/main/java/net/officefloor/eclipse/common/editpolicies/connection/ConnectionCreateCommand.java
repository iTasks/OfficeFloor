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

package net.officefloor.eclipse.common.editpolicies.connection;

import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.commands.Command;

/**
 * {@link Command} to make the source of a {@link ConnectionEditPart} available
 * to create a connection.
 * 
 * @author Daniel Sagenschneider
 */
public class ConnectionCreateCommand extends Command {

	/**
	 * Source of the connection.
	 */
	private final Object source;

	/**
	 * Initiate with source of connection.
	 * 
	 * @param source
	 *            Source of connection.
	 */
	public ConnectionCreateCommand(Object source) {
		this.source = source;
	}

	/**
	 * Obtains the source of the connection.
	 * 
	 * @return Source of the connection.
	 */
	public Object getSource() {
		return this.source;
	}

}