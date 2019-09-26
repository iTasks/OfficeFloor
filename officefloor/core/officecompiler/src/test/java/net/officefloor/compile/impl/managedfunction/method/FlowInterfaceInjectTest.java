/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.compile.impl.managedfunction.method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.Closure;
import net.officefloor.plugin.clazz.FlowInterface;

/**
 * @author Daniel Sagenschneider
 */
public class FlowInterfaceInjectTest {

	/**
	 * Ensure {@link FlowInterface} with single {@link Flow}.
	 */
	public void testSingleFlowInterface() {
		Closure<Object> parameter = new Closure<>();
		Closure<FlowCallback> callback = new Closure<>();
		SingleFlowInterfaceFunction instance = new SingleFlowInterfaceFunction();
		MethodManagedFunctionBuilderUtil.runMethod(instance, "method", (type) -> {
			ManagedFunctionFlowTypeBuilder<Indexed> flow = type.addFlow();
			flow.setArgumentType(String.class);
			flow.setLabel("single");
		}, (context) -> {
			context.setFlow(0, (flowParameter, flowCallback) -> {
				parameter.value = flowParameter;
				callback.value = flowCallback;
			});
		});
		assertSame("Incorrect parameter", SingleFlowInterfaceFunction.PARAMETER, parameter.value);
		assertSame("Incorrect callback", instance, callback.value);
	}

	public static class SingleFlowInterfaceFunction implements FlowCallback {

		public static final String PARAMETER = "FLOW";

		public void method(SingleFlow flow) {
			flow.single(PARAMETER, this);
		}

		@Override
		public void run(Throwable escalation) throws Throwable {
			fail("Should not be invoked");
		}
	}

	@FlowInterface
	public static interface SingleFlow {
		void single(String parameter, FlowCallback callback);
	}

	/**
	 * Ensure {@link FlowInterface} with multiple {@link Flow}.
	 */
	public void testMultipleFlowInterface() {
		MultipleFlowInterfaceFunction instance = new MultipleFlowInterfaceFunction();
		final String[] orderedFlowNames = new String[] { "four", "one", "three", "two" };
		final Object[] expectedParameters = new Object[] { null, "one", "three", Integer.valueOf(2) };
		final FlowCallback[] expectedCallbacks = new FlowCallback[] { null, instance, null, null };
		final boolean[] isInvoked = new boolean[] { false, false, false, false };
		MethodManagedFunctionBuilderUtil.runMethod(instance, "method", (type) -> {
			for (int i = 0; i < orderedFlowNames.length; i++) {
				ManagedFunctionFlowTypeBuilder<Indexed> flow = type.addFlow();
				flow.setLabel(orderedFlowNames[i]);
				Object parameter = expectedParameters[i];
				if (parameter != null) {
					flow.setArgumentType(parameter.getClass());
				}
			}
		}, (context) -> {
			for (int i = 0; i < orderedFlowNames.length; i++) {
				int flowIndex = i;
				context.setFlow(flowIndex, (flowParameter, flowCallback) -> {
					assertEquals("Incorrect parameter for flow " + flowIndex, expectedParameters[flowIndex],
							flowParameter);
					assertSame("Incorrect callback for flow " + flowIndex, expectedCallbacks[flowIndex], flowCallback);
					isInvoked[flowIndex] = true;
				});
			}
		});
		for (int i = 0; i < isInvoked.length; i++) {
			assertTrue("Flow " + i + " not invoked", isInvoked[i]);
		}
	}

	public static class MultipleFlowInterfaceFunction implements FlowCallback {

		public void method(MultipleFlow flow) {
			flow.one("one", this);
			flow.two(2, null);
			flow.three("three");
			flow.four();
		}

		@Override
		public void run(Throwable escalation) throws Throwable {
			fail("Should not be invoked");
		}
	}

	@FlowInterface
	public static interface MultipleFlow {
		void one(String parameter, FlowCallback callback);

		void two(Integer parameter, FlowCallback callback);

		void three(String parameter);

		void four();
	}

}
