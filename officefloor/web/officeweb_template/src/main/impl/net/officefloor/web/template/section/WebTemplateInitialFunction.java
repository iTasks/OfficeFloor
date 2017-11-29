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
package net.officefloor.web.template.section;

import java.io.IOException;
import java.nio.charset.Charset;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.HttpInputPath;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.web.template.parse.ParsedTemplate;

/**
 * Initial {@link ManagedFunction} to ensure appropriate conditions for
 * rendering the {@link ParsedTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateInitialFunction extends
		StaticManagedFunction<WebTemplateInitialFunction.WebTemplateInitialDependencies, WebTemplateInitialFunction.Flows> {

	/**
	 * Keys for the {@link WebTemplateInitialFunction} dependencies.
	 */
	public static enum WebTemplateInitialDependencies {
		SERVER_HTTP_CONNECTION
	}

	/**
	 * Keys for the {@link WebTemplateInitialFunction} flows.
	 */
	public static enum Flows {
		REDIRECT, RENDER
	}

	/**
	 * Indicates if a secure {@link ServerHttpConnection} is required.
	 */
	private final boolean isRequireSecure;

	/**
	 * <code>Content-Type</code> for the {@link ParsedTemplate}. May be
	 * <code>null</code>.
	 */
	private final HttpHeaderValue contentType;

	/**
	 * {@link Charset} for the {@link ParsedTemplate}.
	 */
	private final Charset charset;

	/**
	 * {@link HttpInputPath} for the {@link WebTemplate}.
	 */
	private final HttpInputPath inputPath;

	/**
	 * {@link Character} to use to terminate the path.
	 */
	private final int terminatingPathCharacter;

	/**
	 * Initiate.
	 * 
	 * @param isRequireSecure
	 *            Indicates if a secure {@link ServerHttpConnection} is
	 *            required.
	 * @param contentType
	 *            Content-type for the {@link ParsedTemplate}. May be
	 *            <code>null</code>.
	 * @param charset
	 *            {@link Charset} for {@link ParsedTemplate}.
	 * @param inputPath
	 *            {@link HttpInputPath} for the {@link WebTemplate}.
	 * @param terminatingPathCharacter
	 *            {@link Character} to use to terminate the path.
	 */
	public WebTemplateInitialFunction(boolean isRequireSecure, String contentType, Charset charset,
			HttpInputPath inputPath, int terminatingPathCharacter) {
		this.isRequireSecure = isRequireSecure;
		this.contentType = contentType == null ? null : new HttpHeaderValue(contentType);
		this.charset = charset;
		this.inputPath = inputPath;
		this.terminatingPathCharacter = terminatingPathCharacter;
	}

	/*
	 * ======================= ManagedFunction ===============================
	 */

	@Override
	public Object execute(ManagedFunctionContext<WebTemplateInitialDependencies, Flows> context) throws IOException {

		// Obtain the dependencies
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(WebTemplateInitialDependencies.SERVER_HTTP_CONNECTION);

		// Flag indicating if redirect is required
		boolean isRedirectRequired = false;

		// Determine if requires a secure connection
		if (this.isRequireSecure) {

			/*
			 * Request may have come in on another URL continuation which did
			 * not require a secure connection and is to now to render this HTTP
			 * template. Therefore trigger redirect for a secure connection.
			 * 
			 * Note that do not down grade to non-secure connection as already
			 * have the request and no need to close the existing secure
			 * connection and establish a new non-secure connection.
			 */
			boolean isConnectionSecure = connection.isSecure();
			if (!isConnectionSecure) {
				// Flag redirect for secure connection
				isRedirectRequired = true;
			}
		}

		// Ensure URL for template is correct (otherwise redirect GET)
		if (!isRedirectRequired) {
			HttpRequest request = connection.getRequest();
			String uri = request.getUri();
			if (!(this.inputPath.isMatchPath(uri, this.terminatingPathCharacter))) {
				// Not matching template path, so redirect GET the template
				isRedirectRequired = true;
			}
		}

		// Undertake the redirect
		if (isRedirectRequired) {
			context.doFlow(Flows.REDIRECT, null, null);
			return null;
		}

		// Configure the response
		connection.getResponse().setContentType(this.contentType, this.charset);

		// Render the template
		context.doFlow(Flows.RENDER, null, null);
		return null;
	}

}