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

package net.officefloor.compile.internal.structure;

/**
 * Node within the compilation tree.
 *
 * @author Daniel Sagenschneider
 */
public interface Node {

	/**
	 * Creates the qualified name, handling <code>null</code> names.
	 * 
	 * @param names Names. Entries may be <code>null</code>.
	 * @return Qualified name.
	 */
	static String qualify(String... names) {
		StringBuilder qualifiedName = new StringBuilder();
		if (names != null) {
			for (String name : names) {
				if ((name == null) || (name.trim().length() == 0)) {
					// Determine if already qualified
					if (qualifiedName.length() > 0) {
						// Append null / blank
						qualifiedName.append(".[" + name + "]");
					}
				} else {
					// Have name, so determine if qualifier
					if (qualifiedName.length() > 0) {
						qualifiedName.append(".");
					}
					qualifiedName.append(name);
				}
			}
		}
		return qualifiedName.toString();
	}

	/**
	 * Creates an escaped name.
	 * 
	 * @param name Name.
	 * @return Escaped name.
	 */
	static String escape(String name) {
		return name != null ? name.replace('.', '_') : null;
	}

	/**
	 * Obtains the name of the {@link Node}.
	 * 
	 * @return Name of the {@link Node}.
	 */
	String getNodeName();

	/**
	 * Obtains the {@link Node} type.
	 * 
	 * @return {@link Node} type.
	 */
	String getNodeType();

	/**
	 * Obtains the location of the {@link Node}.
	 * 
	 * @return Location of the {@link Node}. May be <code>null</code> if
	 *         {@link Node} does not support a location.
	 */
	String getLocation();

	/**
	 * Obtains the {@link Node} containing this {@link Node}.
	 * 
	 * @return {@link Node} containing this {@link Node}.
	 */
	Node getParentNode();

	/**
	 * Obtains the qualified name of the {@link Node}.
	 * 
	 * @return Qualified name of the {@link Node}.
	 */
	default String getQualifiedName() {
		String name = escape(this.getNodeName());
		Node parent = this.getParentNode();
		return parent != null ? parent.getQualifiedName(name) : name;
	}

	/**
	 * Obtains the qualified name for child {@link Node}.
	 * 
	 * @param name Name of child {@link Node}.
	 * @return Name qualified by this {@link Node}.
	 */
	default String getQualifiedName(String name) {
		return qualify(this.getQualifiedName(), name);
	}

	/**
	 * Indicates if the {@link Node} has been initialised. {@link Node} instances
	 * should only be initialised once. Initialising the {@link Node} twice is an
	 * issue.
	 * 
	 * @return <code>true</code> if initialised.
	 */
	boolean isInitialised();

	/**
	 * Obtains the child {@link Node} instances.
	 * 
	 * @return Child {@link Node} instances.
	 */
	Node[] getChildNodes();

}
