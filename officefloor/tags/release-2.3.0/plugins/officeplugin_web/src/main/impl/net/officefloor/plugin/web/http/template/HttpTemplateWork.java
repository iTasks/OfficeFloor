/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.template;

import net.officefloor.frame.api.build.NameAwareWorkFactory;
import net.officefloor.frame.api.execute.Work;

/**
 * HTTP template {@link Work}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateWork implements Work,
		NameAwareWorkFactory<HttpTemplateWork> {

	/**
	 * Bound {@link Work} name.
	 */
	private String workName;

	/**
	 * Obtains the bound {@link Work} name.
	 * 
	 * @return {@link Work} name.
	 */
	public String getWorkName() {
		return this.workName;
	}

	/*
	 * ================ NameAwareWorkFactory ======================
	 */

	@Override
	public void setBoundWorkName(String boundWorkName) {
		this.workName = boundWorkName;
	}

	@Override
	public HttpTemplateWork createWork() {
		return this;
	}

}