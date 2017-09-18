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
package net.officefloor.server.http.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpServer;
import net.officefloor.server.http.HttpServerImplementation;
import net.officefloor.server.http.HttpServerImplementationContext;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.WritableHttpHeader;
import net.officefloor.server.http.impl.HttpResponseWriter;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeader;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ByteArrayByteSequence;
import net.officefloor.server.stream.impl.ByteSequence;
import net.officefloor.server.stream.impl.ThreadLocalStreamBufferPool;

/**
 * Mock {@link HttpServer}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHttpServer implements HttpServerImplementation {

	/**
	 * {@link ThreadLocalStreamBufferPool} to use for stress testing.
	 */
	private static ThreadLocalStreamBufferPool STRESS_STREAM_BUFFER_POOL = new ThreadLocalStreamBufferPool(
			() -> ByteBuffer.allocate(1024), Integer.MAX_VALUE, Integer.MAX_VALUE);

	/**
	 * Configures the {@link MockHttpServer} to be serviced by the
	 * {@link DeployedOfficeInput}.
	 * 
	 * @param input
	 *            {@link DeployedOfficeInput}.
	 * @return {@link MockHttpServer} to send {@link HttpRequest} instances.
	 */
	public static MockHttpServer configureMockHttpServer(DeployedOfficeInput input) {
		MockHttpServer httpServer = new MockHttpServer();
		HttpServer.configureHttpServer(-1, -1, httpServer, null, input, null, null);
		return httpServer;
	}

	/**
	 * {@link ExternalServiceInput}.
	 */
	private ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>> serviceInput;

	/**
	 * Instantiated via static methods.
	 */
	private MockHttpServer() {
	}

	/**
	 * Creates the {@link MockHttpRequestBuilder}.
	 * 
	 * @return {@link MockHttpRequestBuilder}.
	 */
	public MockHttpRequestBuilder createMockHttpRequest() {
		return new MockHttpRequestBuilderImpl();
	}

	/**
	 * Sends the {@link MockHttpRequestBuilder}.
	 * 
	 * @param request
	 *            {@link MockHttpRequestBuilder}.
	 * @param callback
	 *            {@link MockHttpRequestCallback} to receive the
	 *            {@link MockHttpResponse}.
	 */
	public void send(MockHttpRequestBuilder request, MockHttpRequestCallback callback) {

		// Ensure have implementation
		if (!(request instanceof MockHttpRequestBuilderImpl)) {
			throw new IllegalArgumentException("Must create request with createMockHttpRequest()");
		}
		MockHttpRequestBuilderImpl impl = (MockHttpRequestBuilderImpl) request;

		// Create the inputs
		boolean isSecure = impl.isSecure;
		Supplier<HttpMethod> methodSupplier = () -> impl.method;
		Supplier<String> requestUriSupplier = () -> impl.requestUri;
		HttpVersion requestVersion = impl.version;
		NonMaterialisedHttpHeaders requestHeaders = impl;
		ByteSequence requestEntity = new ByteArrayByteSequence(impl.entity.toByteArray());

		// Provide the buffer pool (based on efficiency)
		StreamBufferPool<ByteBuffer> bufferPool;
		if (impl.isStress) {
			bufferPool = STRESS_STREAM_BUFFER_POOL;
		} else {
			// Provide mock buffer pool (for checks)
			bufferPool = new MockStreamBufferPool(() -> ByteBuffer.allocate(1024));
		}

		// Handle response
		HttpResponseWriter<ByteBuffer> responseWriter = (responseVersion, status, responseHttpHeader,
				httpEntityContentLength, httpEntityContentType, responseHttpEntity) -> {
			try {

				// Obtain the listing of response HTTP headers
				List<WritableHttpHeader> headers = new ArrayList<>();
				while (responseHttpHeader != null) {
					headers.add(responseHttpHeader);
					responseHttpHeader = responseHttpHeader.next;
				}

				// Copy out the response entity
				InputStream responseEntityInputStream = MockStreamBufferPool.createInputStream(responseHttpEntity);
				byte[] responseEntity = new byte[(int) httpEntityContentLength];
				for (int i = 0; i < httpEntityContentLength; i++) {
					responseEntity[i] = (byte) responseEntityInputStream.read();
				}

				// Release all the buffers (as now considered written)
				StreamBuffer<ByteBuffer> buffer = responseHttpEntity;
				while (buffer != null) {
					StreamBuffer<ByteBuffer> release = buffer;
					buffer = buffer.next;
					release.release();
				}

				// Ensure all buffers released (if not stress test)
				if (!impl.isStress) {
					((MockStreamBufferPool) bufferPool).assertAllBuffersReturned();
				}

				// Create the response
				MockHttpResponseImpl response = new MockHttpResponseImpl(responseVersion, status, headers,
						new ByteArrayInputStream(responseEntity));

				// Response received
				callback.response(response);

			} catch (Throwable ex) {
				// Provide failed HTTP response
				callback.response(new MockHttpResponseImpl(ex));
			}
		};

		// Create the server HTTP connection
		ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<>(
				isSecure, methodSupplier, requestUriSupplier, requestVersion, requestHeaders, requestEntity,
				responseWriter, bufferPool);

		// Service the request
		this.serviceInput.service(connection, connection.getServiceFlowCallback());
	}

	/**
	 * Sends the {@link MockHttpRequestBuilder} and blocks waiting for the
	 * {@link MockHttpResponse}.
	 * 
	 * @param request
	 *            {@link MockHttpRequestBuilder}.
	 * @eturn {@link MockHttpResponse}.
	 */
	public MockHttpResponse send(MockHttpRequestBuilder request) {

		// Create the synchronous callback
		SynchronousMockHttpRequestCallback callback = new SynchronousMockHttpRequestCallback();

		// Undertake the request
		this.send(request, callback);

		// Block waiting for response
		return callback.waitForResponse(1000);
	}

	/*
	 * ================= HttpServerImplementation ==================
	 */

	@Override
	public void configureHttpServer(HttpServerImplementationContext context) {
		@SuppressWarnings("unchecked")
		Class<ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>> managedObjectType = (Class<ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>>) (Object) ProcessAwareServerHttpConnectionManagedObject.class;
		this.serviceInput = context.getExternalServiceInput(managedObjectType,
				ProcessAwareServerHttpConnectionManagedObject.getCleanupEscalationHandler());
	}

	/**
	 * {@link MockHttpRequestBuilder} implementation.
	 */
	private static class MockHttpRequestBuilderImpl implements MockHttpRequestBuilder, NonMaterialisedHttpHeaders {

		/**
		 * Indicates if secure.
		 */
		private boolean isSecure = false;

		/**
		 * {@link HttpMethod}.
		 */
		private HttpMethod method = HttpMethod.GET;

		/**
		 * Request URI.
		 */
		private String requestUri = "/";

		/**
		 * {@link HttpVersion}.
		 */
		private HttpVersion version = HttpVersion.HTTP_1_1;

		/**
		 * {@link HttpHeader} instances.
		 */
		private final List<MockNonMaterialisedHttpHeader> headers = new LinkedList<>();

		/**
		 * HTTP entity.
		 */
		private final ByteArrayOutputStream entity = new ByteArrayOutputStream();

		/**
		 * Indicates if running stress test for request.
		 */
		private boolean isStress = false;

		/*
		 * ================== MockHttpRequestBuilder ========================
		 */

		@Override
		public void setSecure(boolean isSecure) {
			this.isSecure = isSecure;
		}

		@Override
		public void setHttpMethod(HttpMethod method) {
			this.method = method;
		}

		@Override
		public void setRequestUri(String requestUri) {
			this.requestUri = requestUri;
		}

		@Override
		public void setHttpVersion(HttpVersion version) {
			this.version = version;
		}

		@Override
		public void addHttpHeader(String name, String value) {
			this.headers.add(new MockNonMaterialisedHttpHeader(name, value));
		}

		@Override
		public OutputStream getHttpEntity() {
			return this.entity;
		}

		@Override
		public void setEfficientForStressTests(boolean isStress) {
			this.isStress = isStress;
		}

		/*
		 * ================== NonMaterialisedHttpHeaders =====================
		 */

		@Override
		public Iterator<NonMaterialisedHttpHeader> iterator() {
			final Iterator<MockNonMaterialisedHttpHeader> iterator = this.headers.iterator();
			return new Iterator<NonMaterialisedHttpHeader>() {
				@Override
				public boolean hasNext() {
					return iterator.hasNext();
				}

				@Override
				public NonMaterialisedHttpHeader next() {
					return iterator.next();
				}
			};
		}

		@Override
		public int length() {
			return this.headers.size();
		}
	}

	/**
	 * {@link NonMaterialisedHttpHeader}.
	 */
	private static class MockNonMaterialisedHttpHeader implements NonMaterialisedHttpHeader, HttpHeader {

		/**
		 * {@link HttpHeader} name.
		 */
		private final String name;

		/**
		 * {@link HttpHeader} value.
		 */
		private final String value;

		/**
		 * Instantiate.
		 * 
		 * @param name
		 *            {@link HttpHeader} name.
		 * @param value
		 *            {@link HttpHeader} value.
		 */
		public MockNonMaterialisedHttpHeader(String name, String value) {
			this.name = name;
			this.value = value;
		}

		/*
		 * ================= NonMaterialisedHttpHeader ========================
		 */

		@Override
		public HttpHeader materialiseHttpHeader() {
			return this;
		}

		/*
		 * ======================== HttpHeader ================================
		 */

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public String getValue() {
			return this.value;
		}
	}

	/**
	 * {@link MockHttpResponse} implementation.
	 */
	private static class MockHttpResponseImpl implements MockHttpResponse {

		/**
		 * {@link HttpVersion}.
		 */
		private final HttpVersion version;

		/**
		 * {@link HttpStatus}.
		 */
		private final HttpStatus status;

		/**
		 * {@link HttpHeader} instances.
		 */
		private final List<WritableHttpHeader> headers;

		/**
		 * HTTP entity {@link InputStream}.
		 */
		private final InputStream entityInputStream;

		/**
		 * Possible failure in writing the response.
		 */
		private final Throwable failure;

		/**
		 * Loads the response.
		 * 
		 * @param version
		 *            {@link HttpVersion}.
		 * @param status
		 *            {@link HttpStatus}.
		 * @param headers
		 *            {@link List} of {@link HttpHeader} instances.
		 * @param entityInputStream
		 *            HTTP entity {@link InputStream}.
		 */
		private MockHttpResponseImpl(HttpVersion version, HttpStatus status, List<WritableHttpHeader> headers,
				InputStream entityInputStream) {
			this.version = version;
			this.status = status;
			this.headers = headers;
			this.entityInputStream = entityInputStream;
			this.failure = null;
		}

		/**
		 * Loads with response failure.
		 * 
		 * @param version
		 *            {@link HttpVersion}.
		 * @param status
		 *            {@link HttpStatus}.
		 * @param headers
		 *            {@link List} of {@link HttpHeader} instances.
		 * @param entityInputStream
		 *            HTTP entity {@link InputStream}.
		 */
		private MockHttpResponseImpl(Throwable failure) {
			this.failure = failure;
			this.version = null;
			this.status = null;
			this.headers = null;
			this.entityInputStream = null;
		}

		/**
		 * Ensures no failure in writing the response.
		 */
		private void ensureNoFailure() {
			if (this.failure != null) {
				throw OfficeFrameTestCase.fail(this.failure);
			}
		}

		/*
		 * =============== MockHttpResponse =======================
		 */

		@Override
		public HttpVersion getHttpVersion() {
			this.ensureNoFailure();
			return this.version;
		}

		@Override
		public HttpStatus getHttpStatus() {
			this.ensureNoFailure();
			return this.status;
		}

		@Override
		public List<WritableHttpHeader> getHttpHeaders() {
			this.ensureNoFailure();
			return this.headers;
		}

		@Override
		public InputStream getHttpEntity() {
			this.ensureNoFailure();
			return this.entityInputStream;
		}

		@Override
		public String getHttpEntity(Charset charset) {
			this.ensureNoFailure();

			// Read in the contents
			StringWriter writer = new StringWriter();
			try {
				InputStreamReader reader = new InputStreamReader(this.entityInputStream, charset);
				for (int character = reader.read(); character != -1; character = reader.read()) {
					writer.write(character);
				}
				writer.flush();
			} catch (IOException ex) {
				throw OfficeFrameTestCase.fail(ex);
			}

			// Return the entity as text
			return writer.toString();
		}
	}

	/**
	 * {@link MockHttpRequestCallback} to enable synchronous invocation.
	 */
	private static class SynchronousMockHttpRequestCallback implements MockHttpRequestCallback {

		/**
		 * {@link MockHttpResponse}.
		 */
		private MockHttpResponse response = null;

		/**
		 * Wait for the {@link MockHttpResponse}.
		 * 
		 * @param maxWaitTimeInMilliseconds
		 *            Max wait time in milliseconds.
		 * @return {@link MockHttpResponse}.
		 */
		private synchronized MockHttpResponse waitForResponse(int maxWaitTimeInMilliseconds) {

			// Wait for the response
			try {
				long startTime = System.currentTimeMillis();
				while (this.response == null) {

					// Determine if timed out
					long runTimeInSeconds = (System.currentTimeMillis() - startTime);
					if (runTimeInSeconds > maxWaitTimeInMilliseconds) {
						throw new Error("Timed out waiting for " + MockHttpResponse.class.getSimpleName());
					}

					// Wait some time for response
					this.wait(10);
				}
			} catch (InterruptedException ex) {
				throw new Error(ex);
			}

			// Return the response
			return this.response;
		}

		/*
		 * ==================== MockHttpRequestCallback =======================
		 */

		@Override
		public synchronized void response(MockHttpResponse response) {

			// Specify response
			this.response = response;

			// Notify have response
			this.notify();
		}
	}

}