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
package net.officefloor.frame.test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.build.ManagedFunctionFactory;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.escalate.EscalationHandler;
import net.officefloor.frame.api.execute.FlowCallback;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;
import net.officefloor.frame.spi.administration.source.AdministratorSource;
import net.officefloor.frame.spi.administration.source.AdministratorSourceMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * Abstract {@link TestCase} for construction testing of an Office.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeConstructTestCase extends OfficeFrameTestCase implements EscalationHandler {

	/**
	 * Index of the current {@link OfficeFloor} being constructed.
	 */
	private static int OFFICE_FLOOR_INDEX = 0;

	/**
	 * Index of the current {@link Office} being constructed.
	 */
	private static int OFFICE_INDEX = 0;

	/**
	 * Constructed {@link OfficeFloor} instances that need to be closed on tear
	 * down of tests.
	 */
	private final List<OfficeFloor> constructedOfficeFloors = new LinkedList<OfficeFloor>();

	/**
	 * {@link OfficeFloorBuilder}.
	 */
	private OfficeFloorBuilder officeFloorBuilder;

	/**
	 * {@link OfficeBuilder}.
	 */
	private OfficeBuilder officeBuilder;

	/**
	 * List of method names in order they are invoked by the
	 * {@link ReflectiveFunctionBuilder} instances for the test.
	 */
	private List<String> reflectiveFunctionInvokedMethods = new LinkedList<String>();

	/**
	 * <p>
	 * Flag indicating whether to record the invocations of the
	 * {@link ReflectiveFunctionBuilder} instances.
	 * <p>
	 * This is necessary as stress tests using the
	 * {@link ReflectiveFunctionBuilder} will get {@link OutOfMemoryError}
	 * issues should every {@link ManagedFunction} executed be recorded.
	 */
	private boolean isRecordReflectiveFunctionMethodsInvoked = false;

	/**
	 * {@link Throwable} for the {@link EscalationHandler}.
	 */
	private Throwable exception = null;

	/**
	 * Lock for the {@link EscalationHandler}.
	 */
	private final Object exceptionLock = new Object();

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor = null;

	@Override
	protected void setUp() throws Exception {
		// Initiate for constructing office
		OFFICE_FLOOR_INDEX++;
		this.officeFloorBuilder = OfficeFrame.getInstance().createOfficeFloorBuilder(this.getOfficeFloorName());
		OFFICE_INDEX++;
		this.officeBuilder = this.officeFloorBuilder.addOffice(this.getOfficeName());

		// Initiate to receive the top level escalation to report back in tests
		this.officeFloorBuilder.setEscalationHandler(this);
	}

	/*
	 * =================== EscalationHandler =============================
	 */

	@Override
	public void handleEscalation(Throwable escalation) throws Throwable {
		synchronized (this.exceptionLock) {
			// Indicate a office floor level escalation
			System.err.println("OFFICE FLOOR ESCALATION: " + escalation.getMessage() + " ["
					+ escalation.getClass().getSimpleName() + " at " + escalation.getStackTrace()[0].toString() + "]");

			// Record exception to be thrown later
			this.exception = escalation;
		}
	}

	/**
	 * <p>
	 * Validates that no top level escalation occurred.
	 * <p>
	 * This method will clear the escalation on exit.
	 * 
	 * @throws Throwable
	 *             If top level {@link Escalation}.
	 */
	public void validateNoTopLevelEscalation() throws Throwable {
		synchronized (this.exceptionLock) {
			try {
				if (this.exception != null) {
					throw this.exception;
				}
			} finally {
				// Exception thrown, so have it cleared
				this.exception = null;
			}
		}
	}

	@Override
	protected void tearDown() throws Exception {
		try {

			// Close the constructed office floors
			for (OfficeFloor officeFloor : this.constructedOfficeFloors) {
				officeFloor.closeOfficeFloor();
			}

			// Propagate possible failure
			synchronized (this.exceptionLock) {
				// Return if no failure
				if (this.exception == null) {
					return;
				}

				// Propagate failure
				if (this.exception instanceof Exception) {
					throw (Exception) this.exception;
				} else if (this.exception instanceof Error) {
					throw (Error) this.exception;
				} else {
					StringWriter buffer = new StringWriter();
					this.exception.printStackTrace(new PrintWriter(buffer));
					fail("Unknown failure " + this.exception.getClass().getName() + ": " + buffer.toString());
				}
			}
		} finally {
			super.tearDown();
		}
	}

	/**
	 * Obtains the {@link OfficeFloorBuilder}.
	 * 
	 * @return {@link OfficeFloorBuilder}.
	 */
	protected OfficeFloorBuilder getOfficeFloorBuilder() {
		return this.officeFloorBuilder;
	}

	/**
	 * Obtains the {@link OfficeBuilder}.
	 * 
	 * @return {@link OfficeBuilder}.
	 */
	protected OfficeBuilder getOfficeBuilder() {
		return this.officeBuilder;
	}

	/**
	 * Obtains the name of the {@link OfficeFloor} currently being constructed.
	 * 
	 * @return Name of the {@link OfficeFloor} currently being constructed.
	 */
	protected String getOfficeFloorName() {
		return "officefloor-" + OFFICE_FLOOR_INDEX;
	}

	/**
	 * Obtains the name of the {@link Office} currently being constructed.
	 * 
	 * @return Name of the {@link Office} currently being constructed.
	 */
	protected String getOfficeName() {
		return "office-" + OFFICE_INDEX;
	}

	/**
	 * Constructs the {@link ReflectiveFunctionBuilder}.
	 * 
	 * @param object
	 *            {@link Object} containing the {@link Method}.
	 * @param methodName
	 *            Name of the {@link Method}.
	 * @return {@link ReflectiveFunctionBuilder}.
	 */
	public ReflectiveFunctionBuilder constructFunction(Object object, String methodName) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		ReflectiveFunctionBuilder builder = new ReflectiveFunctionBuilder((Class) object.getClass(), object, methodName,
				this.officeBuilder, this);
		return builder;
	}

	/**
	 * Facade method to register a {@link ManagedFunction}.
	 * 
	 * @param object
	 *            {@link Object} containing the {@link Method}.
	 * @param methodName
	 *            Name of the {@link Method}.
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @return {@link ReflectiveFunctionBuilder}.
	 */
	public ReflectiveFunctionBuilder constructFunction(Object object, String methodName, String teamName) {
		ReflectiveFunctionBuilder builder = this.constructFunction(object, methodName);
		builder.getBuilder().setTeam(teamName);
		return builder;
	}

	/**
	 * Constructs the {@link ReflectiveFunctionBuilder} for a static
	 * {@link Method}.
	 * 
	 * @param clazz
	 *            {@link Class} containing the static {@link Method}.
	 * @param methodName
	 *            Name of the {@link Method}.
	 * @return {@link ReflectiveFunctionBuilder}.
	 */
	public ReflectiveFunctionBuilder constructStaticFunction(Class<?> clazz, String methodName) {
		return new ReflectiveFunctionBuilder(clazz, null, methodName, this.officeBuilder, this);
	}

	/**
	 * <p>
	 * Specifies whether to record the invocations of the
	 * {@link ReflectiveFunctionBuilder} instances.
	 * <p>
	 * This is necessary as stress tests using the
	 * {@link ReflectiveFunctionBuilder} will get {@link OutOfMemoryError}
	 * issues should every {@link ManagedFunction} executed be recorded.
	 * <p>
	 * By default this is <code>false</code> to not record.
	 * 
	 * @param isRecord
	 *            <code>true</code> to record the {@link ManagedFunction}
	 *            instances invoked.
	 */
	public void setRecordReflectiveFunctionMethodsInvoked(boolean isRecord) {
		synchronized (this.reflectiveFunctionInvokedMethods) {
			this.isRecordReflectiveFunctionMethodsInvoked = isRecord;
		}
	}

	/**
	 * Invoked by the {@link ReflectiveFunctionBuilder} when it executes the
	 * method.
	 * 
	 * @param methodName
	 *            Name of method being invoked.
	 */
	public void recordReflectiveFunctionMethodInvoked(String methodName) {
		synchronized (this.reflectiveFunctionInvokedMethods) {
			if (this.isRecordReflectiveFunctionMethodsInvoked) {
				this.reflectiveFunctionInvokedMethods.add(methodName);
			}
		}
	}

	/**
	 * Validates the order the {@link ReflectiveFunctionBuilder} invoked the
	 * methods.
	 * 
	 * @param methodNames
	 *            Order that the reflective methods should be invoked.
	 * @see #setRecordReflectiveTaskMethodsInvoked(boolean)
	 */
	public void validateReflectiveMethodOrder(String... methodNames) {
		synchronized (this.reflectiveFunctionInvokedMethods) {

			// Create expected method calls
			StringBuilder actualMethods = new StringBuilder();
			for (String methodName : methodNames) {
				actualMethods.append(methodName.trim() + " ");
			}

			// Create the actual method calls
			StringBuilder expectedMethods = new StringBuilder();
			for (String methodName : this.reflectiveFunctionInvokedMethods) {
				expectedMethods.append(methodName.trim() + " ");
			}

			// Validate appropriate methods called
			assertEquals("Incorrect methods invoked [ " + actualMethods.toString() + "]", actualMethods.toString(),
					expectedMethods.toString());
		}
	}

	/**
	 * Facade method to register a {@link ManagedFunction}.
	 * 
	 * @param <O>
	 *            Dependency key type.
	 * @param <F>
	 *            Flow key type.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @param functionFactory
	 *            {@link ManagedFunctionFactory}.
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @return {@link ManagedFunctionBuilder} for the {@link ManagedFunction}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public <O extends Enum<O>, F extends Enum<F>> ManagedFunctionBuilder<O, F> constructFunction(String functionName,
			ManagedFunctionFactory<O, F> functionFactory, String teamName) {

		// Create the Function Builder
		ManagedFunctionBuilder functionBuilder = this.officeBuilder.addManagedFunction(functionName, functionFactory);

		// Link to Team
		functionBuilder.setTeam(teamName);

		// Return the Function Builder
		return functionBuilder;
	}

	/**
	 * Facade method to register a {@link ManagedFunction}.
	 *
	 * @param <O>
	 *            Dependency key type.
	 * @param <F>
	 *            Flow key type.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @param functionFactory
	 *            {@link ManagedFunctionFactory}.
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @param moName
	 *            Name of the {@link ManagedObject}.
	 * @param moType
	 *            Type of the {@link ManagedObject}.
	 * @param nextFunctionName
	 *            Name of the next {@link ManagedFunction}.
	 * @param nextFunctionArgumentType
	 *            Type of argument to the next {@link ManagedFunction}.
	 * @return {@link ManagedFunctionBuilder} for the {@link ManagedFunction}.
	 */
	public <O extends Enum<O>, F extends Enum<F>> ManagedFunctionBuilder<O, F> constructFunction(String functionName,
			ManagedFunctionFactory<O, F> functionFactory, String teamName, String moName, Class<?> moType,
			String nextFunctionName, Class<?> nextFunctionArgumentType) {

		// Create the Function Builder
		ManagedFunctionBuilder<O, F> functionBuilder = this.constructFunction(functionName, functionFactory, teamName);

		// Register the next function and managed object
		if (nextFunctionName != null) {
			functionBuilder.setNextFunction(nextFunctionName, nextFunctionArgumentType);
		}
		if (moName != null) {
			functionBuilder.linkManagedObject(0, moName, moType);
		}

		// Return the function builder
		return functionBuilder;
	}

	/**
	 * Facade method to register a {@link ManagedFunction}.
	 * 
	 * @param <O>
	 *            Dependency key type.
	 * @param <F>
	 *            Flow key type.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @param function
	 *            {@link ManagedFunction}.
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @param nextFunctionName
	 *            Name of the next {@link ManagedFunction}.
	 * @param nextFunctionArgumentType
	 *            {@link Class} of the argument to the next
	 *            {@link ManagedFunction}.
	 * @return {@link ManagedFunctionBuilder} for the {@link ManagedFunction}.
	 */
	public <O extends Enum<O>, F extends Enum<F>> ManagedFunctionBuilder<O, F> constructFunction(String functionName,
			final ManagedFunction<O, F> function, String teamName, String nextFunctionName,
			Class<?> nextFunctionArgumentType) {

		// Create the Function Factory
		ManagedFunctionFactory<O, F> functionFactory = new ManagedFunctionFactory<O, F>() {
			public ManagedFunction<O, F> createManagedFunction() {
				return function;
			}
		};

		// Construct and return the function
		return this.constructFunction(functionName, functionFactory, teamName, null, null, nextFunctionName,
				nextFunctionArgumentType);
	}

	/**
	 * Facade method to register a {@link ManagedObject}.
	 * 
	 * @param <D>
	 *            Dependency key type.
	 * @param <F>
	 *            Flow key type.
	 * @param <MS>
	 *            {@link ManagedObjectSource} type.
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param managedObjectSourceClass
	 *            {@link ManagedObjectSource} {@link Class}.
	 * @return {@link ManagedObjectBuilder}.
	 */
	public <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> ManagedObjectBuilder<F> constructManagedObject(
			String managedObjectName, Class<MS> managedObjectSourceClass) {

		// Obtain the managed object source name
		String managedObjectSourceName = "of-" + managedObjectName;

		// Create the Managed Object Builder
		ManagedObjectBuilder<F> managedObjectBuilder = this.getOfficeFloorBuilder()
				.addManagedObject(managedObjectSourceName, managedObjectSourceClass);

		// Link into the Office
		this.officeBuilder.registerManagedObjectSource(managedObjectName, managedObjectSourceName);

		// Return the Managed Object Builder
		return managedObjectBuilder;
	}

	/**
	 * Facade method to register a {@link ManagedObject}.
	 * 
	 * @param <D>
	 *            Dependency key type.
	 * @param <F>
	 *            Flow key type.
	 * @param <MS>
	 *            {@link ManagedObjectSource} type.
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param managedObjectSourceClass
	 *            {@link ManagedObjectSource} {@link Class}.
	 * @param managingOffice
	 *            Name of the managing {@link Office}.
	 * @return {@link ManagedObjectBuilder}.
	 */
	public <D extends Enum<D>, F extends Enum<F>, MS extends ManagedObjectSource<D, F>> ManagedObjectBuilder<F> constructManagedObject(
			String managedObjectName, Class<MS> managedObjectSourceClass, String managingOffice) {

		// Obtain the managed object source name
		String managedObjectSourceName = "of-" + managedObjectName;

		// Create the Managed Object Builder
		ManagedObjectBuilder<F> managedObjectBuilder = this.getOfficeFloorBuilder()
				.addManagedObject(managedObjectSourceName, managedObjectSourceClass);

		// Flag managing office
		managedObjectBuilder.setManagingOffice(managingOffice).setInputManagedObjectName(managedObjectName);

		// Link into the Office
		this.officeBuilder.registerManagedObjectSource(managedObjectName, managedObjectSourceName);

		// Return the Managed Object Builder
		return managedObjectBuilder;
	}

	/**
	 * Facade method to register a {@link ManagedObject}.
	 * 
	 * @param <D>
	 *            Dependency key type.
	 * @param <F>
	 *            Flow key type.
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param metaData
	 *            {@link ManagedObjectSourceMetaData}.
	 * @param managedObject
	 *            {@link ManagedObject}.
	 * @param managingOffice
	 *            Name of the managing {@link Office}.
	 * @return {@link ManagedObjectBuilder}.
	 */
	public <D extends Enum<D>, F extends Enum<F>> ManagedObjectBuilder<F> constructManagedObject(
			String managedObjectName, ManagedObjectSourceMetaData<D, F> metaData, ManagedObject managedObject,
			String managingOffice) {

		// Obtain managed object source name
		String managedObjectSourceName = "of-" + managedObjectName;

		// Bind Managed Object
		ManagedObjectBuilder<F> managedObjectBuilder = MockManagedObjectSource
				.bindManagedObject(managedObjectSourceName, managedObject, metaData, this.getOfficeFloorBuilder());

		// Flag managing office
		managedObjectBuilder.setManagingOffice(managingOffice).setInputManagedObjectName(managedObjectName);

		// Link into the Office
		this.officeBuilder.registerManagedObjectSource(managedObjectName, managedObjectSourceName);

		// Return the builder
		return managedObjectBuilder;
	}

	/**
	 * Facade method to register a {@link ManagedObject}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param managedObject
	 *            {@link ManagedObject}.
	 * @param managingOffice
	 *            Name of the managing {@link Office}.
	 * @return {@link ManagedObjectBuilder}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ManagedObjectBuilder<?> constructManagedObject(String managedObjectName, ManagedObject managedObject,
			String managingOffice) {

		// Create the mock Managed Object Source meta-data
		ManagedObjectSourceMetaData metaData = new MockManagedObjectSourceMetaData(managedObject);

		// Register the Managed Object
		return this.constructManagedObject(managedObjectName, metaData, managedObject, managingOffice);
	}

	/**
	 * Facade method to register a {@link ManagedObject}.
	 * 
	 * @param object
	 *            Object for the {@link ManagedObject}.
	 * @param managedObjectName
	 *            Name of the {@link ManagedObject}.
	 * @param managingOffice
	 *            Name of the mananaging {@link Office}.
	 * @return {@link ManagedObjectBuilder}.
	 */
	public ManagedObjectBuilder<?> constructManagedObject(final Object object, String managedObjectName,
			String managingOffice) {

		// Create the wrapping Managed Object
		ManagedObject managedObject = new ManagedObject() {
			public Object getObject() {
				return object;
			}
		};

		// Register the managed object
		return this.constructManagedObject(managedObjectName, managedObject, managingOffice);
	}

	/**
	 * Facade method to create a {@link Team}.
	 * 
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @param team
	 *            {@link Team}.
	 * @return {@link TeamBuilder}.
	 */
	public TeamBuilder<?> constructTeam(String teamName, Team team) {

		// Obtain the office floor team name
		String officeFloorTeamName = "of-" + teamName;

		// Bind the team into the office floor
		TeamBuilder<?> teamBuilder = MockTeamSource.bindTeamBuilder(this.officeFloorBuilder, officeFloorTeamName, team);

		// Link into the Office
		this.officeBuilder.registerTeam(teamName, officeFloorTeamName);

		// Return the team builder
		return teamBuilder;
	}

	/**
	 * Facade method to create a {@link Team}.
	 * 
	 * @param <TS>
	 *            {@link TeamSource} type.
	 * @param teamName
	 *            Name of the {@link Team}.
	 * @param teamSourceClass
	 *            {@link TeamSource} class.
	 * @return {@link TeamBuilder}.
	 */
	public <TS extends TeamSource> TeamBuilder<?> constructTeam(String teamName, Class<TS> teamSourceClass) {

		// Obtain the office floor team name
		String officeFloorTeamName = "of-" + teamName;

		// Add the team to the office floor
		TeamBuilder<?> teamBuilder = this.officeFloorBuilder.addTeam(officeFloorTeamName, teamSourceClass);

		// Link into the office
		this.officeBuilder.registerTeam(teamName, officeFloorTeamName);

		// Return the team builder
		return teamBuilder;
	}

	/**
	 * Facade method to create a {@link Administrator}.
	 * 
	 * @param <I>
	 *            Extension interface type.
	 * @param <A>
	 *            {@link Administrator} type.
	 * @param adminName
	 *            Name of the {@link Administrator}.
	 * @param administrator
	 *            {@link Administrator}.
	 * @param administratorMetaData
	 *            {@link AdministratorSourceMetaData}.
	 * @param teamName
	 *            Name of {@link Team} for {@link Administrator} {@link Duty}
	 *            instances.
	 * @return {@link AdministratorBuilder}.
	 */
	public <E extends Object, A extends Enum<A>> AdministratorBuilder<A> constructAdministrator(String adminName,
			Administrator<E, A> administrator, AdministratorSourceMetaData<E, A> administratorMetaData,
			String teamName) {

		// Bind the Administrator
		AdministratorBuilder<A> adminBuilder = MockAdministratorSource.bindAdministrator(adminName, administrator,
				administratorMetaData, this.officeBuilder);

		// Configure the administrator
		adminBuilder.setTeam(teamName);

		// Return the administrator builder
		return adminBuilder;
	}

	/**
	 * Facade method to construct an {@link Administrator}.
	 * 
	 * @param <E>
	 *            Extension interface type.
	 * @param <A>
	 *            {@link Administrator} type.
	 * @param <AS>
	 *            {@link AdministratorSource} type.
	 * @param adminName
	 *            Name of the {@link Administrator}.
	 * @param adminSource
	 *            {@link AdministratorSource} {@link Class}.
	 * @param teamName
	 *            Name of {@link Team} for {@link Administrator} {@link Duty}
	 *            instances.
	 * @return {@link AdministratorBuilder}.
	 */
	public <E extends Object, A extends Enum<A>, AS extends AdministratorSource<E, A>> AdministratorBuilder<A> constructAdministrator(
			String adminName, Class<AS> adminSource, String teamName) {

		// Create the Administrator Builder
		AdministratorBuilder<A> adminBuilder = this.officeBuilder.addAdministrator(adminName, adminSource);

		// Configure the administrator
		adminBuilder.setTeam(teamName);

		// Return the administrator builder
		return adminBuilder;
	}

	/**
	 * Facade method to create the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 * @throws Exception
	 *             If fails to construct the {@link OfficeFloor}.
	 */
	protected OfficeFloor constructOfficeFloor() throws Exception {

		// Construct the Office Floor
		this.officeFloor = this.officeFloorBuilder.buildOfficeFloor();
		this.constructedOfficeFloors.add(this.officeFloor);

		// Initiate for constructing another office
		OFFICE_FLOOR_INDEX++;
		this.officeFloorBuilder = OfficeFrame.getInstance().createOfficeFloorBuilder(this.getOfficeFloorName());
		OFFICE_INDEX++;
		this.officeBuilder = this.officeFloorBuilder.addOffice(this.getOfficeName());

		// Return the Office Floor
		return this.officeFloor;
	}

	/**
	 * Facade method to invoke {@link ManagedFunction} of an {@link Office}. It
	 * will create the {@link OfficeFloor} if necessary and times out after 3
	 * seconds if invoked {@link ManagedFunction} is not complete.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction} to invoke.
	 * @param parameter
	 *            Parameter.
	 * @throws Exception
	 *             If fails to construct {@link Office} or
	 *             {@link ManagedFunction} invocation failure.
	 */
	protected void invokeFunction(String functionName, Object parameter) throws Exception {
		this.invokeFunction(functionName, parameter, 3);
	}

	/**
	 * Facade method to invoke {@link ManagedFunction} of an {@link Office}. It
	 * will create the {@link OfficeFloor} if necessary.
	 * 
	 * @param functionName
	 *            Name of the {@link ManagedFunction} to invoke.
	 * @param parameter
	 *            Parameter.
	 * @param secondsToRun
	 *            Seconds to run.
	 * @throws Exception
	 *             If fails to construct {@link Office} or
	 *             {@link ManagedFunction} invocation failure.
	 */
	protected void invokeFunction(String functionName, Object parameter, int secondsToRun) throws Exception {

		// Obtain the name of the office being constructed
		String officeName = this.getOfficeName();

		// Determine if required to construct
		if (this.officeFloor == null) {
			// Construct the OfficeFloor
			this.officeFloor = this.constructOfficeFloor();

			// Open the OfficeFloor
			this.officeFloor.openOfficeFloor();
		}

		// Wait on this object
		final boolean[] isComplete = new boolean[] { false };
		final Throwable[] failure = new Throwable[] { null };

		// Invoke the function
		Office office = this.officeFloor.getOffice(officeName);
		FunctionManager functionManager = office.getFunctionManager(functionName);
		functionManager.invokeProcess(parameter, new FlowCallback() {
			@Override
			public void run(Throwable escalation) throws Throwable {

				// Flag complete
				synchronized (isComplete) {
					isComplete[0] = true;
					failure[0] = escalation;
					isComplete.notify();
				}
			}
		});

		// Block until flow is complete (or times out)
		int iteration = 0;
		long startBlockTime = System.currentTimeMillis();
		synchronized (isComplete) {
			while (!isComplete[0]) {

				// Only timeout if positive time to run
				if (secondsToRun > 0) {
					// Provide heap diagnostics and time out
					this.timeout(startBlockTime, secondsToRun);
				}

				// Output heap diagnostics after every approximate 3 seconds
				iteration++;
				if ((iteration % 30) == 0) {
					iteration = 0;
					this.printHeapMemoryDiagnostics();
				}

				// Wait some time as still executing
				isComplete.wait(100);
			}
		}
	}

}