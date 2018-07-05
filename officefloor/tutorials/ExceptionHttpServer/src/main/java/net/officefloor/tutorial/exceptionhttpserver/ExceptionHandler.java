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
package net.officefloor.tutorial.exceptionhttpserver;

import java.sql.SQLException;

import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Handles the exception by logging it.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class ExceptionHandler {

	@NextFunction("TechnicalFault")
	public void handleSqlException(@Parameter SQLException ex) {
		// Production code may take some action and would use a Logger
		System.err.println(ex.getMessage());
	}

}
// END SNIPPET: tutorial