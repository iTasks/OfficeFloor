package net.officefloor.jaxrs.procedure;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.servlet.Servlet;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.servlet.ServletContainer;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.spi.ManagedFunctionProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureManagedFunctionContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.ServletServicer;
import net.officefloor.servlet.supply.ServletSupplierSource;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.state.HttpRequestState;

/**
 * JAX-RS {@link ProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JaxRsProcedureSource implements ManagedFunctionProcedureSource, ProcedureSourceServiceFactory {

	/**
	 * Source name for {@link JaxRsProcedureSource}.
	 */
	public static final String SOURCE_NAME = "JAXRS";

	/*
	 * ================== ProcedureSourceServiceFactory ===================
	 */

	@Override
	public ProcedureSource createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================= ManagedFunctionProcedureSource ==================
	 */

	@Override
	public String getSourceName() {
		return SOURCE_NAME;
	}

	@Override
	public void listProcedures(ProcedureListContext context) throws Exception {
		// TODO Implement ProcedureSource.listProcedures
		throw new IllegalStateException("TODO implement ProcedureSource.listProcedures");
	}

	@Override
	public void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception {
		SourceContext sourceContext = context.getSourceContext();

		// Obtain details
		Class<?> resourceClass = sourceContext.loadClass(context.getResource());
		String resourceMethodName = context.getProcedureName();

		// Find method on resource class
		Method handlingMethod = null;
		for (Method method : resourceClass.getMethods()) {
			if (method.getName().equals(resourceMethodName)) {
				handlingMethod = method;
			}
		}
		if (handlingMethod == null) {
			throw new IllegalStateException("Can not find method " + resourceMethodName + " on JAX-RS resource class "
					+ resourceClass.getName());
		}

		// Load the meta-data
		Resource metaData = Resource.builder(resourceClass).build();

		// Find the method within meta-data
		ResourceMethod metaDataMethod = this.findResourceMethod(metaData, handlingMethod);
		if (metaDataMethod == null) {
			throw new IllegalStateException("Method " + handlingMethod.toString() + " not a JAX-RS handling method");
		}
		String httpMethod = metaDataMethod.getHttpMethod();
		String path = metaDataMethod.getParent().getPath();

		// Build the specific JAX-RS method to service request
		Resource.Builder resourceBuilder = Resource.builder().path(path).extended(false);
		resourceBuilder.addMethod(metaDataMethod).extended(false);
		Resource resource = resourceBuilder.build();

		// Create Servlet for JAX-RS method
		ResourceConfig config = new ResourceConfig();
		config.registerResources(resource);
		config.register(new OfficeFloorApplicationEventListener(httpMethod, path));
		ServletContainer container = new ServletContainer(config);

		// Determine if loading type
		ServletServicer servletServicer = null;
		if (!sourceContext.isLoadingType()) {

			// Obtain the Servlet Manager
			ServletManager servletManager = ServletSupplierSource.getServletManager();

			// Add the Servlet
			String servletName = sourceContext.getName();
			servletServicer = servletManager.addServlet(servletName, container, true, null);
		}

		// Provide managed function
		ManagedFunctionTypeBuilder<DependencyKeys, None> servlet = context
				.setManagedFunction(new JaxRsProcedure(servletServicer), DependencyKeys.class, None.class);
		servlet.addObject(ServerHttpConnection.class).setKey(DependencyKeys.SERVER_HTTP_CONNECTION);
		servlet.addObject(HttpRequestState.class).setKey(DependencyKeys.HTTP_REQUEST_STATE);

		// Must depend on servlet servicer for thread locals to be available
		servlet.addObject(ServletServicer.class).setKey(DependencyKeys.SERVLET_SERVICER);
	}

	/**
	 * Recursively searches the {@link Resource} for the {@link ResourceMethod}.
	 * 
	 * @param root   Root {@link Resource} to search.
	 * @param method {@link Method} to find.
	 * @return {@link ResourceMethod} or <code>null</code> if not found.
	 */
	private ResourceMethod findResourceMethod(Resource root, Method method) {

		// Determine if on root
		for (ResourceMethod resourceMethod : root.getResourceMethods()) {
			if (method.equals(resourceMethod.getInvocable().getDefinitionMethod())) {
				return resourceMethod;
			}
		}

		// Recursively search children for the resource method
		for (Resource child : root.getChildResources()) {

			// Determine if on child
			ResourceMethod resourceMethod = this.findResourceMethod(child, method);
			if (resourceMethod != null) {
				return resourceMethod;
			}
		}

		// As here, did not find the resource method
		return null;
	}

	/**
	 * Dependency keys.
	 */
	private static enum DependencyKeys {
		SERVER_HTTP_CONNECTION, HTTP_REQUEST_STATE, SERVLET_SERVICER
	}

	/**
	 * JAX-RS {@link Procedure}.
	 */
	private class JaxRsProcedure extends StaticManagedFunction<DependencyKeys, None> {

		/**
		 * {@link ServletServicer} for the {@link Servlet}.
		 */
		private final ServletServicer servletServicer;

		/**
		 * Instantiate.
		 * 
		 * @param servletServicer {@link ServletServicer}.
		 */
		private JaxRsProcedure(ServletServicer servletServicer) {
			this.servletServicer = servletServicer;
		}

		/*
		 * ======================= ManagedFunction =================================
		 */

		@Override
		public void execute(ManagedFunctionContext<DependencyKeys, None> context) throws Throwable {

			// Obtain dependencies
			ServerHttpConnection connection = (ServerHttpConnection) context
					.getObject(DependencyKeys.SERVER_HTTP_CONNECTION);
			HttpRequestState requestState = (HttpRequestState) context.getObject(DependencyKeys.HTTP_REQUEST_STATE);

			// Load the path parameters
			Map<String, String> pathParameters = new HashMap<>();
			requestState.loadValues((name, value, location) -> {
				if (location == HttpValueLocation.PATH) {
					pathParameters.put(name, value);
				}
			});

			// Provide path parameters
			Map<String, Object> attributes = new HashMap<>(1);
			attributes.put(OfficeFloorApplicationEventListener.PATH_PARAMETERS_PROPERTY_NAME, pathParameters);

			// Service
			AsynchronousFlow asynchronousFlow = context.createAsynchronousFlow();
			Executor executor = context.getExecutor();
			this.servletServicer.service(connection, executor, asynchronousFlow, null, attributes);
		}
	}

}