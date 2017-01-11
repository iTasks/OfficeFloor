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
package net.officefloor.frame.impl.execute.managedobject.coordinate;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;

/**
 * Ensure can have multiple {@link CoordinatingManagedObject} dependencies that
 * ensure coordination of dependency before the
 * {@link CoordinatingManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class ChainCoordinateManagedObjectTest extends AbstractOfficeConstructTestCase {

	/**
	 * {@link AsynchronousListener} instances to trigger.
	 */
	private static final List<AsynchronousListener> listeners = new ArrayList<AsynchronousListener>();

	/**
	 * Ensure that {@link CoordinatingManagedObject} can depend on another
	 * {@link CoordinatingManagedObject}.
	 */
	public void testChainingTogetherCoordinatingManagedObjects() throws Exception {

		// Ensure no listeners
		listeners.clear();

		// Obtain the office name
		final String officeName = this.getOfficeName();

		// Calculate expected coordination
		String identifier = "0";

		// Construct the seed value
		ManagedObjectBuilder<None> moBuilder = this.constructManagedObject(identifier,
				ChainCoordinatingManagedObjectSource.class, officeName);
		moBuilder.addProperty(ChainCoordinatingManagedObjectSource.PROPERTY_IDENTIFIER, identifier);
		moBuilder.addProperty(ChainCoordinatingManagedObjectSource.PROPERTY_OBTAIN_DEPENDENCY, "false");
		moBuilder.setTimeout(10);
		this.getOfficeBuilder().addProcessManagedObject(identifier, identifier);

		// Create chaining of coordination (start identifier after seed)
		for (int i = 1; i < 100; i++) {

			// Obtain the identifier for this coordination
			identifier = String.valueOf(i);

			// Obtain the identifier for previous in chain
			final String PREVIOUS = String.valueOf(i - 1);

			// Provide the chained coordinating managed object
			moBuilder = this.constructManagedObject(identifier, ChainCoordinatingManagedObjectSource.class, officeName);
			moBuilder.addProperty(ChainCoordinatingManagedObjectSource.PROPERTY_IDENTIFIER, identifier);
			moBuilder.setTimeout(10);
			DependencyMappingBuilder dependencies = this.getOfficeBuilder().addProcessManagedObject(identifier,
					identifier);
			dependencies.mapDependency(0, PREVIOUS);
		}

		// Construct the work
		ChainCoordinatingWork coordinate = new ChainCoordinatingWork();
		this.constructFunction(coordinate, "service").buildObject(identifier);

		// Invoke the function
		boolean[] isComplete = new boolean[] { false };
		this.triggerFunction("service", null, (escalation) -> isComplete[0] = true);

		// Allow processing of work
		while (!isComplete[0]) {

			// Ensure have completion listener
			// (otherwise should be complete)
			assertTrue("Should have completion listener", listeners.size() == 1);
			AsynchronousListener listener = listeners.remove(0);
			listener.notifyComplete();
		}

		// Verify the resulting coordination
		assertNotNull("Should have dependency", coordinate.dependency);
		ChainCoordinatingManagedObject dependency = coordinate.dependency;
		for (int i = 99; i >= 0; i--) {
			assertEquals("Incorrect dependency", String.valueOf(i), dependency.identifier);
			dependency = dependency.dependency;
		}
	}

	/**
	 * Functionality for obtaining the resulting chained coordination.
	 */
	public static class ChainCoordinatingWork {

		/**
		 * Result of chained coordination.
		 */
		public volatile ChainCoordinatingManagedObject dependency = null;

		/**
		 * {@link ManagedFunction} to receive results of chained coordination.
		 * 
		 * @param dependency
		 *            Result of chained coordination.
		 */
		public void service(ChainCoordinatingManagedObject dependency) {
			this.dependency = dependency;
		}
	}

	/**
	 * Chaining together {@link CoordinatingManagedObject}.
	 */
	public static class ChainCoordinatingManagedObjectSource extends AbstractManagedObjectSource<Indexed, None> {

		/**
		 * Name of property to specify the identifier.
		 */
		public static final String PROPERTY_IDENTIFIER = "idenifier";

		/**
		 * Name of property to specify whether to obtain dependency.
		 */
		public static final String PROPERTY_OBTAIN_DEPENDENCY = "obtain.dependency";

		/**
		 * Identifier of the dependency.
		 */
		private String identifier;

		/**
		 * Indicates whether to obtain a dependency;
		 */
		private boolean isObtainDependency;

		/*
		 * ====================== ManagedObjectSource =======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			context.addProperty(PROPERTY_IDENTIFIER);
		}

		@Override
		protected void loadMetaData(MetaDataContext<Indexed, None> context) throws Exception {
			ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

			// Obtain the identifier
			this.identifier = mosContext.getProperty(PROPERTY_IDENTIFIER);

			// Obtain whether to obtain dependency
			this.isObtainDependency = Boolean
					.parseBoolean(mosContext.getProperty(PROPERTY_OBTAIN_DEPENDENCY, Boolean.TRUE.toString()));

			// Specify the meta-data
			context.setObjectClass(ChainCoordinatingManagedObject.class);
			context.setManagedObjectClass(ChainCoordinatingManagedObject.class);
			if (this.isObtainDependency) {
				context.addDependency(ChainCoordinatingManagedObject.class).setLabel("Dependency");
			}
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			return new ChainCoordinatingManagedObject(this.identifier, this.isObtainDependency);
		}
	}

	/**
	 * Chaining together {@link CoordinatingManagedObject}.
	 */
	public static class ChainCoordinatingManagedObject
			implements CoordinatingManagedObject<Indexed>, AsynchronousManagedObject {

		/**
		 * Identifier of the dependency.
		 */
		public final String identifier;

		/**
		 * Flags whether to obtain a dependency.
		 */
		private final boolean isObtainDependency;

		/**
		 * {@link AsynchronousListener}.
		 */
		private AsynchronousListener listener;

		/**
		 * Dependency.
		 */
		public ChainCoordinatingManagedObject dependency;

		/**
		 * Initiate.
		 * 
		 * @param identifier
		 *            Identifier of the dependency.
		 * @param isObtainDependency
		 *            Flags whether to obtain a dependency.
		 */
		public ChainCoordinatingManagedObject(String identifier, boolean isObtainDependency) {
			this.identifier = identifier;
			this.isObtainDependency = isObtainDependency;
		}

		/*
		 * ======================== ManagedObject ============================
		 */

		@Override
		public void registerAsynchronousListener(AsynchronousListener listener) {
			this.listener = listener;
		}

		@Override
		public void loadObjects(ObjectRegistry<Indexed> registry) throws Throwable {

			// Obtain the dependency
			if (this.isObtainDependency) {
				this.dependency = (ChainCoordinatingManagedObject) registry.getObject(0);
			}

			// Trigger asynchronous operation (so continues coordinating)
			this.listener.notifyStarted();

			// Register the lister for triggering completion
			listeners.add(this.listener);
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}