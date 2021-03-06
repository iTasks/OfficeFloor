package net.officefloor.tutorial.swaggerhttpserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.OpenAPI;
import net.officefloor.OfficeFloorMain;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.security.scheme.BasicHttpSecuritySource;
import net.officefloor.woof.mock.MockWoofServer;
import net.officefloor.woof.mock.MockWoofServerExtension;

/**
 * Tests the Swagger.
 * 
 * @author Daniel Sagenschneider
 */
public class SwaggerHttpServerTest {

	public static void main(String[] args) throws Exception {
		OfficeFloorMain.main(args);
	}

	@RegisterExtension
	public MockWoofServerExtension server = new MockWoofServerExtension();

	@Test
	public void openApiJson() {
		OpenAPI openApi = this.server.send(MockWoofServer.mockRequest("/openapi.json")).getJson(200, OpenAPI.class,
				Json.mapper());
		assertEquals(4, openApi.getPaths().size(), "Incorrect number of paths");
	}

	@Test
	public void swagger() {
		this.server.send(MockWoofServer.mockRequest("/swagger")).assertStatus(HttpStatus.OK);
	}

	@Test
	public void template() {
		this.server.send(MockWoofServer.mockRequest("/template")).assertResponse(200,
				"<html><body>template</body></html>");
	}

	@Test
	public void get() {
		this.server.send(MockWoofServer.mockRequest("/get?parameter=test")).assertJson(200, new Response("GET test"));
	}

	@Test
	public void post() {
		this.server.send(MockWoofServer.mockJsonRequest(HttpMethod.POST, "/post", new Request(1))).assertJson(200,
				new Response("POST 1"));
	}

	@Test
	public void security() {
		// Ensure secure
		this.server.send(MockWoofServer.mockRequest("/secure")).assertStatus(HttpStatus.UNAUTHORIZED);

		// Ensure can login
		String credentials = BasicHttpSecuritySource.createAuthorizationHttpHeaderValue("test", "test");
		this.server.send(MockWoofServer.mockRequest("/secure").header("authorization", credentials)).assertJson(200,
				new Response("SECURE test"));
	}

}