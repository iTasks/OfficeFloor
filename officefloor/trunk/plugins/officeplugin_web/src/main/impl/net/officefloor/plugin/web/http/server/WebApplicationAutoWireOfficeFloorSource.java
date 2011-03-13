/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.server;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.plugin.autowire.AutoWireOfficeFloorSource;
import net.officefloor.plugin.autowire.AutoWireSection;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.web.http.resource.source.ClasspathHttpFileSenderWorkSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateSectionSource;

/**
 * {@link AutoWireOfficeFloorSource} providing web application functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class WebApplicationAutoWireOfficeFloorSource extends
		AutoWireOfficeFloorSource {

	/**
	 * Name of the {@link OfficeSection} that handles the {@link HttpRequest}
	 * instances.
	 */
	public static final String HANDLER_SECTION_NAME = "HANDLE_HTTP_SECTION";

	/**
	 * Name of the {@link OfficeSectionInput} that handles the
	 * {@link HttpRequest} instances.
	 */
	public static final String HANDLER_INPUT_NAME = "HANDLE_HTTP_INPUT";

	/**
	 * {@link HttpTemplateAutoWireSection} instances.
	 */
	private final List<HttpTemplateAutoWireSection> httpTemplates = new LinkedList<HttpTemplateAutoWireSection>();

	/**
	 * {@link UriLink} instances.
	 */
	private final List<UriLink> uriLinks = new LinkedList<UriLink>();

	/**
	 * {@link SendLink} instances.
	 */
	private final List<SendLink> sendLinks = new LinkedList<SendLink>();

	/**
	 * Allows for overriding the {@link NonHandledServicer}. <code>null</code>
	 * indicates to use default.
	 */
	private NonHandledServicer nonHandledServicer = null;

	/**
	 * Adds a {@link HttpTemplate} available at the specified URI.
	 * 
	 * @param templatePath
	 *            Path to the template file.
	 * @param templateLogicClass
	 *            Class providing the logic for the template.
	 * @param templateUri
	 *            URI for the template. May be <code>null</code> indicate the
	 *            template not publicly available.
	 * @return {@link HttpTemplateAutoWireSection} to allow linking flows.
	 */
	public HttpTemplateAutoWireSection addHttpTemplate(String templatePath,
			Class<?> templateLogicClass, String templateUri) {

		// Determine section name
		String sectionName;
		if (templateUri != null) {

			// Ensure URI is not already registered
			for (HttpTemplateAutoWireSection template : this.httpTemplates) {
				if (templateUri.equals(template.getTemplateUri())) {
					throw new IllegalStateException(
							"HTTP Template already added for URI '"
									+ templateUri + "'");
				}
			}

			// Specify section name for public template
			sectionName = templateUri;

		} else {
			// Private template so provide private section name
			sectionName = "resource" + this.httpTemplates.size();
		}

		// Add the HTTP template section
		AutoWireSection section = this.addSection(sectionName,
				HttpTemplateSectionSource.class, templatePath);
		section.addProperty(HttpTemplateSectionSource.PROPERTY_CLASS_NAME,
				templateLogicClass.getName());

		// Create and register the HTTP template
		HttpTemplateAutoWireSection wirer = new HttpTemplateAutoWireSection(
				this.getOfficeFloorCompiler(), section, templateLogicClass,
				templateUri);
		this.httpTemplates.add(wirer);

		// Return the wirer
		return wirer;
	}

	/**
	 * <p>
	 * Adds a private {@link HttpTemplate}.
	 * <p>
	 * The {@link HttpTemplate} is not directly available via URI but is linked
	 * by flows. This allows pre-processing before the {@link HttpTemplate} is
	 * attempted to be rendered.
	 * 
	 * @param templatePath
	 *            Path to the template file.
	 * @param templateLogicClass
	 *            Class providing the logic for the template.
	 * @return {@link HttpTemplateAutoWireSection} to allow linking flows.
	 */
	public HttpTemplateAutoWireSection addHttpTemplate(String templatePath,
			Class<?> templateLogicClass) {
		return this.addHttpTemplate(templatePath, templateLogicClass, null);
	}

	/**
	 * Links a URI to an {@link OfficeSectionInput}.
	 * 
	 * @param uri
	 *            URI to be linked.
	 * @param section
	 *            {@link AutoWireSection} servicing the URI.
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput} servicing the URI.
	 */
	public void linkUri(String uri, AutoWireSection section, String inputName) {
		this.uriLinks.add(new UriLink(uri, section, inputName));
	}

	/**
	 * Links the {@link OfficeSectionOutput} to render the {@link HttpTemplate}.
	 * 
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param outputName
	 *            Name of the {@link OfficeSectionOutput}.
	 * @param template
	 *            {@link HttpTemplateAutoWireSection}.
	 */
	public void linkToHttpTemplate(AutoWireSection section, String outputName,
			HttpTemplateAutoWireSection template) {
		this.link(section, outputName, template,
				HttpTemplateSectionSource.RENDER_TEMPLATE_INPUT_NAME);
	}

	/**
	 * Links {@link OfficeSectionOutput} to sending the {@link HttpResponse}.
	 * 
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param outputName
	 *            Name of the {@link OfficeSectionOutput}.
	 */
	public void linkToSendResponse(AutoWireSection section, String outputName) {
		this.sendLinks.add(new SendLink(section, outputName));
	}

	/**
	 * Specifies the {@link OfficeSectionInput} to handle if unable to route
	 * {@link HttpRequest}.
	 * 
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param inputName
	 *            Name of the {@link OfficeSectionInput}.
	 */
	public void setNonHandledServicer(AutoWireSection section, String inputName) {
		this.nonHandledServicer = new NonHandledServicer(section, inputName);
	}

	/**
	 * Obtains the registered URIs.
	 * 
	 * @return Registered URIs.
	 */
	public String[] getURIs() {

		// Create the set of URIs
		Set<String> uris = new HashSet<String>();

		// Add HTTP template URIs
		for (HttpTemplateAutoWireSection httpTemplate : this.httpTemplates) {
			uris.add(httpTemplate.getTemplateUri());
		}

		// Add link URIs
		for (UriLink link : this.uriLinks) {
			uris.add(link.uri);
		}

		// Return the URIs
		return uris.toArray(new String[uris.size()]);
	}

	/*
	 * ===================== AutoWireOfficeFloorSource =======================
	 */

	@Override
	protected void initOfficeFloor(OfficeFloorDeployer deployer,
			OfficeFloorSourceContext context) throws Exception {

		// Add the HTTP section
		AutoWireSection httpSection = this.addSection(HANDLER_SECTION_NAME,
				WebApplicationSectionSource.class, null);

		// Provide the non-handled servicer
		if (this.nonHandledServicer != null) {
			// Use overridden servicer
			this.link(httpSection,
					WebApplicationSectionSource.UNHANDLED_REQUEST_OUTPUT_NAME,
					this.nonHandledServicer.section,
					this.nonHandledServicer.inputName);

		} else {
			// Use default non-handled servicer (file sending)
			AutoWireSection nonHandledServicer = this.addSection(
					"NON_HANDLED_SERVICER", HttpFileSenderSectionSource.class,
					null);
			this.addProperty(
					nonHandledServicer,
					context,
					ClasspathHttpFileSenderWorkSource.PROPERTY_CLASSPATH_PREFIX,
					"PUBLIC");
			this.addProperty(
					nonHandledServicer,
					context,
					ClasspathHttpFileSenderWorkSource.PROPERTY_DEFAULT_FILE_NAME,
					"index.html");
			this.link(httpSection,
					WebApplicationSectionSource.UNHANDLED_REQUEST_OUTPUT_NAME,
					nonHandledServicer,
					HttpFileSenderSectionSource.SERVICE_INPUT_NAME);
			this.linkToSendResponse(nonHandledServicer,
					HttpFileSenderSectionSource.FILE_SENT_OUTPUT_NAME);
		}

		// Link URI's
		for (UriLink link : this.uriLinks) {
			// Register the URI link
			WebApplicationSectionSource.linkRouteToSection(link.uri,
					link.section, link.inputName, httpSection, this);
		}

		// Link template rendering
		for (HttpTemplateAutoWireSection section : this.httpTemplates) {

			// Register the HTTP template for routing
			WebApplicationSectionSource.linkRouteToHttpTemplate(section,
					httpSection, this);

			// Link completion of template rendering (if not already linked)
			if (!this.isLinked(section,
					HttpTemplateSectionSource.ON_COMPLETION_OUTPUT_NAME)) {
				// Not linked, so link to sending HTTP response
				this.linkToSendResponse(section,
						HttpTemplateSectionSource.ON_COMPLETION_OUTPUT_NAME);
			}
		}

		// Link sending the response
		for (SendLink link : this.sendLinks) {
			this.link(link.section, link.outputName, httpSection,
					WebApplicationSectionSource.SEND_RESPONSE_INPUT_NAME);
		}
	}

	/**
	 * Adds the property.
	 * 
	 * @param section
	 *            {@link AutoWireSection}.
	 * @param context
	 *            {@link OfficeFloorSourceContext}.
	 * @param propertyName
	 *            Name of the property.
	 * @param defaultValue
	 *            Default value for the property.
	 */
	private void addProperty(AutoWireSection section,
			OfficeFloorSourceContext context, String propertyName,
			String defaultValue) {
		section.addProperty(propertyName,
				context.getProperty(propertyName, defaultValue));
	}

	/**
	 * URI link.
	 */
	private static class UriLink {

		/**
		 * URI.
		 */
		public final String uri;

		/**
		 * {@link AutoWireSection} to handle the URI.
		 */
		public final AutoWireSection section;

		/**
		 * Name {@link SectionInput} to handle the URI.
		 */
		public final String inputName;

		/**
		 * Initiate.
		 * 
		 * @param uri
		 *            URI.
		 * @param section
		 *            {@link AutoWireSection} to handle the URI.
		 * @param inputName
		 *            Name {@link SectionInput} to handle the URI.
		 */
		public UriLink(String uri, AutoWireSection section, String inputName) {
			this.uri = uri;
			this.section = section;
			this.inputName = inputName;
		}
	}

	/**
	 * Send link.
	 */
	private static class SendLink {

		/**
		 * {@link AutoWireSection}.
		 */
		public final AutoWireSection section;

		/**
		 * Name of the {@link SectionOutput}.
		 */
		public final String outputName;

		/**
		 * Initiate.
		 * 
		 * @param section
		 *            {@link AutoWireSection}.
		 * @param outputName
		 *            Name of the {@link SectionOutput}.
		 */
		public SendLink(AutoWireSection section, String outputName) {
			this.section = section;
			this.outputName = outputName;
		}
	}

	/**
	 * Non-handled servicer.
	 */
	private static class NonHandledServicer {

		/**
		 * {@link AutoWireSection}.
		 */
		public final AutoWireSection section;

		/**
		 * Name of the {@link SectionInput}.
		 */
		public final String inputName;

		/**
		 * Initiate.
		 * 
		 * @param section
		 *            {@link AutoWireSection}.
		 * @param inputName
		 *            Name of the {@link SectionInput}.
		 */
		public NonHandledServicer(AutoWireSection section, String inputName) {
			this.section = section;
			this.inputName = inputName;
		}
	}

}