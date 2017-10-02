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
package net.officefloor.plugin.web.http.security;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.session.HttpSession;

/**
 * {@link ManagedFunction} authentication context.
 * 
 * @author Daniel Sagenschneider
 */
interface FunctionAuthenticateContext<S, C> {

	/**
	 * Obtains the credentials.
	 * 
	 * @return Credentials. May be <code>null</code> if no credentials are
	 *         available.
	 */
	C getCredentials();

	/**
	 * Obtains the {@link ServerHttpConnection} to be secured.
	 * 
	 * @return {@link ServerHttpConnection}.
	 */
	ServerHttpConnection getConnection();

	/**
	 * Obtains the {@link HttpSession}.
	 * 
	 * @return {@link HttpSession}.
	 */
	HttpSession getSession();

	/**
	 * Specifies the HTTP security.
	 * 
	 * @param security
	 *            HTTP security.
	 */
	void setHttpSecurity(S security);

	/**
	 * Specifies a failure occurred in authentication.
	 * 
	 * @param failure
	 *            Failure in authentication.
	 */
	void setFailure(Throwable failure);

}