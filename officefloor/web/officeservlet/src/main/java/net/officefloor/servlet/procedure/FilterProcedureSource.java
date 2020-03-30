package net.officefloor.servlet.procedure;

import java.util.concurrent.Executor;

import javax.servlet.Filter;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.spi.ManagedFunctionProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureManagedFunctionContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.servlet.FilterServicer;
import net.officefloor.servlet.ServletManager;
import net.officefloor.servlet.ServletServicer;
import net.officefloor.servlet.supply.ServletSupplierSource;

/**
 * {@link Filter} {@link ProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class FilterProcedureSource implements ManagedFunctionProcedureSource, ProcedureSourceServiceFactory {

	/**
	 * {@link FilterProcedureSource} source name.
	 */
	public static final String SOURCE_NAME = Filter.class.getSimpleName();

	/*
	 * ===================== ProcedureSourceServiceFactory ========================
	 */

	@Override
	public ProcedureSource createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ============================ ProcedureSource ===============================
	 */

	@Override
	public String getSourceName() {
		return SOURCE_NAME;
	}

	@Override
	public void listProcedures(ProcedureListContext context) throws Exception {

		// Determine if filter
		Class<?> clazz = context.getSourceContext().loadOptionalClass(context.getResource());
		if ((clazz != null) && (Filter.class.isAssignableFrom(clazz))) {

			// Filter so list the procedure
			context.addProcedure(clazz.getSimpleName());
		}
	}

	@Override
	public void loadManagedFunction(ProcedureManagedFunctionContext context) throws Exception {
		SourceContext sourceContext = context.getSourceContext();

		// Obtain the filter class
		@SuppressWarnings("unchecked")
		Class<? extends Filter> filterClass = (Class<? extends Filter>) sourceContext.loadClass(context.getResource());

		// Obtain the Servlet Manager
		ServletManager servletManager = ServletSupplierSource.getServletManager();

		// Add the Filter
		String filterName = sourceContext.getLogger().getName();
		FilterServicer filterServicer = servletManager.addFilter(filterName, filterClass);

		// Provide managed function
		ManagedFunctionTypeBuilder<DependencyKeys, FlowKeys> filter = context
				.setManagedFunction(new FilterProcedure(filterServicer), DependencyKeys.class, FlowKeys.class);
		filter.addObject(ServerHttpConnection.class).setKey(DependencyKeys.SERVER_HTTP_CONNECTION);

		// Must depend on servlet servicer for thread locals to be available
		filter.addObject(ServletServicer.class).setKey(DependencyKeys.SERVLET_SERVICER);

		// Must link in next in chain
		filter.addFlow().setKey(FlowKeys.NEXT);
	}

	/**
	 * Dependency keys.
	 */
	private static enum DependencyKeys {
		SERVER_HTTP_CONNECTION, SERVLET_SERVICER
	}

	/**
	 * Flow keys.
	 */
	public static enum FlowKeys {
		NEXT
	}

	/**
	 * {@link Filter} {@link Procedure}.
	 */
	private class FilterProcedure extends StaticManagedFunction<DependencyKeys, FlowKeys> {

		/**
		 * {@link FilterServicer} for the {@link Filter}.
		 */
		private final FilterServicer filterServicer;

		/**
		 * Instantiate.
		 * 
		 * @param filterServicer {@link FilterServicer}.
		 */
		private FilterProcedure(FilterServicer filterServicer) {
			this.filterServicer = filterServicer;
		}

		/*
		 * ======================= ManagedFunction =================================
		 */

		@Override
		public void execute(ManagedFunctionContext<DependencyKeys, FlowKeys> context) throws Throwable {

			// Obtain dependencies
			ServerHttpConnection connection = (ServerHttpConnection) context
					.getObject(DependencyKeys.SERVER_HTTP_CONNECTION);

			// Service
			AsynchronousFlow asynchronousFlow = context.createAsynchronousFlow();
			Executor executor = context.getExecutor();
			this.filterServicer.service(connection, asynchronousFlow, executor,
					(req, resp) -> context.doFlow(FlowKeys.NEXT, null, null));
		}
	}

}