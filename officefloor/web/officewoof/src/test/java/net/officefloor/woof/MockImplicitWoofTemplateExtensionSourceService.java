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

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

import org.junit.Assert;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.woof.template.WoofTemplateExtensionSource;
import net.officefloor.woof.template.WoofTemplateExtensionSourceContext;
import net.officefloor.woof.template.WoofTemplateExtensionSourceService;
import net.officefloor.woof.template.impl.AbstractWoofTemplateExtensionSource;

/**
 * Mock implicit {@link WoofTemplateExtensionSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockImplicitWoofTemplateExtensionSourceService extends AbstractWoofTemplateExtensionSource
		implements WoofTemplateExtensionSourceService {

	/**
	 * Resets for testing loading implicit {@link WoofTemplateExtensionSource}.
	 * 
	 * @param templateUris
	 *            URIs of the {@link WebTemplate} instances being extended.
	 */
	public static void reset(String... templateUris) {
		MockImplicitWoofTemplateExtensionSourceService.templateApplicationPaths = new LinkedList<String>(
				Arrays.asList(templateUris));
	}

	/**
	 * Expected {@link WebTemplate} URIs.
	 */
	private static Deque<String> templateApplicationPaths = null;

	/*
	 * =================== WoofTemplateExtensionSourceService ==================
	 */

	@Override
	public WoofTemplateExtensionSource createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================== WoofTemplateExtensionSourceService ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		Assert.fail("Should not require specification");
	}

	@Override
	public void extendTemplate(WoofTemplateExtensionSourceContext context) throws Exception {

		// Obtain the template application path
		String applicationPath = context.getApplicationPath();

		// Ensure expecting the template
		String expectedApplicationPath = templateApplicationPaths.pollFirst();
		Assert.assertNotNull("Not expecting template " + applicationPath, expectedApplicationPath);
		Assert.assertEquals("Incorrect template application path for extension", expectedApplicationPath,
				applicationPath);
	}

}