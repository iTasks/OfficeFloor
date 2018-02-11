/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.web.resource.build;

import java.io.IOException;

import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.spi.ResourceSystem;
import net.officefloor.web.resource.spi.ResourceSystemFactory;

/**
 * Builds the {@link HttpResource} for {@link WebArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpResourceArchitect {

	/**
	 * Links the {@link OfficeFlowSourceNode} to the {@link HttpResource}.
	 * 
	 * @param flowSourceNode
	 *            {@link OfficeFlowSourceNode}.
	 * @param resourcePath
	 *            Path to the {@link HttpResource}.
	 */
	void link(OfficeFlowSourceNode flowSourceNode, String resourcePath);

	/**
	 * <p>
	 * Adds {@link HttpResource} instances.
	 * <p>
	 * The {@link ResourceSystem} instances will be interrogated in the order
	 * they are added for a {@link HttpResource}.
	 * 
	 * @param resourceSystemService
	 *            {@link ResourceSystemFactory} to create the
	 *            {@link ResourceSystem} to provide the resources backing the
	 *            {@link HttpResource} instances.
	 * @param location
	 *            {@link ResourceSystemFactory} specific location of the
	 *            resources.
	 * @return {@link HttpResourcesBuilder}.
	 */
	HttpResourcesBuilder addHttpResources(ResourceSystemFactory resourceSystemService, String location);

	/**
	 * <p>
	 * Adds {@link HttpResource} instances via a {@link ResourceSystemFactory}.
	 * <p>
	 * The {@link ResourceSystem} instances will be interrogated in the order
	 * they are added for a {@link HttpResource}.
	 * 
	 * @param protocolLocation
	 *            String configuration of <code>[protocol]:location</code> to
	 *            configure a {@link ResourceSystem} from
	 *            {@link ResourceSystemFactory}.
	 * @return {@link HttpResourcesBuilder}.
	 * 
	 * @see ResourceSystemFactory
	 */
	HttpResourcesBuilder addHttpResources(String protocolLocation);

	/**
	 * Informs the {@link WebArchitect} of the necessary {@link HttpResource}
	 * instances. This is to be invoked once all {@link HttpResource} instances
	 * are configured.
	 * 
	 * @throws IOException
	 *             If fails to configure resources.
	 */
	void informWebArchitect() throws IOException;

}