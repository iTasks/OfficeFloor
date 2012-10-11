/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.socket.server.http.conversation.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.parse.HttpRequestParseException;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpHeaderImpl;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.socket.server.protocol.Connection;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
import net.officefloor.plugin.socket.server.protocol.WriteBufferEnum;
import net.officefloor.plugin.stream.ServerOutputStream;
import net.officefloor.plugin.stream.ServerWriter;
import net.officefloor.plugin.stream.WriteBufferReceiver;
import net.officefloor.plugin.stream.impl.ServerOutputStreamImpl;

/**
 * {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResponseImpl implements HttpResponse {

	/**
	 * HTTP end of line sequence (CR, LF).
	 */
	private static final String EOL = "\r\n";

	/**
	 * Name of the header Content-Type.
	 */
	private static final String HEADER_NAME_CONTENT_TYPE = "Content-Type";

	/**
	 * Name of the header Content-Length.
	 */
	private static final String HEADER_NAME_CONTENT_LENGTH = "Content-Length";

	/**
	 * {@link HttpConversationImpl} that this {@link HttpResponse} is involved.
	 */
	private final HttpConversationImpl conversation;

	/**
	 * {@link Connection}.
	 */
	private final Connection connection;

	/**
	 * Send buffer size.
	 */
	private final int sendBufferSize;

	/**
	 * {@link HttpResponseWriteBufferReceiver}.
	 */
	private final HttpResponseWriteBufferReceiver receiver = new HttpResponseWriteBufferReceiver();

	/**
	 * Version.
	 */
	private String version;

	/**
	 * Status code.
	 */
	private int status;

	/**
	 * Status message.
	 */
	private String statusMessage;

	/**
	 * Headers.
	 */
	private final List<HttpHeader> headers = new LinkedList<HttpHeader>();

	/**
	 * {@link ServerOutputStream} containing the entity content.
	 */
	private final ServerOutputStreamImpl entity;

	/**
	 * Indicates if requested the {@link ServerOutputStream}. In other words,
	 * may not use {@link ServerWriter}.
	 */
	private boolean isOutputStream = false;

	/**
	 * Content-Type.
	 */
	private String contentType = null;

	/**
	 * {@link Charset} for the {@link ServerWriter}.
	 */
	private Charset charset;

	/**
	 * Name of the {@link Charset} to use in the {@link HttpResponse}.
	 */
	private String charsetName;

	/**
	 * Cache the {@link ServerWriter}. Also indicates if using the
	 * {@link ServerWriter}.
	 */
	private ServerWriter entityWriter = null;

	/**
	 * Indicates if closed.
	 */
	private boolean isClosed = false;

	/**
	 * Initiate.
	 * 
	 * @param conversation
	 *            {@link HttpConversationImpl}.
	 * @param connection
	 *            {@link Connection}.
	 * @param httpVersion
	 *            HTTP version.
	 * @param sendBufferSize
	 *            Send buffer size.
	 * @param defaultCharset
	 *            Default {@link Charset} for the {@link ServerWriter}.
	 */
	public HttpResponseImpl(HttpConversationImpl conversation,
			Connection connection, String httpVersion, int sendBufferSize,
			Charset defaultCharset) {
		this.conversation = conversation;
		this.connection = connection;
		this.sendBufferSize = sendBufferSize;
		this.charset = defaultCharset;
		this.charsetName = this.charset.name();

		// Specify initial values
		this.version = httpVersion;
		this.status = HttpStatus.SC_OK;
		this.statusMessage = HttpStatus.getStatusMessage(this.status);
		this.entity = new ServerOutputStreamImpl(this.receiver,
				this.sendBufferSize);
	}

	/**
	 * <p>
	 * Queues the {@link HttpResponse} for sending if it is complete.
	 * 
	 * @return <code>true</code> should the {@link HttpResponse} be queued for
	 *         sending.
	 * @throws IOException
	 *             If fails writing {@link HttpResponse} if no need to queue.
	 */
	boolean queueHttpResponseIfComplete() throws IOException {
		return this.receiver.queueHttpResponseIfComplete();
	}

	/**
	 * Flags failure in processing the {@link HttpRequest}.
	 * 
	 * @param failure
	 *            Failure in processing the {@link HttpRequest}.
	 * @throws IOException
	 *             If fails to send the failure response.
	 */
	void sendFailure(Throwable failure) throws IOException {

		// Lock as called from Escalation Handler
		synchronized (this.connection.getLock()) {

			// Clear the response to write the failure
			this.resetUnsafe();

			// Write the failure header details
			if (failure instanceof HttpRequestParseException) {
				// Parse request failure
				HttpRequestParseException parseFailure = (HttpRequestParseException) failure;
				this.setStatus(parseFailure.getHttpStatus());
			} else {
				// Handling request failure
				this.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			}

			// Write the failure response
			ServerWriter writer = this.getEntityWriter();
			String failMessage = failure.getClass().getSimpleName() + ": "
					+ failure.getMessage();
			writer.write(failMessage);
			if (this.conversation.isSendStackTraceOnFailure()) {
				// Provide the stack trace
				writer.write("\n\n");
				PrintWriter stackTraceWriter = new PrintWriter(writer);
				failure.printStackTrace(stackTraceWriter);
				stackTraceWriter.flush();
			}

			// Send the response containing the failure
			this.send();

			// Close the connection
			this.connection.close();
		}
	}

	/**
	 * Writes the {@link HttpResponse} header to the {@link ServerOutputStream}.
	 * 
	 * @param contentLength
	 *            Content length of the entity.
	 * @throws IOException
	 *             If fails to load the content.
	 */
	private void writeHeader(long contentLength) throws IOException {

		// Create output stream to write header
		ServerOutputStream header = new ServerOutputStreamImpl(this.receiver,
				this.sendBufferSize);

		// Provide the Content-Type HTTP header
		if (this.contentType != null) {
			this.addHeader(HEADER_NAME_CONTENT_TYPE, this.contentType
					+ (this.charsetName == null ? "" : "; charset="
							+ this.charsetName));
		}

		// Provide Content-Length HTTP header
		this.headers.add(new HttpHeaderImpl(HEADER_NAME_CONTENT_LENGTH, String
				.valueOf(contentLength)));

		// Ensure appropriate successful status for no content
		if ((contentLength == 0) && (this.status == HttpStatus.SC_OK)) {
			this.setStatus(HttpStatus.SC_NO_CONTENT);
		}

		// Write the status line
		writeUsAscii(this.version + " " + String.valueOf(this.status) + " "
				+ this.statusMessage + EOL, header);

		// Write the headers
		for (HttpHeader httpHeader : this.headers) {
			String name = httpHeader.getName();
			String value = httpHeader.getValue();
			writeUsAscii(name + ": " + (value == null ? "" : value) + EOL,
					header);
		}
		writeUsAscii(EOL, header);

		// Flush the data
		header.flush();
	}

	/**
	 * Writes the value as US-ASCII to the {@link ServerOutputStream}.
	 * 
	 * @param value
	 *            Value.
	 * @param outputStream
	 *            {@link ServerOutputStream}.
	 * @throws IOException
	 *             If fails to write the value.
	 */
	private static void writeUsAscii(String value,
			ServerOutputStream outputStream) throws IOException {
		outputStream.write(value.getBytes(HttpRequestParserImpl.US_ASCII));
	}

	/**
	 * Resets the {@link HttpResponse} without lock.
	 * 
	 * @throws IOException
	 *             If fails to reset.
	 */
	private void resetUnsafe() throws IOException {

		// Clear the response to write the failure
		this.headers.clear();
		this.entity.clear();
		this.receiver.entityBuffers.clear();
		this.isOutputStream = false;
		this.entityWriter = null;
	}

	/*
	 * ================ HttpResponse =======================================
	 */

	@Override
	public void setVersion(String version) {

		synchronized (this.connection.getLock()) {

			// Specify the version
			this.version = version;
		}
	}

	@Override
	public void setStatus(int status) {

		// Obtain the status message
		String message = HttpStatus.getStatusMessage(status);

		// Set status and message
		this.setStatus(status, message);
	}

	@Override
	public void setStatus(int status, String statusMessage) {

		synchronized (this.connection.getLock()) {

			// Specify the status
			this.status = status;
			this.statusMessage = statusMessage;
		}
	}

	@Override
	public void reset() throws IOException {

		synchronized (this.connection.getLock()) {

			// Reset the response
			this.resetUnsafe();
		}
	}

	@Override
	public HttpHeader addHeader(String name, String value) {

		// Create the HTTP header
		HttpHeader header = new HttpHeaderImpl(name, value);

		// Ignore specifying content length
		if (HEADER_NAME_CONTENT_LENGTH.equalsIgnoreCase(name)) {
			return header;
		}

		// Add the header
		synchronized (this.connection.getLock()) {
			this.headers.add(header);
		}

		// Return the added header
		return header;
	}

	@Override
	public HttpHeader getHeader(String name) {

		synchronized (this.connection.getLock()) {

			// Search for the first header by the name
			for (HttpHeader header : this.headers) {
				if (name.equalsIgnoreCase(header.getName())) {
					// Found first header so return it
					return header;
				}
			}
		}

		// As here did not find header by name
		return null;
	}

	@Override
	public HttpHeader[] getHeaders() {

		synchronized (this.connection.getLock()) {

			// Create the array of headers
			HttpHeader[] headers;
			synchronized (this.connection.getLock()) {
				headers = this.headers.toArray(new HttpHeader[0]);
			}

			// Return the headers
			return headers;
		}
	}

	@Override
	public void removeHeader(HttpHeader header) {

		synchronized (this.connection.getLock()) {

			// Remove the header
			this.headers.remove(header);
		}
	}

	@Override
	public void removeHeaders(String name) {

		synchronized (this.connection.getLock()) {

			// Remove all headers by name
			for (Iterator<HttpHeader> iterator = this.headers.iterator(); iterator
					.hasNext();) {
				HttpHeader header = iterator.next();
				if (name.equalsIgnoreCase(header.getName())) {
					// Remove the header
					iterator.remove();
				}
			}
		}
	}

	@Override
	public ServerOutputStream getEntity() throws IOException {

		synchronized (this.receiver.getLock()) {

			// Ensure not using writer
			if (this.entityWriter != null) {
				throw new IOException(
						"getEntityWriter() has already been invoked");
			}

			// Flag using the output stream
			this.isOutputStream = true;

			// Return the entity
			return this.entity;
		}
	}

	@Override
	public void setContentType(String contentType) {

		synchronized (this.receiver.getLock()) {

			// Specify the content type
			this.contentType = contentType;
		}
	}

	@Override
	public void setContentCharset(Charset charset, String charsetName)
			throws IOException {

		synchronized (this.receiver.getLock()) {

			// Ensure not using entity writer
			if (this.entityWriter != null) {
				throw new IOException(
						"getEntityWriter() has already been invoked");
			}

			// Specify the charset
			this.charset = charset;
			this.charsetName = charsetName;
		}
	}

	@Override
	public ServerWriter getEntityWriter() throws IOException {

		synchronized (this.receiver.getLock()) {

			// Ensure not using output stream
			if (this.isOutputStream) {
				throw new IOException("getEntity() has already been invoked");
			}

			// Provide the default content type
			if (this.contentType == null) {
				this.contentType = "text/html";
			}

			// Create and return the entity writer
			this.entityWriter = new ServerWriter(this.entity, this.charset,
					this.receiver.getLock());
			return this.entityWriter;

		}
	}

	@Override
	public void send() throws IOException {

		synchronized (this.receiver.getLock()) {

			// Close the entity which triggers sending response
			if (this.entityWriter != null) {
				this.entityWriter.close();
			} else {
				this.entity.close();
			}
		}
	}

	/**
	 * {@link HttpResponse} {@link WriteBufferReceiver}.
	 */
	private class HttpResponseWriteBufferReceiver implements
			WriteBufferReceiver {

		/**
		 * HTTP header {@link WriteBuffer} instances.
		 */
		private WriteBuffer[] headerBuffers = null;

		/**
		 * Indicates if writing the HTTP header.
		 */
		private boolean isWritingHeader = false;

		/**
		 * Entity {@link WriteBuffer} instances.
		 */
		private final List<WriteBuffer> entityBuffers = new LinkedList<WriteBuffer>();

		/**
		 * Queues the {@link HttpResponse} for sending if complete.
		 * 
		 * @return <code>true</code> if queued for sending.
		 * @throws IOException
		 *             If fails writing {@link HttpResponse} if no need to
		 *             queue.
		 */
		public boolean queueHttpResponseIfComplete() throws IOException {

			// Ensure is closed (ie complete ready for sending)
			if (!this.isClosed()) {
				return false;
			}

			// Write the data for the response
			WriteBuffer[] responseData = new WriteBuffer[this.headerBuffers.length
					+ this.entityBuffers.size()];
			int index = 0;
			for (WriteBuffer buffer : this.headerBuffers) {
				responseData[index++] = buffer;
			}
			for (WriteBuffer buffer : this.entityBuffers) {
				responseData[index++] = buffer;
			}

			// Queue the HTTP response for sending
			HttpResponseImpl.this.connection.writeData(responseData);

			// Queued for sending
			return true;
		}

		/*
		 * ==================== WriteBufferReceiver =================
		 * 
		 * Thread safe as called within lock of ServerOutputStream
		 */

		@Override
		public Object getLock() {
			return HttpResponseImpl.this.connection.getLock();
		}

		@Override
		public WriteBuffer createWriteBuffer(byte[] data, int length) {
			return HttpResponseImpl.this.connection.createWriteBuffer(data,
					length);
		}

		@Override
		public WriteBuffer createWriteBuffer(ByteBuffer buffer) {
			return HttpResponseImpl.this.connection.createWriteBuffer(buffer);
		}

		@Override
		public void writeData(WriteBuffer[] data) {

			// Determine if writing the header
			if (this.isWritingHeader) {
				this.headerBuffers = data;

			} else {
				// Cache data of entity being written
				for (WriteBuffer buffer : data) {
					this.entityBuffers.add(buffer);
				}
			}
		}

		@Override
		public void close() throws IOException {

			// Note: all data should now be flushed

			// Calculate the content length
			long contentLength = 0;
			for (WriteBuffer buffer : this.entityBuffers) {
				WriteBufferEnum type = buffer.getType();
				switch (type) {
				case BYTE_ARRAY:
					contentLength += buffer.length();
					break;

				case BYTE_BUFFER:
					contentLength += buffer.getDataBuffer().remaining();
					break;

				default:
					throw new IllegalStateException("Unknown "
							+ WriteBuffer.class.getSimpleName() + " type: "
							+ type);
				}
			}

			// Write the header
			this.isWritingHeader = true;
			HttpResponseImpl.this.writeHeader(contentLength);

			// Flag now closed
			HttpResponseImpl.this.isClosed = true;

			// Attempt to queue the HTTP response for sending
			HttpResponseImpl.this.conversation.queueCompleteResponses();
		}

		@Override
		public boolean isClosed() {
			return HttpResponseImpl.this.isClosed;
		}
	}

}