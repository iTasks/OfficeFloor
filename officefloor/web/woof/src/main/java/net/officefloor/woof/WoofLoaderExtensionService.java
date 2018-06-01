/*
 * OOsfficeFloor - http://www.officefloor.net
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import net.officefloor.compile.impl.ApplicationOfficeFloorSource;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.configuration.ConfigurationItem;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.server.http.HttpServer;
import net.officefloor.web.WebArchitectEmployer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.resource.build.HttpResourceArchitect;
import net.officefloor.web.resource.build.HttpResourceArchitectEmployer;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.template.build.WebTemplateArchitect;
import net.officefloor.web.template.build.WebTemplateArchitectEmployer;
import net.officefloor.woof.model.objects.WoofObjectsModel;
import net.officefloor.woof.model.objects.WoofObjectsRepositoryImpl;
import net.officefloor.woof.model.resources.WoofResourcesModel;
import net.officefloor.woof.model.resources.WoofResourcesRepositoryImpl;
import net.officefloor.woof.model.teams.WoofTeamsModel;
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

	/**
	 * Name of the WoOF configuration file. This file is to exist at the root of the
	 * class path for the {@link WoofLoaderExtensionService} to load any
	 * configuration.
	 */
	public static final String APPLICATION_WOOF = "application.woof";

	/**
	 * Determines if a WoOF application.
	 * 
	 * @param context
	 *            {@link SourceContext}.
	 * @return <code>true</code> if a WoOF application.
	 * @throws IOException
	 *             If fails to check if WoOF application.
	 */
	private static boolean isWoofApplication(SourceContext context) throws IOException {

		// Obtain configuration file
		InputStream config = context.getOptionalResource(APPLICATION_WOOF);
		if (config != null) {
			config.close();
		}

		// WoOF application if configuration file
		return config != null;
	}

	/**
	 * Runs within a context.
	 */
	public static interface WoofLoaderRunnable<R, E extends Throwable> {

		/**
		 * Runs.
		 * 
		 * @param context
		 *            {@link WoofLoaderRunnableContext}.
		 * @return Allows for return an object.
		 * @throws E
		 *             Potential failure.
		 */
		R run(WoofLoaderRunnableContext context) throws E;
	}

	/**
	 * {@link WoofLoaderRunnable} context.
	 */
	public static interface WoofLoaderRunnableContext {

		/**
		 * Flags to not load the {@link HttpServer}.
		 */
		void notLoadHttpServer();

		/**
		 * Flags not to load the {@link WoofTeamsModel} configuration.
		 */
		void notLoadTeams();

		/**
		 * Flags not to load the {@link WoofModel} configuration.
		 */
		void notLoadWoof();

		/**
		 * Flags not to load the {@link WoofObjectsModel} configuration.
		 */
		void notLoadObjects();

		/**
		 * Flags not to load the {@link WoofResourcesModel} configuration.
		 */
		void notLoadResources();

		/**
		 * Flags not to load the {@link WoofExtensionService} instances.
		 */
		void notLoadWoofExtensions();
	}

	/**
	 * Indicates to load the {@link HttpServer}.
	 */
	private static boolean isLoadHttpServer = true;

	/**
	 * Indicates to load the {@link WoofTeamsModel} configuration.
	 */
	private static boolean isLoadTeams = true;

	/**
	 * Indicates to load the {@link WoofModel} configuration.
	 */
	private static boolean isLoadWoof = true;

	/**
	 * Indicates to load the {@link WoofObjectsModel} configuration.
	 */
	private static boolean isLoadObjects = true;

	/**
	 * Indicates to load the {@link WoofResourcesModel} configuration.
	 */
	private static boolean isLoadResources = true;

	/**
	 * Indicates to load the {@link WoofExtensionService} instances.
	 */
	private static boolean isLoadWoofExtensions = true;

	/**
	 * Undertakes a contextual load.
	 *
	 * @param <R>
	 *            Return type from {@link WoofLoaderRunnable}.
	 * @param <E>
	 *            Possible {@link Throwable} from {@link WoofLoaderRunnable}.
	 * @param runnable
	 *            {@link WoofLoaderRunnable} to configure the contextual load.
	 * @return Returned object from {@link WoofLoaderRunnable}.
	 * @throws E
	 *             Potential failure.
	 */
	public static <R, E extends Throwable> R contextualLoad(WoofLoaderRunnable<R, E> runnable) throws E {
		try {

			// Undertake runnable
			return runnable.run(new WoofLoaderRunnableContext() {

				@Override
				public void notLoadWoof() {
					isLoadWoof = false;
				}

				@Override
				public void notLoadTeams() {
					isLoadTeams = false;
				}

				@Override
				public void notLoadResources() {
					isLoadResources = false;
				}

				@Override
				public void notLoadObjects() {
					isLoadObjects = false;
				}

				@Override
				public void notLoadHttpServer() {
					isLoadHttpServer = false;
				}

				@Override
				public void notLoadWoofExtensions() {
					isLoadWoofExtensions = false;
				}
			});

		} finally {
			// Reset
			isLoadHttpServer = true;
			isLoadTeams = true;
			isLoadWoof = true;
			isLoadObjects = true;
			isLoadResources = true;
			isLoadWoofExtensions = true;
		}
	}

	/*
	 * ================= OfficeFloorExtensionService ===================
	 */

	@Override
	public void extendOfficeFloor(OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context)
			throws Exception {

		// Determine if WoOF application
		if (!isWoofApplication(context)) {
			return; // not WoOF application
		}

		// Load the HTTP Server
		if (isLoadHttpServer) {

			// Obtain the input to service the HTTP requests
			DeployedOffice office = officeFloorDeployer.getDeployedOffice(ApplicationOfficeFloorSource.OFFICE_NAME);
			DeployedOfficeInput officeInput = office.getDeployedOfficeInput(WebArchitect.HANDLER_SECTION_NAME,
					WebArchitect.HANDLER_INPUT_NAME);

			// Load the HTTP server
			new HttpServer(officeInput, officeFloorDeployer, context);
		}

		// Load the optional teams configuration for the application
		if (isLoadTeams) {
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
	}

	/*
	 * =================== OfficeExtensionService ======================
	 */

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		// Determine if WoOF application
		if (!isWoofApplication(context)) {
			return; // not WoOF application
		}

		// Employ the architects
		WebArchitect web = WebArchitectEmployer.employWebArchitect(officeArchitect, context);
		HttpSecurityArchitect security = HttpSecurityArchitectEmployer.employHttpSecurityArchitect(web, officeArchitect,
				context);
		WebTemplateArchitect templater = WebTemplateArchitectEmployer.employWebTemplater(web, officeArchitect, context);
		HttpResourceArchitect resources = HttpResourceArchitectEmployer.employHttpResourceArchitect(web, security,
				officeArchitect, context);

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
				return context.getConfigurationItem(APPLICATION_WOOF, null);
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
		if (isLoadWoof) {
			WoofLoader woofLoader = new WoofLoaderImpl(new WoofRepositoryImpl(new ModelRepositoryImpl()));
			woofLoader.loadWoofConfiguration(woofContext);
		}

		// Load the optional objects configuration to the application
		if (isLoadObjects) {
			final ConfigurationItem objectsConfiguration = context.getOptionalConfigurationItem("application.objects",
					null);
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
		}

		// Load the optional resources configuration to the application
		if (isLoadResources) {
			final ConfigurationItem resourcesConfiguration = context
					.getOptionalConfigurationItem("application.resources", null);
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
		}

		// Load the woof extensions
		if (isLoadWoofExtensions) {
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
		}

		// Inform Office Architect
		templater.informWebArchitect();
		resources.informWebArchitect();
		security.informWebArchitect();
		web.informOfficeArchitect();
	}

}