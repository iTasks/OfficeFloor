/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.impl.mbean;

import java.lang.management.ManagementFactory;

import javax.management.JMX;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MXBean;
import javax.management.ObjectName;

import net.officefloor.compile.mbean.OfficeFloorMBean;
import net.officefloor.compile.spi.mbean.MBeanRegistrator;
import net.officefloor.extension.CompileOffice;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.Parameter;

/**
 * Ensure register {@link OfficeFloor} as an MBean with ability to run
 * {@link ManagedFunction} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorMBeanTest extends OfficeFrameTestCase {

	/**
	 * Name of the {@link OfficeFloor}.
	 */
	private static final String OFFICE_FLOOR_NAME = "OfficeFloor";

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * {@link OfficeFloor} {@link ObjectName}.
	 */
	private ObjectName objectName;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Compile the OfficeFloor
		CompileOffice compiler = new CompileOffice();
		compiler.getOfficeFloorCompiler().setMBeanRegistrator(MBeanRegistrator.getPlatformMBeanRegistrator());
		this.officeFloor = compiler.compileOffice((extender, context) -> {
			extender.addOfficeSection("SECTION", ClassSectionSource.class.getName(), CompileSection.class.getName());
		});

		// Obtain the OfficeFloor ObjectName
		this.objectName = new ObjectName(
				"net.officefloor:type=" + OfficeFloor.class.getName() + ",name=" + OFFICE_FLOOR_NAME);
	}

	@Override
	protected void tearDown() throws Exception {

		// Ensure close the OfficeFloor
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}

		// Super shutdown
		super.tearDown();
	}

	/**
	 * Ensure registers {@link OfficeFloor} as an {@link MXBean}.
	 */
	public void testRegisterOfficeFloorAsMXBean() throws Exception {

		// Ensure not registered yet as MBean
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		assertFalse("Should not be registered, on just compiling", server.isRegistered(this.objectName));

		// Open the OfficeFloor (registering the MBeans)
		officeFloor.openOfficeFloor();
		assertTrue("Should now be registered", server.isRegistered(this.objectName));

		// Close the OfficeFloor (unregistering the Mbeans)
		officeFloor.closeOfficeFloor();
		assertFalse("Should no longer be registered", server.isRegistered(this.objectName));
	}

	/**
	 * Ensure correct {@link MBeanInfo}.
	 */
	public void testMBeanInfo() throws Exception {

		// Open and register MBean
		this.officeFloor.openOfficeFloor();

		// Obtain the MBean info
		MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		MBeanInfo info = server.getMBeanInfo(this.objectName);

		// Ensure correct attributes
		MBeanAttributeInfo[] attributes = info.getAttributes();
		assertEquals("Incorrect number of attributes", 1, attributes.length);
		assertEquals("Incorrect attribute name", "OfficeNames", attributes[0].getName());

		// Ensure correct operations
		MBeanOperationInfo[] operations = info.getOperations();
		assertEquals("Incorrect number of operations", 4, operations.length);
		assertEquals("Incorrect first operation", "getManagedFunctionNames", operations[0].getName());
		assertEquals("Incorrect first operation", "getManagedFunctionParameterType", operations[1].getName());
		assertEquals("Incorrect second operation", "invokeFunction", operations[2].getName());
		assertEquals("Incorrect third operation", "closeOfficeFloor", operations[3].getName());
	}

	/**
	 * Ensure can list {@link Office} instances within the {@link OfficeFloor}.
	 */
	public void testListOffices() throws Exception {

		// Open and register MBean
		this.officeFloor.openOfficeFloor();

		// Obtain the OfficeFloor MBean
		MBeanServerConnection connection = ManagementFactory.getPlatformMBeanServer();
		OfficeFloorMBean mbean = JMX.newMBeanProxy(connection, this.objectName, OfficeFloorMBean.class);

		// Obtain the list of Office names
		String[] officeNames = mbean.getOfficeNames();
		assertEquals("Incorrect number of offices", 1, officeNames.length);
		assertEquals("Incorrect Office name", "OFFICE", officeNames[0]);
	}

	/**
	 * Ensure can list {@link ManagedFunction} instances within the
	 * {@link OfficeFloor}.
	 */
	public void testListManagedFunctions() throws Exception {

		// Open and register MBean
		this.officeFloor.openOfficeFloor();

		// Obtain the OfficeFloor MBean
		MBeanServerConnection connection = ManagementFactory.getPlatformMBeanServer();
		OfficeFloorMBean mbean = JMX.newMBeanProxy(connection, this.objectName, OfficeFloorMBean.class);

		// Obtain the list of managed function names
		String[] managedFunctionNames = mbean.getManagedFunctionNames("OFFICE");
		assertEquals("Incorrect number of managed functions", 2, managedFunctionNames.length);
		assertEquals("Incorrect first managed function", "SECTION.function", managedFunctionNames[0]);
		assertEquals("Incorrect second managed function", "SECTION.functionWithParameter", managedFunctionNames[1]);
	}

	/**
	 * Ensure can obtain the parameter type for the {@link ManagedFunction}.
	 */
	public void testManagedFunctionParameterType() throws Exception {

		// Open and register MBean
		this.officeFloor.openOfficeFloor();

		// Obtain the OfficeFloor MBean
		MBeanServerConnection connection = ManagementFactory.getPlatformMBeanServer();
		OfficeFloorMBean mbean = JMX.newMBeanProxy(connection, this.objectName, OfficeFloorMBean.class);

		// Obtain the parameter types
		assertNull("Function should not have parameter",
				mbean.getManagedFunctionParameterType("OFFICE", "SECTION.function"));
		assertEquals("Incorrect parameter type", String.class.getName(),
				mbean.getManagedFunctionParameterType("OFFICE", "SECTION.functionWithParameter"));
	}

	/**
	 * Ensure can invoke a {@link ManagedFunction}.
	 */
	public void testInvokeManagedFunction() throws Exception {

		// Reset for testing
		CompileSection.isFunctionInvoked = false;
		CompileSection.invokedParameter = null;

		// Open and register MBean
		this.officeFloor.openOfficeFloor();

		// Obtain the OfficeFloor MBean
		MBeanServerConnection connection = ManagementFactory.getPlatformMBeanServer();
		OfficeFloorMBean mbean = JMX.newMBeanProxy(connection, this.objectName, OfficeFloorMBean.class);

		// Ensure not yet invoked function
		assertFalse("Managed function should not yet be invoked", CompileSection.isFunctionInvoked);

		// Invoke the function
		mbean.invokeFunction("OFFICE", "SECTION.function", "TEST");

		// Ensure the function is invoked
		assertTrue("Managed function should be invoked", CompileSection.isFunctionInvoked);
		assertNull("Should ignore parameter", CompileSection.invokedParameter);
	}

	/**
	 * Ensure can invoke a {@link ManagedFunction} with a parameter.
	 */
	public void testInvokeManagedFunctionWithParameter() throws Exception {

		// Reset for testing
		CompileSection.invokedParameter = null;

		// Open and register MBean
		this.officeFloor.openOfficeFloor();

		// Obtain the OfficeFloor MBean
		MBeanServerConnection connection = ManagementFactory.getPlatformMBeanServer();
		OfficeFloorMBean mbean = JMX.newMBeanProxy(connection, this.objectName, OfficeFloorMBean.class);

		// Ensure not yet invoked function
		assertNull("Managed function should not yet be invoked", CompileSection.invokedParameter);

		// Invoke the function
		mbean.invokeFunction("OFFICE", "SECTION.functionWithParameter", "TEST");

		// Ensure the function is invoked with parameter
		assertEquals("Managed function should be invoked with parameter", "TEST", CompileSection.invokedParameter);
	}

	/**
	 * Ensure able to close {@link OfficeFloor}.
	 */
	public void testCloseOfficeFloor() throws Exception {

		// Open and register MBean
		this.officeFloor.openOfficeFloor();

		// Obtain the OfficeFloor MBean
		MBeanServerConnection connection = ManagementFactory.getPlatformMBeanServer();
		OfficeFloorMBean mbean = JMX.newMBeanProxy(connection, this.objectName, OfficeFloorMBean.class);

		// Close the OfficeFloor (via MBean)
		mbean.closeOfficeFloor();
		assertFalse("OfficeFloor should no longer be registered", connection.isRegistered(this.objectName));
	}

	public static class CompileSection {

		public static boolean isFunctionInvoked = false;

		public static String invokedParameter = null;

		public void function() {
			isFunctionInvoked = true;
		}

		public void functionWithParameter(@Parameter String parameter) {
			invokedParameter = parameter;
		}
	}

}
