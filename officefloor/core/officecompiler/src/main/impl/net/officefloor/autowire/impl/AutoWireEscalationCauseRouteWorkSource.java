/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.autowire.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.autowire.AutoWireSection;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.plugin.section.managedfunction.ManagedFunctionSectionSource;

/**
 * {@link ManagedFunctionSource} to route the cause of an {@link Escalation}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireEscalationCauseRouteWorkSource extends AbstractManagedFunctionSource {

	/**
	 * Name of the {@link ManagedFunction} handling the {@link Escalation}.
	 */
	public static final String HANDLER_TASK_NAME = "Handle";

	/**
	 * Name of {@link Property} prefix containing the {@link Escalation} type.
	 * This is is indexed from <code>0</code> to provide the listing of
	 * {@link Escalation} causes to handle.
	 */
	public static final String PROPERTY_PREFIX_ESCALATION_TYPE = "escalation.type.";

	/**
	 * Configures handling the {@link Escalation} cause.
	 * 
	 * @param section
	 *            {@link AutoWireSection} for the {@link ManagedFunctionSectionSource}
	 *            containing the {@link AutoWireEscalationCauseRouteWorkSource}
	 *            being configured.
	 * @param causeType
	 *            Type of cause.
	 */
	public static void configureEscalationCause(AutoWireSection section, Class<? extends Throwable> causeType) {

		// Obtain the next index
		int index = 0;
		PropertyList properties = section.getProperties();
		while (properties.getProperty(PROPERTY_PREFIX_ESCALATION_TYPE + String.valueOf(index)) != null) {
			index++;
		}

		// Configure the property for the escalation cause
		section.addProperty(PROPERTY_PREFIX_ESCALATION_TYPE + String.valueOf(index), causeType.getName());
	}

	/*
	 * ==================== AbstractWorkSource ======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	@SuppressWarnings("unchecked")
	public void sourceManagedFunctions(FunctionNamespaceBuilder workTypeBuilder, ManagedFunctionSourceContext context)
			throws Exception {

		// Obtain the listing of escalation types
		List<Class<? extends Throwable>> escalationTypes = new LinkedList<Class<? extends Throwable>>();
		int index = 0;
		do {

			// Obtain the escalation
			String escalationTypeName = context.getProperty(PROPERTY_PREFIX_ESCALATION_TYPE + String.valueOf(index++),
					null);
			if (escalationTypeName == null) {
				// No further escalations
				index = -1;

			} else {
				// Add the escalation type
				Class<? extends Throwable> escalationType = (Class<? extends Throwable>) context
						.loadClass(escalationTypeName);
				escalationTypes.add(escalationType);
			}
		} while (index >= 0);

		// Order by more specific escalation first. Allows finer handling first.
		Collections.sort(escalationTypes, new Comparator<Class<? extends Throwable>>() {
			@Override
			public int compare(Class<? extends Throwable> a, Class<? extends Throwable> b) {

				// Compare based on type
				if (a != b) {
					if (a.isAssignableFrom(b)) {
						return 1; // a is super type
					} else if (b.isAssignableFrom(a)) {
						return -1; // b is super type
					}
				}

				// Either same type or no inheritance relationship.
				// Therefore sort alphabetically to have ordering.
				return String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName());
			}
		});
		Class<? extends Throwable>[] escalations = escalationTypes.toArray(new Class[escalationTypes.size()]);

		// Configure the work
		AutoWireEscalationCauseRouteTask factory = new AutoWireEscalationCauseRouteTask(escalations);
		ManagedFunctionTypeBuilder<Dependencies, Indexed> task = workTypeBuilder
				.addManagedFunctionType(HANDLER_TASK_NAME, factory, Dependencies.class, Indexed.class);
		task.addObject(Throwable.class).setKey(Dependencies.ESCALATION);
		for (Class<? extends Throwable> escalation : escalations) {
			ManagedFunctionFlowTypeBuilder<Indexed> flow = task.addFlow();
			flow.setArgumentType(escalation);
			flow.setLabel(escalation.getName());
		}
		task.addEscalation(Throwable.class);
	}

	/**
	 * Keys for the dependencies.
	 */
	public static enum Dependencies {
		ESCALATION
	}

	/**
	 * {@link ManagedFunction} for the routing the {@link Escalation} cause.
	 */
	public static class AutoWireEscalationCauseRouteTask
			implements ManagedFunctionFactory<Dependencies, Indexed>, ManagedFunction<Dependencies, Indexed> {

		/**
		 * {@link Escalation} cause routes in sequential ordering of handling.
		 * Array index matches to flow index.
		 */
		private final Class<? extends Throwable>[] causeRoutes;

		/**
		 * Initiate.
		 * 
		 * @param causeRoutes
		 *            {@link Escalation} cause routes in sequential ordering of
		 *            handling flow indexes. Array index matches to flow index.
		 *            More specific {@link Escalation} types should be first as
		 *            matching is sequential.
		 */
		public AutoWireEscalationCauseRouteTask(Class<? extends Throwable>[] causeRoutes) {
			this.causeRoutes = causeRoutes;
		}

		/*
		 * ================= ManagedFunctionFactory =========================
		 */

		@Override
		public ManagedFunction<Dependencies, Indexed> createManagedFunction() throws Throwable {
			return this;
		}

		/*
		 * ===================== ManagedFunction ===========================
		 */

		@Override
		public Object execute(ManagedFunctionContext<Dependencies, Indexed> context) throws Throwable {

			// Obtain the cause
			Throwable escalation = (Throwable) context.getObject(Dependencies.ESCALATION);
			Throwable cause = escalation.getCause();

			// Attempt to match to escalation cause
			for (int i = 0; i < this.causeRoutes.length; i++) {
				Class<? extends Throwable> causeType = this.causeRoutes[i];
				if (causeType.isInstance(cause)) {
					// Trigger flow to handle cause
					context.doFlow(i, cause, null);
					return null;
				}
			}

			// As here, no routing available for cause so propagate
			throw escalation;
		}
	}

}