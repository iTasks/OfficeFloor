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
package net.officefloor.compile.impl.type;

import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.CompileContext;

/**
 * Tests loading the {@link GovernanceType} from the {@link CompileContext}.
 *
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("rawtypes")
public class GovernanceTypeContextTest extends AbstractTestTypeContext<GovernanceNode, GovernanceType> {

	/**
	 * Instantiate.
	 */
	public GovernanceTypeContextTest() {
		super(GovernanceNode.class, GovernanceType.class, (context, node) -> (GovernanceType) node.loadGovernanceType(),
				(context, node) -> (GovernanceType) context.getOrLoadGovernanceType(node));
	}

}