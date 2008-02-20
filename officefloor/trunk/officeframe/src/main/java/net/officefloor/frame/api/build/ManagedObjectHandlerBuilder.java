/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.Handler;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Meta-data about {@link Handler} instances for a {@link ManagedObject}.
 * 
 * @author Daniel
 */
public interface ManagedObjectHandlerBuilder<H extends Enum<H>> {

	/**
	 * Creates a {@link HandlerBuilder} for a {@link Handler} of the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param key
	 *            Key identifying the {@link Handler}.
	 * @param flowListingEnum
	 *            Type providing the listing of the {@link Flow} instances for
	 *            the {@link Handler}.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	<F extends Enum<F>> HandlerBuilder<F> registerHandler(H key,
			Class<F> processListingEnum) throws BuildException;

	/**
	 * Creates a {@link HandlerBuilder} for a {@link Handler} of the
	 * {@link ManagedObjectSource}.
	 * 
	 * @param key
	 *            Key identifying the {@link Handler}.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	HandlerBuilder<Indexed> registerHandler(H key) throws BuildException;

}
