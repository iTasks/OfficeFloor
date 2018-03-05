/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpClientTestUtil;
import net.officefloor.server.http.HttpRequest;

/**
 * Tests the WoOF server.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofServerTest extends OfficeFrameTestCase {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can invoke {@link HttpRequest} on the WoOF server.
	 */
	public void testWoofServerDefaultPorts() throws IOException {

		// Open the OfficeFloor (on default ports)
		this.officeFloor = WoOF.open();

		// Create the client
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {

			// Ensure can obtain template
			HttpResponse response = client.execute(new HttpGet("http://localhost:7878/template"));
			assertEquals("Incorrect template", "TEMPLATE", HttpClientTestUtil.entityToString(response));
		}
	}

	/**
	 * Ensure can invoke {@link HttpRequest} on the WoOF server.
	 */
	public void testWoofServerNonDefaultPorts() throws IOException {

		// Open the OfficeFloor (on non-default ports)
		this.officeFloor = WoOF.open(8787, 9797);

		// Create the client
		try (CloseableHttpClient client = HttpClientTestUtil.createHttpClient()) {

			// Ensure can obtain template
			HttpResponse response = client.execute(new HttpGet("http://localhost:8787/template"));
			assertEquals("Incorrect template", "TEMPLATE", HttpClientTestUtil.entityToString(response));
		}
	}

}