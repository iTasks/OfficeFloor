/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Listens to the open/close of the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorListener {

	/**
	 * Notifies that the {@link OfficeFloor} has been opened.
	 * 
	 * @param event
	 *            {@link OfficeFloorEvent}.
	 * @throws Exception
	 *             If fails to handle open listen logic.
	 */
	void officeFloorOpened(OfficeFloorEvent event) throws Exception;

	/**
	 * Notifies that the {@link OfficeFloor} has been closed.
	 * 
	 * @param event
	 *            {@link OfficeFloorEvent}.
	 * @throws Exception
	 *             If fails to handle close listen logic.
	 */
	void officeFloorClosed(OfficeFloorEvent event) throws Exception;

}