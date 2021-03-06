/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.teams;

import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.woof.resources.WoofResourcesLoader;

/**
 * Context for the {@link WoofResourcesLoader}.
 *
 * @author Daniel Sagenschneider
 */
public interface WoofTeamsLoaderContext {

	/**
	 * Obtains the {@link ConfigurationItem} containing the configuration of the
	 * teams.
	 * 
	 * @return {@link ConfigurationItem} containing the configuration of the
	 *         objects.
	 */
	ConfigurationItem getConfiguration();

	/**
	 * Obtains the {@link OfficeFloorDeployer} to be configured with the teams.
	 * 
	 * @return {@link OfficeFloorDeployer} to be configured with the objects.
	 */
	OfficeFloorDeployer getOfficeFloorDeployer();

	/**
	 * Obtains the {@link OfficeFloorExtensionContext}.
	 * 
	 * @return {@link OfficeFloorExtensionContext}.
	 */
	OfficeFloorExtensionContext getOfficeFloorExtensionContext();

	/**
	 * Obtains the {@link DeployedOffice} that the WoOF application is being
	 * deployed into.
	 * 
	 * @return {@link DeployedOffice} that the WoOF application is being deployed
	 *         into.
	 */
	DeployedOffice getDeployedOffice();

}
