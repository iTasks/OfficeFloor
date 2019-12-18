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
package net.officefloor.compile.impl.managedobject;

import junit.framework.TestCase;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.api.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObjectSource;

/**
 * Tests the {@link ManagedObjectLoader} in loading the
 * {@link OfficeFloorManagedObjectSourceType}.
 *
 * @author Daniel Sagenschneider
 */
public class LoadOfficeFloorManagedObjectSourceTypeTest extends OfficeFrameTestCase {

	/**
	 * Ensure can load via {@link ClassManagedObjectSource} {@link Class}.
	 */
	public void testLoadByClass() {

		// Node
		final ManagedObjectSourceNode node = this.createMock(ManagedObjectSourceNode.class);

		// Name of the managed object source
		final String MANAGED_OBJECT_SOURCE_NAME = "MOS";

		// Configure test
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());

		// Configure to load simple class
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(MockLoadManagedObject.class.getName());

		// Obtain node context
		NodeContext nodeContext = (NodeContext) compiler;

		// Load the OfficeFloor managed object source type
		ManagedObjectLoader moLoader = nodeContext.getManagedObjectLoader(node);
		OfficeFloorManagedObjectSourceType mosType = moLoader.loadOfficeFloorManagedObjectSourceType(
				MANAGED_OBJECT_SOURCE_NAME, ClassManagedObjectSource.class, properties);
		MockLoadManagedObject.assertOfficeFloorManagedObjectSourceType(mosType, MANAGED_OBJECT_SOURCE_NAME);
	}

	/**
	 * Ensure can load via {@link ClassManagedObjectSource} instance.
	 */
	public void testLoadByInstance() {

		// Node
		final ManagedObjectSourceNode node = this.createMock(ManagedObjectSourceNode.class);

		// Name of the managed object source
		final String MANAGED_OBJECT_SOURCE_NAME = "MOS";

		// Configure test
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(new FailTestCompilerIssues());

		// Configure to load simple class
		PropertyList properties = compiler.createPropertyList();
		properties.addProperty(ClassManagedObjectSource.CLASS_NAME_PROPERTY_NAME)
				.setValue(MockLoadManagedObject.class.getName());

		// Obtain node context
		NodeContext nodeContext = (NodeContext) compiler;

		// Load the OfficeFloor managed object source type
		ManagedObjectLoader moLoader = nodeContext.getManagedObjectLoader(node);
		OfficeFloorManagedObjectSourceType mosType = moLoader.loadOfficeFloorManagedObjectSourceType(
				MANAGED_OBJECT_SOURCE_NAME, new ClassManagedObjectSource(), properties);
		MockLoadManagedObject.assertOfficeFloorManagedObjectSourceType(mosType, MANAGED_OBJECT_SOURCE_NAME);
	}

	/**
	 * Ensures issue if failure in obtaining the
	 * {@link ManagedObjectSourceSpecification}.
	 */
	public void testFailGetManagedObjectSourceSpecification() {

		final ManagedObjectSourceNode node = this.createMock(ManagedObjectSourceNode.class);
		final Error failure = new Error("specification failure");
		final MockCompilerIssues issues = new MockCompilerIssues(this);

		// Record failure to instantiate
		this.recordReturn(node, node.getQualifiedName(), "mos");
		issues.recordIssue("mos", node.getClass(),
				"Failed to obtain ManagedObjectSourceSpecification from " + MockManagedObjectSource.class.getName(),
				failure);

		// Attempt to obtain specification
		MockManagedObjectSource.reset();
		MockManagedObjectSource.specificationFailure = failure;
		this.replayMockObjects();

		// Configure test
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);

		// Obtain node context
		NodeContext nodeContext = (NodeContext) compiler;

		// Load the OfficeFloor managed object source type
		ManagedObjectLoader moLoader = nodeContext.getManagedObjectLoader(node);
		OfficeFloorManagedObjectSourceType mosType = moLoader.loadOfficeFloorManagedObjectSourceType("mos",
				MockManagedObjectSource.class, compiler.createPropertyList());

		// Ensure not loaded
		TestCase.assertNull("Should not load type", mosType);

		this.verifyMockObjects();
	}

	/**
	 * Mock {@link ManagedObjectSource} for testing.
	 */
	@TestSource
	public static class MockManagedObjectSource implements ManagedObjectSource<None, None> {

		/**
		 * Failure to obtain the {@link ManagedObjectSourceSpecification}.
		 */
		public static Error specificationFailure = null;

		/**
		 * Resets the state for next test.
		 */
		public static void reset() {
			specificationFailure = null;
		}

		/*
		 * ================ ManagedObjectSource ================================
		 */

		@Override
		public ManagedObjectSourceSpecification getSpecification() {
			// Throw failure
			throw specificationFailure;
		}

		@Override
		public ManagedObjectSourceMetaData<None, None> init(ManagedObjectSourceContext<None> context) throws Exception {
			fail("Should not be invoked for obtaining specification");
			return null;
		}

		@Override
		public void start(ManagedObjectExecuteContext<None> context) throws Exception {
			fail("Should not be invoked for obtaining specification");
		}

		@Override
		public void sourceManagedObject(ManagedObjectUser user) {
			fail("Should not be invoked for obtaining specification");
		}

		@Override
		public void stop() {
			fail("Should not be invoked for obtaining specification");
		}
	}

}