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
package net.officefloor.web.security.impl;

import java.io.Serializable;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpAccessControlFactory;

/**
 * {@link ManagedObjectSource} for the {@link HttpAccessControl}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpAccessControlManagedObjectSource<AC extends Serializable>
		extends AbstractManagedObjectSource<HttpAccessControlManagedObjectSource.Dependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		ACCESS_CONTROL
	}

	/**
	 * Custom access control type.
	 */
	private final Class<AC> accessControlType;

	/**
	 * {@link HttpAccessControlFactory}.
	 */
	private final HttpAccessControlFactory<AC> httpAccessControlFactory;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecurityType
	 *            {@link HttpSecurityType}.
	 */
	public HttpAccessControlManagedObjectSource(HttpSecurityType<?, AC, ?, ?, ?> httpSecurityType) {
		this.accessControlType = httpSecurityType.getAccessControlType();
		this.httpAccessControlFactory = httpSecurityType.getHttpAccessControlFactory();
	}

	/*
	 * ====================== ManagedObjectSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context) throws Exception {
		context.setObjectClass(HttpAccessControl.class);
		context.setManagedObjectClass(HttpAccessControlManagedObject.class);
		context.addDependency(Dependencies.ACCESS_CONTROL, this.accessControlType);
		context.addManagedObjectExtensionInterface(HttpAccessControl.class,
				(managedObject) -> (HttpAccessControl) managedObject.getObject());
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpAccessControlManagedObject();
	}

	/**
	 * {@link HttpAccessControl} {@link ManagedObject}.
	 */
	private class HttpAccessControlManagedObject implements CoordinatingManagedObject<Dependencies> {

		/**
		 * {@link HttpAccessControl}.
		 */
		private HttpAccessControl httpAccessControl;

		/*
		 * ====================== ManagedObject =====================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {

			// Obtain the custom access control
			@SuppressWarnings("unchecked")
			AC accessControl = (AC) registry.getObject(Dependencies.ACCESS_CONTROL);

			// Adapt the access control
			this.httpAccessControl = HttpAccessControlManagedObjectSource.this.httpAccessControlFactory
					.createHttpAccessControl(accessControl);
		}

		@Override
		public Object getObject() {
			return this.httpAccessControl;
		}
	}

}