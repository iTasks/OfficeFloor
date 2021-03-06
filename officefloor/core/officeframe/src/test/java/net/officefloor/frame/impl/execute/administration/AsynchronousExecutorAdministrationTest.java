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

package net.officefloor.frame.impl.execute.administration;

import static org.junit.Assert.assertNotEquals;

import java.util.concurrent.Executor;

import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveFunctionBuilder;

/**
 * Ensure {@link Executor} runs on another {@link Thread}.
 * 
 * @author Daniel Sagenschneider
 */
public class AsynchronousExecutorAdministrationTest extends AbstractOfficeConstructTestCase {

	/**
	 * Ensure {@link Executor} runs on another {@link Thread}.
	 */
	public void testExecutorOnAnotherThread() throws Exception {

		// Capture the current invoking thread
		Thread currentThread = Thread.currentThread();

		// Construct the functions
		TestWork work = new TestWork();
		ReflectiveFunctionBuilder function = this.constructFunction(work, "function");
		function.preAdminister("preTask").buildAdministrationContext();

		// Run the function
		this.invokeFunction("function", null);

		// Should complete servicing
		assertTrue("Should complete servicing", work.isComplete);

		// Executor thread should be different
		assertNotEquals("Executor should be invoked by different thread", currentThread, work.executorThread);
	}

	public class TestWork {

		private volatile Thread executorThread = null;

		private volatile boolean isComplete = false;

		public void preTask(Object[] exections, AdministrationContext<?, ?, ?> context) {
			AsynchronousFlow flow = context.createAsynchronousFlow();
			context.getExecutor().execute(() -> {
				this.executorThread = Thread.currentThread();
				flow.complete(null);
			});
		}

		public void function() {
			assertNotNull("Should have executor thread", this.executorThread);
			this.isComplete = true;
		}
	}

}
