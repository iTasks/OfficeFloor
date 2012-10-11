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

package net.officefloor.plugin.web.http.route.source;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationMangedObject;
import net.officefloor.plugin.web.http.location.IncorrectHttpRequestContextPathException;
import net.officefloor.plugin.web.http.route.source.HttpRouteTask.HttpRouteTaskDependencies;

/**
 * Tests the {@link HttpRouteTask}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRouteTaskTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link TaskContext}.
	 */
	@SuppressWarnings("unchecked")
	private TaskContext<HttpRouteTask, HttpRouteTaskDependencies, Indexed> taskContext = this
			.createMock(TaskContext.class);

	/**
	 * Mock {@link ServerHttpConnection}.
	 */
	private ServerHttpConnection serverHttpConnection = this
			.createMock(ServerHttpConnection.class);

	/**
	 * Mock {@link FlowFuture}.
	 */
	private FlowFuture flowFuture = this.createMock(FlowFuture.class);

	/**
	 * Mock {@link HttpRequest}.
	 */
	private HttpRequest httpRequest = this.createMock(HttpRequest.class);

	/**
	 * {@link HttpApplicationLocation}.
	 */
	private HttpApplicationLocation location = this
			.createMock(HttpApplicationLocation.class);

	/**
	 * Ensures able to route {@link HttpRequest}.
	 */
	public void testRoute() throws Throwable {
		this.doRouteTest("/path", 0, "/path");
	}

	/**
	 * Ensures able to route {@link HttpRequest} to second flow.
	 */
	public void testRouteToSecond() throws Throwable {
		this.doRouteTest("/second", 1, "/first", "/second", "/third");
	}

	/**
	 * Ensures falls through to default route.
	 */
	public void testDefaultRoute() throws Throwable {
		this.doRouteTest("/default", 1, "/notmatch");
	}

	/**
	 * Ensures to able route {@link HttpRequest} with parameters on the path.
	 */
	public void testRouteWithParameters() throws Throwable {
		this.doRouteTest("/path?param=value", 0, "/path");
	}

	/**
	 * Ensures able to route {@link HttpRequest} with fragment on path.
	 */
	public void testRouteWithFragment() throws Throwable {
		this.doRouteTest("/path#fragment", 0, "/path");
	}

	/**
	 * Ensure able to route {@link HttpRequest} with protocol and domain on
	 * path.
	 */
	public void testRouteWithProtocolDomain() throws Throwable {
		this.doRouteTest("http://www.officefloor.net/path/", 0, "/path");
	}

	/**
	 * Ensures to able route {@link HttpRequest} with no path to root.
	 */
	public void testRouteNoPath() throws Throwable {
		this.doRouteTest("", 0); // not matched
	}

	/**
	 * Ensures able to route {@link HttpRequest} with <code>null</code> path.
	 */
	public void testRouteNullPath() throws Throwable {
		this.doRouteTest(null, 0, "/"); // root path
	}

	/**
	 * Ensures able to route {@link HttpRequest} with root path.
	 */
	public void testRouteRoot() throws Throwable {
		this.doRouteTest("/", 0, "/");
	}

	/**
	 * Ensure able to route {@link HttpRequest} with default root path.
	 */
	public void testRouteDefaultRoot() throws Throwable {
		this.doRouteTest("", 0, "/");
	}

	/**
	 * Ensure able to route {@link HttpRequest} by extension.
	 */
	public void testRouteByExtension() throws Throwable {
		this.doRouteTest("/path.do", 0, ".*\\.do");
	}

	/**
	 * Ensure able to route by regular expression.
	 */
	public void testRouteByRegularExpression() throws Throwable {
		// Should not match on first but then on second
		this.doRouteTest("/path.do", 1, "/path", "/path.+");
	}

	/**
	 * Ensure routes on canonical paths.
	 */
	public void testRouteByCanonicalPath() throws Throwable {
		this.doRouteTest("/./path/../route/", 0, "/route");
	}

	/**
	 * Ensure send to not handled if incorrect context path.
	 */
	public void testIncorrectContextPath() throws Throwable {
		this.doRouteTest("/noContext", 1, "/noContext");
	}

	/**
	 * Does the routing test.
	 * 
	 * @param path
	 *            Path on the {@link HttpRequest}.
	 * @param flowIndex
	 *            Index of expected flow to invoke.
	 * @param routingPatterns
	 *            Listing of routing patterns.
	 */
	private void doRouteTest(String path, int flowIndex,
			String... routingPatterns) throws Throwable {

		// Create the pattern listings
		List<Pattern> patterns = new LinkedList<Pattern>();
		for (String routingPattern : routingPatterns) {
			patterns.add(Pattern.compile(routingPattern));
		}

		// Create the route task
		HttpRouteTask routeTask = new HttpRouteTask(
				patterns.toArray(new Pattern[0]));

		// Record actions on mocks
		this.recordReturn(this.taskContext, this.taskContext
				.getObject(HttpRouteTaskDependencies.SERVER_HTTP_CONNECTION),
				this.serverHttpConnection);
		this.recordReturn(this.serverHttpConnection,
				this.serverHttpConnection.getHttpRequest(), this.httpRequest);
		this.recordReturn(this.httpRequest, this.httpRequest.getRequestURI(),
				path);
		this.recordReturn(
				this.taskContext,
				this.taskContext
						.getObject(HttpRouteTaskDependencies.HTTP_APPLICATION_LOCATION),
				this.location);
		this.location.transformToApplicationCanonicalPath(path);
		if ("/noContext".equals(path)) {
			// Fail exception
			this.control(this.location).setThrowable(
					new IncorrectHttpRequestContextPathException(404, "TEST"));
		} else {
			// Provide path
			String canonicalPath = HttpApplicationLocationMangedObject
					.transformToCanonicalPath(path);
			this.control(this.location).setReturnValue(canonicalPath);
		}
		this.recordReturn(this.taskContext,
				this.taskContext.doFlow(flowIndex, null), this.flowFuture);

		// Replay mocks
		this.replayMockObjects();

		// Execute the task to route the request
		routeTask.doTask(this.taskContext);

		// Verify mocks
		this.verifyMockObjects();
	}

}