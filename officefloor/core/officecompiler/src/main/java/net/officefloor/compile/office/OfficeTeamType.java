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
package net.officefloor.compile.office;

import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.frame.api.team.Team;

/**
 * <code>Type definition</code> of a {@link Team} required by the
 * {@link OfficeType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeTeamType {

	/**
	 * Obtains the name of the required {@link Team}.
	 * 
	 * @return Name of the required {@link Team}.
	 */
	String getOfficeTeamName();

	/**
	 * Obtains the {@link TypeQualification} instances for the
	 * {@link OfficeTeam}.
	 * 
	 * @return {@link TypeQualification} instances for the {@link OfficeTeam}.
	 */
	TypeQualification[] getTypeQualification();

}