/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.api.escalate;

import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * <p>
 * Internal failures within the {@link OfficeFrame} extend {@link Escalation}.
 * <p>
 * However, all {@link Throwable} instances thrown from {@link ManagedFunction}
 * and {@link ManagedObject} instances are considered to follow the
 * {@link Escalation} paradigm. This is that the invoker need not deal with
 * {@link Escalation} instances, and these are handled by other
 * {@link ManagedFunction} instances.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class Escalation extends Throwable {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor.
	 */
	public Escalation() {
		super();
	}

	/**
	 * Allows for a cause of the {@link Escalation}.
	 * 
	 * @param cause Cause of the {@link Escalation}.
	 */
	public Escalation(Throwable cause) {
		super(cause);
	}

}
