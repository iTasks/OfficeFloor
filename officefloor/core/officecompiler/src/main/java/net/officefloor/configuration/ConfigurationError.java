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
package net.officefloor.configuration;

/**
 * <p>
 * Indicates a failure in obtaining configuration.
 * <p>
 * This is a critical error as the source is requiring the
 * {@link ConfigurationItem} to initialise and subsequently start.
 * 
 * @author Daniel Sagenschneider
 */
public class ConfigurationError extends Error {

	/**
	 * Location of the configuration.
	 */
	private final String configurationLocation;

	/**
	 * Name of tag in configuration that is not configured. May be
	 * <code>null</code> to indicate {@link Throwable} cause to configuration
	 * issue.
	 */
	private final String nonconfiguredTagName;

	/**
	 * Initiate.
	 * 
	 * @param configurationLocation
	 *            Location of the {@link ConfigurationItem}.
	 * @param cause
	 *            {@link Throwable} cause.
	 */
	public ConfigurationError(String configurationLocation, Throwable cause) {
		super(cause);
		this.configurationLocation = configurationLocation;
		this.nonconfiguredTagName = null;
	}

	/**
	 * Initiate.
	 * 
	 * @param configurationLocation
	 *            Location of the {@link ConfigurationItem}.
	 * @param nonconfiguredTagName
	 *            Name of tag in configuration that is not configured.
	 */
	public ConfigurationError(String configurationLocation, String nonconfiguredTagName) {
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