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
package net.officefloor.frame.impl.execute.managedobject.nameaware;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.NameAwareManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.TestObject;

/**
 * Ensure handle failure on loading name to {@link NameAwareManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public class _fail_NameAwareManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure handle failure on loading name to {@link ProcessState} bound
	 * {@link ManagedObject}.
	 */
	public void testFailSetName_boundTo_Process() throws Exception {
		this.doFailSetNameTest(ManagedObjectScope.PROCESS);
	}

	/**
	 * Ensure handle failure on loading name to {@link ThreadState} bound
	 * {@link ManagedObject}.
	 */
	public void testFailSetName_boundTo_Thread() throws Exception {
		this.doFailSetNameTest(ManagedObjectScope.THREAD);
	}

	/**
	 * Ensure handle failure on loading name to {@link ManagedFunction} bound
	 * {@link ManagedObject}.
	 */
	public void testFailSetName_boundTo_Function() throws Exception {
		this.doFailSetNameTest(ManagedObjectScope.FUNCTION);
	}

	/**
	 * Ensure can obtain bound {@link ManagedObject} name.
	 */
	private void doFailSetNameTest(ManagedObjectScope scope) throws Exception {

		// Construct the managed object
		TestObject object = new TestObject("MO", this);
		object.isNameAwareManagedObject = true;
		object.bindNameAwareFailure = new RuntimeException("TEST");

		// Construct functions
		TestWork work = new TestWork();
		this.constructFunction(work, "task").buildObject("MO", scope);

		// Ensure invoke function
		try {
			this.invokeFunction("task", null);
			fail("Should not be successful");
		} catch (Exception ex) {
			assertSame("Incorrect cause", object.bindNameAwareFailure, ex);
		}
	}

	/**
	 * Test functionality.
	 */
	public class TestWork {

		public void task(TestObject object) {
			fail("Should not be invoked as fails to set name");
		}
	}

}