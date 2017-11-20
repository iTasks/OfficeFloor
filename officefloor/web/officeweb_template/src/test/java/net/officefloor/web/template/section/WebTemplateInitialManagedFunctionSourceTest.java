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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.template.section.WebTemplateInitialFunction.Flows;
import net.officefloor.web.template.section.WebTemplateInitialFunction.WebTemplateInitialDependencies;

/**
 * Tests the {@link WebTemplateInitialManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateInitialManagedFunctionSourceTest extends OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedFunctionLoaderUtil.validateSpecification(WebTemplateInitialManagedFunctionSource.class);
	}

	/**
	 * Validate the non-secure type by default.
	 */
	public void testDefaultNonSecureType() {
		this.doTypeTest(null, null, "/path", "/path");
	}

	/**
	 * Validate the non-secure type.
	 */
	public void testNonSecureType() {
		this.doTypeTest(Boolean.FALSE, null, "/path", "/path");
	}

	/**
	 * Validate the secure type.
	 */
	public void testSecureType() {
		this.doTypeTest(Boolean.TRUE, Boolean.TRUE, "/path", "/path");
	}

	/**
	 * Validates uses canonical path for URL continuation.
	 */
	public void testNonCanonicalPathType() {
		this.doTypeTest(null, null, "configured/../non/../canoncial/../path", "/path");
	}

	/**
	 * Validate root template type.
	 */
	public void testRootTemplateType() {
		this.doTypeTest(null, null, "/", "/");
	}

	/**
	 * Undertakes the validating the type.
	 * 
	 * @param isSecure
	 *            Whether template should be secure.
	 */
	private void doTypeTest(Boolean isConfiguredSecure, Boolean isUrlContinuationSecure, String configuredUriPath,
			String expectedUrlContinuationPath) {

		// Factory
		WebTemplateInitialFunction factory = new WebTemplateInitialFunction(false, null, null, null);

		// Create the expected type
		FunctionNamespaceBuilder type = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();

		// Initial task
		ManagedFunctionTypeBuilder<WebTemplateInitialDependencies, Flows> initial = type.addManagedFunctionType("TASK",
				factory, WebTemplateInitialDependencies.class, Flows.class);
		initial.addObject(ServerHttpConnection.class).setKey(WebTemplateInitialDependencies.SERVER_HTTP_CONNECTION);
		initial.addFlow().setKey(Flows.RENDER);
		initial.addEscalation(IOException.class);

		// Create the listing of properties
		List<String> properties = new ArrayList<String>(6);
		if (isConfiguredSecure != null) {
			properties.addAll(Arrays.asList(WebTemplateSectionSource.PROPERTY_TEMPLATE_SECURE,
					String.valueOf(isConfiguredSecure)));
		}

		// Validate type (must also convert
		FunctionNamespaceType namespace = ManagedFunctionLoaderUtil.validateManagedFunctionType(type,
				WebTemplateInitialManagedFunctionSource.class, properties.toArray(new String[properties.size()]));

		// Ensure correct URI path
		ManagedFunctionType<?, ?> function = namespace.getManagedFunctionTypes()[0];
	}

	/**
	 * Ensure service as not required to be secure.
	 */
	public void testNonSecureService() {
		this.doServiceTest(false, false, "GET", null, null, null, null);
	}

	/**
	 * <p>
	 * Ensure service secure anyway if not require secure connection.
	 * <p>
	 * No need to redirect and establish a new connection to down grade secure
	 * when already received the request.
	 */
	public void testIgnoreSecureService() {
		this.doServiceTest(false, false, "GET", null, null, null, null);
	}

	/**
	 * Ensure service as appropriately secure.
	 */
	public void testSecureService() {
		this.doServiceTest(true, true, "GET", null, null, null, null);
	}

	/**
	 * Ensure redirect as not secure.
	 */
	public void testSecureRedirect() {
		this.doServiceTest(true, false, null, null, "/redirect", null, null);
	}

	/**
	 * Ensure follow POST/redirect/GET pattern even on secure connection to
	 * allow back button to work.
	 */
	public void testSecurePostRedirect() {
		this.doServiceTest(true, true, "POST", null, "/redirect", null, null);
	}

	/**
	 * Ensure follow POST/redirect/GET pattern to allow back button to work.
	 */
	public void testPostRedirect() {
		this.doServiceTest(false, false, "POST", null, "/redirect", null, null);
	}

	/**
	 * Ensure follow post/redirect/GET pattern to allow back button to work.
	 */
	public void testPostRedirectCaseInsensitive() {
		this.doServiceTest(false, false, "post", null, "/redirect", null, null);
	}

	/**
	 * Ensure follow PUT/redirect/GET pattern to allow back button to work.
	 */
	public void testPutRedirect() {
		this.doServiceTest(false, false, "PUT", null, "/redirect", null, null);
	}

	/**
	 * Ensure follow ALTERNATE/redirect/GET pattern to allow back button to
	 * work.
	 */
	public void testAlternateRedirect() {
		this.doServiceTest(false, false, "OTHER", "POST, OTHER", "/redirect", null, null);
	}

	/**
	 * Ensure follow ALTERNATE/redirect/GET pattern to allow back button to
	 * work.
	 */
	public void testAlternateRedirectCaseInsensitive() {
		this.doServiceTest(false, false, "other", "Post, Other", "/redirect", null, null);
	}

	/**
	 * Ensure able to specify the Content-Type and {@link Charset}.
	 */
	public void testContentTypeAndCharset() {
		this.doServiceTest(false, false, "GET", null, null, "text/plain", Charset.defaultCharset());
	}

	/**
	 * Undertake service test.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void doServiceTest(boolean isRequireSecure, boolean isConnectionSecure, String method,
			String redirectMethods, String redirectUriPath, String contentType, Charset charset) {
		try {

			final ManagedFunctionContext context = this.createMock(ManagedFunctionContext.class);
			final ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);

			// Create the task
			List<String> properties = new ArrayList<String>(6);
			if (isRequireSecure) {
				properties.addAll(Arrays.asList(WebTemplateSectionSource.PROPERTY_TEMPLATE_SECURE,
						String.valueOf(isRequireSecure)));
			}
			if (redirectMethods != null) {
				properties.addAll(
						Arrays.asList(WebTemplateSectionSource.PROPERTY_NOT_REDIRECT_HTTP_METHODS, redirectMethods));
			}
			if (contentType != null) {
				properties.addAll(Arrays.asList(WebTemplateSectionSource.PROPERTY_CONTENT_TYPE, contentType));
			}
			if (charset != null) {
				properties.addAll(Arrays.asList(WebTemplateSectionSource.PROPERTY_CHARSET, charset.name()));
			}
			FunctionNamespaceType namespace = ManagedFunctionLoaderUtil.loadManagedFunctionType(
					WebTemplateInitialManagedFunctionSource.class, properties.toArray(new String[properties.size()]));
			ManagedFunction<?, ?> function = namespace.getManagedFunctionTypes()[0].getManagedFunctionFactory()
					.createManagedFunction();

			// Record obtaining the dependencies
			this.recordReturn(context, context.getObject(WebTemplateInitialDependencies.SERVER_HTTP_CONNECTION),
					connection);

			// Record determining if secure connection
			if (isRequireSecure) {
				this.recordReturn(connection, connection.isSecure(), isConnectionSecure);
			}

			// Record determining method for POST, redirect, GET pattern
			if (method != null) {
				this.recordReturn(connection, connection.getClientRequest(), method);
			}

			// Record redirect or render
			if (redirectUriPath != null) {
				// Record necessary redirect
				// TODO record redirect
			} else {
				// Record triggering the render
				context.doFlow(Flows.RENDER, null, null);
			}

			// Record content type and charset
			final HttpResponse response = this.createMock(HttpResponse.class);
			if (contentType != null) {
				this.recordReturn(connection, connection.getResponse(), response);
				response.setContentType(contentType, charset);
			}

			// Test
			this.replayMockObjects();
			function.execute(context);
			this.verifyMockObjects();

		} catch (Throwable ex) {
			fail(ex);
		}
	}

}