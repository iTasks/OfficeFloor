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
package net.officefloor.model.impl.office;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.OfficeSourceService;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeFunctionType;
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.spi.office.AdministerableManagedObject;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionFunction;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeStart;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.model.impl.repository.ModelRepositoryImpl;
import net.officefloor.model.impl.repository.inputstream.InputStreamConfigurationItem;
import net.officefloor.model.office.AdministrationModel;
import net.officefloor.model.office.AdministrationToOfficeTeamModel;
import net.officefloor.model.office.ExternalManagedObjectModel;
import net.officefloor.model.office.ExternalManagedObjectToAdministrationModel;
import net.officefloor.model.office.ExternalManagedObjectToGovernanceModel;
import net.officefloor.model.office.GovernanceAreaModel;
import net.officefloor.model.office.GovernanceModel;
import net.officefloor.model.office.GovernanceToOfficeTeamModel;
import net.officefloor.model.office.OfficeChanges;
import net.officefloor.model.office.OfficeEscalationModel;
import net.officefloor.model.office.OfficeEscalationToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeFunctionModel;
import net.officefloor.model.office.OfficeFunctionToGovernanceModel;
import net.officefloor.model.office.OfficeFunctionToPostAdministrationModel;
import net.officefloor.model.office.OfficeFunctionToPreAdministrationModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeInputManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectDependencyToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowModel;
import net.officefloor.model.office.OfficeManagedObjectSourceFlowToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamModel;
import net.officefloor.model.office.OfficeManagedObjectSourceTeamToOfficeTeamModel;
import net.officefloor.model.office.OfficeManagedObjectToAdministrationModel;
import net.officefloor.model.office.OfficeManagedObjectToGovernanceModel;
import net.officefloor.model.office.OfficeManagedObjectToOfficeManagedObjectSourceModel;
import net.officefloor.model.office.OfficeModel;
import net.officefloor.model.office.OfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionManagedObjectModel;
import net.officefloor.model.office.OfficeSectionManagedObjectToGovernanceModel;
import net.officefloor.model.office.OfficeSectionModel;
import net.officefloor.model.office.OfficeSectionObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToExternalManagedObjectModel;
import net.officefloor.model.office.OfficeSectionObjectToOfficeManagedObjectModel;
import net.officefloor.model.office.OfficeSectionOutputModel;
import net.officefloor.model.office.OfficeSectionOutputToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSectionResponsibilityModel;
import net.officefloor.model.office.OfficeSectionResponsibilityToOfficeTeamModel;
import net.officefloor.model.office.OfficeStartModel;
import net.officefloor.model.office.OfficeStartToOfficeSectionInputModel;
import net.officefloor.model.office.OfficeSubSectionModel;
import net.officefloor.model.office.OfficeSubSectionToGovernanceModel;
import net.officefloor.model.office.OfficeTeamModel;
import net.officefloor.model.office.PropertyModel;

/**
 * {@link OfficeModel} {@link OfficeSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeModelOfficeSource extends AbstractOfficeSource
		implements OfficeSourceService<OfficeModelOfficeSource> {

	/*
	 * ====================== OfficeSourceService ==============================
	 */

	@Override
	public String getOfficeSourceAlias() {
		return "OFFICE";
	}

	@Override
	public Class<OfficeModelOfficeSource> getOfficeSourceClass() {
		return OfficeModelOfficeSource.class;
	}

	/*
	 * ================= AbstractOfficeSource ================================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceOffice(OfficeArchitect architect, OfficeSourceContext context) throws Exception {

		// Obtain the configuration to the section
		InputStream configuration = context.getResource(context.getOfficeLocation());
		if (configuration == null) {
			// Must have configuration
			throw new FileNotFoundException("Can not find office '" + context.getOfficeLocation() + "'");
		}

		// Retrieve the office model
		OfficeModel office = new OfficeRepositoryImpl(new ModelRepositoryImpl())
				.retrieveOffice(new InputStreamConfigurationItem(configuration));

		// Create aggregate processor to add sub section processing
		AggregateSubSectionProcessor processors = new AggregateSubSectionProcessor();

		// Add the teams, keeping registry of the teams
		Map<String, OfficeTeam> teams = new HashMap<String, OfficeTeam>();
		for (OfficeTeamModel teamModel : office.getOfficeTeams()) {
			String teamName = teamModel.getOfficeTeamName();
			OfficeTeam team = architect.addOfficeTeam(teamName);
			teams.put(teamName, team);
		}

		// Obtain the listing of governances
		Map<String, OfficeGovernance> governances = new HashMap<String, OfficeGovernance>();
		for (GovernanceModel govModel : office.getGovernances()) {

			// Add the governance
			String governanceName = govModel.getGovernanceName();
			OfficeGovernance governance = architect.addOfficeGovernance(governanceName,
					govModel.getGovernanceSourceClassName());
			for (PropertyModel property : govModel.getProperties()) {
				governance.addProperty(property.getName(), property.getValue());
			}

			// Provide team responsible for governance
			GovernanceToOfficeTeamModel govToTeam = govModel.getOfficeTeam();
			if (govToTeam != null) {
				OfficeTeamModel teamModel = govToTeam.getOfficeTeam();
				if (teamModel != null) {
					OfficeTeam team = teams.get(teamModel.getOfficeTeamName());
					if (team != null) {
						architect.link(governance, team);
					}
				}
			}

			// Register the governance
			governances.put(governanceName, governance);
		}

		// Add governance processing for sub sections
		processors.addSubSectionProcessor(new GovernanceSubSectionProcessor(governances));

		// Add the external managed objects, keeping registry of them
		Map<String, OfficeObject> officeObjects = new HashMap<String, OfficeObject>();
		for (ExternalManagedObjectModel object : office.getExternalManagedObjects()) {

			// Create the office object
			String officeObjectName = object.getExternalManagedObjectName();
			OfficeObject officeObject = architect.addOfficeObject(officeObjectName, object.getObjectType());

			// Provide governance over managed object
			for (ExternalManagedObjectToGovernanceModel moToGov : object.getGovernances()) {
				GovernanceModel govModel = moToGov.getGovernance();
				if (govModel != null) {
					OfficeGovernance governance = governances.get(govModel.getGovernanceName());
					if (governance != null) {
						governance.governManagedObject(officeObject);
					}
				}
			}

			// Register the office object
			officeObjects.put(officeObjectName, officeObject);
		}

		// Add the managed object sources, keeping registry of them
		Map<String, OfficeManagedObjectSource> managedObjectSources = new HashMap<String, OfficeManagedObjectSource>();
		for (OfficeManagedObjectSourceModel mosModel : office.getOfficeManagedObjectSources()) {

			// Add the managed object source
			String mosName = mosModel.getOfficeManagedObjectSourceName();
			OfficeManagedObjectSource mos = architect.addOfficeManagedObjectSource(mosName,
					mosModel.getManagedObjectSourceClassName());
			for (PropertyModel property : mosModel.getProperties()) {
				mos.addProperty(property.getName(), property.getValue());
			}

			// Provide timeout
			String timeoutValue = mosModel.getTimeout();
			if (!CompileUtil.isBlank(timeoutValue)) {
				try {
					mos.setTimeout(Long.valueOf(timeoutValue));
				} catch (NumberFormatException ex) {
					architect.addIssue(
							"Invalid timeout value: " + timeoutValue + " for managed object source " + mosName);
				}
			}

			// Register the managed object source
			managedObjectSources.put(mosName, mos);
		}

		// Add the managed objects, keeping registry of them
		Map<String, OfficeManagedObject> managedObjects = new HashMap<String, OfficeManagedObject>();
		for (OfficeManagedObjectModel moModel : office.getOfficeManagedObjects()) {

			// Obtain the managed object details
			String managedObjectName = moModel.getOfficeManagedObjectName();
			ManagedObjectScope managedObjectScope = this.getManagedObjectScope(moModel.getManagedObjectScope(),
					architect, managedObjectName);

			// Obtain the managed object source for the managed object
			OfficeManagedObjectSource moSource = null;
			OfficeManagedObjectToOfficeManagedObjectSourceModel moToSource = moModel.getOfficeManagedObjectSource();
			if (moToSource != null) {
				OfficeManagedObjectSourceModel moSourceModel = moToSource.getOfficeManagedObjectSource();
				if (moSourceModel != null) {
					moSource = managedObjectSources.get(moSourceModel.getOfficeManagedObjectSourceName());
				}
			}
			if (moSource == null) {
				continue; // must have managed object source
			}

			// Add the managed object and also register it
			OfficeManagedObject managedObject = moSource.addOfficeManagedObject(managedObjectName, managedObjectScope);
			managedObjects.put(managedObjectName, managedObject);

			// Provide governance over managed object
			for (OfficeManagedObjectToGovernanceModel moToGov : moModel.getGovernances()) {
				GovernanceModel govModel = moToGov.getGovernance();
				if (govModel != null) {
					OfficeGovernance governance = governances.get(govModel.getGovernanceName());
					if (governance != null) {
						governance.governManagedObject(managedObject);
					}
				}
			}
		}

		// Link the managed object dependencies
		for (OfficeManagedObjectModel moModel : office.getOfficeManagedObjects()) {

			// Obtain the managed object
			OfficeManagedObject managedObject = managedObjects.get(moModel.getOfficeManagedObjectName());
			if (managedObject == null) {
				continue; // should always have
			}

			// Link the dependencies
			for (OfficeManagedObjectDependencyModel dependencyModel : moModel.getOfficeManagedObjectDependencies()) {

				// Obtain the dependency
				ManagedObjectDependency dependency = managedObject
						.getManagedObjectDependency(dependencyModel.getOfficeManagedObjectDependencyName());

				// Determine if linked to managed object
				OfficeManagedObject linkedManagedObject = null;
				OfficeManagedObjectDependencyToOfficeManagedObjectModel dependencyToMo = dependencyModel
						.getOfficeManagedObject();
				if (dependencyToMo != null) {
					OfficeManagedObjectModel linkedMoModel = dependencyToMo.getOfficeManagedObject();
					if (linkedMoModel != null) {
						linkedManagedObject = managedObjects.get(linkedMoModel.getOfficeManagedObjectName());
					}
				}
				if (linkedManagedObject != null) {
					// Link to managed object
					architect.link(dependency, linkedManagedObject);
				}

				// Determine if linked to external managed object
				OfficeObject linkedObject = null;
				OfficeManagedObjectDependencyToExternalManagedObjectModel dependencyToExtMo = dependencyModel
						.getExternalManagedObject();
				if (dependencyToExtMo != null) {
					ExternalManagedObjectModel linkedExtMoModel = dependencyToExtMo.getExternalManagedObject();
					if (linkedExtMoModel != null) {
						linkedObject = officeObjects.get(linkedExtMoModel.getExternalManagedObjectName());
					}
				}
				if (linkedObject != null) {
					// Link to office object
					architect.link(dependency, linkedObject);
				}
			}
		}

		// Link the input managed object dependencies
		for (OfficeManagedObjectSourceModel mosModel : office.getOfficeManagedObjectSources()) {

			// Obtain the managed object source
			OfficeManagedObjectSource mos = managedObjectSources.get(mosModel.getOfficeManagedObjectSourceName());
			if (mos == null) {
				continue; // should always have
			}

			// Link the input dependencies
			for (OfficeInputManagedObjectDependencyModel dependencyModel : mosModel
					.getOfficeInputManagedObjectDependencies()) {

				// Obtain the input dependency
				ManagedObjectDependency dependency = mos
						.getInputManagedObjectDependency(dependencyModel.getOfficeInputManagedObjectDependencyName());

				// Determine if linked to managed object
				OfficeManagedObject linkedManagedObject = null;
				OfficeInputManagedObjectDependencyToOfficeManagedObjectModel dependencyToMo = dependencyModel
						.getOfficeManagedObject();
				if (dependencyToMo != null) {
					OfficeManagedObjectModel linkedMoModel = dependencyToMo.getOfficeManagedObject();
					if (linkedMoModel != null) {
						linkedManagedObject = managedObjects.get(linkedMoModel.getOfficeManagedObjectName());
					}
				}
				if (linkedManagedObject != null) {
					// Link to managed object
					architect.link(dependency, linkedManagedObject);
				}

				// Determine if linked to external managed object
				OfficeObject linkedObject = null;
				OfficeInputManagedObjectDependencyToExternalManagedObjectModel dependencyToExtMo = dependencyModel
						.getExternalManagedObject();
				if (dependencyToExtMo != null) {
					ExternalManagedObjectModel linkedExtMoModel = dependencyToExtMo.getExternalManagedObject();
					if (linkedExtMoModel != null) {
						linkedObject = officeObjects.get(linkedExtMoModel.getExternalManagedObjectName());
					}
				}
				if (linkedObject != null) {
					// Link to external managed object
					architect.link(dependency, linkedObject);
				}
			}
		}

		// Add the sections, keeping registry of them
		Map<String, OfficeSection> sections = new HashMap<String, OfficeSection>();
		Map<String, OfficeSectionType> sectionTypes = new HashMap<String, OfficeSectionType>();
		for (OfficeSectionModel sectionModel : office.getOfficeSections()) {

			// Create the property list to add the section
			PropertyList propertyList = context.createPropertyList();
			for (PropertyModel property : sectionModel.getProperties()) {
				propertyList.addProperty(property.getName()).setValue(property.getValue());
			}

			// Add the section (register for later)
			String sectionName = sectionModel.getOfficeSectionName();
			OfficeSection section = architect.addOfficeSection(sectionName, sectionModel.getSectionSourceClassName(),
					sectionModel.getSectionLocation());
			propertyList.configureProperties(section);
			sections.put(sectionName, section);

			// Obtain the section type (register for later)
			OfficeSectionType sectionType = context.loadOfficeSectionType(sectionName,
					sectionModel.getSectionSourceClassName(), sectionModel.getSectionLocation(), propertyList);
			sectionTypes.put(sectionName, sectionType);

			// Create the listing of responsibilities
			List<Responsibility> responsibilities = new LinkedList<Responsibility>();
			for (OfficeSectionResponsibilityModel responsibilityModel : sectionModel
					.getOfficeSectionResponsibilities()) {

				// Obtain the office team responsible
				OfficeTeam officeTeam = null;
				OfficeSectionResponsibilityToOfficeTeamModel conn = responsibilityModel.getOfficeTeam();
				if (conn != null) {
					OfficeTeamModel teamModel = conn.getOfficeTeam();
					if (teamModel != null) {
						String teamName = teamModel.getOfficeTeamName();
						officeTeam = teams.get(teamName);
					}
				}
				if (officeTeam == null) {
					continue; // must have team responsible
				}

				// Add the responsibility
				responsibilities.add(new Responsibility(officeTeam));
			}

			// Create the listing of all functions
			List<OfficeSectionFunction> functions = new LinkedList<OfficeSectionFunction>();
			this.loadOfficeFunctions(section, sectionType, functions);

			// Assign teams their responsibilities
			for (Responsibility responsibility : responsibilities) {
				for (OfficeSectionFunction function : new ArrayList<OfficeSectionFunction>(functions)) {
					if (responsibility.isResponsible(function)) {
						// Assign the team responsible for function
						architect.link(function.getResponsibleTeam(), responsibility.officeTeam);

						// Remove task from listing as assigned its team
						functions.remove(function);
					}
				}
			}

			// Obtain the governances of section
			GovernanceModel[] governingGovernances = this.getGovernancesOverLocation(sectionModel.getX(),
					sectionModel.getY(), office.getGovernances());
			for (GovernanceModel govModel : governingGovernances) {
				// Obtain the governance to govern the section
				OfficeGovernance governance = governances.get(govModel.getGovernanceName());
				if (governance != null) {
					// Add the governance to the section
					section.addGovernance(governance);
				}
			}
		}

		// Link start-up triggers to office section inputs
		for (OfficeStartModel startModel : office.getOfficeStarts()) {

			// Obtain the office start
			String startName = startModel.getStartName();
			OfficeStart start = architect.addOfficeStart(startName);

			// Obtain the flow to trigger on start-up
			OfficeSectionInput officeSectionInput = null;
			OfficeStartToOfficeSectionInputModel connToInput = startModel.getOfficeSectionInput();
			if (connToInput != null) {
				OfficeSection section = sections.get(connToInput.getOfficeSectionName());
				if (section != null) {
					officeSectionInput = section.getOfficeSectionInput(connToInput.getOfficeSectionInputName());
				}
			}
			if (officeSectionInput != null) {
				// Link start-up to section input
				architect.link(start, officeSectionInput);
			}
		}

		// Link the sections to other sections and external office
		for (OfficeSectionModel sectionModel : office.getOfficeSections()) {

			// Obtain the section
			String sectionName = sectionModel.getOfficeSectionName();
			OfficeSection section = sections.get(sectionName);

			// Link the objects to office objects
			for (OfficeSectionObjectModel objectModel : sectionModel.getOfficeSectionObjects()) {

				// Obtain the object
				OfficeSectionObject object = section.getOfficeSectionObject(objectModel.getOfficeSectionObjectName());

				// Determine if link object to office object
				OfficeObject officeObject = null;
				OfficeSectionObjectToExternalManagedObjectModel connToExtMo = objectModel.getExternalManagedObject();
				if (connToExtMo != null) {
					ExternalManagedObjectModel extMo = connToExtMo.getExternalManagedObject();
					if (extMo != null) {
						officeObject = officeObjects.get(extMo.getExternalManagedObjectName());
					}
				}
				if (officeObject != null) {
					// Link object to office object
					architect.link(object, officeObject);
				}

				// Determine if link object to office managed object
				OfficeManagedObject officeMo = null;
				OfficeSectionObjectToOfficeManagedObjectModel connToMo = objectModel.getOfficeManagedObject();
				if (connToMo != null) {
					OfficeManagedObjectModel mo = connToMo.getOfficeManagedObject();
					if (mo != null) {
						officeMo = managedObjects.get(mo.getOfficeManagedObjectName());
					}
				}
				if (officeMo != null) {
					// Link object to office managed object
					architect.link(object, officeMo);
				}
			}

			// Link the outputs to the inputs
			for (OfficeSectionOutputModel outputModel : sectionModel.getOfficeSectionOutputs()) {

				// Obtain the output
				OfficeSectionOutput output = section.getOfficeSectionOutput(outputModel.getOfficeSectionOutputName());

				// Obtain the input
				OfficeSectionInput input = null;
				OfficeSectionOutputToOfficeSectionInputModel conn = outputModel.getOfficeSectionInput();
				if (conn != null) {
					OfficeSectionInputModel inputModel = conn.getOfficeSectionInput();
					if (inputModel != null) {
						OfficeSectionModel inputSectionModel = this.getOfficeSectionForInput(office, inputModel);
						if (inputSectionModel != null) {
							OfficeSection inputSection = sections.get(inputSectionModel.getOfficeSectionName());
							if (inputSection != null) {
								input = inputSection.getOfficeSectionInput(inputModel.getOfficeSectionInputName());
							}
						}
					}
				}
				if (input == null) {
					continue; // must have the input
				}

				// Link output to the input
				architect.link(output, input);
			}
		}

		// Link managed object source flows to section inputs
		for (OfficeManagedObjectSourceModel mosModel : office.getOfficeManagedObjectSources()) {

			// Obtain the managed object source
			OfficeManagedObjectSource mos = managedObjectSources.get(mosModel.getOfficeManagedObjectSourceName());
			if (mos == null) {
				continue; // should always have managed object source
			}

			// Link managed object source flow to section input
			for (OfficeManagedObjectSourceFlowModel mosFlowModel : mosModel.getOfficeManagedObjectSourceFlows()) {

				// Obtain the managed object source flow
				ManagedObjectFlow mosFlow = mos
						.getManagedObjectFlow(mosFlowModel.getOfficeManagedObjectSourceFlowName());

				// Link to section input
				OfficeSectionInput sectionInput = null;
				OfficeManagedObjectSourceFlowToOfficeSectionInputModel conn = mosFlowModel.getOfficeSectionInput();
				if (conn != null) {
					OfficeSectionInputModel sectionInputModel = conn.getOfficeSectionInput();
					if (sectionInputModel != null) {
						OfficeSectionModel sectionModel = this.getOfficeSectionForInput(office, sectionInputModel);
						if (sectionModel != null) {
							OfficeSection inputSection = sections.get(sectionModel.getOfficeSectionName());
							if (inputSection != null) {
								sectionInput = inputSection
										.getOfficeSectionInput(sectionInputModel.getOfficeSectionInputName());
							}
						}
					}
				}
				if (sectionInput != null) {
					// Link managed object source flow to section input
					architect.link(mosFlow, sectionInput);
				}
			}
		}

		// Link managed object source teams to office teams
		for (OfficeManagedObjectSourceModel mosModel : office.getOfficeManagedObjectSources()) {

			// Obtain the managed object source
			OfficeManagedObjectSource mos = managedObjectSources.get(mosModel.getOfficeManagedObjectSourceName());
			if (mos == null) {
				continue; // should always have managed object source
			}

			// Link managed object source teams to office team
			for (OfficeManagedObjectSourceTeamModel mosTeamModel : mosModel.getOfficeManagedObjectSourceTeams()) {

				// Obtain the managed object source team
				ManagedObjectTeam mosTeam = mos
						.getManagedObjectTeam(mosTeamModel.getOfficeManagedObjectSourceTeamName());

				// Link managed object source team to office team
				OfficeTeam officeTeam = null;
				OfficeManagedObjectSourceTeamToOfficeTeamModel conn = mosTeamModel.getOfficeTeam();
				if (conn != null) {
					OfficeTeamModel teamModel = conn.getOfficeTeam();
					if (teamModel != null) {
						officeTeam = teams.get(teamModel.getOfficeTeamName());
					}
				}
				if (officeTeam != null) {
					// Link managed object source team to office team
					architect.link(mosTeam, officeTeam);
				}
			}
		}

		// Add the administration (keeping registry of administration)
		Map<String, OfficeAdministration> administrations = new HashMap<String, OfficeAdministration>();
		for (AdministrationModel adminModel : office.getAdministrations()) {

			// Add the administration and register it
			String adminName = adminModel.getAdministrationName();
			OfficeAdministration admin = architect.addOfficeAdministration(adminName,
					adminModel.getAdministrationSourceClassName());
			for (PropertyModel property : adminModel.getProperties()) {
				admin.addProperty(property.getName(), property.getValue());
			}
			administrations.put(adminName, admin);

			// Obtain the office team responsible for this administration
			OfficeTeam officeTeam = null;
			AdministrationToOfficeTeamModel adminToTeam = adminModel.getOfficeTeam();
			if (adminToTeam != null) {
				OfficeTeamModel teamModel = adminToTeam.getOfficeTeam();
				if (teamModel != null) {
					officeTeam = teams.get(teamModel.getOfficeTeamName());
				}
			}
			if (officeTeam != null) {
				// Assign the team responsible for administration
				architect.link(admin, officeTeam);
			}
		}

		// Add processor to link administration with functions
		processors.addSubSectionProcessor(new FunctionsToAdministrationSubSectionProcessor(administrations));

		// Create the listing of objects to be administered
		Map<String, List<AdministeredManagedObject>> administration = new HashMap<String, List<AdministeredManagedObject>>();
		for (ExternalManagedObjectModel extMo : office.getExternalManagedObjects()) {

			// Obtain the office object
			OfficeObject officeObject = officeObjects.get(extMo.getExternalManagedObjectName());

			// Add the object for administration
			for (ExternalManagedObjectToAdministrationModel extMoToAdmin : extMo.getAdministrations()) {
				AdministrationModel adminModel = extMoToAdmin.getAdministration();
				if (adminModel != null) {
					String administrationName = adminModel.getAdministrationName();
					List<AdministeredManagedObject> list = administration.get(administrationName);
					if (list == null) {
						list = new LinkedList<AdministeredManagedObject>();
						administration.put(administrationName, list);
					}
					list.add(new AdministeredManagedObject(extMoToAdmin.getOrder(), officeObject));
				}
			}
		}
		for (OfficeManagedObjectModel moModel : office.getOfficeManagedObjects()) {

			// Obtain the office managed object
			OfficeManagedObject mo = managedObjects.get(moModel.getOfficeManagedObjectName());

			// Add the managed object for administration
			for (OfficeManagedObjectToAdministrationModel moToAdmin : moModel.getAdministrations()) {
				AdministrationModel adminModel = moToAdmin.getAdministration();
				if (adminModel != null) {
					String administrationName = adminModel.getAdministrationName();
					List<AdministeredManagedObject> list = administration.get(administrationName);
					if (list == null) {
						list = new LinkedList<AdministeredManagedObject>();
						administration.put(administrationName, list);
					}
					list.add(new AdministeredManagedObject(moToAdmin.getOrder(), mo));
				}
			}
		}

		// Administer the managed objects
		for (AdministrationModel adminModel : office.getAdministrations()) {

			// Obtain the administration
			String administrationName = adminModel.getAdministrationName();
			OfficeAdministration admin = administrations.get(administrationName);

			// Obtain the objects to administer
			List<AdministeredManagedObject> administeredManagedObjects = administration.get(administrationName);
			if (administeredManagedObjects == null) {
				continue; // no managed objects to administer
			}

			// Order the managed objects
			Collections.sort(administeredManagedObjects);

			// Add managed objects for administration
			for (AdministeredManagedObject managedObject : administeredManagedObjects) {
				admin.administerManagedObject(managedObject.managedObject);
			}
		}

		// Handle escalations
		for (OfficeEscalationModel escalationModel : office.getOfficeEscalations()) {

			// Obtain the escalation
			String escalationType = escalationModel.getEscalationType();
			OfficeEscalation escalation = architect.addOfficeEscalation(escalationType);

			// Link to section input for handling
			OfficeSectionInput sectionInput = null;
			OfficeEscalationToOfficeSectionInputModel conn = escalationModel.getOfficeSectionInput();
			if (conn != null) {
				OfficeSectionInputModel sectionInputModel = conn.getOfficeSectionInput();
				if (sectionInputModel != null) {
					OfficeSectionModel sectionModel = this.getOfficeSectionForInput(office, sectionInputModel);
					if (sectionModel != null) {
						OfficeSection inputSection = sections.get(sectionModel.getOfficeSectionName());
						if (inputSection != null) {
							sectionInput = inputSection
									.getOfficeSectionInput(sectionInputModel.getOfficeSectionInputName());
						}
					}
				}
			}
			if (sectionInput != null) {
				// Link escalation to section input handling
				architect.link(escalation, sectionInput);
			}
		}

		// Process the sub sections
		for (OfficeSectionModel sectionModel : office.getOfficeSections()) {

			// Obtain the top level office sections
			String sectionName = sectionModel.getOfficeSectionName();
			OfficeSection section = sections.get(sectionName);
			OfficeSectionType sectionType = sectionTypes.get(sectionName);
			OfficeSubSectionModel subSectionModel = sectionModel.getOfficeSubSection();

			// Process the section and its sub sections
			this.processSubSections(sectionName, section, sectionType, subSectionModel, sectionModel, processors,
					architect);
		}
	}

	/**
	 * Obtains the {@link GovernanceModel} instances that provide
	 * {@link Governance} over the particular location.
	 * 
	 * @param x
	 *            X co-ordinate of location.
	 * @param y
	 *            Y co-ordinate of location.
	 * @param governances
	 *            {@link GovernanceModel} instances.
	 * @return {@link GovernanceModel} instances that provide {@link Governance}
	 *         over the particular location. May be empty array if no
	 *         {@link Governance} for location.
	 */
	private GovernanceModel[] getGovernancesOverLocation(int x, int y, List<GovernanceModel> governances) {

		// Create listing of governances for the location
		List<GovernanceModel> governing = new LinkedList<GovernanceModel>();

		// Add governances that cover the location
		for (GovernanceModel governance : governances) {
			for (GovernanceAreaModel area : governance.getGovernanceAreas()) {

				// Calculate points for area
				int leftX = area.getX();
				int rightX = area.getX() + area.getWidth();
				if (leftX > rightX) {
					// Swap as may be negative width
					int temp = leftX;
					leftX = rightX;
					rightX = temp;
				}
				int topY = area.getY();
				int bottomY = area.getY() + area.getHeight();
				if (topY > bottomY) {
					// Swap as may be negative height
					int temp = topY;
					topY = bottomY;
					bottomY = temp;
				}

				// Determine if governance covers the location
				if (((leftX <= x) && (x <= rightX)) && ((topY <= y) && (y <= bottomY))) {
					// Governance is governing the location
					governing.add(governance);
				}
			}
		}

		// Return the listing of governing governances for the location
		return governing.toArray(new GovernanceModel[governing.size()]);
	}

	/**
	 * Obtains the {@link ManagedObjectScope} from the managed object scope
	 * name.
	 * 
	 * @param managedObjectScope
	 *            Name of the {@link ManagedObjectScope}.
	 * @param architect
	 *            {@link OfficeArchitect}.
	 * @param managedObjectName
	 *            Name of the {@link OfficeManagedObjectModel}.
	 * @return {@link ManagedObjectScope} or <code>null</code> with issue
	 *         reported to the {@link OfficeArchitect}.
	 */
	private ManagedObjectScope getManagedObjectScope(String managedObjectScope, OfficeArchitect architect,
			String managedObjectName) {

		// Obtain the managed object scope
		if (OfficeChanges.PROCESS_MANAGED_OBJECT_SCOPE.equals(managedObjectScope)) {
			return ManagedObjectScope.PROCESS;
		} else if (OfficeChanges.THREAD_MANAGED_OBJECT_SCOPE.equals(managedObjectScope)) {
			return ManagedObjectScope.THREAD;
		} else if (OfficeChanges.FUNCTION_MANAGED_OBJECT_SCOPE.equals(managedObjectScope)) {
			return ManagedObjectScope.FUNCTION;
		}

		// Unknown scope if at this point
		architect.addIssue(
				"Unknown managed object scope " + managedObjectScope + " for managed object " + managedObjectName);
		return null;
	}

	/**
	 * Obtains the {@link OfficeSectionModel} containing the
	 * {@link OfficeSectionInputModel}.
	 * 
	 * @param office
	 *            {@link OfficeModel}.
	 * @param input
	 *            {@link OfficeSectionInput}.
	 * @return {@link OfficeSectionModel} containing the
	 *         {@link OfficeSectionInput}.
	 */
	private OfficeSectionModel getOfficeSectionForInput(OfficeModel office, OfficeSectionInputModel input) {

		// Find and return the office section for the input
		for (OfficeSectionModel section : office.getOfficeSections()) {
			for (OfficeSectionInputModel check : section.getOfficeSectionInputs()) {
				if (check == input) {
					// Found the input so subsequently return section
					return section;
				}
			}
		}

		// As here did not find the section
		return null;
	}

	/**
	 * Loads the {@link OfficeSectionFunction} instances for the
	 * {@link OfficeSubSection} and its {@link OfficeSubSection} instances.
	 * 
	 * @param section
	 *            {@link OfficeSubSection}.
	 * @param sectionType
	 *            {@link OfficeSubSectionType}.
	 * @param functions
	 *            Listing to be populated with the {@link OfficeSubSectionType}
	 *            {@link OfficeSectionFunction} instances.
	 */
	private void loadOfficeFunctions(OfficeSubSection section, OfficeSubSectionType sectionType,
			List<OfficeSectionFunction> functions) {

		// Ensure have section
		if (sectionType == null) {
			return;
		}

		// Add the section office functions
		for (OfficeFunctionType functionType : sectionType.getOfficeFunctionTypes()) {
			String functionName = functionType.getOfficeFunctionName();
			OfficeSectionFunction function = section.getOfficeSectionFunction(functionName);
			functions.add(function);
		}

		// Recursively add the sub section office functions
		for (OfficeSubSectionType subSectionType : sectionType.getOfficeSubSectionTypes()) {
			String subSectionName = subSectionType.getOfficeSectionName();
			OfficeSubSection subSection = section.getOfficeSubSection(subSectionName);
			this.loadOfficeFunctions(subSection, subSectionType, functions);
		}
	}

	/**
	 * Recurses through the {@link OfficeSubSectionModel} instances processing
	 * the {@link OfficeSubSectionModel} instances.
	 * 
	 * @param subSectionPath
	 *            Path from top level {@link OfficeSubSectionModel} to current
	 *            {@link OfficeSubSectionModel}.
	 * @param subSection
	 *            {@link OfficeSubSection}.
	 * @param subSectionType
	 *            {@link OfficeSubSectionType}.
	 * @param subSectionModel
	 *            {@link OfficeSubSectionModel}.
	 * @param sectionModel
	 *            {@link OfficeSectionModel}.
	 * @param processor
	 *            {@link SubSectionProcessor}.
	 * @param architect
	 *            {@link OfficeArchitect}.
	 */
	private void processSubSections(String subSectionPath, OfficeSubSection subSection,
			OfficeSubSectionType subSectionType, OfficeSubSectionModel subSectionModel, OfficeSectionModel sectionModel,
			SubSectionProcessor processor, OfficeArchitect architect) {

		// Ensure have sub section model
		if (subSectionModel == null) {
			return;
		}

		// Process the sub section
		processor.processSubSection(subSectionModel, subSection, architect, subSectionPath);

		// Process managed objects for the current sub section
		for (OfficeSectionManagedObjectModel managedObjectModel : subSectionModel.getOfficeSectionManagedObjects()) {

			// Obtain the corresponding office section managed object
			String managedObjectName = managedObjectModel.getOfficeSectionManagedObjectName();
			OfficeSectionManagedObject managedObject = null;
			for (OfficeSectionManagedObjectType checkManagedObjectType : subSectionType
					.getOfficeSectionManagedObjectTypes()) {
				if (managedObjectName.equals(checkManagedObjectType.getOfficeSectionManagedObjectName())) {

					// Obtain the managed object
					managedObject = subSection.getOfficeSectionManagedObject(managedObjectName);
				}
			}
			if (managedObject == null) {
				architect.addIssue("Office model is out of sync with sections. Can not find managed object '"
						+ managedObjectName + "' [" + subSectionPath + "]");
				continue; // must have managed object
			}

			// Process the managed object
			processor.processManagedObject(managedObjectModel, managedObject, architect, subSectionPath);
		}

		// Process functions for the current sub section
		for (OfficeFunctionModel functionModel : subSectionModel.getOfficeFunctions()) {

			// Obtain the corresponding office function
			String functionName = functionModel.getOfficeFunctionName();
			OfficeSectionFunction function = subSection.getOfficeSectionFunction(functionName);

			// Process the office function
			processor.processOfficeFunction(functionModel, function, architect, subSectionPath);
		}

		// Recurse into the sub sections
		for (OfficeSubSectionModel subSubSectionModel : subSectionModel.getOfficeSubSections()) {

			// Obtain the corresponding sub section
			String subSubSectionName = subSubSectionModel.getOfficeSubSectionName();
			OfficeSubSection subSubSection = subSection.getOfficeSubSection(subSubSectionName);

			// Obtain the sub section type
			OfficeSubSectionType subSubSectionType = null;
			for (OfficeSubSectionType checkSubSectionType : subSectionType.getOfficeSubSectionTypes()) {
				if (subSubSectionName.equals(checkSubSectionType.getOfficeSectionName())) {
					subSubSectionType = checkSubSectionType;
				}
			}
			if (subSubSectionType == null) {
				architect.addIssue("Office model is out of sync with sections. Can not find sub section '"
						+ subSubSectionName + "' [" + subSectionPath + "]");
				continue; // must have sub section
			}

			// Determine the path for the sub section
			String subSubSectionPath = (subSectionPath == null) ? subSubSectionName
					: subSectionPath + "/" + subSubSectionName;

			// Recursively process the sub sections
			this.processSubSections(subSubSectionPath, subSubSection, subSubSectionType, subSubSectionModel,
					sectionModel, processor, architect);
		}
	}

	/**
	 * Processes the {@link OfficeSubSection} instances.
	 */
	private static interface SubSectionProcessor {

		/**
		 * Processes the {@link OfficeSubSection}.
		 * 
		 * @param subSectionModel
		 *            {@link OfficeSubSectionModel}.
		 * @param subSection
		 *            {@link OfficeSubSection}.
		 * @param architect
		 *            {@link OfficeArchitect}.
		 * @param subSectionPath
		 *            Path to the {@link OfficeSubSection}.
		 */
		void processSubSection(OfficeSubSectionModel subSectionModel, OfficeSubSection subSection,
				OfficeArchitect architect, String subSectionPath);

		/**
		 * Processes the {@link OfficeSectionManagedObject}.
		 * 
		 * @param managedObjectModel
		 *            {@link OfficeSectionManagedObjectModel}.
		 * @param managedObject
		 *            {@link OfficeSectionManagedObject}.
		 * @param architect
		 *            {@link OfficeArchitect}.
		 * @param subSectionPath
		 *            Path to the {@link OfficeSubSection}.
		 */
		void processManagedObject(OfficeSectionManagedObjectModel managedObjectModel,
				OfficeSectionManagedObject managedObject, OfficeArchitect architect, String subSectionPath);

		/**
		 * Processes the {@link OfficeSectionFunction}.
		 * 
		 * @param functionModel
		 *            {@link OfficeFunctionModel}.
		 * @param function
		 *            {@link OfficeSectionFunction}.
		 * @param architect
		 *            {@link OfficeArchitect}.
		 * @param subSectionPath
		 *            Path to the {@link OfficeSubSection}.
		 */
		void processOfficeFunction(OfficeFunctionModel functionModel, OfficeSectionFunction function,
				OfficeArchitect architect, String subSectionPath);
	}

	/**
	 * {@link SubSectionProcessor} implementation that by default does nothing.
	 */
	private static abstract class AbstractSubSectionProcessor implements SubSectionProcessor {

		/*
		 * ==================== SubSectionProcessor ====================
		 */

		@Override
		public void processSubSection(OfficeSubSectionModel subSectionModel, OfficeSubSection subSection,
				OfficeArchitect architect, String subSectionPath) {
			// Override to provide processing
		}

		@Override
		public void processManagedObject(OfficeSectionManagedObjectModel managedObjectModel,
				OfficeSectionManagedObject managedObject, OfficeArchitect architect, String subSectionPath) {
			// Override to provide processing
		}

		@Override
		public void processOfficeFunction(OfficeFunctionModel taskModel, OfficeSectionFunction function,
				OfficeArchitect architect, String subSectionPath) {
			// Override to provide processing
		}
	}

	/**
	 * {@link SubSectionProcessor} to process multiple
	 * {@link SubSectionProcessor} instances.
	 */
	private static class AggregateSubSectionProcessor implements SubSectionProcessor {

		/**
		 * {@link SubSectionProcessor} instances.
		 */
		private final List<SubSectionProcessor> processors = new LinkedList<SubSectionProcessor>();

		/**
		 * Adds a {@link SubSectionProcessor}.
		 * 
		 * @param processor
		 *            {@link SubSectionProcessor}.
		 */
		public void addSubSectionProcessor(SubSectionProcessor processor) {
			this.processors.add(processor);
		}

		/*
		 * ================= SubSectionProcessor =========================
		 */

		@Override
		public void processSubSection(OfficeSubSectionModel subSectionModel, OfficeSubSection subSection,
				OfficeArchitect architect, String subSectionPath) {
			for (SubSectionProcessor processor : this.processors) {
				processor.processSubSection(subSectionModel, subSection, architect, subSectionPath);
			}
		}

		@Override
		public void processManagedObject(OfficeSectionManagedObjectModel managedObjectModel,
				OfficeSectionManagedObject managedObject, OfficeArchitect architect, String subSectionPath) {
			for (SubSectionProcessor processor : this.processors) {
				processor.processManagedObject(managedObjectModel, managedObject, architect, subSectionPath);
			}
		}

		@Override
		public void processOfficeFunction(OfficeFunctionModel functionModel, OfficeSectionFunction function,
				OfficeArchitect architect, String subSectionPath) {
			for (SubSectionProcessor processor : this.processors) {
				processor.processOfficeFunction(functionModel, function, architect, subSectionPath);
			}
		}
	}

	/**
	 * {@link SubSectionProcessor} to link {@link Administration} instances to
	 * the {@link ManagedFunction} instances.
	 */
	private static class FunctionsToAdministrationSubSectionProcessor extends AbstractSubSectionProcessor {

		/**
		 * {@link OfficeAdministration} by {@link OfficeAdministration} name.
		 */
		private final Map<String, OfficeAdministration> administrations;

		/**
		 * Initiate.
		 * 
		 * @param administrations
		 *            {@link OfficeAdministration} by
		 *            {@link OfficeAdministration} name.
		 */
		public FunctionsToAdministrationSubSectionProcessor(Map<String, OfficeAdministration> administrations) {
			this.administrations = administrations;
		}

		/*
		 * ==================== SubSectionProcessor ====================
		 */

		@Override
		public void processOfficeFunction(OfficeFunctionModel functionModel, OfficeSectionFunction function,
				OfficeArchitect architect, String subSectionPath) {

			// Determine if function is linked to administration
			List<OfficeFunctionToPreAdministrationModel> preAdmin = functionModel.getPreAdministrations();
			List<OfficeFunctionToPostAdministrationModel> postAdmin = functionModel.getPostAdministrations();
			if ((preAdmin.size() == 0) && (postAdmin.size() == 0)) {
				return; // no administration to link for function
			}

			// Obtain the function name
			String functionName = function.getOfficeFunctionName();

			// Link the pre administration
			for (int i = 0; i < preAdmin.size(); i++) {
				OfficeFunctionToPreAdministrationModel conn = preAdmin.get(i);

				// Obtain the pre administration
				OfficeAdministration preAdministration = null;
				AdministrationModel adminModel = conn.getAdministration();
				if (adminModel != null) {
					preAdministration = this.administrations.get(adminModel.getAdministrationName());
				}
				if (preAdministration == null) {
					architect.addIssue("Can not find pre administration " + i + " for function '" + functionName + "' ["
							+ subSectionPath + "]");
					continue; // must have admin
				}

				// Add the pre administration
				function.addPreAdministration(preAdministration);
			}

			// Link the post administration
			for (int i = 0; i < postAdmin.size(); i++) {
				OfficeFunctionToPostAdministrationModel conn = postAdmin.get(i);

				// Obtain the post administration
				OfficeAdministration postAdministration = null;
				AdministrationModel adminModel = conn.getAdministration();
				if (adminModel != null) {
					postAdministration = this.administrations.get(adminModel.getAdministrationName());
				}
				if (postAdministration == null) {
					architect.addIssue("Can not find post administration " + i + " for function '" + functionName
							+ "' [" + subSectionPath + "]");
					continue; // must have admin
				}

				// Add the post administration
				function.addPostAdministration(postAdministration);
			}
		}
	}

	/**
	 * {@link SubSectionProcessor} to provide {@link Governance} to the
	 * {@link OfficeSubSection} instances.
	 */
	private static class GovernanceSubSectionProcessor extends AbstractSubSectionProcessor {

		/**
		 * {@link Governance} instances by their name.
		 */
		private final Map<String, OfficeGovernance> governances;

		/**
		 * Initiate.
		 * 
		 * @param governances
		 *            {@link Governance} instances by their name.
		 */
		public GovernanceSubSectionProcessor(Map<String, OfficeGovernance> governances) {
			this.governances = governances;
		}

		/*
		 * ================== SubSectionProcessor ======================
		 */

		@Override
		public void processSubSection(OfficeSubSectionModel subSectionModel, OfficeSubSection subSection,
				OfficeArchitect architect, String subSectionPath) {

			// Link the governances
			for (OfficeSubSectionToGovernanceModel conn : subSectionModel.getGovernances()) {
				GovernanceModel govModel = conn.getGovernance();
				if (govModel != null) {

					// Obtain the governance
					OfficeGovernance governance = this.governances.get(govModel.getGovernanceName());
					if (governance != null) {

						// Provide governance over the sub section
						subSection.addGovernance(governance);
					}
				}
			}
		}

		@Override
		public void processManagedObject(OfficeSectionManagedObjectModel managedObjectModel,
				OfficeSectionManagedObject managedObject, OfficeArchitect architect, String subSectionPath) {

			// Link the governances
			for (OfficeSectionManagedObjectToGovernanceModel conn : managedObjectModel.getGovernances()) {
				GovernanceModel govModel = conn.getGovernance();
				if (govModel != null) {

					// Obtain the governance
					OfficeGovernance governance = this.governances.get(govModel.getGovernanceName());
					if (governance != null) {

						// Provide governance over the managed object
						governance.governManagedObject(managedObject);
					}
				}
			}
		}

		@Override
		public void processOfficeFunction(OfficeFunctionModel functionModel, OfficeSectionFunction function,
				OfficeArchitect architect, String subSectionPath) {

			// Link the governances
			for (OfficeFunctionToGovernanceModel conn : functionModel.getGovernances()) {
				GovernanceModel govModel = conn.getGovernance();
				if (govModel != null) {

					// Obtain the governance
					OfficeGovernance governance = this.governances.get(govModel.getGovernanceName());
					if (governance != null) {

						// Provide governance of the function
						function.addGovernance(governance);
					}
				}
			}
		}
	}

	/**
	 * Responsibility.
	 */
	private static class Responsibility {

		/**
		 * {@link OfficeTeam} responsible for this responsibility.
		 */
		public final OfficeTeam officeTeam;

		/**
		 * Initiate.
		 * 
		 * @param officeTeam
		 *            {@link OfficeTeam} responsible for this responsibility.
		 */
		public Responsibility(OfficeTeam officeTeam) {
			this.officeTeam = officeTeam;
		}

		/**
		 * Indicates if {@link OfficeSectionFunction} is within this
		 * responsibility.
		 * 
		 * @param task
		 *            {@link OfficeSectionFunction}.
		 * @return <code>true</code> if {@link OfficeSectionFunction} is within
		 *         this responsibility.
		 */
		public boolean isResponsible(OfficeSectionFunction task) {
			// TODO handle managed object matching for responsibility
			return true; // TODO for now always responsible
		}
	}

	/**
	 * {@link ManagedObject} to be administered.
	 */
	private static class AdministeredManagedObject implements Comparable<AdministeredManagedObject> {

		/**
		 * Position in the order that the objects are administered.
		 */
		public final String order;

		/**
		 * {@link AdministerableManagedObject}.
		 */
		public final AdministerableManagedObject managedObject;

		/**
		 * Initiate.
		 * 
		 * @param order
		 *            Position in the order that the objects are administered.
		 * @param managedObject
		 *            {@link AdministerableManagedObject}.
		 */
		public AdministeredManagedObject(String order, AdministerableManagedObject managedObject) {
			this.order = order;
			this.managedObject = managedObject;
		}

		/*
		 * ================== Comparable ==========================
		 */

		@Override
		public int compareTo(AdministeredManagedObject that) {
			return this.getOrder(this.order) - this.getOrder(that.order);
		}

		/**
		 * Obtains the order as an {@link Integer}.
		 * 
		 * @param order
		 *            Text order value.
		 * @return Numeric order value.
		 */
		private int getOrder(String order) {
			try {
				return Integer.parseInt(order);
			} catch (NumberFormatException ex) {
				return Integer.MAX_VALUE; // invalid number so make last
			}
		}
	}

}