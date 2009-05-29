/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse;

/**
 * Failure of the {@link net.officefloor.eclipse.OfficeFloorPlugin}.
 * 
 * @author Daniel Sagenschneider
 */
// TODO do not throw issue. Handle via providing to UI.
@Deprecated
public class OfficeFloorPluginFailure extends RuntimeException {

	/**
	 * Enforce reason.
	 * 
	 * @param reason
	 *            Reason.
	 */
	public OfficeFloorPluginFailure(String reason) {
		super(reason);
	}

	/**
	 * Enforce reason and allow cause.
	 * 
	 * @param reason
	 *            Reason.
	 * @param cause
	 *            Cause.
	 */
	public OfficeFloorPluginFailure(String reason, Throwable cause) {
		super(reason, cause);
	}

	/**
	 * Allow cause.
	 * 
	 * @param cause
	 *            Cause.
	 */
	public OfficeFloorPluginFailure(Throwable cause) {
		super(cause);
	}

}
