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
package net.officefloor.server.http;

import javax.net.ssl.SSLContext;

import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.ServiceHandler;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.frame.api.source.SourceContext;

/**
 * HTTP Server.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServer {

	/**
	 * Configures the {@link HttpServer}.
	 * 
	 * @param sourceContext
	 *            {@link SourceContext}.
	 */
	public static void configureHttpServer(OfficeFloorDeployer officeFloorDeployer,
			OfficeFloorExtensionContext context) {

	}

	/**
	 * Configures the {@link HttpServer}.
	 * 
	 * @param httpPort
	 *            Port for HTTP traffic.
	 * @param httpsPort
	 *            Port for HTTPS traffic.
	 * @param implementation
	 *            {@link HttpServerImplementation}.
	 * @param sslContext
	 *            {@link SSLContext}.
	 * @param serviceOfficeName
	 *            Name of the {@link DeployedOffice} servicing the
	 *            {@link ServerHttpConnection}.
	 * @param serviceSectionName
	 *            Name of the {@link OfficeSection} within the
	 *            {@link DeployedOffice} servicing the
	 *            {@link ServerHttpConnection}.
	 * @param serviceSectionInputName
	 *            Name of the {@link OfficeSectionInput} servicing the
	 *            {@link ServerHttpConnection}.
	 * @param officeFloorDeployer
	 *            {@link OfficeFloorDeployer}.
	 * @param context
	 *            {@link OfficeFloorExtensionContext}.
	 */
	public static void configureHttpServer(int httpPort, int httpsPort, HttpServerImplementation implementation,
			SSLContext sslContext, String serviceOfficeName, String serviceSectionName, String serviceSectionInputName,
			OfficeFloorDeployer officeFloorDeployer, OfficeFloorExtensionContext context) {

		// Obtain the deployed office
		DeployedOffice office = officeFloorDeployer.getDeployedOffice(serviceOfficeName);

		// Obtain the input for service handling
		DeployedOfficeInput officeInput = office.getDeployedOfficeInput(serviceSectionName, serviceSectionInputName);

		// Configure the HTTP server
		implementation.configureHttpServer(new HttpServerImplementationContext() {

			@Override
			public int getHttpPort() {
				return httpPort;
			}

			@Override
			public int getHttpsPort() {
				return httpsPort;
			}

			@Override
			public SSLContext getSslContext() {
				return sslContext;
			}

			@Override
			public void addServiceHandler(ServiceHandler serviceHandler) {
				officeInput.addServiceHandler(serviceHandler);
			}

			@Override
			public OfficeFloorDeployer getOfficeFloorDeployer() {
				return officeFloorDeployer;
			}

			@Override
			public OfficeFloorExtensionContext getOfficeFloorExtensionContext() {
				return context;
			}
		});
	}

}