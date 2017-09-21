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

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.ssl.OfficeFloorDefaultSslContextSource;

/**
 * Utility class aiding in testing HTTP functionality.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpClientTestUtil {

	/**
	 * Obtains the {@link HttpEntity} content.
	 * 
	 * @param response
	 *            {@link HttpResponse}.
	 * @return Content of {@link HttpEntity}.
	 * @throws IOException
	 *             If fails to obtain content.
	 */
	public static String getEntityBody(HttpResponse response) throws IOException {
		return getEntityBody(response, ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET);
	}

	/**
	 * Obtains the {@link HttpEntity} content.
	 * 
	 * @param response
	 *            {@link HttpResponse}.
	 * @param charset
	 *            {@link Charset}.
	 * @return Content of {@link HttpEntity}.
	 * @throws IOException
	 *             If fails to obtain content.
	 */
	public static String getEntityBody(HttpResponse response, Charset charset) throws IOException {

		// Obtain the entity
		org.apache.http.HttpEntity entity = response.getEntity();
		if (entity == null) {
			return null; // no entity so no content
		}

		// Return the entity body contents
		return EntityUtils.toString(entity, charset);
	}

	/**
	 * Creates a {@link CloseableHttpClient} ready for use with default values.
	 * 
	 * @return {@link CloseableHttpClient}.
	 */
	public static CloseableHttpClient createHttpClient() {
		return createHttpClient(false);
	}

	/**
	 * Creates a {@link CloseableHttpClient} ready for use.
	 * 
	 * @param isSecure
	 *            Indicate if require secure connection.
	 * @return {@link CloseableHttpClient}.
	 */
	public static CloseableHttpClient createHttpClient(boolean isSecure) {

		// Provide timeout of requests
		final int timeout = 1000; // 1 second
		RequestConfig.Builder requestTimeout = RequestConfig.custom();
		requestTimeout.setSocketTimeout(timeout);
		requestTimeout.setConnectTimeout(timeout);
		requestTimeout.setConnectionRequestTimeout(timeout);

		// Create the HTTP client
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setDefaultRequestConfig(requestTimeout.build());

		// Configure to be secure client
		if (isSecure) {
			configureHttps(builder);
		}

		// Create the client
		CloseableHttpClient client = builder.build();

		// Return the client
		return client;
	}

	/**
	 * Configures the {@link HttpClientBuilder} for HTTPS.
	 * 
	 * @param builder
	 *            {@link HttpClientBuilder}.
	 * 
	 * @see #createTestServerSslContext()
	 */
	public static void configureHttps(HttpClientBuilder builder) {
		try {
			// Provide SSL Context
			builder.setSSLContext(OfficeFloorDefaultSslContextSource.createClientSslContext(null));
			builder.setSSLHostnameVerifier(new NoopHostnameVerifier());
		} catch (Exception ex) {
			throw OfficeFrameTestCase.fail(ex);
		}
	}

	/**
	 * Configures no redirects for the {@link HttpClientBuilder}.
	 * 
	 * @param builder
	 *            {@link HttpClientBuilder}.
	 */
	public static void configureNoRedirects(HttpClientBuilder builder) {
		builder.setRedirectStrategy(new RedirectStrategy() {
			@Override
			public boolean isRedirected(HttpRequest request, HttpResponse response, HttpContext context)
					throws ProtocolException {
				// No redirection
				return false;
			}

			@Override
			public HttpUriRequest getRedirect(HttpRequest request, HttpResponse response, HttpContext context)
					throws ProtocolException {
				Assert.fail("Should not need redirect request");
				return null;
			}
		});
	}

	/**
	 * Configures {@link CredentialsProvider} for the {@link HttpClientBuilder}.
	 * 
	 * @param builder
	 *            {@link HttpClientBuilder}.
	 * @param realm
	 *            Security realm.
	 * @param scheme
	 *            Security scheme.
	 * @param username
	 *            User name.
	 * @param password
	 *            Password.
	 * @return {@link CredentialsProvider}.
	 */
	public static CredentialsProvider configureCredentials(HttpClientBuilder builder, String realm, String scheme,
			String username, String password) {

		// Provide credentials
		BasicCredentialsProvider provider = new BasicCredentialsProvider();
		provider.setCredentials(new AuthScope(null, -1, realm, scheme),
				new UsernamePasswordCredentials(username, password));
		builder.setDefaultCredentialsProvider(provider);
				
		// Return the credentials provider
		return provider;
	}

	/**
	 * All access via static methods.
	 */
	private HttpClientTestUtil() {
	}

}