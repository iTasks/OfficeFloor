/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.office;

import net.officefloor.compile.spi.office.OfficeOutput;
import net.officefloor.frame.api.manage.Office;

/**
 * <code>Type definition</code> of an {@link OfficeOutput} from the
 * {@link Office}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeOutputType {

	/**
	 * Obtains the name of {@link OfficeOutput}.
	 * 
	 * @return Name of this {@link OfficeOutput}.
	 */
	String getOfficeOutputName();

	/**
	 * Obtains the fully qualified class name of the argument type from this
	 * {@link OfficeOutput}.
	 * 
	 * @return Argument type to this {@link OfficeOutput}.
	 */
	String getArgumentType();

}
