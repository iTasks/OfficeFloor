/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.woof.teams;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionContext;
import net.officefloor.woof.model.teams.PropertyFileModel;
import net.officefloor.woof.model.teams.PropertyModel;
import net.officefloor.woof.model.teams.PropertySourceModel;
import net.officefloor.woof.model.teams.TypeQualificationModel;
import net.officefloor.woof.model.teams.WoofTeamModel;
import net.officefloor.woof.model.teams.WoofTeamsModel;
import net.officefloor.woof.model.teams.WoofTeamsRepository;
import net.officefloor.woof.teams.WoofTeamsLoader;
import net.officefloor.woof.teams.WoofTeamsLoaderContext;

/**
 * {@link WoofTeamsLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofTeamsLoaderImpl implements WoofTeamsLoader {

	/**
	 * {@link WoofTeamsRepository}.
	 */
	private final WoofTeamsRepository repository;

	/**
	 * Initiate.
	 * 
	 * @param repository
	 *            {@link WoofTeamsRepository}.
	 */
	public WoofTeamsLoaderImpl(WoofTeamsRepository repository) {
		this.repository = repository;
	}

	/*
	 * ======================= WoofTeamsLoader ===========================
	 */

	@Override
	public void loadWoofTeamsConfiguration(WoofTeamsLoaderContext context) throws Exception {

		// Load the teams model
		WoofTeamsModel teams = new WoofTeamsModel();
		this.repository.retrieveWoofTeams(teams, context.getConfiguration());

		// Obtain the deployer and extension context
		OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
		OfficeFloorExtensionContext extensionContext = context.getOfficeFloorExtensionContext();

		// Configure the teams
		for (WoofTeamModel teamModel : teams.getWoofTeams()) {

			// Obtain the team details
			String teamSourceClassName = teamModel.getTeamSourceClassName();

			// Obtain the type qualification
			List<AutoWire> typeQualifications = new LinkedList<AutoWire>();
			String qualifier = teamModel.getQualifier();
			String type = teamModel.getType();
			if (!(CompileUtil.isBlank(type))) {
				// Short-cut type qualification provided
				typeQualifications.add(new AutoWire(qualifier, type));
			}
			for (TypeQualificationModel autoWire : teamModel.getTypeQualifications()) {
				typeQualifications.add(new AutoWire(autoWire.getQualifier(), autoWire.getType()));
			}

			// Obtain the team name
			String teamName = (typeQualifications.size() > 0 ? typeQualifications.get(0).toString()
					: teamSourceClassName);

			// Add the team
			OfficeFloorTeam team = deployer.addTeam(teamName, teamSourceClassName);

			// Load the type qualification
			for (AutoWire autoWire : typeQualifications) {
				team.addTypeQualification(autoWire.getQualifier(), autoWire.getType());
			}

			// Load the properties
			for (PropertySourceModel propertySource : teamModel.getPropertySources()) {

				// Load based on property source type
				if (propertySource instanceof PropertyModel) {
					// Load the property
					PropertyModel property = (PropertyModel) propertySource;
					team.addProperty(property.getName(), property.getValue());

				} else if (propertySource instanceof PropertyFileModel) {
					// Load properties from file
					PropertyFileModel propertyFile = (PropertyFileModel) propertySource;
					InputStream propertyConfiguration = extensionContext.getResource(propertyFile.getPath());
					Properties properties = new Properties();
					properties.load(propertyConfiguration);
					for (String propertyName : properties.stringPropertyNames()) {
						String propertyValue = properties.getProperty(propertyName);
						team.addProperty(propertyName, propertyValue);
					}

				} else {
					// Unknown property source
					throw new IllegalStateException(
							"Unknown property source type " + propertySource.getClass().getName());
				}
			}
		}
	}

}