/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.eclipse.socket;

import org.eclipse.swt.widgets.Composite;

import net.officefloor.eclipse.extension.managedfunctionsource.FunctionDocumentationContext;
import net.officefloor.eclipse.extension.managedfunctionsource.ManagedFunctionSourceExtension;
import net.officefloor.eclipse.extension.managedfunctionsource.ManagedFunctionSourceExtensionContext;
import net.officefloor.eclipse.extension.util.SourceExtensionUtil;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.web.http.application.WebArchitect;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.source.HttpFileSenderManagedFunctionSource;
import net.officefloor.plugin.web.http.resource.source.SourceHttpResourceFactory;

/**
 * {@link ManagedFunctionSourceExtension} for
 * {@link HttpFileSenderManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileSenderManagedFunctionSourceExtension
		extends AbstractSocketManagedFunctionSourceExtension<HttpFileSenderManagedFunctionSource> {

	/**
	 * Initiate.
	 */
	public HttpFileSenderManagedFunctionSourceExtension() {
		super(HttpFileSenderManagedFunctionSource.class, "Send Http File");
	}

	/*
	 * =================== ManagedFunctionSourceExtension ===================
	 */

	@Override
	public void createControl(Composite page, ManagedFunctionSourceExtensionContext context) {

		// Provide properties
		SourceExtensionUtil.loadPropertyLayout(page);
		SourceExtensionUtil.createPropertyText("Package prefix", SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX,
				WebArchitect.WEB_PUBLIC_RESOURCES_CLASS_PATH_PREFIX, page, context, null);
		SourceExtensionUtil.createPropertyText("Directory index file name",
				SourceHttpResourceFactory.PROPERTY_DEFAULT_DIRECTORY_FILE_NAMES, "index.html", page, context, null);
		SourceExtensionUtil.createPropertyText("Resource not found content path",
				HttpFileSenderManagedFunctionSource.PROPERTY_NOT_FOUND_FILE_PATH, "FileNotFound.html", page, context,
				null);
	}

	@Override
	public String getFunctionDocumentation(FunctionDocumentationContext context) throws Throwable {

		// Should always only have the one task

		// Obtain the prefix
		String prefix = context.getPropertyList().getPropertyValue(SourceHttpResourceFactory.PROPERTY_CLASS_PATH_PREFIX,
				"<not specified>");

		// Return the documentation
		return "Sends the " + HttpFile.class.getSimpleName()
				+ " from the class path as specified on the request URI of the " + HttpRequest.class.getSimpleName()
				+ "\n\nIn finding the " + HttpFile.class.getSimpleName() + " the prefix '" + prefix
				+ "' is added to the " + HttpRequest.class.getSimpleName()
				+ " request URI to restrict access to full class path resources.";
	}

}