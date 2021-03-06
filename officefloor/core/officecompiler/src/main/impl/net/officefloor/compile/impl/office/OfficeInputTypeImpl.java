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

package net.officefloor.compile.impl.office;

import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.spi.office.OfficeInput;

/**
 * {@link OfficeInputType} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class OfficeInputTypeImpl implements OfficeInputType {

	/**
	 * Name of the {@link OfficeInput}.
	 */
	private final String inputName;

	/**
	 * {@link Class} name of the parameter.
	 */
	private final String parameterType;

	/**
	 * Instantiate.
	 * 
	 * @param inputName
	 *            Name of the {@link OfficeInput}.
	 * @param parameterType
	 *            {@link Class} name of the parameter.
	 */
	public OfficeInputTypeImpl(String inputName, String parameterType) {
		this.inputName = inputName;
		this.parameterType = parameterType;
	}

	/*
	 * ================== OfficeInputType ======================
	 */

	@Override
	public String getOfficeInputName() {
		return this.inputName;
	}

	@Override
	public String getParameterType() {
		return this.parameterType;
	}

}
