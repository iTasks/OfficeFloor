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
package net.officefloor.web.resource.build;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.resource.spi.ResourceTransformer;
import net.officefloor.web.resource.spi.ResourceTransformerContext;
import net.officefloor.web.resource.spi.ResourceTransformerFactory;
import net.officefloor.web.resource.spi.ResourceTransformerService;

/**
 * Mock {@link ResourceTransformerFactory} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockResourceTransformerService
		implements ResourceTransformerService, ResourceTransformerFactory, ResourceTransformer {

	/*
	 * ================== ResourceTransformerService ================
	 */

	@Override
	public ResourceTransformerFactory createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================== ResourceTransformerFactory ================
	 */

	@Override
	public String getName() {
		return "mock";
	}

	@Override
	public ResourceTransformer createResourceTransformer() {
		return this;
	}

	/*
	 * ===================== ResourceTransformer =====================
	 */

	@Override
	public void transform(ResourceTransformerContext context) throws IOException {

		// Obtain the resource to transform
		Path resource = context.getResource();

		// Provide content
		Path transformed = context.createFile();
		Files.copy(resource, transformed, StandardCopyOption.REPLACE_EXISTING);
		Writer writer = Files.newBufferedWriter(transformed, StandardOpenOption.APPEND);
		writer.write(" - transformed");
		writer.close();

		// Provide transformed file
		context.setTransformedResource(transformed);
	}

}