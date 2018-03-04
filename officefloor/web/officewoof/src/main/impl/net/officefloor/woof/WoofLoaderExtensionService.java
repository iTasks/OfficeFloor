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
package net.officefloor.woof;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.web.WebArchitectEmployer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.resource.build.HttpResourceArchitect;
import net.officefloor.web.resource.build.HttpResourceArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.template.build.WebTemplateArchitect;
import net.officefloor.web.template.build.WebTemplateArchitectEmployer;
import net.officefloor.woof.model.objects.WoofObjectsRepositoryImpl;
import net.officefloor.woof.model.resources.WoofResourcesRepositoryImpl;
import net.officefloor.woof.model.teams.WoofTeamsRepositoryImpl;
import net.officefloor.woof.model.woof.WoofModel;
import net.officefloor.woof.model.woof.WoofRepositoryImpl;
import net.officefloor.woof.objects.WoofObjectsLoader;
import net.officefloor.woof.objects.WoofObjectsLoaderContext;
import net.officefloor.woof.objects.WoofObjectsLoaderImpl;
import net.officefloor.woof.resources.WoofResourcesLoader;
import net.officefloor.woof.resources.WoofResourcesLoaderContext;
import net.officefloor.woof.resources.WoofResourcesLoaderImpl;
import net.officefloor.woof.teams.WoofTeamsLoader;
import net.officefloor.woof.teams.WoofTeamsLoaderContext;
import net.officefloor.woof.teams.WoofTeamsLoaderImpl;

/**
 * {@link OfficeFloorExtensionService} / {@link OfficeExtensionService} to
 * configure the {@link WoofModel} into the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLoaderExtensionService implements OfficeFloorExtensionService, OfficeExtensionService {

	/*
	 * ================= OfficeFloorExtensionService ===================
	 */

	@Override
	public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception {

		// Load the optional teams configuration for the application
		ConfigurationItem teamsConfiguration = context.getOptionalConfigurationItem("application.teams", null);
		if (teamsConfiguration != null) {
			// Load the teams configuration
			WoofTeamsLoader teamsLoader = new WoofTeamsLoaderImpl(
					new WoofTeamsRepositoryImpl(new ModelRepositoryImpl()));
			teamsLoader.loadWoofTeamsConfiguration(new WoofTeamsLoaderContext() {

				@Override
				public OfficeFloorExtensionContext getOfficeFloorExtensionContext() {
					return context;
				}

				@Override
				public OfficeFloorDeployer getOfficeFloorDeployer() {
					return officeFloorDeployer;
				}

				@Override
				public ConfigurationItem getConfiguration() {
					return teamsConfiguration;
				}
			});
		}
	}

	/*
	 * =================== OfficeExtensionService ======================
	 */

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		// Employ the architects
		WebArchitect web = WebArchitectEmployer.employWebArchitect(officeArchitect, context);
		HttpSecurityArchitect security = HttpSecurityArchitectEmployer.employHttpSecurityArchitect(web, officeArchitect,
				context);
		WebTemplateArchitect templater = WebTemplateArchitectEmployer.employWebTemplater(web, officeArchitect, context);
		HttpResourceArchitect resources = HttpResourceArchitectEmployer.employHttpResourceArchitect(web, security,
				officeArchitect, context);

		// Obtain the woof configuration (ensuring exists)
		String woofLocation = "application.woof";
		ConfigurationItem woofConfiguration = context.getConfigurationItem(woofLocation, null);
		if (woofConfiguration == null) {
			officeArchitect.addIssue("Can not find WoOF configuration file '" + woofLocation + "'");
			return; // must have WoOF configuration
		}

		// Create the WoOF context
		WoofContext woofContext = new WoofContext() {
			@Override
			public OfficeExtensionContext getOfficeExtensionContext() {
				return context;
			}

			@Override
			public OfficeArchitect getOfficeArchitect() {
				return officeArchitect;
			}

			@Override
			public ConfigurationItem getConfiguration() {
				return woofConfiguration;
			}

			@Override
			public WebArchitect getWebArchitect() {
				return web;
			}

			@Override
			public HttpSecurityArchitect getHttpSecurityArchitect() {
				return security;
			}

			@Override
			public WebTemplateArchitect getWebTemplater() {
				return templater;
			}

			@Override
			public HttpResourceArchitect getHttpResourceArchitect() {
				return resources;
			}
		};

		// Load the WoOF configuration to the application
		WoofLoader woofLoader = new WoofLoaderImpl(new WoofRepositoryImpl(new ModelRepositoryImpl()));
		woofLoader.loadWoofConfiguration(woofContext);

		// Load the optional objects configuration to the application
		final ConfigurationItem objectsConfiguration = context.getConfigurationItem("application.objects", null);
		if (objectsConfiguration != null) {

			// Load the objects configuration
			WoofObjectsLoader objectsLoader = new WoofObjectsLoaderImpl(
					new WoofObjectsRepositoryImpl(new ModelRepositoryImpl()));
			objectsLoader.loadWoofObjectsConfiguration(new WoofObjectsLoaderContext() {

				@Override
				public OfficeExtensionContext getOfficeExtensionContext() {
					return context;
				}

				@Override
				public OfficeArchitect getOfficeArchitect() {
					return officeArchitect;
				}

				@Override
				public ConfigurationItem getConfiguration() {
					return objectsConfiguration;
				}
			});
		}

		// Load the optional resources configuration to the application
		final ConfigurationItem resourcesConfiguration = context.getConfigurationItem("application.resources", null);
		if (resourcesConfiguration != null) {

			// Load the resources configuration
			WoofResourcesLoader resourcesLoader = new WoofResourcesLoaderImpl(
					new WoofResourcesRepositoryImpl(new ModelRepositoryImpl()));
			resourcesLoader.loadWoofResourcesConfiguration(new WoofResourcesLoaderContext() {

				@Override
				public OfficeExtensionContext getOfficeExtensionContext() {
					return context;
				}

				@Override
				public OfficeArchitect getOfficeArchitect() {
					return officeArchitect;
				}

				@Override
				public HttpResourceArchitect getHttpResourceArchitect() {
					return resources;
				}

				@Override
				public ConfigurationItem getConfiguration() {
					return resourcesConfiguration;
				}
			});
		}

		// Load the woof extensions
		ClassLoader classLoader = context.getClassLoader();
		ServiceLoader<WoofExtensionService> extensionServiceLoader = ServiceLoader.load(WoofExtensionService.class,
				classLoader);
		Iterator<WoofExtensionService> extensionIterator = extensionServiceLoader.iterator();
		while (extensionIterator.hasNext()) {

			// Obtain the next extension service
			WoofExtensionService extensionService;
			try {
				extensionService = extensionIterator.next();
			} catch (ServiceConfigurationError ex) {
				// Issue loading service
				officeArchitect.addIssue(ex.getMessage(), ex);

				// Not loaded, so continue onto next
				continue;
			}

			// Extend the application
			try {
				extensionService.extend(woofContext);

			} catch (Throwable ex) {
				// Issue with service
				officeArchitect.addIssue(WoofLoaderExtensionService.class.getSimpleName() + " "
						+ extensionService.getClass().getName() + " configuration failure: " + ex.getMessage(), ex);
			}
		}

		// Inform Office Architect
		templater.informWebArchitect();
		resources.informWebArchitect();
		security.informWebArchitect();
		web.informOfficeArchitect();
	}

}