/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.eclipse.skin.woof;

import org.eclipse.draw2d.IFigure;

import net.officefloor.model.woof.WoofTemplateModel;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

/**
 * Context for the {@link WoofTemplateModel} {@link IFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TemplateFigureContext {

	/**
	 * Obtains the name of the {@link HttpTemplate}.
	 * 
	 * @return Name of the {@link HttpTemplate}.
	 */
	String getTemplateName();

	/**
	 * Obtains the URI.
	 * 
	 * @return URI. May be <code>null</code>.
	 */
	String getUri();

}