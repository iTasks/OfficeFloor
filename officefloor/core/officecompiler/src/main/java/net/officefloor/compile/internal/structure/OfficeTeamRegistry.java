/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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

import net.officefloor.compile.spi.office.OfficeTeam;

/**
 * Factory for the creation of an {@link OfficeTeam}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeTeamRegistry {

	/**
	 * Obtains the {@link OfficeTeamNode} instances.
	 * 
	 * @return {@link OfficeTeamNode} instances.
	 */
	OfficeTeamNode[] getOfficeTeams();

	/**
	 * <p>
	 * Creates the {@link OfficeTeamNode}.
	 * <p>
	 * The name of the {@link OfficeTeamNode} may be adjusted to ensure
	 * uniqueness.
	 * 
	 * @param officeTeamName
	 *            {@link OfficeTeam} name.
	 * @return {@link OfficeTeamNode}.
	 */
	OfficeTeamNode createOfficeTeam(String officeTeamName);

}