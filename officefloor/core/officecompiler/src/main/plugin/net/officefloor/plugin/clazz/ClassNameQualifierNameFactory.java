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

package net.officefloor.plugin.clazz;

import java.lang.annotation.Annotation;

/**
 * {@link QualifierNameFactory} providing the fully qualified {@link Class}
 * name.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassNameQualifierNameFactory implements QualifierNameFactory<Annotation> {

	/*
	 * ================ QualifierNameFactory ====================
	 */

	@Override
	public String getQualifierName(Annotation annotation) {
		return annotation.annotationType().getName();
	}
}
