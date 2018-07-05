/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.section;

import net.officefloor.compile.spi.office.OfficeSection;

/**
 * <code>Type definition</code> of a section of the {@link OfficeSection}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionType extends OfficeSubSectionType {

	/**
	 * Obtains the {@link OfficeSectionInputType} instances for this
	 * {@link OfficeSection}.
	 * 
	 * @return {@link OfficeSectionInputType} instances for this
	 *         {@link OfficeSection}.
	 */
	OfficeSectionInputType[] getOfficeSectionInputTypes();

	/**
	 * Obtains the {@link OfficeSectionOutputType} instances for this
	 * {@link OfficeSection}.
	 * 
	 * @return {@link OfficeSectionOutputType} instances for this
	 *         {@link OfficeSection}.
	 */
	OfficeSectionOutputType[] getOfficeSectionOutputTypes();

	/**
	 * Obtains the {@link OfficeSectionObjectType} instances required by this
	 * {@link OfficeSection}.
	 * 
	 * @return {@link OfficeSectionObjectType} instances required by this
	 *         {@link OfficeSection}.
	 */
	OfficeSectionObjectType[] getOfficeSectionObjectTypes();

}