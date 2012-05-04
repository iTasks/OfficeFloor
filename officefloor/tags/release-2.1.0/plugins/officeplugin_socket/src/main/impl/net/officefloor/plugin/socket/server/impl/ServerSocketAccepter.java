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

package net.officefloor.plugin.socket.server.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.ConnectionHandler;
import net.officefloor.plugin.socket.server.ServerSocketHandler;
import net.officefloor.plugin.stream.BufferSquirtFactory;

/**
 * Accepts connections.
 * 
 * @author Daniel Sagenschneider
 */
public class ServerSocketAccepter<CH extends ConnectionHandler>
		extends
		AbstractSingleTask<ServerSocketAccepter<CH>, None, ServerSocketAccepter.ServerSocketAccepterFlows> {

	/**
	 * Flow instances for the {@link ServerSocketAccepter}.
	 */
	public static enum ServerSocketAccepterFlows {
		LISTEN
	}

	/**
	 * {@link ConnectionManager}.
	 */
	private final ConnectionManager<CH> connectionManager;

	/**
	 * {@link BufferSquirtFactory}.
	 */
	private final BufferSquirtFactory bufferSquirtFactory;

	/**
	 * {@link ServerSocketHandler}.
	 */
	private final ServerSocketHandler<CH> serverSocketHandler;

	/**
	 * {@link InetSocketAddress} to listen for connections.
	 */
	private InetSocketAddress serverSocketAddress;

	/**
	 * Initiate.
	 * 
	 * @param serverSocketAddress
	 *            {@link InetSocketAddress} to listen for connections.
	 * @param serverSocketHandler
	 *            {@link ServerSocketHandler}.
	 * @param connectionManager
	 *            {@link ConnectionManager}.
	 * @param bufferSquirtFactory
	 *            {@link BufferSquirtFactory}.
	 * @throws IOException
	 *             If fails to set up the {@link ServerSocket}.
	 */
	public ServerSocketAccepter(InetSocketAddress serverSocketAddress,
			ServerSocketHandler<CH> serverSocketHandler,
			ConnectionManager<CH> connectionManager,
			BufferSquirtFactory bufferSquirtFactory) throws IOException {
		this.serverSocketHandler = serverSocketHandler;
		this.connectionManager = connectionManager;
		this.bufferSquirtFactory = bufferSquirtFactory;
		this.serverSocketAddress = serverSocketAddress;
	}

	/**
	 * {@link ServerSocketChannel} to listen for connections.
	 */
	private ServerSocketChannel channel;

	/**
	 * {@link Selector} to aid in listening for connections.
	 */
	private Selector selector;

	/**
	 * Flag indicating if should stop accepting further connections.
	 */
	private volatile boolean isComplete = false;

	/**
	 * Flag indicating if unbound the {@link ServerSocketChannel}.
	 */
	private volatile boolean isUnbound = false;

	/**
	 * Opens and binds the {@link ServerSocketChannel}.
	 * 
	 * @throws IOException
	 *             {@link IOException}.
	 */
	public void bindToSocket() throws IOException {

		// Bind to the socket to start listening
		this.channel = ServerSocketChannel.open();
		this.channel.configureBlocking(false);
		this.channel.socket().bind(this.serverSocketAddress);

		// Register the channel with the selector
		this.selector = Selector.open();
		this.channel.register(this.selector, SelectionKey.OP_ACCEPT);
	}

	/**
	 * Unbinds from the {@link ServerSocketChannel}.
	 */
	public void unbindFromSocket() {

		// Flag to complete processing
		this.isComplete = true;

		// Wait until unbound server socket (or timed out waiting)
		try {
			long startTime = System.currentTimeMillis();
			while ((!this.isUnbound)
					&& ((System.currentTimeMillis() - startTime) < 20000)) {

				// Process accepting connections
				if (this.selector != null) {
					this.selector.wakeup();
				}

				// Wait some time for accepting connections
				Thread.sleep(100);
			}
		} catch (InterruptedException ex) {
			// Assume to be unbound and carry on
		}
	}

	/*
	 * ======================= Task ============================================
	 */

	@Override
	public Object doTask(
			TaskContext<ServerSocketAccepter<CH>, None, ServerSocketAccepterFlows> context)
			throws Exception {

		// Loop accepting connections
		for (;;) {

			// Wait some time for a connection (10 seconds)
			if (this.selector.select(10000) == 0) {

				// Determine if complete
				boolean isComplete = this.isComplete;

				// Flag whether task complete (but allow for closing office)
				context.setComplete(isComplete);

				// Unbind socket if complete
				if (isComplete) {
					try {
						// Close the selector
						this.selector.close();

						// Unbind the socket
						this.channel.socket().close();

					} finally {
						// Ensure flagged unbound
						this.isUnbound = true;
					}
				}

				// Iteration of this task finished
				return null;

			} else {
				// Obtain the selection key
				for (Iterator<SelectionKey> iterator = this.selector
						.selectedKeys().iterator(); iterator.hasNext();) {
					SelectionKey key = iterator.next();
					iterator.remove();

					// Check if accepting a connection
					if (key.isAcceptable()) {

						// Obtain the socket channel
						SocketChannel socketChannel = this.channel.accept();
						if (socketChannel != null) {

							// Flag socket as unblocking
							socketChannel.configureBlocking(false);

							// Create the connection
							ConnectionImpl<CH> connection = new ConnectionImpl<CH>(
									new NonblockingSocketChannelImpl(
											socketChannel),
									this.serverSocketHandler,
									this.bufferSquirtFactory);

							// Register the connection for management
							this.connectionManager.registerConnection(
									connection, context);
						}
					}
				}
			}
		}
	}

	/**
	 * Implementation of the {@link NonblockingSocketChannel}.
	 */
	private static class NonblockingSocketChannelImpl implements
			NonblockingSocketChannel {

		/**
		 * {@link SocketChannel}.
		 */
		private final SocketChannel socketChannel;

		/**
		 * Initiate.
		 * 
		 * @param socketChannel
		 *            {@link SocketChannel}.
		 */
		public NonblockingSocketChannelImpl(SocketChannel socketChannel) {
			this.socketChannel = socketChannel;
		}

		/*
		 * ======================= NonblockingSocketChannel ====================
		 */

		@Override
		public SelectionKey register(Selector selector, int ops,
				Object attachment) throws IOException {
			return this.socketChannel.register(selector, ops, attachment);
		}

		@Override
		public InetSocketAddress getLocalAddress() {
			Socket socket = this.socketChannel.socket();
			return new InetSocketAddress(socket.getLocalAddress(),
					socket.getLocalPort());
		}

		@Override
		public InetSocketAddress getRemoteAddress() {
			Socket socket = this.socketChannel.socket();
			return new InetSocketAddress(socket.getInetAddress(),
					socket.getPort());
		}

		@Override
		public int read(ByteBuffer buffer) throws IOException {
			return this.socketChannel.read(buffer);
		}

		@Override
		public int write(ByteBuffer data) throws IOException {
			return this.socketChannel.write(data);
		}

		@Override
		public void close() throws IOException {
			this.socketChannel.close();
		}
	}

}