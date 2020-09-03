/*-
 * #%L
 * Hibernate JPA Persistence
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

package net.officefloor.jpa.hibernate;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;

import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.test.AbstractJpaTestCase;
import net.officefloor.jpa.test.IMockEntity;

/**
 * Hibernate {@link AbstractJpaTestCase}.
 * 
 * @author Daniel Sagenschneider
 */
public class HibernateJpaTest extends AbstractJpaTestCase {

	@BeforeEach
	public void initiateLogger() throws Exception {

		// Ignore hibernate logging
		Logger.getLogger("org.hibernate").setLevel(Level.WARNING);
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
	protected Class<? extends IMockEntity> getMockEntityClass() {
		return MockEntity.class;
	}

}
