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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.manage.Office;

/**
 * Clock for the {@link Office}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeClock {

	/**
	 * <p>
	 * Obtains the approximate current time.
	 * <p>
	 * This is more efficient means to obtain {@link System#currentTimeMillis()}
	 * as complete millisecond accuracy is not required.
	 * 
	 * @return Approximate {@link System#currentTimeMillis()}.
	 */
	long currentTimeMillis();

}