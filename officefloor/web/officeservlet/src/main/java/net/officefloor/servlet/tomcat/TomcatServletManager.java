package net.officefloor.servlet.tomcat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.coyote.InputBuffer;
import org.apache.coyote.OutputBuffer;
import org.apache.coyote.Request;
import org.apache.coyote.Response;
import org.apache.tomcat.util.http.MimeHeaders;
import org.apache.tomcat.util.net.ApplicationBufferHandler;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.ServletServicer;
import net.officefloor.servlet.inject.InjectContext;
import net.officefloor.servlet.inject.InjectContextFactory;
import net.officefloor.servlet.inject.InjectionRegistry;
import net.officefloor.servlet.supply.ServletSupplierSource;

/**
 * {@link Tomcat} {@link ServletServicer}.
 * 
 * @author Daniel Sagenschneider
 */
public class TomcatServletManager implements ServletManager, ServletServicer {

	/**
	 * Operation to run.
	 */
	@FunctionalInterface
	public static interface Operation<R, T extends Throwable> {

		/**
		 * Logic of operation.
		 * 
		 * @return Result.
		 * @throws T Possible failure.
		 */
		R run() throws T;
	}

	/**
	 * Indicates if within Maven <code>war</code> project.
	 */
	private static final ThreadLocal<Boolean> isWithinMavenWarProject = new ThreadLocal<>();

	/**
	 * Runs the {@link Operation} assuming within Maven <code>war</code> project.
	 * 
	 * @param <R>       Return type.
	 * @param <T>       Possible exception type.
	 * @param operation {@link Operation}.
	 * @return Result.
	 * @throws T Possible failure.
	 */
	public static <R, T extends Throwable> R runInMavenWarProject(Operation<R, T> operation) throws T {
		Boolean original = isWithinMavenWarProject.get();
		try {
			// Flag within war project
			isWithinMavenWarProject.set(Boolean.TRUE);

			// Undertake operation
			return operation.run();

		} finally {
			// Determine if clear (as specified)
			if (original == null) {
				isWithinMavenWarProject.remove();
			}
		}
	}

	/**
	 * {@link ThreadLocal} for this {@link TomcatServletManager}.
	 */
	private static final ThreadLocal<TomcatServletManager> tomcatServletManager = new ThreadLocal<>();

	/**
	 * {@link Tomcat} for embedded {@link Servlet} container.
	 */
	private final Tomcat tomcat;

	/**
	 * {@link Connector}.
	 */
	private final Connector connector;

	/**
	 * {@link Context}.
	 */
	private final Context context;

	/**
	 * {@link InjectionRegistry}.
	 */
	private final InjectionRegistry injectionRegistry;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * {@link OfficeFloorProtocol}.
	 */
	private final OfficeFloorProtocol protocol;

	/**
	 * Registered {@link Servlet} instances.
	 */
	private final Map<String, ServletServicer> registeredServlets = new HashMap<String, ServletServicer>();

	/**
	 * {@link InjectContextFactory}.
	 */
	private InjectContextFactory injectContextFactory;

	/**
	 * Instantiate.
	 * 
	 * @param contextPath       Context path.
	 * @param injectionRegistry {@link InjectionRegistry}.
	 * @param classLoader       {@link ClassLoader}.
	 * @throws IOException If fails to setup container.
	 */
	public TomcatServletManager(String contextPath, InjectionRegistry injectionRegistry, ClassLoader classLoader)
			throws IOException {
		this.injectionRegistry = injectionRegistry;
		this.classLoader = classLoader;

		// Create OfficeFloor connector
		this.connector = new Connector(OfficeFloorProtocol.class.getName());
		this.connector.setPort(1);

		// Setup tomcat
		this.tomcat = new Tomcat();
		this.tomcat.setConnector(this.connector);

		// Configure webapp directory
		String username = System.getProperty("user.name");
		Path tempWebApp = Files.createTempDirectory(username + "_webapp");
		String tempWebAppPath = tempWebApp.toAbsolutePath().toString();

		// Create the context
		String contextName = ((contextPath == null) || (contextPath.equals("/"))) ? "" : contextPath;
		this.context = this.tomcat.addWebapp(contextName, tempWebAppPath);

		// Obtain OfficeFloor protocol to input request
		this.protocol = (OfficeFloorProtocol) this.connector.getProtocolHandler();

		// Listen for setup
		tomcatServletManager.set(this);
		this.context.addApplicationListener(SetupApplicationListener.class.getName());

		// Determine if load for running in Maven war project
		if (isWithinMavenWarProject.get() != null) {
			File additionWebInfClasses = new File("target/test-classes");
			WebResourceRoot resources = new StandardRoot(this.context);
			resources.addPreResources(
					new DirResourceSet(resources, "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));
			resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes",
					new File("target/classes").getAbsolutePath(), "/"));
			this.context.setResources(resources);
		}
	}

	public static class SetupApplicationListener implements ServletContextListener {
		@Override
		public void contextInitialized(ServletContextEvent sce) {
			ServletContext servletContext = sce.getServletContext();

			// Register servlets
			servletContext.getServletRegistrations().forEach((name, registration) -> {
				ServletSupplierSource.registerForInjection(registration.getClassName());
			});

			// Load the instance manager
			TomcatServletManager servletManager = tomcatServletManager.get();
			InjectContextFactory factory = servletManager.injectionRegistry.createInjectContextFactory();
			servletManager.context
					.setInstanceManager(new OfficeFloorInstanceManager(factory, servletManager.classLoader));
			tomcatServletManager.remove();
		}
	}

	/**
	 * Starts the {@link Servlet} container.
	 * 
	 * @throws Exception If fails to start.
	 */
	public void start() throws Exception {

		// Start tomcat
		this.tomcat.start();

		// Instantiate context factory
		this.injectContextFactory = this.injectionRegistry.createInjectContextFactory();
	}

	/**
	 * Stops the {@link Servlet} container.
	 * 
	 * @throws Exception If fails to stop.
	 */
	public void stop() throws Exception {
		this.tomcat.stop();
	}

	/*
	 * ===================== ServletServicer =======================
	 */

	@Override
	public void service(ServerHttpConnection connection, AsynchronousFlow asynchronousFlow, Executor executor)
			throws Exception {
		this.service(connection, asynchronousFlow, executor, this.protocol.getAdapter()::service);
	}

	/*
	 * ===================== ServletManager ========================
	 */

	@Override
	public ServletServicer addServlet(String name, Class<? extends Servlet> servletClass) {

		// Determine if already registered
		ServletServicer servletServicer = this.registeredServlets.get(name);
		if (servletServicer != null) {
			return servletServicer;
		}

		// Add the servlet
		Wrapper wrapper = Tomcat.addServlet(this.context, name, servletClass.getName());

		// Always support async
		wrapper.setAsyncSupported(true);

		// Provide servicer
		ContainerAdapter adapter = new ContainerAdapter(wrapper, this.connector, this.classLoader);
		servletServicer = (connection, asynchronousFlow, executor) -> this.service(connection, asynchronousFlow,
				executor, adapter::service);

		// Register and return servicer
		this.registeredServlets.put(name, servletServicer);
		return servletServicer;
	}

	/**
	 * Servicer.
	 */
	@FunctionalInterface
	private static interface Servicer {

		/**
		 * Undertakes servicing.
		 * 
		 * @param request  {@link Request}.
		 * @param response {@link Response}.
		 * @throws Exception If fails servicing.
		 */
		void service(Request request, Response response) throws Exception;
	}

	/**
	 * Services the {@link ServerHttpConnection} via {@link Servicer}.
	 * 
	 * @param connection       {@link ServerHttpConnection}.
	 * @param asynchronousFlow {@link AsynchronousFlow}.
	 * @param executor         {@link Executor}.
	 * @param servicer         {@link Servicer}.
	 * @throws Exception If fails servicing.
	 */
	private void service(ServerHttpConnection connection, AsynchronousFlow asynchronousFlow, Executor executor,
			Servicer servicer) throws Exception {

		// Create the request
		Request request = new Request();
		HttpRequest httpRequest = connection.getRequest();
		request.scheme().setString(connection.isSecure() ? "https" : "http");
		request.method().setString(httpRequest.getMethod().getName());
		request.requestURI().setString(httpRequest.getUri());
		request.decodedURI().setString(httpRequest.getUri());
		request.protocol().setString(httpRequest.getVersion().getName());
		MimeHeaders headers = request.getMimeHeaders();
		for (HttpHeader header : httpRequest.getHeaders()) {
			headers.addValue(header.getName()).setString(header.getValue());
		}
		request.setInputBuffer(new OfficeFloorInputBuffer(httpRequest));

		// Provide injection of context
		InjectContext injectContext = this.injectContextFactory.createInjectContext();
		injectContext.activate();
		request.setAttribute(InjectContext.REQUEST_ATTRIBUTE_NAME, injectContext);

		// Create the response
		Response response = new Response();
		HttpResponse httpResponse = connection.getResponse();
		response.setOutputBuffer(new OfficeFloorOutputBuffer(httpResponse));

		// Create processor for request
		new OfficeFloorProcessor(this.protocol, request, response, connection, asynchronousFlow, executor);

		// Undertake servicing
		servicer.service(request, response);
	}

	/**
	 * {@link InputBuffer} for {@link ServerHttpConnection}.
	 */
	private static class OfficeFloorInputBuffer implements InputBuffer {

		/**
		 * {@link InputStream} to {@link HttpRequest} entity.
		 */
		private final InputStream entity;

		/**
		 * Instantiate.
		 * 
		 * @param httpRequest {@link HttpRequest}.
		 */
		private OfficeFloorInputBuffer(HttpRequest httpRequest) {
			this.entity = httpRequest.getEntity();
		}

		/*
		 * ================ InputBuffer =====================
		 */

		@Override
		public int doRead(ApplicationBufferHandler handler) throws IOException {

			// Initiate the buffer
			ByteBuffer buffer = handler.getByteBuffer();
			buffer.limit(buffer.capacity());

			// Write content to buffer
			int bytesRead = 0;
			int value;
			while ((value = this.entity.read()) != -1) {

				// Load the byte
				buffer.put(bytesRead, (byte) value);
				bytesRead++;

				// Determine if buffer full
				if (bytesRead == buffer.capacity()) {
					buffer.limit(bytesRead);
					return bytesRead; // buffer full
				}
			}

			// Finished writing
			if (bytesRead == 0) {
				buffer.limit(0);
				return -1; // end of entity
			} else {
				// Provide last entity
				buffer.limit(bytesRead);
				return bytesRead;
			}
		}
	}

	/**
	 * {@link OutputBuffer} for {@link ServerHttpConnection}.
	 */
	private static class OfficeFloorOutputBuffer implements OutputBuffer {

		/**
		 * {@link HttpResponse}.
		 */
		private final HttpResponse httpResponse;

		/**
		 * Bytes written.
		 */
		private long bytesWritten = 0;

		/**
		 * Instantiate.
		 * 
		 * @param httpResponse {@link HttpResponse}.
		 */
		private OfficeFloorOutputBuffer(HttpResponse httpResponse) {
			this.httpResponse = httpResponse;
		}

		/*
		 * ================= OutputBuffer ======================
		 */

		@Override
		public int doWrite(ByteBuffer chunk) throws IOException {
			int size = chunk.remaining();
			this.httpResponse.getEntity().write(chunk);
			this.bytesWritten += size;
			return size;
		}

		@Override
		public long getBytesWritten() {
			return this.bytesWritten;
		}
	}

}