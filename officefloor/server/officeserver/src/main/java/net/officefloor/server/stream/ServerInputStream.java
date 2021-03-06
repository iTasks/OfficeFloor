/*-
 * #%L
 * HTTP Server
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

package net.officefloor.server.stream;

import java.io.InputStream;

/**
 * Provides non-blocking {@link InputStream} for servicing.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class ServerInputStream extends InputStream {

	/**
	 * Obtains a new {@link InputStream} that starts browsing the input content
	 * from the current position of the {@link ServerInputStream} within the
	 * input stream of data.
	 * 
	 * @return {@link InputStream}.
	 */
	public abstract InputStream createBrowseInputStream();

}
