/*-
 * #%L
 * Web Security
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

package net.officefloor.web.security.type;

import java.io.Serializable;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.web.spi.security.HttpAccessControlFactory;
import net.officefloor.web.spi.security.HttpAuthenticationFactory;

/**
 * {@link HttpSecurityType} adapted from the {@link ManagedObjectType}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityTypeImpl<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>>
		implements HttpSecurityType<A, AC, C, O, F> {

	/**
	 * Authentication type.
	 */
	private final Class<A> authenticationType;

	/**
	 * {@link HttpAuthenticationFactory}.
	 */
	private final HttpAuthenticationFactory<A, C> httpAuthenticationFactory;

	/**
	 * Access control type.
	 */
	private final Class<AC> accessControlType;

	/**
	 * {@link HttpAccessControlFactory}.
	 */
	private final HttpAccessControlFactory<AC> httpAccessControlFactory;

	/**
	 * Credentials type.
	 */
	private final Class<C> credentialsType;

	/**
	 * {@link ManagedObjectType}.
	 */
	private final ManagedObjectType<O> moAccessControlType;

	/**
	 * {@link HttpSecuritySupportingManagedObjectType} instances.
	 */
	private final HttpSecuritySupportingManagedObjectType<?>[] supportingManagedObjectTypes;

	/**
	 * Initiate.
	 * 
	 * @param authenticationType           Authentication type.
	 * @param httpAuthenticationFactory    {@link HttpAccessControlFactory}.
	 * @param moAccessControlType          {@link ManagedObjectType}.
	 * @param httpAccessControlFactory     {@link HttpAccessControlFactory}.
	 * @param credentialsType              Credentials type.
	 * @param supportingManagedObjectTypes {@link HttpSecuritySupportingManagedObjectType}
	 *                                     instances.
	 */
	public HttpSecurityTypeImpl(Class<A> authenticationType, HttpAuthenticationFactory<A, C> httpAuthenticationFactory,
			Class<AC> accessControlType, HttpAccessControlFactory<AC> httpAccessControlFactory,
			Class<C> credentialsType, ManagedObjectType<O> moAccessControlType,
			HttpSecuritySupportingManagedObjectType<?>[] supportingManagedObjectTypes) {
		this.authenticationType = authenticationType;
		this.httpAuthenticationFactory = httpAuthenticationFactory;
		this.accessControlType = accessControlType;
		this.httpAccessControlFactory = httpAccessControlFactory;
		this.credentialsType = credentialsType;
		this.moAccessControlType = moAccessControlType;
		this.supportingManagedObjectTypes = supportingManagedObjectTypes;
	}

	/*
	 * ================= HttpSecurityType =====================
	 */

	@Override
	public Class<A> getAuthenticationType() {
		return this.authenticationType;
	}

	@Override
	public HttpAuthenticationFactory<A, C> getHttpAuthenticationFactory() {
		return this.httpAuthenticationFactory;
	}

	@Override
	public Class<AC> getAccessControlType() {
		return this.accessControlType;
	}

	@Override
	public HttpAccessControlFactory<AC> getHttpAccessControlFactory() {
		return this.httpAccessControlFactory;
	}

	@Override
	public Class<C> getCredentialsType() {
		return this.credentialsType;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HttpSecurityDependencyType<O>[] getDependencyTypes() {
		return AdaptFactory.adaptArray(this.moAccessControlType.getDependencyTypes(), HttpSecurityDependencyType.class,
				new AdaptFactory<HttpSecurityDependencyType, ManagedObjectDependencyType<O>>() {
					@Override
					public HttpSecurityDependencyType<O> createAdaptedObject(ManagedObjectDependencyType<O> delegate) {
						return new HttpSecurityDependencyTypeImpl<O>(delegate);
					}
				});
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public HttpSecurityFlowType<F>[] getFlowTypes() {
		return AdaptFactory.adaptArray(this.moAccessControlType.getFlowTypes(), HttpSecurityFlowType.class,
				new AdaptFactory<HttpSecurityFlowType, ManagedObjectFlowType>() {
					@Override
					public HttpSecurityFlowType createAdaptedObject(ManagedObjectFlowType delegate) {
						return new HttpSecurityFlowTypeImpl<F>(delegate);
					}
				});
	}

	@Override
	public HttpSecuritySupportingManagedObjectType<?>[] getSupportingManagedObjectTypes() {
		return this.supportingManagedObjectTypes;
	}
}
