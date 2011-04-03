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

package net.officefloor.plugin.autowire;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.source.SectionSource;

/**
 * Section for configuring auto-wiring.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireSectionImpl extends AutoWirePropertiesImpl implements
		AutoWireSection {

	/**
	 * Name of section.
	 */
	private final String name;

	/**
	 * {@link SectionSource} class.
	 */
	private final Class<?> sourceClass;

	/**
	 * Location of section.
	 */
	private final String location;

	/**
	 * Initiate.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @param name
	 *            Name of section.
	 * @param sourceClass
	 *            {@link SectionSource} class.
	 * @param location
	 *            Location of section.
	 * @param properties
	 *            Properties for the section.
	 */
	public AutoWireSectionImpl(OfficeFloorCompiler compiler, String name,
			Class<?> sourceClass, String location, PropertyList properties) {
		super(compiler, properties);
		this.name = name;
		this.sourceClass = sourceClass;
		this.location = location;
	}

	/**
	 * Allow for extending this to provide additional functionality for an
	 * section.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @param section
	 *            Section to copy in state.
	 */
	protected AutoWireSectionImpl(OfficeFloorCompiler compiler,
			AutoWireSection section) {
		this(compiler, section.getSectionName(), section
				.getSectionSourceClass(), section.getSectionLocation(), section
				.getProperties());
	}

	/*
	 * ========================= AutoWireSection ========================
	 */

	@Override
	public String getSectionName() {
		return this.name;
	}

	@Override
	public Class<?> getSectionSourceClass() {
		return this.sourceClass;
	}

	@Override
	public String getSectionLocation() {
		return this.location;
	}

}