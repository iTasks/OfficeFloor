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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.type.TypeContext;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;

/**
 * Node within the hierarchy of {@link OfficeSection} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionNode extends Node, ManagedObjectRegistry, ManagedFunctionRegistry,
		SectionDesigner, SubSection, OfficeSection {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Section";

	/**
	 * Initialises this {@link SectionNode}.
	 * 
	 * @param sectionSourceClassName
	 *            {@link SectionSource} class name.
	 * @param sectionSource
	 *            Optional instantiated {@link SectionSource}. May be
	 *            <code>null</code>.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection}.
	 */
	void initialise(String sectionSourceClassName, SectionSource sectionSource,
			String sectionLocation);

	/**
	 * <p>
	 * Sources the section into this {@link SectionNode}.
	 * <p>
	 * This will only source the top level {@link OfficeSection}.
	 * 
	 * @return <code>true</code> if successfully sourced. Otherwise
	 *         <code>false</code> with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceSection();

	/**
	 * Sources this {@link SectionNode} and all its descendant {@link Node}
	 * instances recursively.
	 * 
	 * @return <code>true</code> if successfully sourced. Otherwise
	 *         <code>false</code> with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceSectionTree();

	/**
	 * Loads the {@link SectionType}.
	 * 
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return {@link SectionType} or <code>null</code> if issue loading with
	 *         issue reported to the {@link CompilerIssues}.
	 * 
	 * @see #sourceSection()
	 */
	SectionType loadSectionType(TypeContext typeContext);

	/**
	 * Loads the {@link OfficeSectionType}.
	 * 
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return {@link OfficeSectionType} or <code>null</code> if issue loading
	 *         with issue reported to the {@link CompilerIssues}.
	 * 
	 * @see #sourceSectionTree()
	 */
	OfficeSectionType loadOfficeSectionType(TypeContext typeContext);

	/**
	 * Loads the {@link OfficeSubSectionType}.
	 * 
	 * @param parentSectionType
	 *            Parent {@link OfficeSubSectionType}.
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return {@link OfficeSubSectionType} or <code>null</code> if issue
	 *         loading with issue reported to the {@link CompilerIssues}.
	 */
	OfficeSubSectionType loadOfficeSubSectionType(
			OfficeSubSectionType parentSectionType, TypeContext typeContext);

	/**
	 * Loads the {@link OfficeAvailableSectionInputType} instances.
	 * 
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return {@link OfficeAvailableSectionInputType} instances or
	 *         <code>null</code> with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	OfficeAvailableSectionInputType[] loadOfficeAvailableSectionInputTypes(
			TypeContext typeContext);

	/**
	 * Obtains the {@link DeployedOfficeInput}.
	 * 
	 * @param inputName
	 *            Input name as per the {@link OfficeAvailableSectionInputType}.
	 * @return {@link DeployedOfficeInput}.
	 */
	DeployedOfficeInput getDeployedOfficeInput(String inputName);

	/**
	 * <p>
	 * Obtains the {@link GovernanceNode} instances providing {@link Governance}
	 * over this {@link SectionNode}.
	 * <p>
	 * This is list comprised of the:
	 * <ol>
	 * <li>{@link GovernanceNode} assigned to this particular
	 * {@link SectionNode}</li>
	 * <li>{@link GovernanceNode} assigned to any parent {@link SectionNode}</li>
	 * </ol>
	 * 
	 * @return {@link GovernanceNode} instances providing {@link Governance}
	 *         over this {@link SectionNode}.
	 */
	GovernanceNode[] getGoverningGovernances();

	/**
	 * Obtains the parent {@link SectionNode} containing this
	 * {@link SectionNode}.
	 * 
	 * @return Parent {@link SectionNode} or <code>null</code> if this
	 *         {@link SectionNode} is the top level {@link SectionNode} (in
	 *         other words a {@link OfficeSection}).
	 */
	SectionNode getParentSectionNode();

	/**
	 * Obtains the {@link OfficeNode} containing this {@link SectionNode}.
	 * 
	 * @return {@link OfficeNode} containing this {@link SectionNode}.
	 */
	OfficeNode getOfficeNode();

	/**
	 * Obtain the {@link OfficeSection} qualified name.
	 * 
	 * @param simpleName
	 *            Simple name to qualify with the {@link OfficeSection} name
	 *            space.
	 * @return {@link OfficeSection} qualified name.
	 */
	String getSectionQualifiedName(String simpleName);

	/**
	 * Builds this {@link OfficeSection} for this {@link SectionNode}.
	 * 
	 * @param officeBuilder
	 *            {@link OfficeBuilder} of the {@link Office} containing this
	 *            {@link SectionNode}.
	 * @param officeBindings
	 *            {@link OfficeBindings} of the {@link Office} containing this
	 *            {@link SectionNode}.
	 * @param typeContext
	 *            {@link TypeContext}.
	 */
	void buildSection(OfficeBuilder officeBuilder,
			OfficeBindings officeBindings, TypeContext typeContext);

}