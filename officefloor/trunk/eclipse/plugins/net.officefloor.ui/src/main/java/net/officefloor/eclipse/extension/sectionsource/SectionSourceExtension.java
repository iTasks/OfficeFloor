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
package net.officefloor.eclipse.extension.sectionsource;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.eclipse.extension.ExtensionUtil;
import net.officefloor.eclipse.extension.classpath.ExtensionClasspathProvider;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Interface for extension to provide enriched {@link SectionSource} usage.
 * 
 * @author Daniel
 * 
 * @see ExtensionClasspathProvider
 */
public interface SectionSourceExtension<S extends SectionSource> {

	/**
	 * Extension ID.
	 */
	public static final String EXTENSION_ID = ExtensionUtil
			.getExtensionId("sectionsource");

	/**
	 * Obtains the class of the {@link SectionSource} being enriched in its
	 * usage.
	 * 
	 * @return Class of the {@link SectionSource} being enriched in its usage.
	 */
	Class<S> getSectionSourceClass();

	/**
	 * <p>
	 * Obtains the label for the {@link SectionSource}.
	 * <p>
	 * This is a descriptive name that can be used other than the fully
	 * qualified name of the {@link SectionSource}.
	 * 
	 * @return Label for the {@link SectionSource}.
	 */
	String getSectionSourceLabel();

	/**
	 * Loads the input page with the necessary {@link Control} instances to
	 * populate the {@link PropertyList}. Also allows notifying of changes to
	 * {@link Property} instances via the {@link SectionSourceExtensionContext}.
	 * 
	 * @param page
	 *            Page to be setup for populating the {@link PropertyList}.
	 * @param context
	 *            {@link SectionSourceExtensionContext}.
	 */
	void createControl(Composite page, SectionSourceExtensionContext context);

}