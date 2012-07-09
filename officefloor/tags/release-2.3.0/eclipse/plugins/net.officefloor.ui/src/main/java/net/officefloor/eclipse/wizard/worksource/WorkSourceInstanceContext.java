/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.eclipse.wizard.worksource;

import net.officefloor.compile.work.WorkType;

/**
 * Context for a {@link WorkSourceInstance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WorkSourceInstanceContext {

	/**
	 * Specifies the title.
	 * 
	 * @param title
	 *            Title.
	 */
	void setTitle(String title);

	/**
	 * Specifies an error message.
	 * 
	 * @param message
	 *            Error message or <code>null</code> to indicate no error.
	 */
	void setErrorMessage(String message);

	/**
	 * Flags if {@link WorkType} is loaded.
	 * 
	 * @param isWorkTypeLoade
	 *            <code>true</code> {@link WorkType} loaded.
	 */
	void setWorkTypeLoaded(boolean isWorkTypeLoaded);

}