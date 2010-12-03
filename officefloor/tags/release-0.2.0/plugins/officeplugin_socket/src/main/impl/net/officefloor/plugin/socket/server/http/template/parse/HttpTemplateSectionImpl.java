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

package net.officefloor.plugin.socket.server.http.template.parse;

/**
 * {@link HttpTemplateSection} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class HttpTemplateSectionImpl implements HttpTemplateSection {

	/**
	 * {@link HttpTemplateSection} name.
	 */
	private final String sectionName;

	/**
	 * {@link HttpTemplateSectionContent} instances.
	 */
	private final HttpTemplateSectionContent[] content;

	/**
	 * Initiate.
	 *
	 * @param sectionName
	 *            {@link HttpTemplateSection} name.
	 * @param content
	 *            {@link HttpTemplateSectionContent} instances.
	 */
	public HttpTemplateSectionImpl(String sectionName,
			HttpTemplateSectionContent[] content) {
		this.sectionName = sectionName;
		this.content = content;
	}

	/*
	 * ================= HttpTemplateSection =========================
	 */

	@Override
	public String getSectionName() {
		return this.sectionName;
	}

	@Override
	public HttpTemplateSectionContent[] getContent() {
		return this.content;
	}

}