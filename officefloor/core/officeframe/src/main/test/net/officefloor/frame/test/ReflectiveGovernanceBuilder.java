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
package net.officefloor.frame.test;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;

import junit.framework.TestCase;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.governance.GovernanceContext;
import net.officefloor.frame.api.governance.GovernanceFactory;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.GovernanceActivity;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * Reflective {@link GovernanceBuilder}.
 *
 * @author Daniel Sagenschneider
 */
public class ReflectiveGovernanceBuilder implements GovernanceFactory<Object, Indexed> {

	/**
	 * {@link AbstractOfficeConstructTestCase}.
	 */
	private AbstractOfficeConstructTestCase testCase;

	/**
	 * {@link Class} to obtain the {@link Method}.
	 */
	private final Class<?> clazz;

	/**
	 * Object should the method not be <code>static</code>.
	 */
	private final Object object;

	/**
	 * Name of this {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * {@link OfficeBuilder}.
	 */
	private final OfficeBuilder officeBuilder;

	/**
	 * Extension interface.
	 */
	private Class<?> extensionInterface;

	/**
	 * {@link ReflectiveGovernanceActivityBuilder} to register the
	 * {@link ManagedObject}. May be <code>null</code>.
	 */
	private ReflectiveGovernanceActivityBuilder registerMaangedObject;

	/**
	 * {@link ReflectiveGovernanceActivityBuilder} to enforce the
	 * {@link Governance}. May be <code>null</code>.
	 */
	private ReflectiveGovernanceActivityBuilder enforce;

	/**
	 * {@link ReflectiveGovernanceActivityBuilder} to disregard the
	 * {@link Governance}. May be <code>null</code>.
	 */
	private ReflectiveGovernanceActivityBuilder disregard;

	/**
	 * {@link GovernanceBuilder}.
	 */
	private GovernanceBuilder<Indexed> governanceBuilder;

	/**
	 * Next index to specify {@link Flow}.
	 */
	private int flowIndex = 0;

	/**
	 * Instantiate.
	 *
	 * @param <C>
	 *            {@link Governance} {@link Class} type.
	 * @param clazz
	 *            {@link Class}.
	 * @param object
	 *            Object should the method not be <code>static</code>. May be
	 *            <code>null</code> if <code>static</code> {@link Method} of the
	 *            {@link Class}.
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param officeBuilder
	 *            {@link OfficeBuilder}.
	 * @param testCase
	 *            {@link AbstractOfficeConstructTestCase}.
	 */
	public <C> ReflectiveGovernanceBuilder(Class<C> clazz, C object, String governanceName, OfficeBuilder officeBuilder,
			AbstractOfficeConstructTestCase testCase) {
		this.clazz = clazz;
		this.object = object;
		this.governanceName = governanceName;
		this.testCase = testCase;
		this.officeBuilder = officeBuilder;
	}

	/**
	 * Obtains the {@link GovernanceBuilder}.
	 * 
	 * @return {@link GovernanceBuilder}.
	 */
	public GovernanceBuilder<Indexed> getBuilder() {

		// Ensure the governance is registered
		if (this.governanceBuilder == null) {
			this.governanceBuilder = this.officeBuilder.addGovernance(this.governanceName, this.extensionInterface,
					this);
		}

		// Return the governance builder
		return this.governanceBuilder;
	}

	/**
	 * Constructs the register {@link ManagedObject} {@link GovernanceActivity}.
	 * 
	 * @param methodName
	 *            Name of {@link Method} to register the {@link ManagedObject}.
	 * @return {@link ReflectiveGovernanceActivityBuilder}.
	 */
	public ReflectiveGovernanceActivityBuilder register(String methodName) {
		this.registerMaangedObject = this.createActivity("register ManagedObject", methodName,
				this.registerMaangedObject, this.extensionInterface, false);
		return this.registerMaangedObject;
	}

	/**
	 * Constructs the enforce {@link GovernanceActivity}.
	 * 
	 * @param methodName
	 *            Name of {@link Method} for enforcing the {@link Governance}.
	 * @return {@link ReflectiveGovernanceActivityBuilder}.
	 */
	public ReflectiveGovernanceActivityBuilder enforce(String methodName) {
		this.enforce = this.createActivity("enforce", methodName, this.enforce, this.extensionInterface, true);
		return this.enforce;
	}

	/**
	 * Constructs the disregard {@link GovernanceActivity}.
	 * 
	 * @param methodName
	 *            Name of {@link Method} for disregarding the {@link Governance}.
	 * @return {@link ReflectiveGovernanceActivityBuilder}.
	 */
	public ReflectiveGovernanceActivityBuilder disregard(String methodName) {
		this.disregard = this.createActivity("disregard", methodName, this.disregard, this.extensionInterface, true);
		return this.disregard;
	}

	/**
	 * Creates the {@link ReflectiveGovernanceActivityBuilder}.
	 * 
	 * @param activityType
	 *            Type of activity.
	 * @param methodName
	 *            Name of {@link Method} for the activity.
	 * @param existing
	 *            Existing {@link ReflectiveGovernanceActivityBuilder}. Most likely
	 *            <code>null</code>.
	 * @param expectedExtensionInterface
	 *            Expected extension interface. May be <code>null</code>.
	 * @param isExtensionAnArray
	 *            Flags whether extension interface an array paramaeter.
	 * @return {@link ReflectiveGovernanceActivityBuilder}.
	 */
	private ReflectiveGovernanceActivityBuilder createActivity(String activityType, String methodName,
			ReflectiveGovernanceActivityBuilder existing, Class<?> expectedExtensionInterface,
			boolean isExtensionAnArray) {

		// Create the single activity for the type
		Assert.assertNull("Already registered " + activityType, existing);
		ReflectiveGovernanceActivityBuilder activity = new ReflectiveGovernanceActivityBuilder(this.clazz, methodName);

		// Ensure extension is matching other activities
		this.extensionInterface = activity.extractExtensionInterface(isExtensionAnArray, expectedExtensionInterface);

		// First parameter is always the extensions
		activity.parameterFactories[0] = (isExtensionAnArray ? new ActivityExtensionParameterFactory()
				: new RegisterExtensionParameterFactory());

		// Ensure have governance
		this.getBuilder();

		// Return the activity
		return activity;
	}

	/*
	 * ======================= GovernanceFactory ============================
	 */

	@Override
	public Governance<Object, Indexed> createGovernance() throws Throwable {
		return new ReflectiveGovernance();
	}

	/**
	 * Reflective {@link Governance}.
	 */
	private class ReflectiveGovernance implements Governance<Object, Indexed> {

		/**
		 * Registered extensions.
		 */
		private final List<Object> extensions = new LinkedList<>();

		/**
		 * Obtain the array of extensions.
		 * 
		 * @return Array of extensions.
		 */
		private Object[] getExtensions() {
			Object[] array = (Object[]) Array.newInstance(ReflectiveGovernanceBuilder.this.extensionInterface,
					this.extensions.size());
			return this.extensions.toArray(array);
		}

		/*
		 * ========================== Governance ==============================
		 */

		@Override
		public void governManagedObject(Object managedObjectExtension, GovernanceContext<Indexed> context)
				throws Throwable {

			// Register the extension
			this.extensions.add(managedObjectExtension);

			// Notify of registering
			if (ReflectiveGovernanceBuilder.this.registerMaangedObject != null) {
				Object[] array = (Object[]) Array.newInstance(ReflectiveGovernanceBuilder.this.extensionInterface, 1);
				array[0] = managedObjectExtension;
				ReflectiveGovernanceBuilder.this.registerMaangedObject.execute(array, context);
			}
		}

		@Override
		public void enforceGovernance(GovernanceContext<Indexed> context) throws Throwable {
			Assert.assertNotNull("No enforce configured for governance", ReflectiveGovernanceBuilder.this.enforce);
			ReflectiveGovernanceBuilder.this.enforce.execute(this.getExtensions(), context);
		}

		@Override
		public void disregardGovernance(GovernanceContext<Indexed> context) throws Throwable {
			Assert.assertNotNull("No disregard configured for governance", ReflectiveGovernanceBuilder.this.disregard);
			ReflectiveGovernanceBuilder.this.disregard.execute(this.getExtensions(), context);
		}
	}

	/**
	 * Reflective {@link Governance} action.
	 */
	public class ReflectiveGovernanceActivityBuilder {

		/**
		 * {@link Method} for the {@link Governance} action.
		 */
		private final Method method;

		/**
		 * Types for the parameters of the {@link Method}.
		 */
		private final Class<?>[] parameterTypes;

		/**
		 * {@link ParameterFactory} instances for the parameters.
		 */
		private final ParameterFactory[] parameterFactories;

		/**
		 * Next index to specify the {@link ParameterFactory}.
		 */
		private int parameterIndex = 1; // 0 is extensions

		/**
		 * Builder for the particular {@link Governance} action.
		 * 
		 * @param clazz
		 *            {@link Class} containing the {@link Method}.
		 * @param methodName
		 *            Name of the {@link Method} for the {@link Governance} action.
		 */
		private ReflectiveGovernanceActivityBuilder(Class<?> clazz, String methodName) {

			// Obtain the method
			Method functionMethod = null;
			for (Method method : clazz.getMethods()) {
				if (method.getName().equals(methodName)) {
					functionMethod = method;
				}
			}
			if (functionMethod == null) {
				TestCase.fail("No method '" + methodName + "' on class " + clazz.getName());
			}
			this.method = functionMethod;

			// Create the parameter factories for the method
			this.parameterTypes = this.method.getParameterTypes();
			this.parameterFactories = new ParameterFactory[this.parameterTypes.length];
		}

		/**
		 * Builds the {@link Flow}.
		 * 
		 * @param functionName
		 *            {@link ManagedFunction} name.
		 * @param argumentType
		 *            Type of argument passed to the {@link Flow}.
		 * @param isSpawnThread
		 *            Flags whether to spawn a {@link ThreadState}.
		 */
		public void buildFlow(String functionName, Class<?> argumentType, boolean isSpawnThread) {

			// Link in the flow and allow for invocation
			ReflectiveGovernanceBuilder.this.governanceBuilder.linkFlow(ReflectiveGovernanceBuilder.this.flowIndex,
					functionName, argumentType, isSpawnThread);
			this.parameterFactories[this.parameterIndex] = new ReflectiveFlowParameterFactory(
					ReflectiveGovernanceBuilder.this.flowIndex);

			// Set for next flow and parameter
			ReflectiveGovernanceBuilder.this.flowIndex++;
			this.parameterIndex++;
		}

		/**
		 * Extracts the extension interface type from the {@link Method} (first
		 * parameter).
		 * 
		 * @param isArray
		 *            Indicates if expecting extension interface to be in an array.
		 * @param expectedExtensionInterface
		 *            Optional expected extension interface. May be <code>null</code> to
		 *            be any interface.
		 * @return Extension interface.
		 */
		private Class<?> extractExtensionInterface(boolean isArray, Class<?> expectedExtensionInterface) {

			// The first parameter is always the extension array
			Assert.assertTrue("Should have at least one parameter being the extension array",
					this.parameterTypes.length >= 1);

			// Obtain based on whether array or just extension
			Class<?> extensionInterface;
			if (isArray) {
				Class<?> extensionArrayType = this.parameterTypes[0];
				Assert.assertTrue("First parameter should be extension array", extensionArrayType.isArray());
				extensionInterface = extensionArrayType.getComponentType();
			} else {
				// Just use value
				extensionInterface = this.parameterTypes[0];
			}

			// Ensure of expected type
			if (expectedExtensionInterface != null) {
				Assert.assertEquals("Mis-match on extension interface between methods", expectedExtensionInterface,
						extensionInterface);
			}

			// Return the extension interface
			return extensionInterface;
		}

		/**
		 * Executes the {@link Method}.
		 * 
		 * @param extensions
		 *            Extensions.
		 * @param context
		 *            {@link GovernanceContext}.
		 * @throws Throwable
		 *             If fails.
		 */
		private void execute(Object[] extensions, GovernanceContext<Indexed> context) throws Throwable {

			// Create the parameters
			Object[] parameters = new Object[this.method.getParameterTypes().length];
			for (int i = 0; i < parameters.length; i++) {
				parameters[i] = this.parameterFactories[i].createParamater(extensions, context);
			}

			// Record invoking method
			ReflectiveGovernanceBuilder.this.testCase.recordReflectiveFunctionMethodInvoked(this.method.getName());

			// Invoke the method for governance
			try {
				this.method.invoke(ReflectiveGovernanceBuilder.this.object, parameters);
			} catch (InvocationTargetException ex) {
				// Throw cause of exception
				throw ex.getCause();
			}
		}
	}

	/**
	 * Interface for a factory to create the parameter from the
	 * {@link GovernanceContext}.
	 */
	private static interface ParameterFactory {
		Object createParamater(Object[] extensions, GovernanceContext<Indexed> context);
	}

	/**
	 * {@link ParameterFactory} to provide the registered {@link ManagedObject}
	 * extension.
	 */
	private class RegisterExtensionParameterFactory implements ParameterFactory {
		@Override
		public Object createParamater(Object[] extensions, GovernanceContext<Indexed> context) {
			// Only ever the one extension being registered
			return extensions[0];
		}
	}

	/**
	 * {@link ParameterFactory} to provide the {@link ManagedObject} extensions.
	 */
	private class ActivityExtensionParameterFactory implements ParameterFactory {
		@Override
		public Object createParamater(Object[] extensions, GovernanceContext<Indexed> context) {
			return extensions;
		}
	}

	/**
	 * {@link ParameterFactory} to obtain the {@link ReflectiveFlow}.
	 */
	private class ReflectiveFlowParameterFactory implements ParameterFactory {

		/**
		 * Index of the flow.
		 */
		private final int index;

		/**
		 * Initiate.
		 * 
		 * @param index
		 *            Index of the {@link FlowMetaData}.
		 */
		public ReflectiveFlowParameterFactory(int index) {
			this.index = index;
		}

		@Override
		public Object createParamater(Object[] extensions, GovernanceContext<Indexed> context) {
			return new ReflectiveFlow() {
				@Override
				public void doFlow(Object parameter, FlowCallback callback) {
					context.doFlow(ReflectiveFlowParameterFactory.this.index, parameter, callback);
				}
			};
		}
	}

}