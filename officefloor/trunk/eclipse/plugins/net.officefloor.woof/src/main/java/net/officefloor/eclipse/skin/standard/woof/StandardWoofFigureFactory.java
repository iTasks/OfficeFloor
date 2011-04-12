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
package net.officefloor.eclipse.skin.standard.woof;

import net.officefloor.eclipse.skin.woof.TemplateFigure;
import net.officefloor.eclipse.skin.woof.TemplateFigureContext;
import net.officefloor.eclipse.skin.woof.TemplateOutputFigure;
import net.officefloor.eclipse.skin.woof.TemplateOutputFigureContext;
import net.officefloor.eclipse.skin.woof.WoofFigureFactory;

/**
 * Standard {@link WoofFigureFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardWoofFigureFactory implements WoofFigureFactory {

	/*
	 * ======================== WoofFigureFactory =========================
	 */

	@Override
	public TemplateFigure createTemplateFigure(TemplateFigureContext context) {
		return new StandardTemplateFigure(context);
	}

	@Override
	public TemplateOutputFigure createTemplateOutputFigure(
			TemplateOutputFigureContext context) {
		return new StandardTemplateOutputFigure(context);
	}

}