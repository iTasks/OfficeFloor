/*-
 * #%L
 * JPA Persistence on top of PostgreSql
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

package net.officefloor.jpa.postgresql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.frame.test.OfficeFrameTestCase.UsesDockerTest;
import net.officefloor.jdbc.ConnectionManagedObjectSource;
import net.officefloor.jdbc.postgresql.PostgreSqlDataSourceManagedObjectSource;
import net.officefloor.jdbc.postgresql.test.PostgreSqlRule;
import net.officefloor.jdbc.postgresql.test.PostgreSqlRule.Configuration;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.hibernate.HibernateJpaManagedObjectSource;
import net.officefloor.jpa.test.AbstractJpaTestCase;
import net.officefloor.jpa.test.IMockEntity;

/**
 * Hibernate {@link AbstractJpaTestCase}.
 * 
 * @author Daniel Sagenschneider
 */
@UsesDockerTest
public class HibernatePostgreSqlJpaTest extends AbstractJpaTestCase {

	/**
	 * PostgreSql database.
	 */
	private static PostgreSqlRule database = new PostgreSqlRule(
			new Configuration().port(5433).username("testuser").password("testpassword"));

	/**
	 * Manage PostgreSql before/after class (rather than each test) to improve
	 * performance.
	 */
	public static Test suite() {
		return new TestSetup(new TestSuite(HibernatePostgreSqlJpaTest.class)) {

			protected void setUp() throws Exception {
				if (isSkipTestsUsingDocker()) {
					return;
				}
				database.startPostgreSql();

				// Ignore hibernate logging
				Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
			}

			protected void tearDown() throws Exception {
				if (isSkipTestsUsingDocker()) {
					return;
				}
				database.stopPostgreSql();
			}
		};
	}

	@Override
	protected void cleanDatabase(Connection connection) throws SQLException {

		// Ensure not in transaction
		if (!connection.getAutoCommit()) {
			connection.setAutoCommit(true);
		}

		// Create the database
		try (Statement statement = connection.createStatement()) {
			statement.execute("DROP TABLE IF EXISTS MOCKENTITY");
		}
	}

	@Override
	protected Class<? extends JpaManagedObjectSource> getJpaManagedObjectSourceClass() {
		return HibernateJpaManagedObjectSource.class;
	}

	@Override
	protected void loadJpaProperties(PropertyConfigurable jpa) {
		jpa.addProperty(JpaManagedObjectSource.PROPERTY_PERSISTENCE_UNIT, "test");
	}

	@Override
	protected void loadDatabaseProperties(PropertyConfigurable mos) {
		mos.addProperty(ConnectionManagedObjectSource.PROPERTY_DATA_SOURCE_FACTORY,
				PostgreSqlDataSourceManagedObjectSource.class.getName());
		mos.addProperty(PostgreSqlDataSourceManagedObjectSource.PROPERTY_SERVER_NAME, "localhost");
		mos.addProperty(PostgreSqlDataSourceManagedObjectSource.PROPERTY_PORT, "5433");
		mos.addProperty(PostgreSqlDataSourceManagedObjectSource.PROPERTY_USER, "testuser");
		mos.addProperty(PostgreSqlDataSourceManagedObjectSource.PROPERTY_PASSWORD, "testpassword");
	}

	@Override
	protected Class<? extends IMockEntity> getMockEntityClass() {
		return MockEntity.class;
	}

}
