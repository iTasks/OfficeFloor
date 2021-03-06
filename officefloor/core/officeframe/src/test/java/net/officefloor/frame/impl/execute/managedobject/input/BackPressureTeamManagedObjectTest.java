/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.managedobject.input;

import java.util.function.BiConsumer;

import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.BackPressureTeamSource;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;
import net.officefloor.frame.test.TestObject;

/**
 * Ensures appropriately handles the back pressure.
 *
 * @author Daniel Sagenschneider
 */
public class BackPressureTeamManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure handles immediate failure.
	 */
	public void testBackPressure() throws Exception {
		this.doBackPressureTest(0, (work, escalation) -> {
			assertSame("Should propagate the escalation", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION, escalation);
		});
	}

	/**
	 * Ensure handle {@link FlowCallback} propagating failure.
	 */
	public void testFlowBackPressure() throws Exception {
		this.doBackPressureTest(1, (work, escalation) -> {
			assertSame("Should propagate via function", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION, work.failure);
			assertSame("Should propagate the escalation", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION, escalation);
		});
	}

	/**
	 * Ensure handle next {@link ManagedFunction} propagating failure.
	 */
	public void testNextBackPressure() throws Exception {
		this.doBackPressureTest(2, (work, escalation) -> {
			assertSame("Should propagate the escalation", BackPressureTeamSource.BACK_PRESSURE_EXCEPTION, escalation);
		});
	}

	/**
	 * Undertakes the back pressure test.
	 * 
	 * @param flowIndex Index of {@link ManagedObjectExecuteContext} to invoke.
	 * @param validator Validates the result.
	 */
	private void doBackPressureTest(int flowIndex, BiConsumer<TestWork, Throwable> validator) throws Exception {

		// Construct the object
		TestObject object = new TestObject("MO", this);
		object.enhanceMetaData = (context) -> {
			context.addFlow(null);
			context.addFlow(null);
			context.addFlow(null);
		};
		ManagingOfficeBuilder<?> managingOffice = object.managedObjectBuilder.setManagingOffice(this.getOfficeName());
		managingOffice.setInputManagedObjectName("MO");
		managingOffice.linkFlow(0, "backPressure");
		managingOffice.linkFlow(1, "flow");
		managingOffice.linkFlow(2, "next");

		// Construct the functions
		TestWork work = new TestWork();

		// Flow
		ReflectiveFunctionBuilder flow = this.constructFunction(work, "flow");
		flow.buildFlow("backPressure", null, false);

		// Next
		this.constructFunction(work, "next").setNextFunction("backPressure");

		// Function causing back pressure by team
		this.constructTeam("BACK_PRESSURE", BackPressureTeamSource.class);
		this.constructFunction(work, "backPressure").getBuilder().setResponsibleTeam("BACK_PRESSURE");

		// Open OfficeFloor
		try (OfficeFloor officeFloor = this.constructOfficeFloor()) {
			officeFloor.openOfficeFloor();

			// Undertake flow
			Closure<Throwable> propagatedFailure = new Closure<>();
			object.managedObjectServiceContext.invokeProcess(flowIndex, null, object, 0, (escalation) -> {
				propagatedFailure.value = escalation;
			});

			// Ensure handle escalation
			validator.accept(work, propagatedFailure.value);
		}
	}

	/**
	 * Test functionality.
	 */
	public static class TestWork {

		private volatile Throwable failure = null;

		public void flow(ReflectiveFlow flow) {
			flow.doFlow(null, (escalation) -> {
				this.failure = escalation;
				if (escalation != null) {
					throw escalation;
				}
			});
		}

		public void next() {
			// ensure next also propagates the back pressure
		}

		public void backPressure() throws Exception {
			fail("Back pressure function should not be invoked");
		}
	}

}
