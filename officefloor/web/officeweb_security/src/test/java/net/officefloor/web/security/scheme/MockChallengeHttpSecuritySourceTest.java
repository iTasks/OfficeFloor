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
package net.officefloor.web.security.scheme;

import java.io.IOException;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.type.HttpSecurityLoaderUtil;
import net.officefloor.web.security.type.HttpSecurityTypeBuilder;
import net.officefloor.web.spi.security.HttpSecurity;

/**
 * Tests the {@link BasicHttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockChallengeHttpSecuritySourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		HttpSecurityLoaderUtil.validateSpecification(MockChallengeHttpSecuritySource.class, "realm", "Realm");
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		HttpSecurityTypeBuilder type = HttpSecurityLoaderUtil.createHttpSecurityTypeBuilder();
		type.setAuthenticationClass(MockAuthentication.class);
		type.setAccessControlClass(MockAccessControl.class);

		// Validate type
		HttpSecurityLoaderUtil.validateHttpSecurityType(type, MockChallengeHttpSecuritySource.class, "realm", "test");
	}

	/**
	 * Ensure can ratify from cached {@link HttpAccessControl}.
	 */
	public void testRatifyFromSession() throws IOException {

		final MockHttpRatifyContext<MockAccessControl> ratifyContext = new MockHttpRatifyContext<>();
		new MockAccessControl("scheme", "user", null);

		// Make access control available in session
		final MockAccessControl accessControl = new MockAccessControl("test", "test");
		ratifyContext.getSession().setAttribute("http.security.mock.challenge", accessControl);

		// Create and initialise the security
		HttpSecurity<MockAuthentication, MockAccessControl, Void, None, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockChallengeHttpSecuritySource.class, "realm", "test");

		// Undertake ratify
		assertFalse("Should not need to authenticate as cached", security.ratify(null, ratifyContext));
		assertSame("Incorrect access control", accessControl, ratifyContext.getAccessControl());
	}

	/**
	 * Ensure can ratify if have authorization header.
	 */
	public void testRatifyWithAuthorizationHeader() throws IOException {

		final MockHttpRatifyContext<MockAccessControl> ratifyContext = new MockHttpRatifyContext<>(
				"Mock daniel,daniel");

		// Create and initialise the security
		HttpSecurity<MockAuthentication, MockAccessControl, Void, None, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockChallengeHttpSecuritySource.class, "realm", "test");

		// Undertake ratify
		assertTrue("Should indicate that may attempt to authenticate", security.ratify(null, ratifyContext));
		assertNull("Should not yet have security", ratifyContext.getAccessControl());
	}

	/**
	 * Ensure ratify indicates no authentication credentials.
	 */
	public void testRatifyNoAuthentication() throws IOException {

		final MockHttpRatifyContext<MockAccessControl> ratifyContext = new MockHttpRatifyContext<>();

		// Create and initialise the security
		HttpSecurity<MockAuthentication, MockAccessControl, Void, None, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockChallengeHttpSecuritySource.class, "realm", "test");

		// Undertake ratify
		assertFalse("Should not attempt authentication", security.ratify(null, ratifyContext));
		assertNull("Should not yet have security", ratifyContext.getAccessControl());
	}

	/**
	 * Ensure can load challenge.
	 */
	public void testChallenge() throws IOException {

		final MockHttpChallengeContext<None, None> challengeContext = new MockHttpChallengeContext<>();

		// Test
		this.replayMockObjects();

		// Create and initialise the security
		HttpSecurity<MockAuthentication, MockAccessControl, Void, None, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockChallengeHttpSecuritySource.class, "realm", "Test");

		// Undertake the challenge
		security.challenge(challengeContext);

		// Verify mock objects
		this.verifyMockObjects();

		// Ensure correct challenge
		assertEquals("Incorrect challenge", "Mock realm=\"Test\"", challengeContext.getChallenge());
	}

	/**
	 * Ensure not authenticated with no authorization header.
	 */
	public void testNoAuthorizationHeader() throws Exception {
		this.doAuthenticate(null, null);
	}

	/**
	 * Ensure handle incorrect authentication scheme.
	 */
	public void testIncorrectAuthenticationScheme() throws Exception {
		this.doAuthenticate("Incorrect scheme", null);
	}

	/**
	 * Ensure handle invalid Base64 encoding.
	 */
	public void testInvalidAuthorizationHeader() throws Exception {
		this.doAuthenticate("Mock wrong", null);
	}

	/**
	 * Ensure can authenticate.
	 */
	public void testSimpleAuthenticate() throws Exception {
		this.doAuthenticate("Mock daniel,daniel", "daniel", "daniel");
	}

	/**
	 * Ensure can authenticate with multiple roles.
	 */
	public void testMultipleRoleAuthenticate() throws Exception {
		this.doAuthenticate("Mock daniel,daniel,founder,another", "daniel", "daniel", "founder", "another");
	}

	/**
	 * Ensure can extra spacing.
	 */
	public void testExtraSpacing() throws Exception {
		this.doAuthenticate("  Mock    daniel , daniel  ", "daniel", "daniel");
	}

	/**
	 * Ensure can log out.
	 */
	public void testLogout() throws Exception {

		final MockHttpLogoutContext<None> logoutContext = new MockHttpLogoutContext<None>();

		// Provide access control in session
		logoutContext.getSession().setAttribute("http.security.mock.challenge", new MockAccessControl("test"));

		// Create and initialise the security
		HttpSecurity<MockAuthentication, MockAccessControl, Void, None, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockChallengeHttpSecuritySource.class, "realm", "test");

		// Logout
		security.logout(logoutContext);

		// Ensure access control removed from session
		assertNull("Should clear access control",
				logoutContext.getSession().getAttribute("http.security.mock.challenge"));
	}

	/**
	 * Undertakes the authentication.
	 * 
	 * @param authoriseHttpHeaderValue <code>Authorize</code> {@link HttpHeader}
	 *                                 value.
	 * @param userName                 User name if authenticated. <code>null</code>
	 *                                 if not authenticated.
	 * @param roles                    Expected roles.
	 */
	private void doAuthenticate(String authoriseHttpHeaderValue, String userName, String... roles) throws IOException {

		// Create the authentication context
		MockHttpAuthenticateContext<MockAccessControl, None> authenticationContext = new MockHttpAuthenticateContext<MockAccessControl, None>(
				authoriseHttpHeaderValue);

		// Create and initialise the source
		HttpSecurity<MockAuthentication, MockAccessControl, Void, None, None> security = HttpSecurityLoaderUtil
				.loadHttpSecurity(MockChallengeHttpSecuritySource.class, "realm", "test");

		// Undertake the authenticate
		security.authenticate(null, authenticationContext);

		// Validate authentication
		MockAccessControl accessControl = authenticationContext.getAccessControl();
		MockAccessControl sessionAccessControl = (MockAccessControl) authenticationContext.getSession()
				.getAttribute("http.security.mock.challenge");
		if (userName == null) {
			assertNull("Should not be authenticated", accessControl);
			assertNull("Should not load session", sessionAccessControl);

		} else {
			assertNotNull("Should be authenticated", accessControl);
			assertEquals("Incorrect authentication scheme", "Mock", accessControl.getAuthenticationScheme());
			assertEquals("Incorrect user", userName, accessControl.getUserName());
			for (String role : roles) {
				assertTrue("Should have role: " + role, accessControl.getRoles().contains(role));
			}
			assertSame("Incorrect session access control", accessControl, sessionAccessControl);
		}
	}

}