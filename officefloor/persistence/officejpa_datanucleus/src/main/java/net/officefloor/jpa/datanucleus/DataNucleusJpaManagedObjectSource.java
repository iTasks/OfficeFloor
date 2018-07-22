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
package net.officefloor.jpa.datanucleus;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.sql.DataSource;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.jpa.JpaManagedObjectSource;
import net.officefloor.jpa.JpaManagedObjectSource.PersistenceFactory;

/**
 * DataNucleus {@link JpaManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class DataNucleusJpaManagedObjectSource extends JpaManagedObjectSource implements PersistenceFactory {

	/*
	 * =============== JpaManagedObjectSource =================
	 */

	@Override
	protected PersistenceFactory getPersistenceFactory(MetaDataContext<Indexed, None> context) throws Exception {
		return this;
	}

	@Override
	protected boolean isRunWithinTransaction() {
		return false;
	}

	/*
	 * ================= PersistenceFactory ===================
	 */

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public EntityManagerFactory createEntityManagerFactory(String persistenceUnitName, DataSource dataSource,
			Properties properties) throws Exception {
		Map configuration = new HashMap<>(properties);
		if (dataSource != null) {
			configuration.put("datanucleus.ConnectionFactory", dataSource);
		}
		return Persistence.createEntityManagerFactory(persistenceUnitName, configuration);
	}

}