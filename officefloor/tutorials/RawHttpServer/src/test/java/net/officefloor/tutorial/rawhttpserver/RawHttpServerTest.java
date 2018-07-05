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
package net.officefloor.tutorial.rawhttpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;

import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.woof.mock.MockWoofServerRule;

/**
 * Tests the web application is returning correctly.
 * 
 * @author Daniel Sagenschneider
 */
public class RawHttpServerTest {

	/**
	 * Run application.
	 */
	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	@Rule
	public MockWoofServerRule server = new MockWoofServerRule();

	/**
	 * Ensure able to obtain the Raw HTML.
	 */
	@Test
	public void testRawHtml() throws Exception {

		// Send request for dynamic page
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/example"));
		assertEquals("Request should be successful", 200, response.getStatus().getStatusCode());

		// Ensure raw html rendered to page
		String responseText = response.getEntity(null);
		assertTrue("Should have raw HTML rendered", responseText.contains("Web on OfficeFloor (WoOF)"));
	}

}