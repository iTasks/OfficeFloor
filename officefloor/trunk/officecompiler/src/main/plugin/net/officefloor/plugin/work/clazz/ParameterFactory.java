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
package net.officefloor.plugin.work.clazz;

import net.officefloor.frame.api.execute.TaskContext;

/**
 * Creates the parameter for the {@link ClassTask}.
 * 
 * @author Daniel
 */
public interface ParameterFactory {

	/**
	 * Creates the parameter from the {@link TaskContext}.
	 * 
	 * @param context
	 *            {@link TaskContext}.
	 * @return Parameter.
	 * @throws Exception
	 *             If fails to create the parameter.
	 */
	Object createParameter(TaskContext<?, ?, ?> context) throws Exception;

}