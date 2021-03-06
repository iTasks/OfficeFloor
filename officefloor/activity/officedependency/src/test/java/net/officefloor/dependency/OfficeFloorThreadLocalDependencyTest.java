/*-
 * #%L
 * Dependency
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

package net.officefloor.dependency;

import java.lang.reflect.Proxy;
import java.util.function.Supplier;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.sf.cglib.proxy.Enhancer;

/**
 * Tests the {@link OfficeFloorThreadLocalDependency}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorThreadLocalDependencyTest extends OfficeFrameTestCase {

	/**
	 * Ensure can proxy interface.
	 */
	@SuppressWarnings("unchecked")
	public void testInterfaceDependency() {

		// Create mocks
		Supplier<InterfaceDependency> supplierThreadLocal = this.createMock(Supplier.class);
		InterfaceDependency dependencyOne = this.createMock(InterfaceDependency.class);
		InterfaceDependency dependencyTwo = this.createMock(InterfaceDependency.class);

		// Record accessing thread local
		this.recordReturn(supplierThreadLocal, supplierThreadLocal.get(), dependencyOne);
		this.recordReturn(dependencyOne, dependencyOne.getValue(), "interface");
		this.recordReturn(supplierThreadLocal, supplierThreadLocal.get(), dependencyTwo);
		dependencyTwo.setValue("interface");

		// Create class loader
		ClassLoader classLoader = this.getClass().getClassLoader();

		// Test
		this.replayMockObjects();

		// Create the proxy
		InterfaceDependency proxy = OfficeFloorThreadLocalDependency.newStaticProxy(InterfaceDependency.class,
				classLoader, supplierThreadLocal);
		assertTrue("Dependency should be a proxy", Proxy.isProxyClass(proxy.getClass()));

		// Ensure dependencies obtain thread local
		assertEquals("Incorrect first dependency value", "interface", proxy.getValue());
		proxy.setValue("interface");

		// Verify
		this.verifyMockObjects();
	}

	public static interface InterfaceDependency {

		String getValue();

		void setValue(String value);
	}

	/**
	 * Ensure can proxy object.
	 */
	@SuppressWarnings("unchecked")
	public void testObjectDependency() {

		// Create mocks
		Supplier<ObjectDependency> supplierThreadLocal = this.createMock(Supplier.class);
		ObjectDependency dependencyOne = new ObjectDependency();
		ObjectDependency dependencyTwo = new ObjectDependency();

		// Record accessing thread local
		this.recordReturn(supplierThreadLocal, supplierThreadLocal.get(), dependencyOne);
		this.recordReturn(supplierThreadLocal, supplierThreadLocal.get(), dependencyTwo);

		// Create class loader
		ClassLoader classLoader = this.getClass().getClassLoader();

		// Test
		this.replayMockObjects();

		// Create the proxy
		ObjectDependency proxy = OfficeFloorThreadLocalDependency.newStaticProxy(ObjectDependency.class, classLoader,
				supplierThreadLocal);
		assertFalse("Dependency should not be a reflection proxy", Proxy.isProxyClass(proxy.getClass()));
		assertTrue("Dependency should be a cglib enhanced", Enhancer.isEnhanced(proxy.getClass()));

		// Ensure correct first dependency
		proxy.setValue("value");
		assertEquals("Should set on first dependency", "value", dependencyOne.getValue());

		// Ensure correct second dependency
		assertEquals("Incorrect second dependency", "object", proxy.getValue());

		// Verify
		this.verifyMockObjects();
	}

	public static class ObjectDependency {

		private String value = "object";

		public String getValue() {
			return this.value;
		}

		public void setValue(String value) {
			this.value = value;
		}
	}

}
