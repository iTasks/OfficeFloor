/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.stream;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Processes a batch of {@link ByteBuffer} instances on a
 * {@link InputBufferStream} read.
 *
 * @author Daniel Sagenschneider
 */
public interface GatheringBufferProcessor {

	/**
	 * Processes the {@link ByteBuffer} instances from the
	 * {@link InputBufferStream}.
	 *
	 * @param buffers
	 *            {@link ByteBuffer} instances from the
	 *            {@link InputBufferStream}.
	 * @throws IOException
	 *             If fails to process the {@link ByteBuffer} instances.
	 */
	void process(ByteBuffer[] buffers) throws IOException;

}