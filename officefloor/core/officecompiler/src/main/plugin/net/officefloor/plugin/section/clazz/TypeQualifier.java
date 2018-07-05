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
package net.officefloor.plugin.section.clazz;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;

import net.officefloor.compile.section.TypeQualification;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link TypeQualification} of a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
@Documented
public @interface TypeQualifier {

	/**
	 * Qualifier.
	 * 
	 * @return Qualifier.
	 */
	Class<? extends Annotation> qualifier() default TypeQualifier.class;

	/**
	 * Type.
	 * 
	 * @return Type.
	 */
	Class<?> type();

}