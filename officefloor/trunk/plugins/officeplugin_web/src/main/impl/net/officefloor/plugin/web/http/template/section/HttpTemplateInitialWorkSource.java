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
package net.officefloor.plugin.web.http.template.section;

import java.io.IOException;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationDifferentiatorImpl;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.template.HttpTemplateWorkSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateInitialTask.Dependencies;
import net.officefloor.plugin.web.http.template.section.HttpTemplateInitialTask.Flows;

/**
 * {@link WorkSource} to provide the {@link HttpTemplateInitialTask}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateInitialWorkSource extends
		AbstractWorkSource<HttpTemplateInitialTask> {

	/**
	 * Property name for the {@link HttpTemplate} URI path.
	 */
	public static final String PROPERTY_TEMPLATE_URI = HttpTemplateWorkSource.PROPERTY_TEMPLATE_URI;

	/**
	 * Name of the {@link HttpTemplateInitialTask}.
	 */
	public static final String TASK_NAME = "TASK";

	/*
	 * ======================= WorkSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_TEMPLATE_URI, "URI Path");
	}

	@Override
	public void sourceWork(
			WorkTypeBuilder<HttpTemplateInitialTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the template URI path
		String templateUriPath = HttpTemplateWorkSource
				.getHttpTemplateUrlContinuationPath(context);

		// Determine if the template is secure
		boolean isSecure = HttpTemplateWorkSource.isHttpTemplateSecure(context);

		/*
		 * Only trigger redirect if not secure. If sent on secure connection but
		 * no need for secure, service anyway. This is to save establishing a
		 * new connection and a round trip when already have the request.
		 */
		Boolean isRequireSecure = (isSecure ? Boolean.TRUE : null);

		// Create the HTTP Template initial task
		HttpTemplateInitialTask factory = new HttpTemplateInitialTask(
				templateUriPath, isSecure);

		// Configure the task
		workTypeBuilder.setWorkFactory(factory);
		TaskTypeBuilder<Dependencies, Flows> task = workTypeBuilder
				.addTaskType("TASK", factory, Dependencies.class, Flows.class);
		task.addObject(ServerHttpConnection.class).setKey(
				Dependencies.SERVER_HTTP_CONNECTION);
		task.addObject(HttpApplicationLocation.class).setKey(
				Dependencies.HTTP_APPLICATION_LOCATION);
		task.addObject(HttpRequestState.class).setKey(
				Dependencies.REQUEST_STATE);
		task.addObject(HttpSession.class).setKey(Dependencies.HTTP_SESSION);
		task.addFlow().setKey(Flows.RENDER);
		task.addEscalation(IOException.class);
		task.setDifferentiator(new HttpUrlContinuationDifferentiatorImpl(
				templateUriPath, isRequireSecure));
	}

}