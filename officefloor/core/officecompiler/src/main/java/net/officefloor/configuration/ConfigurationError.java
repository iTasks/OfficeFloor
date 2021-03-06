/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.configuration;

import net.officefloor.frame.api.source.AbstractSourceError;

/**
 * <p>
 * Indicates a failure in obtaining configuration.
 * <p>
 * This is a critical error as the source is requiring the
 * {@link ConfigurationItem} to initialise and subsequently start.
 * 
 * @author Daniel Sagenschneider
 */
public class ConfigurationError extends AbstractSourceError {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Location of the configuration.
	 */
	private final String configurationLocation;

	/**
	 * Name of tag in configuration that is not configured. May be <code>null</code>
	 * to indicate {@link Throwable} cause to configuration issue.
	 */
	private final String nonconfiguredTagName;

	/**
	 * Instantiate.
	 * 
	 * @param missingLocation Location of missing {@link ConfigurationItem}.
	 */
	public ConfigurationError(String missingLocation) {
		super("Can not obtain " + ConfigurationItem.class.getSimpleName() + " at location '" + missingLocation + "'");
		this.configurationLocation = missingLocation;
		this.nonconfiguredTagName = null;
	}

	/**
	 * Initiate.
	 * 
	 * @param configurationLocation Location of the {@link ConfigurationItem}.
	 * @param cause                 {@link Throwable} cause.
	 */
	public ConfigurationError(String configurationLocation, Throwable cause) {
		super("Failed to obtain " + ConfigurationItem.class.getSimpleName() + " at location '" + configurationLocation
				+ "': " + cause.getMessage(), cause);
		this.configurationLocation = configurationLocation;
		this.nonconfiguredTagName = null;
	}

	/**
	 * Initiate.
	 * 
	 * @param configurationLocation Location of the {@link ConfigurationItem}.
	 * @param nonconfiguredTagName  Name of tag in configuration that is not
	 *                              configured.
	 */
	public ConfigurationError(String configurationLocation, String nonconfiguredTagName) {
		super("Can not obtain " + ConfigurationItem.class.getSimpleName() + " at location '" + configurationLocation
				+ "' as missing property '" + nonconfiguredTagName + "'");
		this.configurationLocation = configurationLocation;
		this.nonconfiguredTagName = nonconfiguredTagName;
	}

	/**
	 * Obtains the location of the {@link ConfigurationItem}.
	 * 
	 * @return Location of the {@link ConfigurationItem}.
	 */
	public String getConfigurationLocation() {
		return this.configurationLocation;
	}

	/**
	 * Obtains the non-configured tag name.
	 * 
	 * @return Non-configured tag name.
	 */
	public String getNonconfiguredTagName() {
		return this.nonconfiguredTagName;
	}

}
