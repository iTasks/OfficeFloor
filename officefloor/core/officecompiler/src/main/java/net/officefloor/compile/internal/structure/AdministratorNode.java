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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.administration.source.AdministratorSource;
import net.officefloor.compile.spi.office.OfficeAdministrator;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.OfficeBuilder;

/**
 * {@link OfficeAdministrator} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministratorNode extends LinkTeamNode, OfficeAdministrator {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Administrator";

	/**
	 * Initialises this {@link AdministratorNode}.
	 * 
	 * @param administratorSourceClassName
	 *            Class name of the {@link AdministratorSource}.
	 * @param administratorSource
	 *            Optional instantiated {@link AdministratorSource}. May be
	 *            <code>null</code>.
	 */
	void initialise(String administratorSourceClassName,
			AdministratorSource<?, ?> administratorSource);

	/**
	 * <p>
	 * Obtains the {@link AdministratorType} for this {@link AdministratorNode}.
	 * <p>
	 * The {@link OfficeAdministrator} must be fully populated with the
	 * necessary {@link Property} instances before calling this.
	 * 
	 * @return {@link AdministratorType} for this {@link AdministratorNode}.
	 */
	AdministratorType<?, ?> loadAdministratorType();

	/**
	 * Builds this {@link Administration} into the {@link OfficeBuilder}.
	 * 
	 * @param officeBuilder
	 *            {@link OfficeBuilder}.
	 */
	void buildAdministrator(OfficeBuilder officeBuilder);

}