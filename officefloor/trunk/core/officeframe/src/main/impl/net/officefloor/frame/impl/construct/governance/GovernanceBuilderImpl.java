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
package net.officefloor.frame.impl.construct.governance;

import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.impl.construct.source.SourcePropertiesImpl;
import net.officefloor.frame.internal.configuration.GovernanceConfiguration;
import net.officefloor.frame.spi.governance.Governance;
import net.officefloor.frame.spi.governance.source.GovernanceSource;
import net.officefloor.frame.spi.source.SourceProperties;
import net.officefloor.frame.spi.team.Team;

/**
 * {@link GovernanceBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceBuilderImpl<I, F extends Enum<F>, GS extends GovernanceSource<I, F>>
		implements GovernanceBuilder, GovernanceConfiguration<I, F, GS> {

	/**
	 * Name of the {@link Governance}.
	 */
	private final String governanceName;

	/**
	 * {@link GovernanceSource} instance.
	 */
	private final GS governanceSourceInstance;

	/**
	 * {@link GovernanceSource} {@link Class}.
	 */
	private final Class<GS> governanceSourceClass;

	/**
	 * {@link SourceProperties} for the {@link GovernanceSource}.
	 */
	private final SourcePropertiesImpl properties = new SourcePropertiesImpl();

	/**
	 * {@link Team} name responsible to undertake the {@link Governance}
	 * {@link Task} instances.
	 */
	private String teamName;

	/**
	 * Initiate.
	 * 
	 * @param governanceName
	 *            Name of the {@link Governance}.
	 * @param governanceSourceInstance
	 *            {@link GovernanceSource} instance.
	 */
	public GovernanceBuilderImpl(String governanceName,
			GS governanceSourceInstance, Class<GS> governanceSourceClass) {
		this.governanceName = governanceName;
		this.governanceSourceInstance = governanceSourceInstance;
		this.governanceSourceClass = governanceSourceClass;
	}

	/*
	 * ================= GovernanceBuilder =======================
	 */

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name, value);
	}

	@Override
	public void setTeamName(String teamName) {
		this.teamName = teamName;
	}

	/*
	 * =============== GovernanceConfiguration ====================
	 */

	@Override
	public String getGovernanceName() {
		return this.governanceName;
	}

	@Override
	public GS getGovernanceSource() {
		return this.governanceSourceInstance;
	}

	@Override
	public Class<GS> getGovernanceSourceClass() {
		return this.governanceSourceClass;
	}

	@Override
	public SourceProperties getProperties() {
		return this.properties;
	}

	@Override
	public String getTeamName() {
		return this.teamName;
	}

}