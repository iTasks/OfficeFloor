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
package net.officefloor.plugin.web.http.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.ManagedFunctionSourceService;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.source.SourceProperties;
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationManagedFunctionSource;
import net.officefloor.plugin.web.http.template.parse.BeanHttpTemplateSectionContent;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateParserImpl;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateSection;
import net.officefloor.plugin.web.http.template.parse.HttpTemplateSectionContent;
import net.officefloor.plugin.web.http.template.parse.LinkHttpTemplateSectionContent;
import net.officefloor.plugin.web.http.template.parse.PropertyHttpTemplateSectionContent;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.route.InvalidRequestUriHttpException;

/**
 * {@link ManagedFunctionSource} for the HTTP template.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateManagedFunctionSource extends AbstractManagedFunctionSource
		implements ManagedFunctionSourceService<HttpTemplateManagedFunctionSource> {

	/**
	 * Property to specify the {@link HttpTemplate} file.
	 */
	public static final String PROPERTY_TEMPLATE_FILE = "template.path";

	/**
	 * Property to obtain the raw {@link HttpTemplate} content. This is tried if
	 * there is no template path.
	 */
	public static final String PROPERTY_TEMPLATE_CONTENT = "template.content";

	/**
	 * Property to obtain the {@link HttpTemplate} URI path.
	 */
	public static final String PROPERTY_TEMPLATE_URI = "template.uri";

	/**
	 * Property to obtain the {@link HttpTemplate} URI suffix on the template
	 * URI path and various link URI paths.
	 */
	public static final String PROPERTY_TEMPLATE_URI_SUFFIX = "template.uri.suffix";

	/**
	 * Property to indicate if the {@link HttpTemplate} requires a secure
	 * {@link ServerHttpConnection}.
	 */
	public static final String PROPERTY_TEMPLATE_SECURE = "template.secure";

	/**
	 * Property to specify the {@link Charset} for reading in the template.
	 */
	public static final String PROPERTY_TEMPLATE_FILE_CHARSET = "template.file.charset";

	/**
	 * Property prefix to obtain whether a specific link requires a secure
	 * {@link ServerHttpConnection}.
	 */
	public static final String PROPERTY_LINK_SECURE_PREFIX = HttpTemplateFunction.PROPERTY_LINK_SECURE_PREFIX;

	/**
	 * Property to specify the {@link Charset} for outputting the template.
	 */
	public static final String PROPERTY_CHARSET = "http.template.charset";

	/**
	 * Property prefix to obtain the bean for the {@link HttpTemplateSection}.
	 */
	public static final String PROPERTY_BEAN_PREFIX = HttpTemplateFunction.PROPERTY_BEAN_PREFIX;

	/**
	 * Obtains the {@link HttpTemplate}.
	 * 
	 * @param context
	 *            {@link SourceContext} providing details about the
	 *            {@link HttpTemplate}.
	 * @return {@link HttpTemplate}.
	 * @throws IOException
	 *             If fails to obtain the {@link HttpTemplate}.
	 */
	public static HttpTemplate getHttpTemplate(SourceContext context) throws IOException {

		// Obtain the template content
		Reader content = getHttpTemplateContent(context);

		// Obtain the template
		HttpTemplate template = getHttpTemplate(content);

		// Template content read
		content.close();

		// Return the template
		return template;
	}

	/**
	 * Obtains the raw {@link HttpTemplate} content.
	 * 
	 * @param context
	 *            {@link SourceContext} providing details about the
	 *            {@link HttpTemplate}.
	 * @return Raw {@link HttpTemplate} content.
	 * @throws IOException
	 *             If fails to obtain the raw {@link HttpTemplate} content.
	 */
	public static Reader getHttpTemplateContent(SourceContext context) throws IOException {

		// Determine if content provided by property
		String templateContent = context.getProperty(PROPERTY_TEMPLATE_CONTENT, null);
		if (templateContent != null) {
			// Provided template content
			return new StringReader(templateContent);
		}

		// Not in property, so obtain details from file
		String templateFilePath = context.getProperty(PROPERTY_TEMPLATE_FILE);
		String templateFileCharsetName = context.getProperty(PROPERTY_TEMPLATE_FILE_CHARSET, null);
		Charset templateFileCharset = null;
		if (templateFileCharsetName != null) {
			templateFileCharset = Charset.forName(templateFileCharsetName);
		}

		// Obtain the configuration
		InputStream configuration = context.getResource(templateFilePath);

		// Return the reader to the template content
		return (templateFileCharset == null ? new InputStreamReader(configuration)
				: new InputStreamReader(configuration, templateFileCharset));
	}

	/**
	 * Obtains the {@link HttpTemplate}.
	 * 
	 * @param templateContent
	 *            Raw {@link HttpTemplate} content.
	 * @return {@link HttpTemplate}.
	 * @throws IOException
	 *             If fails to obtain the {@link HttpTemplate}.
	 */
	public static HttpTemplate getHttpTemplate(Reader templateContent) throws IOException {

		// Parse the template
		HttpTemplate template = new HttpTemplateParserImpl(templateContent).parse();

		// Return the template
		return template;
	}

	/**
	 * Determines if the {@link HttpTemplateSection} requires a bean.
	 * 
	 * @param section
	 *            {@link HttpTemplateSection}.
	 * @return <code>true</code> if the {@link HttpTemplateSection} requires a
	 *         bean.
	 */
	public static boolean isHttpTemplateSectionRequireBean(HttpTemplateSection section) {

		// Determine if contains reference content
		for (HttpTemplateSectionContent content : section.getContent()) {
			if ((content instanceof PropertyHttpTemplateSectionContent)
					|| (content instanceof BeanHttpTemplateSectionContent)) {
				// Section contains property/bean reference, so requires bean
				return true;
			}
		}

		// No reference to property/bean, so does not require bean
		return false;
	}

	/**
	 * Obtains the {@link HttpTemplate} URL continuation path.
	 * 
	 * @param properties
	 *            {@link SourceProperties}.
	 * @return URL continuation path for the {@link HttpTemplate}.
	 * @throws InvalidRequestUriHttpException
	 *             Should the configured {@link HttpTemplate} path be invalid.
	 */
	public static String getHttpTemplateUrlContinuationPath(SourceProperties properties)
			throws InvalidRequestUriHttpException {

		// Obtain the URI path and URI suffix for the template
		String templateUriPath = properties.getProperty(PROPERTY_TEMPLATE_URI);

		// Provide suffix only if NOT root template
		String templateUriSuffix = "";
		if (!("/".equals(templateUriPath))) {
			templateUriSuffix = properties.getProperty(PROPERTY_TEMPLATE_URI_SUFFIX, "");
		}

		// Return path ready for HTTP URL continuation
		return HttpUrlContinuationManagedFunctionSource.getApplicationUriPath(templateUriPath + templateUriSuffix);
	}

	/**
	 * Obtains the {@link HttpTemplate} link URL continuation path.
	 * 
	 * @param templateUriPath
	 *            {@link HttpTemplate} URI path.
	 * @param linkName
	 *            Name of the link.
	 * @param templateUriSuffix
	 *            {@link HttpTemplate} URI suffix. May be <code>null</code> for
	 *            no suffix.
	 * @return {@link HttpTemplate} link URI path.
	 * @throws InvalidRequestUriHttpException
	 *             Should the resulting URI be invalid.
	 */
	public static String getHttpTemplateLinkUrlContinuationPath(String templateUriPath, String linkName,
			String templateUriSuffix) throws InvalidRequestUriHttpException {

		// Create the link URI path
		String linkUriPath = templateUriPath + "-" + linkName + (templateUriSuffix == null ? "" : templateUriSuffix);
		linkUriPath = HttpUrlContinuationManagedFunctionSource.getApplicationUriPath(linkUriPath);

		// Return the link URI path
		return linkUriPath;
	}

	/**
	 * Determines if the {@link HttpTemplate} is secure.
	 * 
	 * @param properties
	 *            {@link SourceProperties}.
	 * @return <code>true</code> should the {@link HttpTemplate} be secure.
	 */
	public static boolean isHttpTemplateSecure(SourceProperties properties) {

		// Determine if template is secure
		boolean isTemplateSecure = Boolean
				.valueOf(properties.getProperty(PROPERTY_TEMPLATE_SECURE, String.valueOf(false)));

		// Return whether secure
		return isTemplateSecure;
	}

	/**
	 * Obtains the link names for the {@link HttpTemplate}.
	 * 
	 * @param template
	 *            {@link HttpTemplate}.
	 * @return Link names.
	 */
	public static String[] getHttpTemplateLinkNames(HttpTemplate template) {

		// Obtain the listing of link names
		List<String> linkNames = new LinkedList<String>();
		for (HttpTemplateSection section : template.getSections()) {
			loadLinkNames(section.getContent(), linkNames);
		}

		// Return the link names
		return linkNames.toArray(new String[linkNames.size()]);
	}

	/**
	 * Loads the link names from the {@link HttpTemplateSectionContent}
	 * instances.
	 * 
	 * @param contents
	 *            {@link HttpTemplateSectionContent} instances.
	 * @param linkNames
	 *            {@link List} to receive the unique link names.
	 */
	private static void loadLinkNames(HttpTemplateSectionContent[] contents, List<String> linkNames) {

		// Interrogate contents for links
		for (HttpTemplateSectionContent content : contents) {

			// Add the link
			if (content instanceof LinkHttpTemplateSectionContent) {
				LinkHttpTemplateSectionContent link = (LinkHttpTemplateSectionContent) content;

				// Obtain the link name
				String linkName = link.getName();

				// Add the link name
				if (!linkNames.contains(linkName)) {
					linkNames.add(linkName);
				}
			}

			// Recursively check bean content for links
			if (content instanceof BeanHttpTemplateSectionContent) {
				BeanHttpTemplateSectionContent beanContent = (BeanHttpTemplateSectionContent) content;
				loadLinkNames(beanContent.getContent(), linkNames);
			}
		}
	}

	/*
	 * ================== ManagedFunctionSourceService ==================
	 */

	@Override
	public String getManagedFunctionSourceAlias() {
		return "HTTP_TEMPLATE";
	}

	@Override
	public Class<HttpTemplateManagedFunctionSource> getManagedFunctionSourceClass() {
		return HttpTemplateManagedFunctionSource.class;
	}

	/*
	 * =================== AbstractWorkSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_TEMPLATE_FILE, "Template");
		context.addProperty(PROPERTY_TEMPLATE_URI, "URI Path");
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Obtain the template
		HttpTemplate template = getHttpTemplate(context);

		// Obtain the details of the template
		Charset charset = null;
		String charsetName = context.getProperty(PROPERTY_CHARSET, null);
		if (!CompileUtil.isBlank(charsetName)) {
			charset = Charset.forName(charsetName);
		}

		// Obtain the URI path and URI suffix for the template
		String templateUriPath = context.getProperty(PROPERTY_TEMPLATE_URI);
		String templateUriSuffix = context.getProperty(PROPERTY_TEMPLATE_URI_SUFFIX, null);

		// Obtain whether the template is secure
		boolean isTemplateSecure = isHttpTemplateSecure(context);

		// Define the functions
		for (HttpTemplateSection section : template.getSections()) {

			// Load the function to write the section
			HttpTemplateFunction.loadFunctionType(section, charset, templateUriPath, templateUriSuffix,
					isTemplateSecure, namespaceTypeBuilder, context);
		}
	}

}