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
package net.officefloor.eclipse.editor;

import net.officefloor.model.Model;

/**
 * Builder of an {@link AdaptedParent}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedParentBuilder<R extends Model, O, M extends Model, E extends Enum<E>>
		extends AdaptedChildBuilder<R, O, M, E> {

	/**
	 * Configures creating the {@link Model}.
	 * 
	 * @param provideParentAction
	 *            {@link ModelAction} to provide parent.
	 */
	void create(ModelAction<R, O, M> provideParentAction);

	/**
	 * Configures an {@link ModelAction} for the parent {@link Model}.
	 * 
	 * @param action
	 *            {@link ModelAction}.
	 * @param visualFactory
	 *            {@link AdaptedActionVisualFactory}.
	 */
	void action(ModelAction<R, O, M> action, AdaptedActionVisualFactory visualFactory);

}