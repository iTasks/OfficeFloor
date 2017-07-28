/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.compile.internal.structure;

/**
 * {@link LinkSynchronousNode} that can be linked to another
 * {@link LinkSynchronousNode}.
 * 
 * @author Daniel Sagenschneider
 */
@Deprecated // external integration via queues (no synchronous)
public interface LinkSynchronousNode {

	/**
	 * Links the input {@link LinkSynchronousNode} to this
	 * {@link LinkSynchronousNode}.
	 * 
	 * @param node
	 *            {@link LinkSynchronousNode} to link to this
	 *            {@link LinkSynchronousNode}.
	 * @return <code>true</code> if linked.
	 */
	boolean linkSynchronousNode(LinkSynchronousNode node);

	/**
	 * Obtains the {@link LinkSynchronousNode} linked to this
	 * {@link LinkSynchronousNode}.
	 * 
	 * @return {@link LinkSynchronousNode} linked to this
	 *         {@link LinkSynchronousNode}.
	 */
	LinkSynchronousNode getLinkedSynchronousNode();

}