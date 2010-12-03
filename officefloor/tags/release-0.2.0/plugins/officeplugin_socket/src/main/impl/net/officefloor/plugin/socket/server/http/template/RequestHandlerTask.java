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
package net.officefloor.plugin.socket.server.http.template;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.template.parse.HttpTemplate;
import net.officefloor.plugin.socket.server.http.template.route.HttpTemplateRouteWorkSource;

/**
 * <p>
 * {@link Task} to handle request from {@link HttpTemplate}.
 * <p>
 * This {@link Task} does not actually do anything but is a place holder
 * {@link Task} to link processing of a request from the {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class RequestHandlerTask extends
		AbstractSingleTask<HttpTemplateWork, Indexed, None> {

	/**
	 * <p>
	 * Identifier class that is only dependency for {@link Task}.
	 * <p>
	 * This allows for the {@link HttpTemplateRouteWorkSource} to find these
	 * {@link RequestHandlerTask} instances within the {@link Office}.
	 */
	public static final class RequestHandlerIdentifier {
	}

	/*
	 * ================== Task =============================
	 */

	@Override
	public Object doTask(TaskContext<HttpTemplateWork, Indexed, None> context)
			throws Throwable {
		// Only a place holder task to start processing so do nothing
		return null;
	}

}