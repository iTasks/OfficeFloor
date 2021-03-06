/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.model.woof;

import net.officefloor.configuration.ConfigurationContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;

/**
 * {@link WoofTemplateChangeContext} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTemplateChangeContextImpl extends SourceContextImpl implements WoofTemplateChangeContext {

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext;

	/**
	 * {@link WoofChangeIssues}.
	 */
	private final WoofChangeIssues issues;

	/**
	 * Initiate.
	 * 
	 * @param isLoadingType        Indicates if loading type.
	 * @param sourceContext        {@link SourceContext}.
	 * @param configurationContext {@link ConfigurationContext}.
	 * @param issues               {@link WoofChangeIssues}.
	 */
	public WoofTemplateChangeContextImpl(boolean isLoadingType, SourceContext sourceContext,
			ConfigurationContext configurationContext, WoofChangeIssues issues) {
		super(sourceContext.getName(), isLoadingType, null, sourceContext, new SourcePropertiesImpl());
		this.configurationContext = configurationContext;
		this.issues = issues;
	}

	/*
	 * =============== WoofTemplateChangeContext =======================
	 */

	@Override
	public ConfigurationContext getConfigurationContext() {
		return this.configurationContext;
	}

	@Override
	public WoofChangeIssues getWoofChangeIssues() {
		return this.issues;
	}

}
