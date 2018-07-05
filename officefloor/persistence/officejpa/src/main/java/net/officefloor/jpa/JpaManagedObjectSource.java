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
package net.officefloor.jpa;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.util.Properties;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * JPA {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class JpaManagedObjectSource extends AbstractManagedObjectSource<JpaManagedObjectSource.Dependencies, None> {

	/**
	 * {@link Property} for the name of the persistence unit.
	 */
	public static final String PROPERTY_PERSISTENCE_UNIT = "persistence.unit.name";

	/**
	 * {@link Property} to obtain the {@link Class} name for the
	 * {@link PersistenceFactory}.
	 */
	public static final String PROPERTY_PERSISTENCE_FACTORY = "persistence.factory";

	/**
	 * {@link DataSource} type for {@link Proxy}.
	 */
	private static final Class<?>[] DATA_SOURCE_TYPE = new Class[] { DataSource.class };

	/**
	 * {@link EntityManager} type for {@link Proxy}.
	 */
	private static final Class<?>[] ENTITY_MANAGER_TYPE = new Class[] { EntityManager.class };

	/**
	 * Dependencies.
	 */
	public static enum Dependencies {
		CONNECTION
	}

	/**
	 * <p>
	 * {@link FunctionalInterface} to create the {@link EntityManagerFactory}.
	 * <p>
	 * Note: the {@link EntityManagerFactory} is required to be configured with the
	 * input {@link DataSource}.
	 */
	@FunctionalInterface
	public static interface PersistenceFactory {

		/**
		 * Creates the {@link EntityManagerFactory}.
		 * 
		 * @param persistenceUnitName Persistence Unit name.
		 * @param dataSource          {@link DataSource} to use for the
		 *                            {@link EntityManagerFactory}.
		 * @param properties          Existing properties configured to the
		 *                            {@link JpaManagedObjectSource}.
		 * @return Configuration for the {@link EntityManagerFactory} to use the
		 *         {@link DataSource}.
		 * @throws Exception If fails to create the {@link EntityManagerFactory}.
		 */
		EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, DataSource dataSource,
				Properties properties) throws Exception;
	}

	/**
	 * {@link DataSource} {@link Connection}.
	 */
	private final ThreadLocal<Connection> dataSourceConnection = new ThreadLocal<>();

	/**
	 * {@link DataSource}.
	 */
	private DataSource dataSource;

	/**
	 * Persistence unit name.
	 */
	private String persistenceUnitName;

	/**
	 * {@link Properties} for the {@link EntityManagerFactory}.
	 */
	private Properties properties;

	/**
	 * {@link ClassLoader}.
	 */
	private ClassLoader classLoader;

	/**
	 * {@link PersistenceFactory}.
	 */
	private PersistenceFactory persistenceFactory;

	/**
	 * {@link EntityManagerFactory}.
	 */
	private EntityManagerFactory entityManagerFactory;

	/**
	 * <p>
	 * Obtains the {@link PersistenceFactory}.
	 * <p>
	 * Specific vendor implementations may override this method to specify the
	 * {@link PersistenceFactory}.
	 * <p>
	 * By default, this method uses the {@link #PROPERTY_PERSISTENCE_FACTORY}
	 * {@link Property} to load the {@link PersistenceFactory}.
	 * 
	 * @param context {@link MetaDataContext}.
	 * @return {@link PersistenceFactory}.
	 * @throws Exception If fails to create the {@link PersistenceFactory}.
	 */
	protected PersistenceFactory getPersistenceFactory(MetaDataContext<Dependencies, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Obtain the class name
		String className = mosContext.getProperty(PROPERTY_PERSISTENCE_FACTORY);

		// Create instance and return
		return (PersistenceFactory) mosContext.loadClass(className).getDeclaredConstructor().newInstance();
	}

	/*
	 * ================== ManagedObjectSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_PERSISTENCE_UNIT, "Persistence Unit");

		// Determine if using default implementation
		if (this.getClass() == JpaManagedObjectSource.class) {
			context.addProperty(PROPERTY_PERSISTENCE_FACTORY, "Persistence Factory");
		}
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context) throws Exception {
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		// Load the meta-data
		context.setObjectClass(EntityManager.class);
		context.setManagedObjectClass(JpaManagedObject.class);
		context.addDependency(Dependencies.CONNECTION, Connection.class);
		context.addManagedObjectExtension(EntityManager.class,
				(managedObject) -> ((JpaManagedObject) managedObject).entityManager);
		context.getManagedObjectSourceContext().getRecycleFunction(new RecycleFunction()).linkParameter(0,
				RecycleManagedObjectParameter.class);

		// Obtain the details to create Entity Manager Factory
		this.persistenceUnitName = mosContext.getProperty(PROPERTY_PERSISTENCE_UNIT);
		this.properties = mosContext.getProperties();
		this.classLoader = mosContext.getClassLoader();

		// Create the DataSource
		this.dataSource = (DataSource) Proxy.newProxyInstance(this.classLoader, DATA_SOURCE_TYPE,
				(proxy, method, args) -> {
					switch (method.getName()) {
					case "getConnection":
						return this.dataSourceConnection.get();
					}
					throw new UnsupportedOperationException("Method " + method.getName()
							+ " not available from JPA proxy " + DataSource.class.getSimpleName());
				});

		// Obtain the persistence factory
		this.persistenceFactory = this.getPersistenceFactory(context);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new JpaManagedObject();
	}

	/**
	 * JPA {@link ManagedObject}.
	 */
	private class JpaManagedObject implements CoordinatingManagedObject<Dependencies> {

		/**
		 * {@link Proxy} {@link EntityManager}.
		 */
		private EntityManager proxy;

		/**
		 * {@link EntityManager}.
		 */
		private EntityManager entityManager;

		/*
		 * ================== ManagedObject =======================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {

			// Obtain the connection
			Connection connection = (Connection) registry.getObject(Dependencies.CONNECTION);

			// Easy access to managed object source
			JpaManagedObjectSource mos = JpaManagedObjectSource.this;

			// Specify connection for setup of entity manager factory and entity manager
			mos.dataSourceConnection.set(connection);

			// If still reference to persistence factory (then likely factory not created)
			if (mos.persistenceFactory != null) {

				// Attempt to ensure only one factory created
				synchronized (mos) {
					if (mos.entityManagerFactory == null) {

						// Create the entity manager factory
						mos.entityManagerFactory = mos.persistenceFactory
								.createEntityManagerFactory(mos.persistenceUnitName, mos.dataSource, mos.properties);
					}

					// Factory created (so indicate no need to create anymore)
					mos.persistenceFactory = null;
				}
			}

			// Create the entity manager
			EntityManagerFactory factory = JpaManagedObjectSource.this.entityManagerFactory;
			this.entityManager = factory.createEntityManager();

			// Run entity manager within managed transaction
			this.entityManager.getTransaction().begin();

			// Provide proxy entity manager (to specify connection)
			this.proxy = (EntityManager) Proxy.newProxyInstance(mos.classLoader, ENTITY_MANAGER_TYPE,
					(proxy, method, args) -> {

						// Disallow access to transaction (as managed)
						switch (method.getName()) {
						case "getTransaction":
							throw new IllegalStateException(EntityManager.class.getSimpleName()
									+ ".getTransaction() may not be invoked as transaction managed by "
									+ OfficeFloor.class.getSimpleName());

						case "close":
							throw new IllegalStateException(EntityManager.class.getSimpleName()
									+ ".close() may not be invoked as managed by " + OfficeFloor.class.getName());
						}

						// Specify the connection
						// All entity manager operations synchronous so will use
						mos.dataSourceConnection.set(connection);

						// Obtain the delegate method on the entity manager
						Method delegateMethod = this.entityManager.getClass().getMethod(method.getName(),
								method.getParameterTypes());

						// Invoke (and retry forcing access)
						try {
							return delegateMethod.invoke(this.entityManager, args);
						} catch (IllegalAccessException ex) {
							delegateMethod.setAccessible(true);
							return delegateMethod.invoke(this.entityManager, args);
						}
					});
		}

		@Override
		public Object getObject() throws Throwable {
			return this.proxy;
		}
	}

	/**
	 * Recycles the {@link EntityManager}.
	 */
	private static class RecycleFunction extends StaticManagedFunction<Indexed, None> {

		@Override
		public Object execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

			// Obtain the entity manager
			RecycleManagedObjectParameter<JpaManagedObject> recycle = RecycleManagedObjectParameter
					.getRecycleManagedObjectParameter(context);
			EntityManager entityManager = recycle.getManagedObject().entityManager;
			try {

				// If no escalations, then commit changes
				CleanupEscalation[] escalations = recycle.getCleanupEscalations();
				if ((escalations != null) && (escalations.length > 0)) {
					// Escalations, so rollback transaction
					entityManager.getTransaction().rollback();
				} else {
					// No escalations, so commit the transaction
					entityManager.getTransaction().commit();
				}

			} finally {
				// Ensure close the entity manager
				entityManager.close();
			}

			// Reuse the connection
			recycle.reuseManagedObject();

			// Nothing further
			return null;
		}
	}

}