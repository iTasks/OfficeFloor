/*-
 * #%L
 * Default OfficeFloor HTTP Server
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

package net.officefloor.server;

import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.function.Function;

import net.officefloor.server.stream.StreamBuffer;

/**
 * Handles requests.
 * 
 * @author Daniel Sagenschneider
 */
public interface RequestHandler<R> {

	/**
	 * {@link Function} interface to run an execution on the {@link Socket}
	 * {@link Thread}.
	 */
	public static interface Execution {

		/**
		 * Runs the execution.
		 * 
		 * @throws Throwable
		 *             If execution fails.
		 */
		void run() throws Throwable;
	}

	/**
	 * Executes the {@link Execution} on the {@link Socket} {@link Thread}.
	 * 
	 * @param execution
	 *            {@link Execution}.
	 */
	void execute(Execution execution);

	/**
	 * <p>
	 * Handles a request.
	 * <p>
	 * This may only be invoked by the {@link Socket} {@link Thread}.
	 * 
	 * @param request
	 *            Request.
	 * @throws IllegalStateException
	 *             If invoked from another {@link Thread}.
	 */
	void handleRequest(R request) throws IllegalStateException;

	/**
	 * <p>
	 * Sends data immediately.
	 * <p>
	 * This may only be invoked by the {@link Socket} {@link Thread}.
	 * 
	 * @param immediateHead
	 *            Head {@link StreamBuffer} to linked list of
	 *            {@link StreamBuffer} instances of data to send immediately.
	 * @throws IllegalStateException
	 *             If invoked from another {@link Thread}.
	 */
	void sendImmediateData(StreamBuffer<ByteBuffer> immediateHead) throws IllegalStateException;

	/**
	 * Allows to close connection.
	 * 
	 * @param exception
	 *            Optional {@link Exception} for the cause of closing the
	 *            connection. <code>null</code> to indicate normal close.
	 */
	void closeConnection(Throwable exception);

}
